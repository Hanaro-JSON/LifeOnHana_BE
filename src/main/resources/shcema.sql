USE lifeonhana_localDB;

CREATE TABLE IF NOT EXISTS User(
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(127) NOT NULL,
    user_gender VARCHAR(1) NOT NULL,
    user_email VARCHAR(127) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    user_birth VARCHAR(8) NOT NULL,
    user_registered_date DATE NOT NULL,
    user_kakao_id VARCHAR(255)
);
