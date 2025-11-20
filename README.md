# ImageAPI

# DB Database 
create database naver_image;
use naver_image;

# DB table
CREATE TABLE search_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(50) NOT NULL,
    rank_num INT NOT NULL,
    title VARCHAR(255),
    original_image_url TEXT,   -- (변경) 원본 이미지 주소 (link)
    thumbnail_url TEXT,        -- (변경) 썸네일 주소 (thumbnail)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

select * from search_results;
