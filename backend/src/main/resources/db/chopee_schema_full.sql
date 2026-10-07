-- ==============================================================================================
-- CHOPEE MARKETPLACE - FULL PRODUCTION DATABASE SCHEMA & PROCEDURAL DEFINITIONS
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- Architecture: Modular Monolith | Separation: Multi-Vendor Marketplace with Dynamic Specifications
-- ==============================================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------------------------
-- 1. TABLE: users (Tài khoản người dùng toàn sàn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(255) NULL,
    role ENUM('ROLE_BUYER', 'ROLE_SELLER', 'ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_BUYER',
    status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 2. TABLE: user_addresses (Sổ địa chỉ nhận hàng của người mua)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_addresses_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 3. TABLE: shops (Gian hàng / Người bán trên sàn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NULL,
    logo_url VARCHAR(255) NULL,
    banner_url VARCHAR(255) NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    shop_type ENUM('GENERAL', 'FOOD_FRESH', 'OFFICIAL_MALL') NOT NULL DEFAULT 'GENERAL',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'LOCKED') NOT NULL DEFAULT 'APPROVED',
    rating DECIMAL(2,1) NOT NULL DEFAULT 5.0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shops_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_shops_status (status),
    INDEX idx_shops_slug (slug),
    INDEX idx_shops_type (shop_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 4. TABLE: categories (Cây danh mục phân cấp cha - con: Gốc -> Nhánh -> Ngọn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    icon_url VARCHAR(255) NULL,
    parent_id BIGINT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 5. TABLE: products (Mặt hàng đa ngành & Chợ tươi sống - "Gọn từ gốc")
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    original_price DECIMAL(12,2) NOT NULL,
    selling_price DECIMAL(12,2) NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sold_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(30) NOT NULL DEFAULT 'chiếc',
    has_variants BOOLEAN NOT NULL DEFAULT FALSE,
    weight_grams INT NOT NULL DEFAULT 500,
    min_order_quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    step_quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    storage_type ENUM('NORMAL', 'FRESH', 'FROZEN_CHILLED') NOT NULL DEFAULT 'NORMAL',
    shelf_life VARCHAR(100) NULL,
    origin VARCHAR(150) NULL,
    attributes TEXT NULL,
    rating_avg DECIMAL(2,1) NOT NULL DEFAULT 5.0,
    review_count INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_products_shop (shop_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_status_price (status, selling_price),
    INDEX idx_products_storage_type (storage_type),
    FULLTEXT INDEX ft_products_search (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 6. TABLE: product_images (Thư viện hình ảnh sản phẩm)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_images_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 7. TABLE: product_variants (Biến thể phân loại: Size, Trọng lượng gói, Màu sắc)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_variants_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 8. TABLE: cart_items (Giỏ hàng người mua)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    INDEX idx_cart_items_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 9. TABLE: orders (Đơn hàng con phân tách theo từng Shop - Đảm bảo Multi-Vendor)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    group_order_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    shipping_name VARCHAR(100) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    shipping_method ENUM('STANDARD', 'EXPRESS_FRESH') NOT NULL DEFAULT 'STANDARD',
    total_amount DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shop_discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    platform_discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('COD', 'VNPAY') NOT NULL DEFAULT 'COD',
    payment_status ENUM('UNPAID', 'PAID', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'UNPAID',
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    note VARCHAR(255) NULL,
    cancelled_by ENUM('BUYER', 'SELLER', 'ADMIN', 'SYSTEM') NULL,
    cancellation_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    INDEX idx_orders_group_code (group_order_code),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_shop (shop_id),
    INDEX idx_orders_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 10. TABLE: order_items (Chi tiết món hàng đã chốt trong đơn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(100) NULL,
    unit VARCHAR(30) NOT NULL,
    product_price DECIMAL(12,2) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 11. TABLE: order_status_history (Nhật ký hành trình đơn hàng - Audit Trail)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(100) NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_status_history_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 12. TABLE: payments (Lịch sử thanh toán giao dịch VNPay)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_order_code VARCHAR(50) NOT NULL UNIQUE,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'VNPAY',
    amount DECIMAL(12,2) NOT NULL,
    status ENUM('UNPAID', 'PAID', 'FAILED') NOT NULL DEFAULT 'UNPAID',
    transaction_reference VARCHAR(100) NULL,
    bank_code VARCHAR(50) NULL,
    pay_date DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 13. TABLE: vouchers (Mã giảm giá toàn sàn & riêng từng shop)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    voucher_name VARCHAR(150) NOT NULL,
    discount_type ENUM('PERCENT', 'FIXED_AMOUNT') NOT NULL DEFAULT 'PERCENT',
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_value DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    max_discount_amount DECIMAL(10,2) NULL,
    usage_limit INT NOT NULL DEFAULT 100,
    used_count INT NOT NULL DEFAULT 0,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vouchers_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    INDEX idx_vouchers_code (code),
    INDEX idx_vouchers_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 14. TABLE: reviews (Đánh giá sao & nhận xét trải nghiệm kèm ảnh thực tế)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NULL,
    images_json TEXT NULL,
    shop_reply TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================================
-- 15. DATABASE TRIGGERS (BỘ KÍCH HOẠT TỰ ĐỘNG HÓA TẦNG DB)
-- ==============================================================================================

DELIMITER $$

-- Trigger 1: Chống âm kho tuyệt đối (Zero/Negative Stock Guard)
DROP TRIGGER IF EXISTS trg_prevent_negative_stock$$
CREATE TRIGGER trg_prevent_negative_stock
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    IF NEW.stock_quantity < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'LỖI CSDL: Số lượng tồn kho không thể nhỏ hơn 0! Giao dịch bị hủy.';
    END IF;
END$$

-- Trigger 2: Tự động ghi vết lịch sử khi đổi trạng thái đơn hàng (Audit Trail)
DROP TRIGGER IF EXISTS trg_order_status_audit$$
CREATE TRIGGER trg_order_status_audit
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO order_status_history (order_id, previous_status, new_status, changed_by, note, created_at)
        VALUES (NEW.id, OLD.status, NEW.status, COALESCE(NEW.cancelled_by, 'SYSTEM'), NEW.cancellation_reason, NOW());
    END IF;
END$$

-- Trigger 3: Tự động cập nhật số lượng đã bán (Auto Sold Quantity Counter)
DROP TRIGGER IF EXISTS trg_update_sold_count$$
CREATE TRIGGER trg_update_sold_count
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE products
    SET sold_quantity = sold_quantity + NEW.quantity
    WHERE id = NEW.product_id;
END$$

-- Trigger 4: Tự động tính điểm sao trung bình cho Sản phẩm và Toàn gian hàng khi có Đánh giá
DROP TRIGGER IF EXISTS trg_after_review_insert$$
CREATE TRIGGER trg_after_review_insert
AFTER INSERT ON reviews
FOR EACH ROW
BEGIN
    DECLARE target_shop_id BIGINT;

    -- 1. Cập nhật sao trung bình của sản phẩm
    UPDATE products
    SET rating_avg = (SELECT ROUND(AVG(rating), 1) FROM reviews WHERE product_id = NEW.product_id),
        review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = NEW.product_id)
    WHERE id = NEW.product_id;

    -- 2. Tìm shop sở hữu sản phẩm này
    SELECT shop_id INTO target_shop_id FROM products WHERE id = NEW.product_id;

    -- 3. Cập nhật sao uy tín của toàn bộ gian hàng
    IF target_shop_id IS NOT NULL THEN
        UPDATE shops
        SET rating = (
            SELECT ROUND(COALESCE(AVG(r.rating), 5.0), 1)
            FROM reviews r
            JOIN products p ON r.product_id = p.id
            WHERE p.shop_id = target_shop_id
        )
        WHERE id = target_shop_id;
    END IF;
END$$

-- ==============================================================================================
-- 16. STORED PROCEDURES (THỦ TỤC XỬ LÝ NGHIỆP VỤ NẶNG TẦNG DB)
-- ==============================================================================================

-- Procedure 1: Hủy đơn hàng và tự động hoàn trả tồn kho nguyên tử
DROP PROCEDURE IF EXISTS sp_cancel_order_and_restock$$
CREATE PROCEDURE sp_cancel_order_and_restock(
    IN p_order_code VARCHAR(50),
    IN p_cancelled_by VARCHAR(30),
    IN p_reason VARCHAR(255)
)
BEGIN
    DECLARE v_order_id BIGINT;
    DECLARE v_current_status VARCHAR(30);

    START TRANSACTION;

    SELECT id, status INTO v_order_id, v_current_status
    FROM orders
    WHERE order_code = p_order_code
    FOR UPDATE;

    IF v_order_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy đơn hàng!';
    END IF;

    IF v_current_status = 'CANCELLED' OR v_current_status = 'DELIVERED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Đơn hàng không thể hủy ở trạng thái hiện tại!';
    END IF;

    -- Hoàn trả tồn kho cho từng sản phẩm trong đơn
    UPDATE products p
    JOIN order_items oi ON p.id = oi.product_id
    SET p.stock_quantity = p.stock_quantity + oi.quantity
    WHERE oi.order_id = v_order_id;

    -- Cập nhật trạng thái đơn
    UPDATE orders
    SET status = 'CANCELLED',
        cancelled_by = p_cancelled_by,
        cancellation_reason = p_reason,
        updated_at = NOW()
    WHERE id = v_order_id;

    COMMIT;
END$$

-- Procedure 2: Tái tính toán điểm sao toàn bộ các Shop định kỳ
DROP PROCEDURE IF EXISTS sp_recalculate_shop_rating$$
CREATE PROCEDURE sp_recalculate_shop_rating(IN p_shop_id BIGINT)
BEGIN
    UPDATE shops s
    SET s.rating = (
        SELECT ROUND(COALESCE(AVG(r.rating), 5.0), 1)
        FROM reviews r
        JOIN products p ON r.product_id = p.id
        WHERE p.shop_id = s.id
    )
    WHERE (p_shop_id IS NULL OR s.id = p_shop_id);
END$$

-- ==============================================================================================
-- 17. STORED FUNCTIONS (HÀM TÍNH TOÁN DÙNG CHUNG)
-- ==============================================================================================

-- Function 1: Sinh mã đơn hàng ngẫu nhiên duy nhất
DROP FUNCTION IF EXISTS fn_generate_order_code$$
CREATE FUNCTION fn_generate_order_code()
RETURNS VARCHAR(50)
DETERMINISTIC
BEGIN
    DECLARE v_date_prefix VARCHAR(8);
    DECLARE v_rand_suffix INT;
    SET v_date_prefix = DATE_FORMAT(NOW(), '%Y%m%d');
    SET v_rand_suffix = FLOOR(100000 + (RAND() * 900000));
    RETURN CONCAT('ORD-', v_date_prefix, '-', v_rand_suffix);
END$$

-- Function 2: Tính cước vận chuyển theo hình thức và khối lượng
DROP FUNCTION IF EXISTS fn_calculate_shipping_fee$$
CREATE FUNCTION fn_calculate_shipping_fee(
    p_shipping_method VARCHAR(30),
    p_weight_grams INT
)
RETURNS DECIMAL(12,2)
DETERMINISTIC
BEGIN
    DECLARE v_base_fee DECIMAL(12,2);
    DECLARE v_extra_fee DECIMAL(12,2) DEFAULT 0.00;

    IF p_shipping_method = 'EXPRESS_FRESH' THEN
        SET v_base_fee = 25000.00;
    ELSE
        SET v_base_fee = 15000.00;
    END IF;

    -- Phụ thu nếu nặng trên 3kg (3000g)
    IF p_weight_grams > 3000 THEN
        SET v_extra_fee = CEIL((p_weight_grams - 3000) / 1000) * 5000.00;
    END IF;

    RETURN v_base_fee + v_extra_fee;
END$$

DELIMITER ;
