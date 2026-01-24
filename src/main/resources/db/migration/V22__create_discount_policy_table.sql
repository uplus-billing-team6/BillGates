-- 할인 정책 테이블 생성

CREATE TABLE discount_policy (
    policy_id       BIGINT       NOT NULL COMMENT '할인 정책 ID',
    name            VARCHAR(100) NOT NULL COMMENT '할인 명칭',
    discount_type   VARCHAR(20)  NOT NULL COMMENT 'PERCENT(정률) / FIXED(정액)',
    discount_value  BIGINT       NOT NULL COMMENT '할인 값 (퍼센트 or 금액)',
    target_category VARCHAR(50)  NOT NULL COMMENT '적용 대상 카테고리',
    PRIMARY KEY (policy_id)
);

-- 초기 할인 정책 데이터 삽입

INSERT INTO discount_policy (policy_id, name, discount_type, discount_value, target_category)
VALUES
    (1, '선택약정 할인(25%)', 'PERCENT', 25,   'PLAN'),
    (2, 'LTE/5G 프리미어 약정 할인', 'FIXED',   5250, 'PLAN'),
    (3, '복지 감면 할인(35%)', 'PERCENT', 35,   'PLAN'),
    (4, 'U+ 투게더 결합 할인', 'FIXED',   10000,'PLAN');
