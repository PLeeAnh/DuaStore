-- Thêm cột giá lúc thêm vào giỏ để detect price change
ALTER TABLE CartItems ADD GiaLucThem DECIMAL(12,0) NULL;
