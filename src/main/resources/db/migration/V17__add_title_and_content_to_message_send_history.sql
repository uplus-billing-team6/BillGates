-- 메시지 발송 이력 테이블에 제목, 본문 컬럼 추가

ALTER TABLE MESSAGE_SEND_HISTORY
    ADD COLUMN title VARCHAR(255) NULL COMMENT '메시지 제목',
    ADD COLUMN content TEXT NULL COMMENT '메시지 본문';