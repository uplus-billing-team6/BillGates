-- billing_id 조회 성능용 인덱스
CREATE INDEX idx_billing_id
ON billing_discount (billing_id);
