USE chopee_db;
SET NAMES utf8mb4;

-- 1. Cà chua beef Đà Lạt mọng nước (id=1)
UPDATE products SET
  tier_variation = '[{"name":"Loại","options":["Tươi ngon","Sấy dẻo / Khô"]},{"name":"Quy cách đóng gói","options":["Túi 500g","Túi 1.0 kg","Túi 2.0 kg"]}]',
  has_variants = TRUE,
  selling_price = 14000.00
WHERE id = 1;

DELETE FROM product_variants WHERE product_id = 1;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(1, 'Tươi ngon - Túi 500g', 'CACHUA-TUOI-500G', '{"Loại":"Tươi ngon","Quy cách đóng gói":"Túi 500g"}', 14000.00, 100),
(1, 'Tươi ngon - Túi 1.0 kg', 'CACHUA-TUOI-1KG', '{"Loại":"Tươi ngon","Quy cách đóng gói":"Túi 1.0 kg"}', 28000.00, 100),
(1, 'Tươi ngon - Túi 2.0 kg', 'CACHUA-TUOI-2KG', '{"Loại":"Tươi ngon","Quy cách đóng gói":"Túi 2.0 kg"}', 55000.00, 80),
(1, 'Sấy dẻo / Khô - Túi 500g', 'CACHUA-KHO-500G', '{"Loại":"Sấy dẻo / Khô","Quy cách đóng gói":"Túi 500g"}', 35000.00, 50),
(1, 'Sấy dẻo / Khô - Túi 1.0 kg', 'CACHUA-KHO-1KG', '{"Loại":"Sấy dẻo / Khô","Quy cách đóng gói":"Túi 1.0 kg"}', 68000.00, 50),
(1, 'Sấy dẻo / Khô - Túi 2.0 kg', 'CACHUA-KHO-2KG', '{"Loại":"Sấy dẻo / Khô","Quy cách đóng gói":"Túi 2.0 kg"}', 130000.00, 40);

-- 2. Dưa leo baby giòn ngọt VietGAP (id=2)
UPDATE products SET
  tier_variation = '[{"name":"Loại","options":["Tươi giòn","Ngâm chua ngọt"]},{"name":"Quy cách đóng gói","options":["Túi 500g","Túi 1.0 kg","Túi 2.0 kg"]}]',
  has_variants = TRUE,
  selling_price = 12000.00
WHERE id = 2;

DELETE FROM product_variants WHERE product_id = 2;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(2, 'Tươi giòn - Túi 500g', 'DUALEO-TUOI-500G', '{"Loại":"Tươi giòn","Quy cách đóng gói":"Túi 500g"}', 12000.00, 80),
(2, 'Tươi giòn - Túi 1.0 kg', 'DUALEO-TUOI-1KG', '{"Loại":"Tươi giòn","Quy cách đóng gói":"Túi 1.0 kg"}', 24000.00, 80),
(2, 'Tươi giòn - Túi 2.0 kg', 'DUALEO-TUOI-2KG', '{"Loại":"Tươi giòn","Quy cách đóng gói":"Túi 2.0 kg"}', 46000.00, 60),
(2, 'Ngâm chua ngọt - Túi 500g', 'DUALEO-CHUA-500G', '{"Loại":"Ngâm chua ngọt","Quy cách đóng gói":"Túi 500g"}', 22000.00, 40),
(2, 'Ngâm chua ngọt - Túi 1.0 kg', 'DUALEO-CHUA-1KG', '{"Loại":"Ngâm chua ngọt","Quy cách đóng gói":"Túi 1.0 kg"}', 42000.00, 40),
(2, 'Ngâm chua ngọt - Túi 2.0 kg', 'DUALEO-CHUA-2KG', '{"Loại":"Ngâm chua ngọt","Quy cách đóng gói":"Túi 2.0 kg"}', 80000.00, 30);

-- 3. Thịt ba chỉ heo sạch chuẩn CP (id=7)
UPDATE products SET
  tier_variation = '[{"name":"Phân loại thịt","options":["Ba chỉ rút sườn","Sườn non heo","Nạc dăm"]},{"name":"Khối lượng khay","options":["Khay 300g","Khay 500g","Khay 1.0 kg"]}]',
  has_variants = TRUE,
  selling_price = 45000.00
WHERE id = 7;

