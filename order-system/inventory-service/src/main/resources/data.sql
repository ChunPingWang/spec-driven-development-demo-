-- Seed data for inventory service

-- Insert sample product (iPhone 17 Pro Max) if not exists
MERGE INTO products (product_id, product_name, stock_quantity, created_at, updated_at)
KEY (product_id)
VALUES ('IPHONE-17-PRO-MAX', 'iPhone 17 Pro Max', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
