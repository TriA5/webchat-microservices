-- Fix video URL column length
-- Chạy lệnh này trong MySQL để tăng độ dài cột url và thumbnail_url

USE db_poster_service;

ALTER TABLE video MODIFY COLUMN url VARCHAR(2000) NOT NULL;
ALTER TABLE video MODIFY COLUMN thumbnail_url VARCHAR(2000);

-- Kiểm tra lại cấu trúc table
DESCRIBE video;
