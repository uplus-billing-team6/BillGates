-- 메세지 히스토리 테이블 컬럼 삭제
-- 히스토리는 로그성 테이블이라 변경 가능 x 때문
ALTER TABLE MESSAGE_SEND_HISTORY
    DROP COLUMN recipient,
    DROP COLUMN title,
    DROP COLUMN content,
    DROP COLUMN status;

