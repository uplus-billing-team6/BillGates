-- =====================================================
-- 1. 청구 할인 이력 테이블 (영수증 상세)
-- =====================================================

CREATE TABLE billing_discount (
    discount_id     BIGINT       NOT NULL,
    billing_id      BIGINT       NOT NULL,
    policy_id       BIGINT       NOT NULL,
    discount_name   VARCHAR(100) NOT NULL,
    discount_amount BIGINT       NOT NULL,
    target_category VARCHAR(50)  NULL,
    PRIMARY KEY (discount_id)
);

-- =====================================================
-- 2. 회원별 보유 할인 정책 테이블 (입력값)
-- =====================================================

CREATE TABLE member_discount_policy (
    id        BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_policy (member_id, policy_id)
);

-- =====================================================
-- 3. 메시지 템플릿 수정 (EMAIL)
-- =====================================================

UPDATE message_template
SET body = '[수신: {recipient}]

안녕하세요, 고객님.
BillGates 서비스를 이용해 주셔서 감사합니다.
{month} 이용 요금 명세서를 안내해 드립니다.

■ 청구 요약
- 기본 요금 합계: {originalAmount}원
- 할인 요금 합계: {discountAmount}원
────────────────────────
👉 최종 청구 금액: {totalAmount}원

■ 상세 이용 내역
{itemList}

■ 할인 적용 내역
{discountList}

납부 기한 내에 납부 부탁드립니다.
감사합니다.'
WHERE template_id = 1;

-- =====================================================
-- 4. 메시지 템플릿 수정 (SMS)
-- =====================================================

UPDATE message_template
SET body = '[수신: {recipient}]

[BillGates] {month} 요금 안내
고객님, 이번 달 청구 금액을 안내해 드립니다.

■ 청구 요약
- 기본 요금: {originalAmount}원
- 할인 금액: {discountAmount}원
────────────────
▶ 납부 금액: {totalAmount}원

■ 상세 이용 내역
{itemList}

■ 할인 적용 내역
{discountList}

납부 기한 내 수납 바랍니다.
감사합니다.'
WHERE template_id = 2;
