USE lifeonhana_localDB;

CREATE TABLE `article`(
                          `article_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `title` VARCHAR(255) NOT NULL,
                          `category` ENUM('') NOT NULL,
                          `thumbnail_s3_key` VARCHAR(255) NOT NULL,
                          `shorts` VARCHAR(255) NOT NULL,
                          `content` JSON NOT NULL,
                          `like_count` INT UNSIGNED NOT NULL,
                          `published_at` DATETIME NOT NULL
);
CREATE TABLE `user`(
                       `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       `name` VARCHAR(100) NOT NULL,
                       `auth_id` VARCHAR(100) NOT NULL COMMENT '이메일',
                       `password` VARCHAR(100) NULL,
                       `provider` ENUM('') NOT NULL,
                       `provider_id` VARCHAR(255) NOT NULL,
                       `birthday` VARCHAR(255) NOT NULL,
                       `sound_speed` ENUM('') NOT NULL,
                       `text_size` INT NOT NULL
);
CREATE TABLE `article_like`(
                               `user_id` BIGINT UNSIGNED NOT NULL,
                               `article_id` BIGINT UNSIGNED NOT NULL,
                               `is_like` BOOLEAN NOT NULL
);
CREATE TABLE `mydata`(
                         `mydata_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         `user_id` BIGINT UNSIGNED NOT NULL,
                         `total_asset` DECIMAL(15, 0) NOT NULL,
                         `deposit_amount` DECIMAL(15, 0) NOT NULL DEFAULT '0',
                         `savings_amount` DECIMAL(15, 0) NOT NULL DEFAULT '0',
                         `loan_amount` DECIMAL(15, 0) NOT NULL DEFAULT '0',
                         `stock_amount` DECIMAL(15, 0) NOT NULL DEFAULT '0',
                         `real_estate_amount` DECIMAL(15, 0) NOT NULL DEFAULT '0',
                         `last_updated_at` TIMESTAMP NOT NULL,
                         `pension_start_year` YEAR NOT NULL
);
CREATE TABLE `salary`(
                         `salary_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         `user_id` BIGINT UNSIGNED NOT NULL,
                         `salary_amount` BIGINT NOT NULL,
                         `payment_day` ENUM('') NOT NULL COMMENT '1,15',
                         `start_date` TIMESTAMP NOT NULL,
                         `end_date` TIMESTAMP NOT NULL COMMENT '수정은 이것만 기본 100세'
);
CREATE TABLE `history`(
                          `history_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `user_id` BIGINT UNSIGNED NOT NULL,
                          `category` ENUM(
        '식비',
        '커피/간식',
        '교육',
        '취미/여가',
        '건강',
        '고정지출',
        '여행',
        '기타',
        '입금',
        '이자'
    ) NOT NULL,
                          `amount` DECIMAL(15, 0) NOT NULL,
                          `description` VARCHAR(100) NOT NULL,
                          `history_datetime` DATETIME NOT NULL,
                          `is_fixed` BOOLEAN NOT NULL DEFAULT '0' COMMENT '고정지출 여부',
                          `is_expense` BOOLEAN NOT NULL COMMENT 'false = +
true = -'
);
CREATE TABLE `dictionary`(
                             `dictionary_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             `word` VARCHAR(100) NOT NULL,
                             `description` VARCHAR(255) NOT NULL
);
CREATE TABLE `lump_sum`(
                           `lump_sum_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           `user_id` BIGINT UNSIGNED NOT NULL,
                           `amount` DECIMAL(15, 0) NOT NULL,
                           `source` ENUM('') NOT NULL,
                           `reason` ENUM('') NOT NULL,
                           `reason_detail` VARCHAR(255) NULL,
                           `request_date` DATETIME NOT NULL
);
CREATE TABLE `article_dictionary`(
                                     `article_id` BIGINT UNSIGNED NOT NULL,
                                     `dictionary_id` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `product`(
                          `product_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `category` ENUM('') NOT NULL,
                          `name` VARCHAR(255) NOT NULL,
                          `description` VARCHAR(255) NOT NULL,
                          `feature` VARCHAR(255) NULL,
                          `target` VARCHAR(255) NULL,
                          `link` VARCHAR(255) NOT NULL,
                          `min_amount` DECIMAL(15, 0) NULL,
                          `max_amount` DECIMAL(15, 0) NULL,
                          `basic_interest_rate` DECIMAL(5, 2) NULL,
                          `max_interest_rate` DECIMAL(5, 2) NULL,
                          `min_period` INT NULL,
                          `max_period` INT NULL,
                          `min_credit_score` BIGINT NULL
);
CREATE TABLE `article_product`(
                                  `article_id` BIGINT UNSIGNED NOT NULL,
                                  `product_id` BIGINT UNSIGNED NOT NULL
);
CREATE TABLE `account`(
                          `account_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `mydata_id` BIGINT UNSIGNED NOT NULL,
                          `bank` ENUM('') NOT NULL,
                          `account_number` VARCHAR(255) NOT NULL,
                          `account_name` VARCHAR(255) NOT NULL,
                          `balance` DECIMAL(8, 2) NOT NULL DEFAULT '0',
                          `is_main` BOOLEAN NOT NULL
);
CREATE TABLE `product_like`(
                               `user_id` BIGINT UNSIGNED NOT NULL,
                               `product_id` BIGINT UNSIGNED NOT NULL,
                               `is_like` BOOLEAN NOT NULL
);
CREATE TABLE `whilick`(
                          `whilick_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `article_id` BIGINT UNSIGNED NOT NULL,
                          `paragraph_id` BIGINT NOT NULL,
                          `paragraph` VARCHAR(255) NOT NULL,
                          `start_time` TIMESTAMP NOT NULL,
                          `end_time` TIMESTAMP NOT NULL
);
ALTER TABLE
    `whilick` ADD CONSTRAINT `whilick_article_id_foreign` FOREIGN KEY(`article_id`) REFERENCES `article`(`article_id`);
