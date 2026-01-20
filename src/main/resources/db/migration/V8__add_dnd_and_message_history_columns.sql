-- Member 테이블 컬럼 추가 -> 금지시간대 설정
ALTER TABLE MEMBER
    ADD COLUMN use_dnd BOOLEAN NOT NULL DEFAULT FALSE COMMENT '방해금지 사용 여부',
    ADD COLUMN dnd_start_time TIME NULL COMMENT '방해금지 시작 시간',
    ADD COLUMN dnd_end_time TIME NULL COMMENT '방해금지 종료 시간';


-- message_send_history 테이블 컬럼 추가 -> 스케줄러용
ALTER TABLE MESSAGE_SEND_HISTORY
    ADD COLUMN recipient VARCHAR(255) NULL COMMENT '수신자 정보 (전화번호 또는 이메일)',
    ADD COLUMN title VARCHAR(255) NULL COMMENT '메시지 제목',
    ADD COLUMN content TEXT NULL COMMENT '메시지 내용',
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SENT' COMMENT '전송 상태';
