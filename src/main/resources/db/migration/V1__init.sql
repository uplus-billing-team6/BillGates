-- 사용이력 --
CREATE TABLE `USAGE_HISTORY` (
    `usage_id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `item_id` BIGINT NOT NULL,
    `usage_date` DATETIME NOT NULL,
    `amount` BIGINT NOT NULL,
    PRIMARY KEY (`usage_id`)
);

-- 상품정보 --
CREATE TABLE `ITEM` (
    `item_id` BIGINT NOT NULL AUTO_INCREMENT,
    `category` VARCHAR(100) NOT NULL COMMENT 'PLAN / ADDON / ROAMING_PASS / MICRO_PAYMENT',
    `name` VARCHAR(200) NOT NULL,
    `price` BIGINT NOT NULL,
    PRIMARY KEY (`item_id`)
);

-- 사용자 --
CREATE TABLE `MEMBER` (
    `member_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(500) NOT NULL,
    `phone_number` VARCHAR(500) NOT NULL,
    PRIMARY KEY (`member_id`)
);

-- 메세지 템플릿 --
CREATE TABLE `MESSAGE_TEMPLATE` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT,
    `channel` VARCHAR(50) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `body` VARCHAR(500) NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`template_id`)
);

-- 정산 결과 --
CREATE TABLE `BILLING` (
    `billing_id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `billing_month` CHAR(7) NOT NULL,     -- YYYY-MM (정산 기준 월)
    `created_at` DATETIME NOT NULL,     -- 생성 시각
    `total_amount` BIGINT NOT NULL,

    PRIMARY KEY (`billing_id`),
    UNIQUE KEY `uk_member_billing_month` (`member_id`, `billing_month`)
);

-- 정산 결과 상세 --
CREATE TABLE `BILLING_ITEM` (
    `billing_item_id` BIGINT NOT NULL AUTO_INCREMENT,
    `billing_id` BIGINT NOT NULL,
    `category` VARCHAR(100) NOT NULL,
    `item_name` VARCHAR(200) NOT NULL,
    `amount` BIGINT NOT NULL,
    PRIMARY KEY (`billing_item_id`),
    CONSTRAINT `fk_billing_item_billing`
        FOREIGN KEY (`billing_id`) REFERENCES `BILLING`(`billing_id`)
);

-- 메세지 --
CREATE TABLE `MESSAGE` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `billing_id` BIGINT NOT NULL,
    `channel` VARCHAR(50) NOT NULL,
    `status` VARCHAR(50) NOT NULL COMMENT 'READY, SENT, FAILED',
    `reserved_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    `template_code` BIGINT NOT NULL,
    PRIMARY KEY (`message_id`),
    UNIQUE KEY `uk_billing_channel` (`billing_id`, `channel`)

);

-- 메세지 발송 이력 --
CREATE TABLE `MESSAGE_SEND_HISTORY` (
    `history_id` BIGINT NOT NULL AUTO_INCREMENT,
    `message_id` BIGINT NOT NULL,
    `channel` VARCHAR(50) NOT NULL,
    `success` BOOLEAN NOT NULL DEFAULT true,
    `sent_at` DATETIME NOT NULL,
    PRIMARY KEY (`history_id`)
);