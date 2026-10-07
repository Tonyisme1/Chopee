-- ==============================================================================================
-- CHOPEE MARKETPLACE - FULL PRODUCTION DATABASE SCHEMA & PROCEDURAL DEFINITIONS (ENTERPRISE 24-TABLE ARCHITECTURE)
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- Architecture: Multi-Vendor Marketplace with 3-Tier Data Lifecycle, Soft Deletes, Escrow Wallets, and Inventory Audits
-- Native DB Capabilities: Check Constraints, Generated Virtual Columns, Triggers, Views, Events, Procedures & Functions
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
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_status (status),
    INDEX idx_users_is_deleted (is_deleted)
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
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shops_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_shops_rating CHECK (rating BETWEEN 1.0 AND 5.0),
    INDEX idx_shops_status (status),
    INDEX idx_shops_slug (slug),
    INDEX idx_shops_type (shop_type),
    INDEX idx_shops_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 4. TABLE: brands (Thương hiệu chính hãng toàn sàn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    logo_url VARCHAR(255) NULL,
    description TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_brands_slug (slug),
    INDEX idx_brands_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 5. TABLE: categories (Cây danh mục phân cấp cha - con: Gốc -> Nhánh -> Ngọn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    icon_url VARCHAR(255) NULL,
    parent_id BIGINT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 6. TABLE: category_attributes (Đặc tả thông số chuẩn động theo từng ngành hàng - "Gọn từ gốc")
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS category_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_type ENUM('TEXT', 'SELECT', 'NUMBER', 'BOOLEAN') NOT NULL DEFAULT 'TEXT',
    options_json TEXT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cat_attr_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_cat_attr_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 7. TABLE: products (Mặt hàng đa ngành & Chợ tươi sống)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    original_price DECIMAL(12,2) NOT NULL,
    selling_price DECIMAL(12,2) NOT NULL,
    -- Cột sinh tự động nội tại DB: Tính % giảm giá không tốn dung lượng ổ đĩa (Virtual Column)
    discount_percentage INT GENERATED ALWAYS AS (
        IF(original_price > selling_price, ROUND(((original_price - selling_price) / original_price) * 100), 0)
    ) VIRTUAL,
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
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
    CONSTRAINT chk_products_prices CHECK (selling_price >= 0 AND original_price >= selling_price),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0 AND sold_quantity >= 0),
    CONSTRAINT chk_products_weight CHECK (weight_grams > 0),
    CONSTRAINT chk_products_moq CHECK (min_order_quantity > 0 AND step_quantity > 0),
    CONSTRAINT chk_products_rating CHECK (rating_avg BETWEEN 1.0 AND 5.0),
    INDEX idx_products_shop (shop_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_brand (brand_id),
    INDEX idx_products_status_price (status, selling_price),
    INDEX idx_products_discount (discount_percentage),
    INDEX idx_products_is_deleted (is_deleted),
    INDEX idx_products_storage_type (storage_type),
    FULLTEXT INDEX ft_products_search (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 8. TABLE: product_images (Thư viện hình ảnh sản phẩm)
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
-- 9. TABLE: product_variants (Biến thể phân loại: Size, Trọng lượng gói, Màu sắc)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_variants_price CHECK (price >= 0),
    CONSTRAINT chk_variants_stock CHECK (stock_quantity >= 0),
    INDEX idx_product_variants_product (product_id),
    INDEX idx_product_variants_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 10. TABLE: cart_items (Giỏ hàng người mua)
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
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0),
    INDEX idx_cart_items_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 11. TABLE: orders (Đơn hàng con phân tách theo từng Shop - Sẵn sàng phân tầng Hot/Warm/Cold)
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
    storage_tier ENUM('HOT', 'WARM', 'COLD') NOT NULL DEFAULT 'HOT',
    note VARCHAR(255) NULL,
    cancelled_by ENUM('BUYER', 'SELLER', 'ADMIN', 'SYSTEM') NULL,
    cancellation_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_amounts CHECK (total_amount >= 0 AND shipping_fee >= 0 AND final_amount >= 0),
    CONSTRAINT chk_orders_discounts CHECK (discount_amount >= 0 AND shop_discount_amount >= 0 AND platform_discount_amount >= 0),
    INDEX idx_orders_group_code (group_order_code),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_shop (shop_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_tier_created (storage_tier, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 12. TABLE: order_items (Chi tiết món hàng đã chốt trong đơn - Snapshot tên & biến thể)
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
    CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price CHECK (product_price >= 0 AND subtotal >= 0),
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 13. TABLE: order_status_history (Nhật ký hành trình đơn hàng - Audit Trail)
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
    INDEX idx_order_status_history_order (order_id),
    INDEX idx_order_status_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 14. TABLE: payments (Lịch sử thanh toán giao dịch VNPay)
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    INDEX idx_payments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 15. TABLE: vouchers (Mã giảm giá toàn sàn & riêng từng shop)
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
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vouchers_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    CONSTRAINT chk_vouchers_dates CHECK (start_date < end_date),
    CONSTRAINT chk_vouchers_discount CHECK (discount_value > 0),
    CONSTRAINT chk_vouchers_usage CHECK (used_count >= 0 AND used_count <= usage_limit),
    INDEX idx_vouchers_code (code),
    INDEX idx_vouchers_shop (shop_id),
    INDEX idx_vouchers_active (is_active, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 16. TABLE: reviews (Đánh giá sao & nhận xét trải nghiệm kèm ảnh thực tế)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL,
    comment TEXT NULL,
    images_json TEXT NULL,
    shop_reply TEXT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_reviews_product (product_id),
    INDEX idx_reviews_user (user_id),
    INDEX idx_reviews_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 17. TABLE: shop_wallets (Ví tiền gian hàng - Quản lý dòng tiền ký quỹ Escrow Multi-Vendor)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shop_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL UNIQUE,
    available_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    pending_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    locked_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_wallets_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    CONSTRAINT chk_wallet_available CHECK (available_balance >= 0),
    CONSTRAINT chk_wallet_pending CHECK (pending_balance >= 0),
    CONSTRAINT chk_wallet_locked CHECK (locked_balance >= 0),
    INDEX idx_shop_wallets_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 18. TABLE: wallet_transactions (Lịch sử biến động số dư ví người bán)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    transaction_type ENUM('ORDER_PAYOUT', 'COMMISSION_FEE', 'WITHDRAWAL', 'REFUND_DEDUCT', 'ADJUSTMENT') NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    balance_after DECIMAL(14,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY (wallet_id) REFERENCES shop_wallets(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_tx_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    INDEX idx_wallet_tx_wallet (wallet_id),
    INDEX idx_wallet_tx_order (order_id),
    INDEX idx_wallet_tx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 19. TABLE: payout_requests (Yêu cầu rút tiền từ Ví về Tài khoản ngân hàng)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payout_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'TRANSFERRED') NOT NULL DEFAULT 'PENDING',
    admin_note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payout_requests_wallet FOREIGN KEY (wallet_id) REFERENCES shop_wallets(id) ON DELETE RESTRICT,
    CONSTRAINT chk_payout_amount CHECK (amount > 0),
    INDEX idx_payout_requests_wallet (wallet_id),
    INDEX idx_payout_requests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 20. TABLE: refund_requests (Yêu cầu Đổi trả / Hoàn tiền & Tranh chấp người mua - người bán)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refund_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    reason ENUM('DAMAGED_GOODS', 'EXPIRED_FOOD', 'WRONG_ITEM', 'NOT_RECEIVED', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    evidence_images_json TEXT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING_SHOP', 'SHOP_REJECTED', 'ADMIN_MEDIATION', 'APPROVED', 'REFUNDED', 'CANCELLED') NOT NULL DEFAULT 'PENDING_SHOP',
    shop_response TEXT NULL,
    admin_note TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_refund_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT,
    CONSTRAINT chk_refund_amount CHECK (refund_amount > 0),
    INDEX idx_refund_order (order_id),
    INDEX idx_refund_shop (shop_id),
    INDEX idx_refund_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 21. TABLE: inventory_logs (Thẻ kho - Nhật ký kiểm kê biến động xuất nhập tồn)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    change_type ENUM('IMPORT', 'ORDER_SALE', 'ORDER_CANCEL_RESTOCK', 'RETURN_RESTOCK', 'DAMAGE_LOSS', 'MANUAL_CORRECTION') NOT NULL,
    quantity_before DECIMAL(10,2) NOT NULL,
    quantity_change DECIMAL(10,2) NOT NULL,
    quantity_after DECIMAL(10,2) NOT NULL,
    reference_id VARCHAR(50) NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_logs_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_logs_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    CONSTRAINT chk_inv_qty_after CHECK (quantity_after >= 0),
    INDEX idx_inv_logs_product (product_id),
    INDEX idx_inv_logs_variant (variant_id),
    INDEX idx_inv_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 22. TABLE: notifications (Trung tâm thông báo tài khoản & đơn hàng)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    notification_type ENUM('ORDER', 'PROMOTION', 'WALLET', 'SYSTEM') NOT NULL DEFAULT 'ORDER',
    reference_id VARCHAR(100) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 23. TABLE: flash_sales (Chiến dịch Khuyến mãi Giờ vàng / Flash Sale)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS flash_sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('UPCOMING', 'ACTIVE', 'ENDED') NOT NULL DEFAULT 'UPCOMING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_flash_sales_time CHECK (start_time < end_time),
    INDEX idx_flash_sales_time (start_time, end_time),
    INDEX idx_flash_sales_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------------------------
-- 24. TABLE: flash_sale_items (Sản phẩm tham gia Flash Sale với giá và hạn ngạch riêng)
-- ----------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS flash_sale_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NULL,
    flash_sale_price DECIMAL(12,2) NOT NULL,
    stock_limit DECIMAL(10,2) NOT NULL,
    sold_count DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fsi_flash_sale FOREIGN KEY (flash_sale_id) REFERENCES flash_sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_fsi_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_fsi_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    CONSTRAINT chk_fsi_price CHECK (flash_sale_price > 0),
    CONSTRAINT chk_fsi_stock CHECK (stock_limit > 0 AND sold_count >= 0 AND sold_count <= stock_limit),
    INDEX idx_fsi_sale (flash_sale_id),
    INDEX idx_fsi_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================================
-- 25. DATABASE TRIGGERS (BỘ KÍCH HOẠT TỰ ĐỘNG HÓA TẦNG DB)
-- ==============================================================================================

DELIMITER $$

-- Trigger 1: Chống âm kho tuyệt đối trên sản phẩm đơn lẻ (Zero/Negative Stock Guard)
DROP TRIGGER IF EXISTS trg_prevent_negative_stock$$
CREATE TRIGGER trg_prevent_negative_stock
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    IF NEW.stock_quantity < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'LỖI CSDL: Số lượng tồn kho sản phẩm không thể nhỏ hơn 0! Giao dịch bị hủy.';
    END IF;
END$$

-- Trigger 2: Chống âm kho tuyệt đối trên biến thể phân loại (Variant Stock Guard)
DROP TRIGGER IF EXISTS trg_prevent_negative_variant_stock$$
CREATE TRIGGER trg_prevent_negative_variant_stock
BEFORE UPDATE ON product_variants
FOR EACH ROW
BEGIN
    IF NEW.stock_quantity < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'LỖI CSDL: Số lượng tồn kho phân loại biến thể không thể nhỏ hơn 0! Giao dịch bị hủy.';
    END IF;
END$$

-- Trigger 3: Tự động ghi vết lịch sử khi đổi trạng thái đơn hàng (Audit Trail)
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

-- Trigger 4: Tự động cập nhật số lượng đã bán và ghi thẻ kho xuất bán
DROP TRIGGER IF EXISTS trg_update_sold_count$$
CREATE TRIGGER trg_update_sold_count
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    DECLARE v_current_stock DECIMAL(10,2);

    -- Lấy tồn kho trước đó của sản phẩm
    SELECT stock_quantity INTO v_current_stock FROM products WHERE id = NEW.product_id;

    -- Tăng số lượng đã bán
    UPDATE products
    SET sold_quantity = sold_quantity + NEW.quantity
    WHERE id = NEW.product_id;

    -- Tự động ghi nhận vào Thẻ kho (Inventory Audit Log)
    IF v_current_stock IS NOT NULL THEN
        INSERT INTO inventory_logs (product_id, variant_id, change_type, quantity_before, quantity_change, quantity_after, reference_id, note, created_at)
        VALUES (
            NEW.product_id,
            NEW.variant_id,
            'ORDER_SALE',
            v_current_stock,
            -NEW.quantity,
            GREATEST(0.00, v_current_stock - NEW.quantity),
            CONCAT('ORDER_ITEM_', NEW.id),
            CONCAT('Xuất bán đơn hàng #', NEW.order_id),
            NOW()
        );
    END IF;
END$$

-- Trigger 5: Tự động tính điểm sao trung bình cho Sản phẩm và Toàn gian hàng khi có Đánh giá
DROP TRIGGER IF EXISTS trg_after_review_insert$$
CREATE TRIGGER trg_after_review_insert
AFTER INSERT ON reviews
FOR EACH ROW
BEGIN
    DECLARE target_shop_id BIGINT;

    -- 1. Cập nhật sao trung bình của sản phẩm
    UPDATE products
    SET rating_avg = (SELECT ROUND(AVG(rating), 1) FROM reviews WHERE product_id = NEW.product_id AND is_deleted = FALSE),
        review_count = (SELECT COUNT(*) FROM reviews WHERE product_id = NEW.product_id AND is_deleted = FALSE)
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
            WHERE p.shop_id = target_shop_id AND r.is_deleted = FALSE
        )
        WHERE id = target_shop_id;
    END IF;
END$$

-- Trigger 6: Chống âm ví người bán tuyệt đối (Zero Negative Wallet Guard)
DROP TRIGGER IF EXISTS trg_prevent_negative_wallet$$
CREATE TRIGGER trg_prevent_negative_wallet
BEFORE UPDATE ON shop_wallets
FOR EACH ROW
BEGIN
    IF NEW.available_balance < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'LỖI CSDL: Số dư khả dụng của ví không đủ để thực hiện giao dịch!';
    END IF;
END$$

-- Trigger 7: Tự động đồng bộ cờ has_variants trên sản phẩm cha khi thêm biến thể
DROP TRIGGER IF EXISTS trg_auto_sync_has_variants$$
CREATE TRIGGER trg_auto_sync_has_variants
AFTER INSERT ON product_variants
FOR EACH ROW
BEGIN
    UPDATE products
    SET has_variants = TRUE
    WHERE id = NEW.product_id;
END$$

-- ==============================================================================================
-- 26. STORED PROCEDURES (THỦ TỤC XỬ LÝ NGHIỆP VỤ NẶNG TẦNG DB)
-- ==============================================================================================

-- Procedure 1: Hủy đơn hàng, tự động hoàn trả tồn kho nguyên tử và ghi vết thẻ kho
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

    -- Hoàn trả tồn kho phân loại biến thể nếu có
    UPDATE product_variants pv
    JOIN order_items oi ON pv.id = oi.variant_id
    SET pv.stock_quantity = pv.stock_quantity + oi.quantity
    WHERE oi.order_id = v_order_id;

    -- Ghi nhận lịch sử hoàn trả vào thẻ kho
    INSERT INTO inventory_logs (product_id, variant_id, change_type, quantity_before, quantity_change, quantity_after, reference_id, note, created_at)
    SELECT oi.product_id, oi.variant_id, 'ORDER_CANCEL_RESTOCK', p.stock_quantity - oi.quantity, oi.quantity, p.stock_quantity, p_order_code, CONCAT('Hoàn kho do hủy đơn bởi ', p_cancelled_by), NOW()
    FROM order_items oi
    JOIN products p ON oi.product_id = p.id
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

-- Procedure 2: Tự động quyết toán đơn hàng sang Ví người bán khi giao thành công (Escrow Settlement)
DROP PROCEDURE IF EXISTS sp_settle_order_payout$$
CREATE PROCEDURE sp_settle_order_payout(
    IN p_order_id BIGINT,
    IN p_commission_rate DECIMAL(5,2) -- Ví dụ: 5.00 nghĩa là 5%
)
BEGIN
    DECLARE v_shop_id BIGINT;
    DECLARE v_status VARCHAR(30);
    DECLARE v_final_amount DECIMAL(12,2);
    DECLARE v_shop_discount DECIMAL(12,2);
    DECLARE v_platform_discount DECIMAL(12,2);
    DECLARE v_commission_fee DECIMAL(12,2);
    DECLARE v_net_payout DECIMAL(14,2);
    DECLARE v_wallet_id BIGINT;
    DECLARE v_current_balance DECIMAL(14,2);

    START TRANSACTION;

    SELECT shop_id, status, final_amount, shop_discount_amount, platform_discount_amount
    INTO v_shop_id, v_status, v_final_amount, v_shop_discount, v_platform_discount
    FROM orders
    WHERE id = p_order_id
    FOR UPDATE;

    IF v_status <> 'DELIVERED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Chỉ có thể quyết toán đơn hàng đã giao thành công!';
    END IF;

    -- Đảm bảo ví shop tồn tại
    INSERT INTO shop_wallets (shop_id, available_balance, pending_balance, locked_balance, created_at)
    VALUES (v_shop_id, 0.00, 0.00, 0.00, NOW())
    ON DUPLICATE KEY UPDATE updated_at = NOW();

    SELECT id, available_balance INTO v_wallet_id, v_current_balance
    FROM shop_wallets
    WHERE shop_id = v_shop_id
    FOR UPDATE;

    -- Tính toán phí sàn và số tiền thực nhận của Shop:
    -- Tiền shop nhận = Tiền thanh toán của khách + Tiền sàn trợ giá - Phí hoa hồng sàn
    SET v_commission_fee = ROUND(v_final_amount * (p_commission_rate / 100.0), 2);
    SET v_net_payout = (v_final_amount + v_platform_discount) - v_commission_fee;

    -- Cộng tiền vào số dư khả dụng
    UPDATE shop_wallets
    SET available_balance = available_balance + v_net_payout,
        updated_at = NOW()
    WHERE id = v_wallet_id;

    -- Ghi nhật ký biến động ví
    INSERT INTO wallet_transactions (wallet_id, order_id, transaction_type, amount, balance_after, description, created_at)
    VALUES (
        v_wallet_id,
        p_order_id,
        'ORDER_PAYOUT',
        v_net_payout,
        v_current_balance + v_net_payout,
        CONCAT('Quyết toán đơn hàng #', p_order_id, ' (Đã trừ ', p_commission_rate, '% phí sàn)'),
        NOW()
    );

    COMMIT;
END$$

-- Procedure 3: Tái tính toán điểm sao toàn bộ các Shop định kỳ
DROP PROCEDURE IF EXISTS sp_recalculate_shop_rating$$
CREATE PROCEDURE sp_recalculate_shop_rating(IN p_shop_id BIGINT)
BEGIN
    UPDATE shops s
    SET s.rating = (
        SELECT ROUND(COALESCE(AVG(r.rating), 5.0), 1)
        FROM reviews r
        JOIN products p ON r.product_id = p.id
        WHERE p.shop_id = s.id AND r.is_deleted = FALSE
    )
    WHERE (p_shop_id IS NULL OR s.id = p_shop_id);
END$$

-- Procedure 4: Tự động lưu chuyển đơn hàng cũ sang Tầng Lạnh (3-Tier Archival Pipeline)
DROP PROCEDURE IF EXISTS sp_archive_cold_orders$$
CREATE PROCEDURE sp_archive_cold_orders(IN p_days_threshold INT)
BEGIN
    DECLARE v_cutoff_date DATETIME;
    SET v_cutoff_date = DATE_SUB(NOW(), INTERVAL p_days_threshold DAY);

    -- Đánh dấu đơn hàng cũ hoàn tất sang Tầng Lạnh (COLD)
    UPDATE orders
    SET storage_tier = 'COLD'
    WHERE created_at < v_cutoff_date
      AND status IN ('DELIVERED', 'CANCELLED')
      AND storage_tier <> 'COLD';
END$$

-- ==============================================================================================
-- 27. STORED FUNCTIONS (HÀM TÍNH TOÁN DÙNG CHUNG)
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

-- Function 3: Kiểm tra nhanh tính khả dụng tồn kho
DROP FUNCTION IF EXISTS fn_is_product_in_stock$$
CREATE FUNCTION fn_is_product_in_stock(
    p_product_id BIGINT,
    p_variant_id BIGINT,
    p_requested_qty DECIMAL(10,2)
)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_stock DECIMAL(10,2) DEFAULT 0.00;

    IF p_variant_id IS NOT NULL THEN
        SELECT stock_quantity INTO v_stock FROM product_variants WHERE id = p_variant_id AND is_deleted = FALSE;
    ELSE
        SELECT stock_quantity INTO v_stock FROM products WHERE id = p_product_id AND is_deleted = FALSE;
    END IF;

    RETURN (v_stock >= p_requested_qty);
END$$

-- ==============================================================================================
-- 28. DATABASE VIEWS (KHUNG NHÌN TỐI ƯU HÓA HIỆU NĂNG TẦNG NỘI TẠI DB)
-- ==============================================================================================

-- View 1: Khung nhìn danh sách mặt hàng đang mở bán hợp lệ (Active Catalog View)
DROP VIEW IF EXISTS vw_active_products;
CREATE VIEW vw_active_products AS
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.slug AS product_slug,
    p.thumbnail_url,
    p.original_price,
    p.selling_price,
    p.discount_percentage,
    p.stock_quantity,
    p.sold_quantity,
    p.unit,
    p.storage_type,
    p.rating_avg,
    p.review_count,
    p.has_variants,
    s.id AS shop_id,
    s.name AS shop_name,
    s.rating AS shop_rating,
    c.id AS category_id,
    c.name AS category_name,
    b.id AS brand_id,
    b.name AS brand_name
FROM products p
JOIN shops s ON p.shop_id = s.id AND s.status = 'APPROVED' AND s.is_deleted = FALSE
JOIN categories c ON p.category_id = c.id AND c.is_deleted = FALSE
LEFT JOIN brands b ON p.brand_id = b.id AND b.is_active = TRUE
WHERE p.status = 'ACTIVE' 
  AND p.is_deleted = FALSE;

-- View 2: Khung nhìn tổng quan tài chính Người bán (Seller Financial Summary View)
DROP VIEW IF EXISTS vw_seller_financial_summary;
CREATE VIEW vw_seller_financial_summary AS
SELECT 
    s.id AS shop_id,
    s.name AS shop_name,
    COALESCE(w.available_balance, 0.00) AS available_balance,
    COALESCE(w.pending_balance, 0.00) AS pending_balance,
    COALESCE(w.locked_balance, 0.00) AS locked_balance,
    COUNT(o.id) AS total_delivered_orders,
    COALESCE(SUM(o.final_amount), 0.00) AS total_delivered_revenue
FROM shops s
LEFT JOIN shop_wallets w ON s.id = w.shop_id
LEFT JOIN orders o ON s.id = o.shop_id AND o.status = 'DELIVERED'
WHERE s.is_deleted = FALSE
GROUP BY s.id, s.name, w.available_balance, w.pending_balance, w.locked_balance;

-- ==============================================================================================
-- 29. DATABASE EVENT SCHEDULER (LẬP LỊCH TỰ ĐỘNG HÓA NỘI TẠI DB)
-- ==============================================================================================

-- Bật tính năng Event Scheduler của MySQL Server
SET GLOBAL event_scheduler = ON;

-- Event 1: Tự động lưu chuyển đơn hàng > 365 ngày sang Cold Tier vào lúc 02:00 sáng mỗi ngày
DROP EVENT IF EXISTS evt_daily_cold_archive;
CREATE EVENT evt_daily_cold_archive
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 2 HOUR)
COMMENT 'Tự động dời đơn hàng hoàn tất trên 1 năm sang Tầng Lạnh COLD'
DO
    CALL sp_archive_cold_orders(365);

-- Event 2: Tự động kiểm tra và chuyển trạng thái Flash Sale mỗi 1 phút
DROP EVENT IF EXISTS evt_auto_update_flash_sale_status;
CREATE EVENT evt_auto_update_flash_sale_status
ON SCHEDULE EVERY 1 MINUTE
COMMENT 'Tự động kích hoạt và kết thúc chiến dịch Flash Sale theo khung giờ'
DO
BEGIN
    -- Kích hoạt chiến dịch đến giờ
    UPDATE flash_sales
    SET status = 'ACTIVE'
    WHERE start_time <= NOW() AND end_time > NOW() AND status = 'UPCOMING';

    -- Đóng chiến dịch hết giờ
    UPDATE flash_sales
    SET status = 'ENDED'
    WHERE end_time <= NOW() AND status = 'ACTIVE';
END;

DELIMITER ;
