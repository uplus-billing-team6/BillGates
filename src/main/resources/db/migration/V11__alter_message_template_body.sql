-- message template body 길이 text 변환
-- 가변 길이 본문
-- 확장 채널 대응
ALTER TABLE MESSAGE_TEMPLATE
MODIFY COLUMN body TEXT NOT NULL COMMENT '메시지 본문';

