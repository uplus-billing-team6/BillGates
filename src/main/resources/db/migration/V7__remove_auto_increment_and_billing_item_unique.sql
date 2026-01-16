-- 1. FK 제거
ALTER TABLE billing_item
DROP FOREIGN KEY fk_billing_item_billing;

-- 2. BILLING : PK 유지 + AUTO_INCREMENT 제거
ALTER TABLE billing
MODIFY billing_id BIGINT NOT NULL;

-- 3. MESSAGE : PK 유지 + AUTO_INCREMENT 제거
ALTER TABLE message
MODIFY message_id BIGINT NOT NULL;

-- 4. BILLING_ITEM UNIQUE 제거
ALTER TABLE billing_item
DROP INDEX uk_billing_item;

-- 5. FK 복구
ALTER TABLE billing_item
ADD CONSTRAINT fk_billing_item_billing
FOREIGN KEY (billing_id)
REFERENCES billing(billing_id);



