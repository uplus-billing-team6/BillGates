-- 예약 설정 초기 데이터 (단일 Row 관리)
INSERT INTO reservation_setting (setting_id, is_reservation_active, reservation_time)
SELECT 1, FALSE, '04:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM reservation_setting WHERE setting_id = 1
);

