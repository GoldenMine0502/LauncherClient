CREATE DATABASE inu_minecraft;

USE inu_minecraft;

CREATE TABLE microsoft_accounts(
	id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE KEY,
    password VARCHAR(50) NOT NULL,
    token_expire BIGINT(21) NOT NULL,
    minecraft_username VARCHAR(50) UNIQUE KEY,
    minecraft_uuid VARCHAR(40),
    server_borrowed INT(11) NOT NULL,
    server_borrowed_expire BIGINT(21) NOT NULL,
    server_joined INT(11) NOT NULL,
    server_quitted INT(11) NOT NULL,
    recent_accessed_ip VARCHAR(20),
    recent_code TEXT,
    recent_access_token TEXT,
    recent_refresh_token TEXT,
    recent_profile_token TEXT
);

ALTER TABLE microsoft_accounts DROP recent_code;

DELETE FROM microsoft_accounts WHERE id = 1;

SELECT * FROM microsoft_accounts;
DROP TABLE microsoft_accounts;
DESC microsoft_accounts;

CREATE TABLE microsoft_keys(
	id INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
	client_id VARCHAR(50) NOT NULL UNIQUE KEY,
    client_secret VARCHAR(50) NOT NULL
);

SELECT * FROM microsoft_keys;

DELETE FROM microsoft_keys WHERE id = 1;
DROP TABLE microsoft_keys;

SHOW TABLES;
DROP TABLE hibernate_sequence;

SELECT account.id FROM microsoft_accounts account WHERE account.token_expire <= 100000 OR account.token_expire IS NULL;