DELETE FROM product_variants WHERE product_id = 7;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(7, 'Ba chỉ rút sườn - Khay 300g', 'HEO-BACHI-300G', '{"Phân loại thịt":"Ba chỉ rút sườn","Khối lượng khay":"Khay 300g"}', 45000.00, 50),
(7, 'Ba chỉ rút sườn - Khay 500g', 'HEO-BACHI-500G', '{"Phân loại thịt":"Ba chỉ rút sườn","Khối lượng khay":"Khay 500g"}', 75000.00, 50),
(7, 'Ba chỉ rút sườn - Khay 1.0 kg', 'HEO-BACHI-1KG', '{"Phân loại thịt":"Ba chỉ rút sườn","Khối lượng khay":"Khay 1.0 kg"}', 145000.00, 40),
(7, 'Sườn non heo - Khay 300g', 'HEO-SUON-300G', '{"Phân loại thịt":"Sườn non heo","Khối lượng khay":"Khay 300g"}', 55000.00, 40),
(7, 'Sườn non heo - Khay 500g', 'HEO-SUON-500G', '{"Phân loại thịt":"Sườn non heo","Khối lượng khay":"Khay 500g"}', 90000.00, 40),
(7, 'Sườn non heo - Khay 1.0 kg', 'HEO-SUON-1KG', '{"Phân loại thịt":"Sườn non heo","Khối lượng khay":"Khay 1.0 kg"}', 175000.00, 30),
(7, 'Nạc dăm - Khay 300g', 'HEO-NAC-300G', '{"Phân loại thịt":"Nạc dăm","Khối lượng khay":"Khay 300g"}', 40000.00, 40),
(7, 'Nạc dăm - Khay 500g', 'HEO-NAC-500G', '{"Phân loại thịt":"Nạc dăm","Khối lượng khay":"Khay 500g"}', 65000.00, 40),
(7, 'Nạc dăm - Khay 1.0 kg', 'HEO-NAC-1KG', '{"Phân loại thịt":"Nạc dăm","Khối lượng khay":"Khay 1.0 kg"}', 125000.00, 30);

-- 4. Bàn phím cơ Keychron K2 Pro (id=26)
UPDATE products SET
  tier_variation = '[{"name":"Layout","options":["Layout 75%","TKL 87 phím"]},{"name":"Switch","options":["Red Switch (Êm)","Brown Switch (Khấc)","Blue Switch (Clicky)"]}]',
  has_variants = TRUE,
  selling_price = 1650000.00
WHERE id = 26;

DELETE FROM product_variants WHERE product_id = 26;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(26, 'Layout 75% - Red Switch (Êm)', 'K2PRO-75-RED', '{"Layout":"Layout 75%","Switch":"Red Switch (Êm)"}', 1650000.00, 25),
(26, 'Layout 75% - Brown Switch (Khấc)', 'K2PRO-75-BROWN', '{"Layout":"Layout 75%","Switch":"Brown Switch (Khấc)"}', 1650000.00, 20),
(26, 'Layout 75% - Blue Switch (Clicky)', 'K2PRO-75-BLUE', '{"Layout":"Layout 75%","Switch":"Blue Switch (Clicky)"}', 1650000.00, 15),
(26, 'TKL 87 phím - Red Switch (Êm)', 'K2PRO-87-RED', '{"Layout":"TKL 87 phím","Switch":"Red Switch (Êm)"}', 1750000.00, 20),
(26, 'TKL 87 phím - Brown Switch (Khấc)', 'K2PRO-87-BROWN', '{"Layout":"TKL 87 phím","Switch":"Brown Switch (Khấc)"}', 1750000.00, 15),
(26, 'TKL 87 phím - Blue Switch (Clicky)', 'K2PRO-87-BLUE', '{"Layout":"TKL 87 phím","Switch":"Blue Switch (Clicky)"}', 1750000.00, 10);

-- 5. Áo thun nam UniStyle (id=29)
UPDATE products SET
  tier_variation = '[{"name":"Màu sắc","options":["Trắng Basic","Đen Tuyền","Xám Tiêu"]},{"name":"Kích cỡ","options":["Size M","Size L","Size XL"]}]',
  has_variants = TRUE,
  selling_price = 180000.00
WHERE id = 29;

