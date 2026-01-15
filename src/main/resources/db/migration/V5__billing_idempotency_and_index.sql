-- BILLING 테이블 멱등성 보장
-- 한 회원(member_id)은 한 billing_month에 1건만 존재
ALTER TABLE BILLING
ADD UNIQUE KEY uk_billing_member_month (member_id, billing_month);

-- BILLING_ITEM 테이블 중복 방지
-- 하나의 billing_id 내에서 동일 항목은 1번만 존재
ALTER TABLE BILLING_ITEM
ADD UNIQUE KEY uk_billing_item (billing_id, category, item_name);
