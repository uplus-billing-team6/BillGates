-- BILLING 테이블에 금액 상세 컬럼 추가

ALTER TABLE billing
    ADD COLUMN original_amount BIGINT DEFAULT 0 COMMENT '할인 전 원금 합계',
    ADD COLUMN total_discount_amount BIGINT DEFAULT 0 COMMENT '총 할인 금액';
