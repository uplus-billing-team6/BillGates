package springboot.billgates.domain.admin.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.domain.admin.dto.DummyDataRequest;
import springboot.billgates.domain.billing.batch.service.BatchService;
import springboot.billgates.global.utils.EncryptUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDataService {

    private final JdbcTemplate jdbcTemplate;
    private final EncryptUtils encryptUtils;
    private final BatchService billingBatchService; // 배치 서비스 주입

    private volatile boolean isProcessing = false;
    private volatile boolean isBatchProcessing = false;

    private static final int BATCH_SIZE = 5000;

    private static final String[] LAST_NAMES = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"};
    private static final String[] FIRST_NAMES = {"민수", "서준", "도윤", "예준", "시우", "하준", "지호", "주원", "지우", "서현", "서연", "지유", "하은", "수아", "다은", "지안", "나은", "건우", "우진", "선우"};
    private static final String[] ID_PREFIXES = {"happy", "sky", "blue", "star", "moon", "sun", "cloud", "rain", "love", "dream", "cool", "hot", "best", "top", "king", "master", "james", "john", "david", "sarah", "michael", "chris", "tom", "jerry", "dragon", "tiger", "lion", "wolf", "bear", "eagle", "shark", "whale", "red", "green", "white", "black", "gold", "silver", "pink", "purple"};
    private static final String[] EMAIL_DOMAINS = {"gmail.com", "naver.com", "uplus.co.kr", "kakao.com", "billgates.com"};

    public boolean isProcessing() {
        return isProcessing;
    }

    public boolean isBatchProcessing() {
        return isBatchProcessing;
    }

    @Async("dummyDataExecutor")
    public void generateDummyData(DummyDataRequest request) {
        isProcessing = true;
        long startTime = System.currentTimeMillis();
        log.info(">>> [Admin] 더미 데이터 생성 작업을 시작합니다. (목표 Member: {}명, Usage: {}건)", request.getMemberCount(), request.getUsageCount());

        try {
            if (request.getMemberCount() > 0) {
                createMembers(request.getMemberCount());
                createMemberDiscountPolicies();
            }

            if (request.getUsageCount() > 0) {
                createUsageHistories(request.getUsageCount());
            }

        } catch (Exception e) {
            log.error(">>> [Admin] 데이터 생성 중 오류 발생: ", e);
            throw e;
        } finally {
            isProcessing = false;
            long endTime = System.currentTimeMillis();
            log.info(">>> [Admin] 모든 작업 완료! 총 소요 시간: {}ms", (endTime - startTime));
        }
    }

    @Async("dummyDataExecutor")
    public void runBatchJob(String billingMonth) {
        if (isBatchProcessing) return;

        isBatchProcessing = true;
        log.info(">>> [Admin] {}월 Spring Batch 실행 시작", billingMonth);

        try {
            billingBatchService.runBillingJob(billingMonth, false);
            log.info(">>> [Admin] {}월 배치 완료", billingMonth);
        } catch (Exception e) {
            log.error(">>> [Admin] 배치 작업 중 오류: ", e);
        } finally {
            isBatchProcessing = false;
        }
    }

    private void resetTables() {
        log.info(">>> 기존 데이터를 초기화합니다...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE USAGE_HISTORY");
        jdbcTemplate.execute("TRUNCATE TABLE MEMBER");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        log.info(">>> 초기화 완료.");
    }

    private void createMembers(int totalCount) {
        log.info(">>> Member 데이터 생성을 시작합니다. ({}건)", totalCount);
        long startId = 1;
        String sql = "INSERT INTO MEMBER (member_id, name, email, phone_number, use_dnd, dnd_start_time, dnd_end_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);
        Random random = ThreadLocalRandom.current();

        for (int i = 0; i < totalCount; i++) {
            long currentId = startId + i;
            String name = generateRandomName(random);
            String phoneNumber = generateRandomPhoneNumber(random);
            String email = generateRandomEmail(currentId, random);

            String encryptedPhoneNumber = encryptUtils.encrypt(phoneNumber);
            String encryptedEmail = encryptUtils.encrypt(email);

            boolean useDnd = random.nextInt(100) < 30;
            Object startTime = null;
            Object endTime = null;

            if (useDnd) {
                int startHour = 21 + random.nextInt(5);
                if (startHour >= 24) startHour -= 24;
                int endHour = 5 + random.nextInt(5);
                String startStr = (startHour < 10 ? "0" : "") + startHour + ":00:00";
                String endStr = (endHour < 10 ? "0" : "") + endHour + ":00:00";
                startTime = java.sql.Time.valueOf(startStr);
                endTime = java.sql.Time.valueOf(endStr);
            }

            batchArgs.add(new Object[]{currentId, name, encryptedEmail, encryptedPhoneNumber, useDnd, startTime, endTime});

            if (batchArgs.size() == BATCH_SIZE || i == totalCount - 1) {
                executeBatch(sql, batchArgs);
                if ((i + 1) % 100000 == 0) {
                    log.info("Member Insert Progress: {} / {}", (i + 1), totalCount);
                }
            }
        }
        log.info(">>> Member {}건 생성 완료.", totalCount);
    }

    private void createMemberDiscountPolicies() {
        log.info(">>> Member Discount Policy 매핑 생성 시작");

        Long minMemberId = jdbcTemplate.queryForObject("SELECT MIN(member_id) FROM MEMBER", Long.class);
        Long maxMemberId = jdbcTemplate.queryForObject("SELECT MAX(member_id) FROM MEMBER", Long.class);

        if (minMemberId == null) return;

        String sql = "INSERT INTO member_discount_policy (member_id, policy_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);
        Random random = ThreadLocalRandom.current();

        long memberCount = 0;
        long policyCount = 0;

        for (long memberId = minMemberId; memberId <= maxMemberId; memberId++) {
            if (random.nextInt(100) < 50) continue;

            memberCount++;

            if (random.nextInt(100) < 80) {
                batchArgs.add(new Object[]{memberId, 1L});
                policyCount++;
            }
            if (random.nextInt(100) < 20) {
                batchArgs.add(new Object[]{memberId, 2L});
                policyCount++;
            }
            if (random.nextInt(100) < 5) {
                batchArgs.add(new Object[]{memberId, 3L});
                policyCount++;
            }
            if (random.nextInt(100) < 30) {
                batchArgs.add(new Object[]{memberId, 4L});
                policyCount++;
            }

            if (batchArgs.size() >= BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        log.info(">>> 매핑 완료. (할인 대상 회원: 약 {}명, 발급된 총 쿠폰: {}개)", memberCount, policyCount);
    }

    private void createUsageHistories(int targetTotalCount) {
        log.info(">>> Usage History 생성 시작 (목표: {}건)", targetTotalCount);
        Map<String, List<ItemInfo>> itemsByCategory = loadAndGroupItems();
        if (itemsByCategory.isEmpty()) throw new RuntimeException("Item 데이터가 없습니다.");

        Long minMemberId = jdbcTemplate.queryForObject("SELECT MIN(member_id) FROM MEMBER", Long.class);
        Long maxMemberId = jdbcTemplate.queryForObject("SELECT MAX(member_id) FROM MEMBER", Long.class);
        if (minMemberId == null) throw new RuntimeException("Member 데이터가 없습니다.");

        long totalMembers = maxMemberId - minMemberId + 1;
        if (targetTotalCount < totalMembers) {
            targetTotalCount = (int) totalMembers;
        }

        long usageIdCounter = 1;
        String sql = "INSERT INTO USAGE_HISTORY (usage_id, member_id, item_id, usage_date, amount) VALUES (?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);
        Random random = ThreadLocalRandom.current();

        List<ItemInfo> planItems = itemsByCategory.get("PLAN");
        for (long memberId = minMemberId; memberId <= maxMemberId; memberId++) {
            ItemInfo plan = getRandomItem(planItems, random);
            addUsageToBatch(batchArgs, usageIdCounter++, memberId, plan, random);
            if (batchArgs.size() >= BATCH_SIZE || memberId == maxMemberId) {
                executeBatch(sql, batchArgs);
            }
        }

        long currentCount = totalMembers;
        long remainingCount = targetTotalCount - currentCount;
        List<ItemInfo> microItems = itemsByCategory.getOrDefault("MICRO_PAYMENT", new ArrayList<>());
        List<ItemInfo> addonItems = itemsByCategory.getOrDefault("ADDON", new ArrayList<>());
        List<ItemInfo> roamingItems = itemsByCategory.getOrDefault("ROAMING_PASS", new ArrayList<>());

        for (int i = 0; i < remainingCount; i++) {
            long randomMemberId = random.nextLong(minMemberId, maxMemberId + 1);
            int rand = random.nextInt(100);
            ItemInfo selectedItem;

            if (rand < 60 && !microItems.isEmpty()) selectedItem = getRandomItem(microItems, random);
            else if (rand < 90 && !addonItems.isEmpty()) selectedItem = getRandomItem(addonItems, random);
            else if (!roamingItems.isEmpty()) selectedItem = getRandomItem(roamingItems, random);
            else selectedItem = getRandomItem(planItems, random);

            addUsageToBatch(batchArgs, usageIdCounter++, randomMemberId, selectedItem, random);

            if (batchArgs.size() >= BATCH_SIZE || i == remainingCount - 1) {
                executeBatch(sql, batchArgs);
                if ((i + 1) % 100000 == 0) {
                    log.info("Phase 2 Progress: {} / {}", i + 1, remainingCount);
                }
            }
        }
        log.info(">>> Usage History 총 {}건 생성 완료.", targetTotalCount);
    }

    private void executeBatch(String sql, List<Object[]> batchArgs) {
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            batchArgs.clear();
        }
    }

    private String generateRandomName(Random random) {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)] + FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private String generateRandomPhoneNumber(Random random) {
        return "010-" + random.nextInt(1000, 10000) + "-" + random.nextInt(1000, 10000);
    }

    private String generateRandomEmail(long memberId, Random random) {
        return ID_PREFIXES[random.nextInt(ID_PREFIXES.length)] + memberId + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
    }

    private void addUsageToBatch(List<Object[]> batchArgs, long usageId, long memberId, ItemInfo item, Random random) {
        LocalDateTime randomDate = generateDate(random);
        long amount = calculateAmount(item, random);
        batchArgs.add(new Object[]{usageId, memberId, item.getItemId(), Timestamp.valueOf(randomDate), amount});
    }

    private Map<String, List<ItemInfo>> loadAndGroupItems() {
        List<ItemInfo> allItems = jdbcTemplate.query(
            "SELECT item_id, category, price FROM ITEM",
            (rs, rowNum) -> new ItemInfo(rs.getLong("item_id"), rs.getString("category"), rs.getLong("price"))
        );
        return allItems.stream().collect(Collectors.groupingBy(ItemInfo::getCategory));
    }

    private ItemInfo getRandomItem(List<ItemInfo> items, Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private long calculateAmount(ItemInfo item, Random random) {
        if ("MICRO_PAYMENT".equals(item.getCategory())) {
            return random.nextLong(10, 1001) * 100;
        }
        return item.getPrice();
    }

    private LocalDateTime generateDate(Random random) {
        int year = 2025, month = 4;
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        int daysInMonth = startOfMonth.toLocalDate().lengthOfMonth();
        return startOfMonth.plusMinutes(random.nextLong(daysInMonth * 24 * 60));
    }

    @Getter
    @AllArgsConstructor
    private static class ItemInfo {
        private Long itemId;
        private String category;
        private Long price;
    }
}











