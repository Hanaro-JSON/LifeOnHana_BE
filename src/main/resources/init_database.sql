CREATE DATABASE IF NOT EXISTS lifeonhana_localDB;
CREATE USER IF NOT EXISTS 'LifeOnHana'@'localhost' IDENTIFIED BY 'LifeOnHana1!';
GRANT ALL PRIVILEGES ON lifeonhana_localDB.* TO 'LifeOnHana'@'localhost';
FLUSH PRIVILEGES;

/*
로컬 개발 환경 설정
데이터베이스 초기 설정
1. MySQL이 설치되어 있지 않다면 설치.
2. MySQL 루트 계정으로 로그인 후 init_database.sql 실행:
   mysql -u root -p
*/
