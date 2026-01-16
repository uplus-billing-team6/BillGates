package springboot.billgates.domain.admin.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.admin.dto.DummyDataRequest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDataService {

    private final JdbcTemplate jdbcTemplate;

    // batch size 설정
    private static final int BATCH_SIZE = 5000;

    // 리얼한 이름을 위한 데이터 셋
    private static final String[] LAST_NAMES = {
        "김", "이", "박", "최", "정",
        "강", "조", "윤", "장", "임",
        "한", "오", "서", "신", "권",
        "황", "안", "송", "류", "전"
    };
    private static final String[] FIRST_NAMES = {
        "민수", "서준", "도윤", "예준", "시우",
        "하준", "지호", "주원", "지우", "서현",
        "서연", "지유", "하은", "수아", "다은",
        "지안", "나은", "건우", "우진", "선우"
    };
    private static final String[] ID_PREFIXES = {
        "happy", "sky", "blue", "star", "moon", "sun", "cloud", "rain",
        "love", "dream", "cool", "hot", "best", "top", "king", "master",
        "james", "john", "david", "sarah", "michael", "chris", "tom", "jerry",
        "dragon", "tiger", "lion", "wolf", "bear", "eagle", "shark", "whale",
        "red", "green", "white", "black", "gold", "silver", "pink", "purple"
    };
    private static final String[] EMAIL_DOMAINS = {
        "gmail.com", "naver.com", "uplus.co.kr", "kakao.com", "billgates.com"
    };

    @Async("dummyDataExecutor")
    @Transactional
    public void generateDummyData(DummyDataRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> [Admin] 더미 데이터 생성 작업을 시작합니다. (목표 Member: {}명, Usage: {}건)", request.getMemberCount(), request.getUsageCount());

        try {
            // 0. 기존 데이터 초기화 (Reset)
            resetTables();

            // 1. Member 데이터 생성
            if (request.getMemberCount() > 0) {
                createMembers(request.getMemberCount());
            }

            // 2. Usage History 데이터 생성
            if (request.getUsageCount() > 0) {
                createUsageHistories(request.getUsageCount());
            }

        } catch (Exception e) {
            log.error(">>> [Admin] 데이터 생성 중 오류 발생: ", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        log.info(">>> [Admin] 모든 작업 완료! 총 소요 시간: {}ms", (endTime - startTime));
    }

    /**
     * [테이블 초기화 로직]
     * - FK 제약조건을 잠시 해제하고 데이터를 모두 비움 (TRUNCATE)
     * - auto_increment 도 1로 초기화됨
     */
    private void resetTables() {
        log.info(">>> 기존 데이터를 초기화합니다...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE USAGE_HISTORY");
        jdbcTemplate.execute("TRUNCATE TABLE MEMBER");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        log.info(">>> 초기화 완료.");
    }

    /**
     * [Member 대량 생성 로직]
     * - JDBC Batch Update 사용
     * - 기존 ID의 MAX 값을 조회하여 이어서 생성
     */
    private void createMembers(int totalCount) {
        log.info(">>> Member 데이터 생성을 시작합니다. ({}건)", totalCount);

        // 1. 시작 ID는 항상 1
        long startId = 1;

        String sql = "INSERT INTO MEMBER (member_id, name, email, phone_number) VALUES (?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < totalCount; i++) {
            long currentId = startId + i;

            // 랜덤 데이터 생성
            String name = generateRandomName();
            String phoneNumber = generateRandomPhoneNumber();
            String email = generateRandomEmail(currentId);

            // 파라미터 추가
            batchArgs.add(new Object[]{currentId, name, email, phoneNumber});

            // 배치 사이즈가 차거나 마지막 데이터면 DB에 전송
            if (batchArgs.size() == BATCH_SIZE || i == totalCount - 1) {
                executeBatch(sql, batchArgs);

                // 진행 상황 로그 (10만 건 단위로만 출력하여 로그 과부하 방지)
                if ((i + 1) % 100000 == 0) {
                    log.info("Member Insert Progress: {} / {}", (i + 1), totalCount);
                }
            }
        }
        log.info(">>> Member {}건 생성 완료.", totalCount);
    }

    /**
     * [Usage History 생성 로직]
     * 1. 모든 회원에게 1개의 요금제(PLAN) 필수 할당 (Phase 1)
     * 2. 나머지 개수는 비율에 맞춰 소액결제/부가서비스/로밍 등으로 채움 (Phase 2)
     * 3. 날짜 범위: 1년 -> 1달
     */
    private void createUsageHistories(int targetTotalCount) {
        log.info(">>> Usage History 생성 시작 (목표: {}건)", targetTotalCount);

        // 1. 아이템 로드 및 카테고리별 분류
        Map<String, List<ItemInfo>> itemsByCategory = loadAndGroupItems();
        if (itemsByCategory.isEmpty()) throw new RuntimeException("Item 데이터가 없습니다.");

        // 2. 1인 1요금제 필수 할당을 위해 회원 ID 범위 조회
        Long minMemberId = jdbcTemplate.queryForObject("SELECT MIN(member_id) FROM MEMBER", Long.class);
        Long maxMemberId = jdbcTemplate.queryForObject("SELECT MAX(member_id) FROM MEMBER", Long.class);
        if (minMemberId == null) throw new RuntimeException("Member 데이터가 없습니다.");

        long totalMembers = maxMemberId - minMemberId + 1;

        // 유효성 검사: 요청된 Usage 수보다 회원 수가 더 많으면, 최소 회원 수만큼은 만들어야 함 (1인 1요금제 원칙)
        if (targetTotalCount < totalMembers) {
            log.warn("요청된 Usage 수({})가 회원 수({})보다 적습니다. 1인 1요금제 원칙을 위해 목표를 회원 수만큼 자동 상향합니다.", targetTotalCount, totalMembers);
            targetTotalCount = (int) totalMembers;
        }

        // 3. Usage ID 시작값은 항상 1
        long usageIdCounter = 1;

        String sql = "INSERT INTO USAGE_HISTORY (usage_id, member_id, item_id, usage_date, amount) VALUES (?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);
        long currentCount = 0;

        // --- Phase 1: 1인 1요금제 (PLAN) 할당 ---
        log.info(">>> Phase 1: 모든 회원에게 기본 요금제 할당 시작");
        List<ItemInfo> planItems = itemsByCategory.get("PLAN");

        for (long memberId = minMemberId; memberId <= maxMemberId; memberId++) {
            ItemInfo plan = getRandomItem(planItems);
            addUsageToBatch(batchArgs, usageIdCounter++, memberId, plan);
            currentCount++;

            // 배치 실행
            if (batchArgs.size() >= BATCH_SIZE || memberId == maxMemberId) {
                executeBatch(sql, batchArgs);

                // 로그
                if (memberId % 100000 == 0) {
                    log.info("Phase 1 Progress: {} / {}", memberId, maxMemberId);
                }
            }
        }
        log.info(">>> Phase 1 완료. (현재 {}건)", currentCount);

        // --- Phase 2: 나머지 랜덤 채우기 (소액결제, 부가서비스 등) ---
        log.info(">>> Phase 2: 추가 사용 내역 랜덤 생성 시작");
        long remainingCount = targetTotalCount - currentCount;

        // 나머지 카테고리 아이템들
        List<ItemInfo> microItems = itemsByCategory.getOrDefault("MICRO_PAYMENT", new ArrayList<>());
        List<ItemInfo> addonItems = itemsByCategory.getOrDefault("ADDON", new ArrayList<>());
        List<ItemInfo> roamingItems = itemsByCategory.getOrDefault("ROAMING_PASS", new ArrayList<>());

        for (int i = 0; i < remainingCount; i++) {
            long randomMemberId = ThreadLocalRandom.current().nextLong(minMemberId, maxMemberId + 1);

            // 카테고리 확률 선택 (소액결제 60%, 부가서비스 30%, 로밍 10%)
            int rand = ThreadLocalRandom.current().nextInt(100);
            ItemInfo selectedItem;

            if (rand < 60 && !microItems.isEmpty()) {       // 0 ~ 59 (60%)
                selectedItem = getRandomItem(microItems);
            } else if (rand < 90 && !addonItems.isEmpty()) { // 60 ~ 89 (30%)
                selectedItem = getRandomItem(addonItems);
            } else if (!roamingItems.isEmpty()) {            // 90 ~ 99 (10%)
                selectedItem = getRandomItem(roamingItems);
            } else {
                // 예외 시 소액결제 혹은 있는 것 중 아무거나
                selectedItem = getRandomItem(microItems.isEmpty() ? planItems : microItems);
            }

            addUsageToBatch(batchArgs, usageIdCounter++, randomMemberId, selectedItem);

            // 배치 실행
            if (batchArgs.size() >= BATCH_SIZE || i == remainingCount - 1) {
                executeBatch(sql, batchArgs);

                // 로그 (너무 자주 찍히지 않게)
                if ((i + 1) % 100000 == 0) {
                    log.info("Phase 2 Progress: {} / {}", i + 1, remainingCount);
                }
            }
        }

        log.info(">>> Usage History 총 {}건 생성 완료.", targetTotalCount);
    }

    // --- Helper 메서드 ---

    // 배치 실행 공통 메서드
    private void executeBatch(String sql, List<Object[]> batchArgs) {
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            batchArgs.clear();
        }
    }

    // 이름 조합 (성 + 이름)
    private String generateRandomName() {
        Random random = ThreadLocalRandom.current();
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)] +
            FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    // 전화번호 생성 (010-XXXX-YYYY)
    private String generateRandomPhoneNumber() {
        Random random = ThreadLocalRandom.current();
        return String.format("010-%04d-%04d",
            random.nextInt(10000), // 0 ~ 9999
            random.nextInt(10000));
    }

    // 이메일 생성 (memberId 기반으로 고유성 보장 + 랜덤 도메인)
    private String generateRandomEmail(long memberId) {
        Random random = ThreadLocalRandom.current();
        String prefix = ID_PREFIXES[random.nextInt(ID_PREFIXES.length)];
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        return prefix + memberId + "@" + domain;
    }

    // Usage 파라미터 생성 및 리스트 추가 (날짜, 금액 계산 포함)
    private void addUsageToBatch(List<Object[]> batchArgs, long usageId, long memberId, ItemInfo item) {
        //LocalDateTime randomDate = generateWideRandomDate();
        LocalDateTime randomDate = generateDate();
        long amount = calculateAmount(item);
        batchArgs.add(new Object[] {usageId, memberId, item.getItemId(), Timestamp.valueOf(randomDate), amount});
    }

    // item 테이블 항목 load 및 그룹화
    private Map<String, List<ItemInfo>> loadAndGroupItems() {
        List<ItemInfo> allItems = jdbcTemplate.query("SELECT item_id, category, price FROM ITEM",
            (rs, rowNum) -> new ItemInfo(
                rs.getLong("item_id"),
                rs.getString("category"),
                rs.getLong("price")
            ));
        return allItems.stream().collect(Collectors.groupingBy(ItemInfo::getCategory));
    }

    // items 리스트에서 랜덤 상품 추출
    private ItemInfo getRandomItem(List<ItemInfo> items) {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

    // 가격 결정 로직
    private long calculateAmount(ItemInfo item) {
        // 소액결제의 경우
        if ("MICRO_PAYMENT".equals(item.getCategory())) {
            // 1,000원 ~ 100,000원 사이 (100원 단위)
            long randomVal = ThreadLocalRandom.current().nextLong(10, 1001); // 10 ~ 1000
            return randomVal * 100;
        }
        // 나머지의 경우. item 의 price 를 그대로 따라간다.
        return item.getPrice();
    }

    // 날짜 생성 로직 (1년 범위)
    private LocalDateTime generateWideRandomDate() {
        // 현재로부터 1년(365일) 전 ~ 현재 사이 랜덤
        long minutes = ThreadLocalRandom.current().nextLong(365 * 24 * 60);
        return LocalDateTime.now().minusMinutes(minutes);
    }

    // 날짜 생성 로직 (특정 월 범위: 2025-04-01 00:00 ~ 2025-04-30 23:59)
    private LocalDateTime generateDate() {
        int year = 2025;
        int month = 4;

        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0 ,0 ,0);
        int daysInMonth = startOfMonth.toLocalDate().lengthOfMonth();
        long randomMinutes = ThreadLocalRandom.current().nextLong(daysInMonth * 24 * 60);
        return startOfMonth.plusMinutes(randomMinutes);
    }

    @Getter
    @AllArgsConstructor
    private static class ItemInfo {
        private Long itemId;
        private String category;
        private Long price;
    }
}