ALTER TABLE
    `history` ADD CONSTRAINT `history_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
ALTER TABLE
    `article_product` ADD CONSTRAINT `article_product_article_id_foreign` FOREIGN KEY(`article_id`) REFERENCES `article`(`article_id`);
ALTER TABLE
    `product_like` ADD CONSTRAINT `product_like_product_id_foreign` FOREIGN KEY(`product_id`) REFERENCES `product`(`product_id`);
ALTER TABLE
    `account` ADD CONSTRAINT `account_mydata_id_foreign` FOREIGN KEY(`mydata_id`) REFERENCES `mydata`(`mydata_id`);
ALTER TABLE
    `lump_sum` ADD CONSTRAINT `lump_sum_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
ALTER TABLE
    `article_product` ADD CONSTRAINT `article_product_product_id_foreign` FOREIGN KEY(`product_id`) REFERENCES `product`(`product_id`);
ALTER TABLE
    `salary` ADD CONSTRAINT `salary_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
ALTER TABLE
    `article_like` ADD CONSTRAINT `article_like_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
ALTER TABLE
    `article_like` ADD CONSTRAINT `article_like_article_id_foreign` FOREIGN KEY(`article_id`) REFERENCES `article`(`article_id`);
ALTER TABLE
    `article_dictionary` ADD CONSTRAINT `article_dictionary_dictionary_id_foreign` FOREIGN KEY(`dictionary_id`) REFERENCES `dictionary`(`dictionary_id`);
ALTER TABLE
    `product_like` ADD CONSTRAINT `product_like_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
ALTER TABLE
    `article_dictionary` ADD CONSTRAINT `article_dictionary_article_id_foreign` FOREIGN KEY(`article_id`) REFERENCES `article`(`article_id`);
ALTER TABLE
    `mydata` ADD CONSTRAINT `mydata_user_id_foreign` FOREIGN KEY(`user_id`) REFERENCES `user`(`user_id`);
