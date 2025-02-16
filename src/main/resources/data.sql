-- 제품 데이터 생성
INSERT INTO products (id, name, stock_quantity, created_at, updated_at)
VALUES ('ac5720dc-90fb-4891-aa9d-910248f9fd25', 'product-1', 100, current_timestamp, current_timestamp),
       ('6efce639-3a34-46e0-9658-24f3e8d502be', 'product-2', 200, current_timestamp, current_timestamp);

-- INBOUND 재고 이력 데이터 생성
INSERT INTO product_stock_histories (id, product_id, type, quantity, order_id, created_at, updated_at)
VALUES ('02cbae69-42bd-4c04-948a-b08ef6f708b2', 'ac5720dc-90fb-4891-aa9d-910248f9fd25', 'INBOUND', 100, NULL, current_timestamp, current_timestamp),
       ('1d9d0703-e48c-484e-aee6-d671e5604e59', '6efce639-3a34-46e0-9658-24f3e8d502be', 'INBOUND', 200, NULL, current_timestamp, current_timestamp);
