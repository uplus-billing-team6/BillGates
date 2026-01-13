INSERT IGNORE INTO `ITEM` (`item_id`, `category`, `name`, `price`) VALUES
-- 요금제 (PLAN)
(1, 'PLAN', '(5G) 프리미어 에센셜', 85000),
(2, 'PLAN', '(5G) 5G 스탠다드', 75000),
(3, 'PLAN', '(5G) 5G 프리미어 레귤러', 95000),
(4, 'PLAN', '(5G) 유쓰 5G 스탠다드', 75000),
(5, 'PLAN', '(5G) 5G 데이터 레귤러', 63000),
(6, 'PLAN', '(LTE) 현역병사 데이터 55', 55000),
(7, 'PLAN', '(LTE) 추가 요금 걱정 없는 데이터 시니어 69', 69000),
(8, 'PLAN', '(LTE) 복지 33', 33000),
(9, 'PLAN', '(LTE) 키즈 22 (만 12세 이하)', 22000),
(10, 'PLAN', '(LTE) 추가 요금 걱정 없는 데이터 청소년 59', 59000),

-- 부가서비스 (ADDON)
(11, 'ADDON', '벨링모아A(벨소리,통화연결음,MP3)+통화가능안내', 2750),
(12, 'ADDON', 'V컬러링+통화가능안내', 3850),
(13, 'ADDON', '매너콜', 1100),
(14, 'ADDON', '통화가능안내', 550),
(15, 'ADDON', '듀얼넘버 온앤오프', 3850),

-- 소액 결제 (MICRO_PAYMENT)
(16, 'MICRO_PAYMENT', '인게임 구매', 0),
(17, 'MICRO_PAYMENT', '앱 구매', 0),
(18, 'MICRO_PAYMENT', '기프티콘 구매', 0),
(19, 'MICRO_PAYMENT', '앱 내 구독', 0),
(20, 'MICRO_PAYMENT', '음악 스트리밍', 0),

-- 로밍 패스 (ROAMING_PASS)
(21, 'ROAMING_PASS', '로밍패스 4GB', 29000),
(22, 'ROAMING_PASS', '프로모션 로밍패스 17GB', 44000),
(23, 'ROAMING_PASS', '프로모션 로밍패스 27GB', 59000),
(24, 'ROAMING_PASS', '프로모션 로밍패스 51GB', 79000),
(25, 'ROAMING_PASS', '아시아 로밍패스 7GB', 39000);