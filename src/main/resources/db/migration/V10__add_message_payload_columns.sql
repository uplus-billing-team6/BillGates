-- 메세지 테이블 컬럼 추가 -> 스케줄러용
ALTER TABLE MESSAGE
    ADD COLUMN title VARCHAR(255) NULL COMMENT '메시지 제목',
    ADD COLUMN content TEXT NULL COMMENT '메시지 본문',
    ADD COLUMN email VARCHAR(500) NULL COMMENT '수신자 이메일',
    ADD COLUMN phone_number VARCHAR(500) NULL COMMENT '수신자 전화번호';