DELETE FROM product_variants WHERE product_id = 29;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(29, 'Trắng Basic - Size M', 'UNISTYLE-W-M', '{"Màu sắc":"Trắng Basic","Kích cỡ":"Size M"}', 180000.00, 30),
(29, 'Trắng Basic - Size L', 'UNISTYLE-W-L', '{"Màu sắc":"Trắng Basic","Kích cỡ":"Size L"}', 180000.00, 30),
(29, 'Trắng Basic - Size XL', 'UNISTYLE-W-XL', '{"Màu sắc":"Trắng Basic","Kích cỡ":"Size XL"}', 180000.00, 20),
(29, 'Đen Tuyền - Size M', 'UNISTYLE-B-M', '{"Màu sắc":"Đen Tuyền","Kích cỡ":"Size M"}', 180000.00, 30),
(29, 'Đen Tuyền - Size L', 'UNISTYLE-B-L', '{"Màu sắc":"Đen Tuyền","Kích cỡ":"Size L"}', 180000.00, 30),
(29, 'Đen Tuyền - Size XL', 'UNISTYLE-B-XL', '{"Màu sắc":"Đen Tuyền","Kích cỡ":"Size XL"}', 180000.00, 20),
(29, 'Xám Tiêu - Size M', 'UNISTYLE-G-M', '{"Màu sắc":"Xám Tiêu","Kích cỡ":"Size M"}', 180000.00, 25),
(29, 'Xám Tiêu - Size L', 'UNISTYLE-G-L', '{"Màu sắc":"Xám Tiêu","Kích cỡ":"Size L"}', 180000.00, 25),
(29, 'Xám Tiêu - Size XL', 'UNISTYLE-G-XL', '{"Màu sắc":"Xám Tiêu","Kích cỡ":"Size XL"}', 180000.00, 15);

-- 6. Tai nghe Sony WH-1000XM5 (id=23) - 1 tier
UPDATE products SET
  tier_variation = '[{"name":"Màu sắc","options":["Đen Nhám (Matte Black)","Bạc Ánh Kim (Silver)","Xanh Navy (Midnight Blue)"]}]',
  has_variants = TRUE,
  selling_price = 7990000.00
WHERE id = 23;

DELETE FROM product_variants WHERE product_id = 23;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(23, 'Đen Nhám (Matte Black)', 'WH1000XM5-BLACK', '{"Màu sắc":"Đen Nhám (Matte Black)"}', 7990000.00, 30),
(23, 'Bạc Ánh Kim (Silver)', 'WH1000XM5-SILVER', '{"Màu sắc":"Bạc Ánh Kim (Silver)"}', 7990000.00, 25),
(23, 'Xanh Navy (Midnight Blue)', 'WH1000XM5-NAVY', '{"Màu sắc":"Xanh Navy (Midnight Blue)"}', 8190000.00, 15);

-- 7. Thùng Bia Tiger Crystal 330ml (id=12) - 1 tier
UPDATE products SET
  tier_variation = '[{"name":"Quy cách đóng gói","options":["Lon lẻ 330ml","Lốc 6 lon","Thùng 24 lon"]}]',
  has_variants = TRUE,
  selling_price = 17000.00
WHERE id = 12;

DELETE FROM product_variants WHERE product_id = 12;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(12, 'Lon lẻ 330ml', 'TIGER-LON-330', '{"Quy cách đóng gói":"Lon lẻ 330ml"}', 17000.00, 300),
(12, 'Lốc 6 lon', 'TIGER-LOC-6', '{"Quy cách đóng gói":"Lốc 6 lon"}', 99000.00, 150),
(12, 'Thùng 24 lon', 'TIGER-THUNG-24', '{"Quy cách đóng gói":"Thùng 24 lon"}', 385000.00, 100);

-- 8. Nồi chiên không dầu Philips HD9252 (id=17)
UPDATE products SET
  tier_variation = '[{"name":"Phiên bản dung tích","options":["Bản 4.1L (Gia đình nhỏ)","Bản 6.2L XXL (Gia đình lớn)"]},{"name":"Màu sắc","options":["Đen bóng","Trắng ngọc trai"]}]',
  has_variants = TRUE,
  selling_price = 1850000.00
WHERE id = 17;

