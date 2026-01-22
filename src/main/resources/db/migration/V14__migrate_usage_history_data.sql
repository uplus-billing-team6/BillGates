-- 목적: 기존 USAGE_HISTORY 데이터를 파티션 테이블로 이관
INSERT INTO USAGE_HISTORY_P (
    usage_id,
    member_id,
    item_id,
    usage_date,
    amount
)
SELECT
    usage_id,
    member_id,
    item_id,
    usage_date,
    amount
FROM USAGE_HISTORY;