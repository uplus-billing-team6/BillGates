-- 예약시간 테이블
CREATE TABLE RESERVATION_SETTING (
    setting_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '설정 ID',
    is_reservation_active BOOLEAN NOT NULL DEFAULT FALSE COMMENT '예약 발송 기능 사용 여부',
    reservation_time TIME NOT NULL DEFAULT '04:00:00' COMMENT '예약 발송 시간',
    PRIMARY KEY (setting_id)
);