DELETE FROM product_variants WHERE product_id = 17;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(17, 'Bản 4.1L (Gia đình nhỏ) - Đen bóng', 'PHILIPS-41L-BLK', '{"Phiên bản dung tích":"Bản 4.1L (Gia đình nhỏ)","Màu sắc":"Đen bóng"}', 1850000.00, 25),
(17, 'Bản 4.1L (Gia đình nhỏ) - Trắng ngọc trai', 'PHILIPS-41L-WHT', '{"Phiên bản dung tích":"Bản 4.1L (Gia đình nhỏ)","Màu sắc":"Trắng ngọc trai"}', 1950000.00, 20),
(17, 'Bản 6.2L XXL (Gia đình lớn) - Đen bóng', 'PHILIPS-62L-BLK', '{"Phiên bản dung tích":"Bản 6.2L XXL (Gia đình lớn)","Màu sắc":"Đen bóng"}', 2850000.00, 20),
(17, 'Bản 6.2L XXL (Gia đình lớn) - Trắng ngọc trai', 'PHILIPS-62L-WHT', '{"Phiên bản dung tích":"Bản 6.2L XXL (Gia đình lớn)","Màu sắc":"Trắng ngọc trai"}', 2950000.00, 15);

-- 9. Thêm Sản phẩm Gạo ST25 Ông Cua Thượng Hạng (id=32 nếu chưa có)
INSERT INTO products (id, shop_id, category_id, name, slug, description, thumbnail_url, original_price, selling_price, stock_quantity, sold_quantity, unit, step_quantity, min_order_quantity, storage_type, shelf_life, origin, attributes, tier_variation, has_variants, rating_avg, review_count, status)
VALUES (32, 1, 2, 'Gạo ST25 Ông Cua Thượng Hạng Chuẩn Gạo Ngon Thế Giới', 'gao-st25-ong-cua-thuong-hang',
'Gạo ST25 đạt giải gạo ngon nhất thế giới. Hạt thon dài, trắng trong, dẻo thơm mùi lá dứa tự nhiên dù để nguội.',
'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500',
220000.00, 38000.00, 500.0, 142.0, 'bao', 1.0, 1.0, 'NORMAL', '12 tháng', 'Sóc Trăng, Việt Nam',
'{"cert":"VietGAP, Chuẩn Quốc Tế","origin":"Sóc Trăng"}',
'[{"name":"Loại gạo","options":["ST25 Lúa Tôm Thượng Hạng","Gạo Lứt Đỏ ST25"]},{"name":"Quy cách đóng gói","options":["Túi 1.0 kg","Bao 5.0 kg","Bao 10 kg","Bao 25 kg"]}]',
TRUE, 5.0, 210, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  tier_variation = VALUES(tier_variation),
  has_variants = TRUE,
  selling_price = VALUES(selling_price);

DELETE FROM product_variants WHERE product_id = 32;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(32, 'ST25 Lúa Tôm Thượng Hạng - Túi 1.0 kg', 'ST25-TOM-1KG', '{"Loại gạo":"ST25 Lúa Tôm Thượng Hạng","Quy cách đóng gói":"Túi 1.0 kg"}', 38000.00, 100),
(32, 'ST25 Lúa Tôm Thượng Hạng - Bao 5.0 kg', 'ST25-TOM-5KG', '{"Loại gạo":"ST25 Lúa Tôm Thượng Hạng","Quy cách đóng gói":"Bao 5.0 kg"}', 180000.00, 100),
(32, 'ST25 Lúa Tôm Thượng Hạng - Bao 10 kg', 'ST25-TOM-10KG', '{"Loại gạo":"ST25 Lúa Tôm Thượng Hạng","Quy cách đóng gói":"Bao 10 kg"}', 350000.00, 80),
(32, 'ST25 Lúa Tôm Thượng Hạng - Bao 25 kg', 'ST25-TOM-25KG', '{"Loại gạo":"ST25 Lúa Tôm Thượng Hạng","Quy cách đóng gói":"Bao 25 kg"}', 850000.00, 50),
(32, 'Gạo Lứt Đỏ ST25 - Túi 1.0 kg', 'ST25-LUT-1KG', '{"Loại gạo":"Gạo Lứt Đỏ ST25","Quy cách đóng gói":"Túi 1.0 kg"}', 42000.00, 80),
(32, 'Gạo Lứt Đỏ ST25 - Bao 5.0 kg', 'ST25-LUT-5KG', '{"Loại gạo":"Gạo Lứt Đỏ ST25","Quy cách đóng gói":"Bao 5.0 kg"}', 200000.00, 80),
(32, 'Gạo Lứt Đỏ ST25 - Bao 10 kg', 'ST25-LUT-10KG', '{"Loại gạo":"Gạo Lứt Đỏ ST25","Quy cách đóng gói":"Bao 10 kg"}', 390000.00, 60),
(32, 'Gạo Lứt Đỏ ST25 - Bao 25 kg', 'ST25-LUT-25KG', '{"Loại gạo":"Gạo Lứt Đỏ ST25","Quy cách đóng gói":"Bao 25 kg"}', 950000.00, 40);

-- 10. Thêm Sản phẩm iPhone 15 Pro Max (id=33 nếu chưa có)
INSERT INTO products (id, shop_id, category_id, name, slug, description, thumbnail_url, original_price, selling_price, stock_quantity, sold_quantity, unit, step_quantity, min_order_quantity, storage_type, shelf_life, origin, attributes, tier_variation, has_variants, rating_avg, review_count, status)
VALUES (33, 4, 10, 'Điện Thoại iPhone 15 Pro Max 5G Chính Hãng Apple VN/A', 'iphone-15-pro-max-vna',
'Khung viền Titan chuẩn hàng không vũ trụ, chip A17 Pro mạnh mẽ vượt trội, camera zoom quang học 5x sắc nét đỉnh cao.',
'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500',
34990000.00, 24990000.00, 150.0, 86.0, 'chiếc', 1.0, 1.0, 'NORMAL', '12 tháng bảo hành chính hãng', 'Chính Hãng Apple VN/A',
'{"chip":"Apple A17 Pro","screen":"6.7 inch Super Retina XDR OLED","origin":"Chính Hãng Apple VN/A"}',
'[{"name":"Màu sắc","options":["Titan Tự Nhiên","Đen Midnight","Trắng Starlight"]},{"name":"Cấu hình RAM/ROM","options":["256GB","512GB","1TB"]}]',
TRUE, 5.0, 450, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  tier_variation = VALUES(tier_variation),
  has_variants = TRUE,
  selling_price = VALUES(selling_price);

DELETE FROM product_variants WHERE product_id = 33;
INSERT INTO product_variants (product_id, variant_name, sku, attributes, price, stock_quantity) VALUES
(33, 'Titan Tự Nhiên - 256GB', 'IP15-NAT-256', '{"Màu sắc":"Titan Tự Nhiên","Cấu hình RAM/ROM":"256GB"}', 24990000.00, 25),
(33, 'Titan Tự Nhiên - 512GB', 'IP15-NAT-512', '{"Màu sắc":"Titan Tự Nhiên","Cấu hình RAM/ROM":"512GB"}', 29990000.00, 20),
(33, 'Titan Tự Nhiên - 1TB', 'IP15-NAT-1TB', '{"Màu sắc":"Titan Tự Nhiên","Cấu hình RAM/ROM":"1TB"}', 34990000.00, 15),
(33, 'Đen Midnight - 256GB', 'IP15-BLK-256', '{"Màu sắc":"Đen Midnight","Cấu hình RAM/ROM":"256GB"}', 24990000.00, 25),
(33, 'Đen Midnight - 512GB', 'IP15-BLK-512', '{"Màu sắc":"Đen Midnight","Cấu hình RAM/ROM":"512GB"}', 29990000.00, 20),
(33, 'Đen Midnight - 1TB', 'IP15-BLK-1TB', '{"Màu sắc":"Đen Midnight","Cấu hình RAM/ROM":"1TB"}', 34990000.00, 15),
(33, 'Trắng Starlight - 256GB', 'IP15-WHT-256', '{"Màu sắc":"Trắng Starlight","Cấu hình RAM/ROM":"256GB"}', 24990000.00, 20),
(33, 'Trắng Starlight - 512GB', 'IP15-WHT-512', '{"Màu sắc":"Trắng Starlight","Cấu hình RAM/ROM":"512GB"}', 29990000.00, 15),
(33, 'Trắng Starlight - 1TB', 'IP15-WHT-1TB', '{"Màu sắc":"Trắng Starlight","Cấu hình RAM/ROM":"1TB"}', 34990000.00, 10);

