-- V12__verify_usage_history_migration.sql
-- 목적: 이관 데이터 정합성 검증

-- 1. 전체 row 수 비교
SELECT
    (SELECT COUNT(*) FROM USAGE_HISTORY)   AS origin_count,
    (SELECT COUNT(*) FROM USAGE_HISTORY_P) AS partition_count;

-- 2. 월별 데이터 분포 비교 (기존 테이블)
SELECT
    DATE_FORMAT(usage_date, '%Y-%m') AS ym,
    COUNT(*) AS cnt
FROM USAGE_HISTORY
GROUP BY ym
ORDER BY ym;

-- 3. 월별 데이터 분포 비교 (파티션 테이블)
SELECT
    DATE_FORMAT(usage_date, '%Y-%m') AS ym,
    COUNT(*) AS cnt
FROM USAGE_HISTORY_P
GROUP BY ym
ORDER BY ym;

-- 4. 실제 파티션별 row 분포 확인
SELECT
    PARTITION_NAME,
    TABLE_ROWS
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'USAGE_HISTORY_P'
ORDER BY PARTITION_NAME;
