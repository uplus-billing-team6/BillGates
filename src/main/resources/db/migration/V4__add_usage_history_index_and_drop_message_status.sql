-- USAGE_HISTORY: 회원 + 기간 조회 성능 개선용 인덱스 추가
CREATE INDEX idx_usage_member_date
ON USAGE_HISTORY (member_id, usage_date);

-- MESSAGE_SEND_HISTORY: 사용하지 않는 status 컬럼 제거
ALTER TABLE MESSAGE_SEND_HISTORY
DROP COLUMN status;
