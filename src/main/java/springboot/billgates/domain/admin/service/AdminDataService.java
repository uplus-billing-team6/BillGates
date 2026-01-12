package springboot.billgates.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.admin.dto.DummyDataRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

    @Async("taskExecutor")
    @Transactional
    public void generateDummyData(DummyDataRequest request) {
        long startTime = System.currentTimeMillis();
        log.info(">>> [Admin] 더미 데이터 생성 작업을 시작합니다. (목표 Member: {}명)", request.getMemberCount());

        try {
            // 1. Member 데이터 생성
            if (request.getMemberCount() > 0) {
                createMembers(request.getMemberCount());
            }

            // 2. Usage History 데이터 생성 (다음 단계에서 구현 예정)
            // if (request.getUsageCount() > 0) { ... }

        } catch (Exception e) {
            log.error(">>> [Admin] 데이터 생성 중 오류 발생: ", e);
        }

        long endTime = System.currentTimeMillis();
        log.info(">>> [Admin] 모든 작업 완료! 총 소요 시간: {}ms", (endTime - startTime));
    }

    /**
     * [Member 대량 생성 로직]
     * - JDBC Batch Update 사용
     * - 기존 ID의 MAX 값을 조회하여 이어서 생성
     */
    private void createMembers(int totalCount) {
        log.info(">>> Member 데이터 생성을 시작합니다. ({}건)", totalCount);

        // 1. 시작 ID 계산 (기존 데이터 보존을 위해)
        Long maxId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(member_id), 0) FROM MEMBER", Long.class);
        long startId = maxId + 1;

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
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();

                // 진행 상황 로그 (10만 건 단위로만 출력하여 로그 과부하 방지)
                if ((i + 1) % 100000 == 0) {
                    log.info("Member Insert Progress: {} / {}", (i + 1), totalCount);
                }
            }
        }
        log.info(">>> Member {}건 생성 완료.", totalCount);
    }

    // --- 랜덤 데이터 생성 유틸 메서드 ---

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
}
