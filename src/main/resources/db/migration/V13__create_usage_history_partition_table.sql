-- 목적: USAGE_HISTORY 파티션 테이블 신규 생성 (월 단위 RANGE 파티션)

CREATE TABLE USAGE_HISTORY_P (
    usage_id   BIGINT NOT NULL,
    member_id  BIGINT NOT NULL,
    item_id    BIGINT NOT NULL,
    usage_date DATETIME NOT NULL,
    amount     BIGINT NOT NULL,

    -- 파티션 테이블 제약:
    -- 파티션 키(usage_date)는 반드시 PK에 포함되어야 함
    PRIMARY KEY (usage_id, usage_date),

    -- 기존 조회 패턴 유지용 인덱스
    KEY idx_usage_member_date (member_id, usage_date)
)
PARTITION BY RANGE (TO_DAYS(usage_date)) (
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION pmax    VALUES LESS THAN MAXVALUE
);
