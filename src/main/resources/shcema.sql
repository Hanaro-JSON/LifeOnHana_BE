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
