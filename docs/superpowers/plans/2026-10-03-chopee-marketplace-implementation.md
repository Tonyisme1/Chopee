# Chopee Marketplace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, production-grade Multi-Vendor E-Commerce Marketplace platform ("Chopee") specializing in multi-industry commerce, fresh grocery foods, beverages, and household/tech goods, integrated with a floating AI Shopping Copilot, COD & VNPay Sandbox payments, Address Book, Product Reviews, Voucher Discounts, and dedicated Buyer, Seller, and Admin portals.

**Architecture:** Modular Monolith using Spring Boot 3 (Java 17) for the REST backend with Spring Data JPA and Spring Security 6 (Stateless JWT). A single React (Vite + TypeScript + Tailwind CSS) frontend organized into 3 nested layout portals: Client Marketplace (`/`), Seller Center (`/seller`), and Admin Management (`/admin`). MySQL 8 serves as the enterprise relational data store with 24 tables, 3-tier data lifecycle (Hot, Warm, Cold), covering indexes, triggers, stored procedures, views, and ACID transaction safety for multi-vendor order splitting and atomic stock deductions.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Spring Data JPA, Hibernate, Spring Security 6, JJWT, Springdoc OpenAPI, MySQL 8, React 18/19, TypeScript, Vite, Tailwind CSS, Lucide Icons, Zustand, Axios, VNPay Sandbox, Google Gemini 1.5 Flash API.

**Current Implementation State:**
- **Database Layer:** 100% Complete (24 enterprise tables, 3-tier lifecycle, triggers, views, event schedulers, `chopee_schema_full.sql`).
- **Backend API Layer:** 100% Complete (52 RESTful endpoints across 12 functional areas: Catalog, Cart, Order, Payment VNPay, Seller, Admin, AI Copilot, Health, Address Book, Product Reviews, Vouchers).
- **Backend Quality Gates:** 60/60 JUnit integration tests passing (100% coverage across 13 test suites).
- **Server Status:** Spring Boot running on `http://localhost:8080`, WAMP MySQL on `localhost:3306` (`chopee_db`).
- **Frontend Current Milestone:** Transitioning into Task 11 (Frontend Core Architecture, Stores & Services).

**Spec:** [docs/superpowers/specs/2026-10-03-shopee-marketplace-design.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly\Quan_Ly_Cho_Online\docs\superpowers\specs\2026-10-03-shopee-marketplace-design.md) & [docs/specs/00_index.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly\Quan_Ly_Cho_Online\docs\specs\00_index.md).

## Global Constraints
- Java version: 17 LTS; Spring Boot 3.3.x; Maven 3.9+.
- Node version: 20+; Package manager: npm.
- Database: MySQL 8 (InnoDB, utf8mb4_unicode_ci); Port: 3306; DB name: `chopee_db`.
- Ports: Backend runs on `http://localhost:8080`, Frontend on `http://localhost:5173`.
- Passwords must be hashed using BCrypt (`strength = 10`).
- API envelope format: `ApiResponse<T>` with `success`, `message`, `data`, and `errors`.
- Concurrency: Conditional atomic stock deduction (`UPDATE products SET stock_quantity = stock_quantity - :qty WHERE id = :id AND stock_quantity >= :qty`).
- Fractional quantities supported for fresh produce (`0.5kg`, `step = 0.5`).

## Review Focus
1. Multi-vendor order splitting: A single cart with items from N shops must create exactly N independent `orders` linked by a common `group_order_code`.
2. Stock over-selling concurrency: Concurrent purchases exceeding remaining inventory must fail cleanly with HTTP 400 and trigger a full database rollback.
3. IDOR security: A seller authenticated for Shop A must receive HTTP 403 when attempting to mutate products, orders, reviews, or vouchers belonging to Shop B.
4. VNPay checksum verification: Tampered or invalid `vnp_SecureHash` in IPN webhook callbacks must be rejected with HTTP 400 and not mark orders as `PAID`.
5. AI Copilot grounding: AI chat must enrich context with real database product items so it never hallucinates non-existent products.
6. Voucher & Promotion validation: Enforce minimum spend, usage limit, valid date window, and single-usage rules.

---

### Task 1: Project Scaffolding & Environment Setup

**Files:**
- Create: `docker-compose.yml`
- Create: `backend/pom.xml`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/chopee/ChopeeApplication.java`
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tailwind.config.js`
- Create: `frontend/src/index.css`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/App.tsx`

**Interfaces:**
- Produces: Base running backend on `:8080` with Swagger UI at `/swagger-ui.html` and frontend on `:5173`.

- [x] **Step 1: Create `docker-compose.yml` for local MySQL 8**
  Define `mysql` service on port 3306 with database `chopee_db`, username `root`, password `root`, and persistent volume.

- [x] **Step 2: Scaffold Spring Boot 3 Maven project in `backend/`**
  Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`, `mysql-connector-j`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `lombok`, `springdoc-openapi-starter-webmvc-ui`.
  Configure `application.yml` with datasource, JPA ddl-auto `update`, and port `8080`.

- [x] **Step 3: Scaffold React + Vite + Tailwind CSS project in `frontend/`**
  Install dependencies: `react`, `react-dom`, `react-router-dom`, `lucide-react`, `zustand`, `axios`, `clsx`, `tailwind-merge`.
  Configure Tailwind theme with primary orange color `#EE4D2D`.

- [x] **Step 4: Verify Backend & Frontend build and startup**
  Run: `mvn clean test` in `backend/` and `npm run build` in `frontend/`.
  Expected: BUILD SUCCESS for both.

- [x] **Step 5: Commit scaffolding**
  `git add . && git commit -m "chore: scaffold Spring Boot 3 backend and React Vite Tailwind frontend"`

---


### Task 2: Core Data Model, JPA Entity Layer & Enterprise Database Architecture

**Files:**
- Schema: `backend/src/main/resources/db/chopee_schema_full.sql`
- Config: `backend/src/main/resources/db/my_production.cnf`
- Entity classes: `backend/src/main/java/com/chopee/entity/*.java` (`User`, `UserAddress`, `Shop`, `Category`, `Product`, `ProductImage`, `ProductVariant`, `CartItem`, `Order`, `OrderItem`, `OrderStatusHistory`, `Voucher`, `Payment`, `Review`, `ShopWallet`, `WalletTransaction`, `PayoutRequest`, `RefundRequest`, `InventoryLog`, `Notification`, `Brand`, `CategoryAttribute`, `FlashSale`, `FlashSaleItem`)
- Repositories: `backend/src/main/java/com/chopee/repository/*.java`
- Tests: `backend/src/test/java/com/chopee/repository/EntityMappingTest.java`

**Interfaces:**
- Produces: 24 enterprise database tables with 3-tier storage lifecycle (Hot, Warm, Cold), covering indexes (`idx_products_cat_sold`, `idx_orders_user_created`), Vietnamese N-gram full-text search, triggers (`trg_prevent_negative_wallet`, `trg_auto_sync_has_variants`), views (`vw_active_products`, `vw_seller_financial_summary`, `vw_platform_daily_metrics`), stored procedures (`sp_cleanup_abandoned_carts`, `sp_defragment_and_analyze_tables`), and event schedulers (`evt_daily_cold_archive`, `evt_auto_update_flash_sale_status`).

- [x] **Step 1: Write integration test for entity creation and relations**
  Test creating a user with `ROLE_SELLER`, a shop, a category, and a product with `attributes` JSON.
  `mvn test -Dtest=EntityMappingTest` - PASS.

- [x] **Step 2: Implement Enums and Base Entities**
  Define `Role`, `UserStatus`, `ShopType`, `ShopStatus`, `StorageType`, `ProductStatus`, `ShippingMethod`, `PaymentMethod`, `PaymentStatus`, `OrderStatus`, `DiscountType`, `CancelledBy`.

- [x] **Step 3: Implement User, Shop, Category, and Product Entities**
  Include fresh food fields: `unit`, `minOrderQuantity`, `stepQuantity`, `storageType`, `shelfLife`, `origin`, and JSON `attributes` string with converter.

- [x] **Step 4: Implement CartItem, Order, OrderItem, Voucher, Payment, Review, Address Entities**
  Ensure `Order` contains `groupOrderCode`, `shop`, `user`, amounts, status, and shipping fields. Support multi-vendor separation and audit trail `OrderStatusHistory`.

- [x] **Step 5: Create Spring Data JPA Repositories**
  `UserRepository`, `UserAddressRepository`, `ShopRepository`, `CategoryRepository`, `ProductRepository`, `CartItemRepository`, `OrderRepository`, `VoucherRepository`, `PaymentRepository`, `ReviewRepository`.

- [x] **Step 6: Run tests to verify entity mappings and database constraints**
  Run: `mvn test -Dtest=EntityMappingTest`. Expected: PASS (Verified with H2 in-memory and MySQL 8.4 compatibility).

- [x] **Step 7: Commit**
  `git add backend/ && git commit -m "feat: implement 24 enterprise JPA entities, 3-tier DB lifecycle, and repositories"`

---

### Task 3: Authentication, Security & RBAC (Spring Security 6 + JWT)

**Files:**
- Create: `backend/src/main/java/com/chopee/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/chopee/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/chopee/security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/com/chopee/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/chopee/modules/auth/dto/*.java`
- Create: `backend/src/main/java/com/chopee/modules/auth/AuthService.java`
- Create: `backend/src/main/java/com/chopee/modules/auth/AuthController.java`
- Test: `backend/src/test/java/com/chopee/modules/auth/AuthControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/register-seller`.

- [x] **Step 1: Write tests for authentication endpoints**
  Test registration with valid & duplicate email, login with correct & invalid password, accessing protected `/me` with and without Bearer token.

- [x] **Step 2: Implement `JwtTokenProvider` & `JwtAuthenticationFilter`**
  Generate JWT tokens with claims (username, role, userId, shopId if seller), validate token expiration and HMAC signature.

- [x] **Step 3: Configure `SecurityConfig` with CORS & stateless session**
  Permit public paths: `/api/v1/auth/**`, `/api/v1/public/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
  Require `ROLE_SELLER` for `/api/v1/seller/**`, `ROLE_ADMIN` for `/api/v1/admin/**`.

- [x] **Step 4: Implement `AuthService` and `AuthController`**
  Implement BCrypt password hashing, authenticate user via `AuthenticationManager`, return `AuthResponse` containing token and user profile.

- [x] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=AuthControllerTest`. Expected: PASS.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement Spring Security 6 stateless JWT authentication and RBAC"`

---

### Task 4: Product Catalog & Multi-Industry Categories with Fresh Food Attributes

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/catalog/dto/*.java`
- Create: `backend/src/main/java/com/chopee/modules/catalog/CategoryService.java`
- Create: `backend/src/main/java/com/chopee/modules/catalog/ProductService.java`
- Create: `backend/src/main/java/com/chopee/modules/catalog/CatalogController.java`
- Test: `backend/src/test/java/com/chopee/modules/catalog/CatalogControllerTest.java`

**Interfaces:**
- Produces: `GET /api/v1/public/categories`, `GET /api/v1/public/products`, `GET /api/v1/public/products/{id}`, `GET /api/v1/public/shops/{id}`.

- [x] **Step 1: Write tests for catalog browsing and filtering**
  Test category tree retrieval, product search by keyword, filter by storage type (`FRESH`), price range, and page sorting.

- [x] **Step 2: Implement category hierarchy service**
  Map parent-child category tree for fast display on navigation menus.

- [x] **Step 3: Implement product search specification with JPA Criteria / QueryDSL**
  Support dynamic filter criteria: `keyword`, `categoryId`, `minPrice`, `maxPrice`, `storageType`, `unit`, `rating`.

- [x] **Step 4: Implement `CatalogController` with OpenAPI annotations**
  Expose endpoints with documentation and pagination metadata (`page`, `size`, `totalElements`).

- [x] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=CatalogControllerTest`. Expected: PASS.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement category and product catalog with multi-industry search filters"`

---

### Task 5: Multi-Vendor Cart Management

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/cart/dto/*.java`
- Create: `backend/src/main/java/com/chopee/modules/cart/CartService.java`
- Create: `backend/src/main/java/com/chopee/modules/cart/CartController.java`
- Test: `backend/src/test/java/com/chopee/modules/cart/CartServiceTest.java`

**Interfaces:**
- Produces: `GET /api/v1/buyer/cart`, `POST /api/v1/buyer/cart/items`, `PUT /api/v1/buyer/cart/items/{id}`, `DELETE /api/v1/buyer/cart/items/{id}`.

- [x] **Step 1: Write unit tests for Cart operations**
  Test adding fractional quantity (e.g., `0.5kg`), incrementing quantity by `stepQuantity`, grouping cart items by shop.

- [x] **Step 2: Implement `CartService`**
  Validate product existence, stock availability, and enforce `minOrderQuantity`.
  Return `CartResponse` containing items grouped by `shopId` with subtotal per shop.

- [x] **Step 3: Implement `CartController`**
  Annotate with `@PreAuthorize("hasRole('BUYER')")`.

- [x] **Step 4: Run tests and verify**
  Run: `mvn test -Dtest=CartServiceTest`. Expected: PASS.

- [x] **Step 5: Commit**
  `git add backend/ && git commit -m "feat: implement multi-vendor cart with fractional quantity support"`

---

### Task 6: Multi-Vendor Order Splitting & ACID Concurrency Stock Deduction

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/order/dto/*.java`
- Create: `backend/src/main/java/com/chopee/modules/order/OrderService.java`
- Create: `backend/src/main/java/com/chopee/modules/order/OrderController.java`
- Test: `backend/src/test/java/com/chopee/modules/order/OrderSplittingTest.java`

**Interfaces:**
- Produces: `POST /api/v1/buyer/orders/checkout-preview`, `POST /api/v1/buyer/orders`, `GET /api/v1/buyer/orders`, `GET /api/v1/buyer/orders/{orderCode}`, `PUT /api/v1/buyer/orders/{orderCode}/cancel`.

- [x] **Step 1: Write integration tests for multi-shop order splitting and stock deduction**
  Test ordering items from Shop 1 and Shop 2 in a single checkout, verify 2 `Order` records created with matching `groupOrderCode`.
  Test overselling scenario: verify transaction rollbacks and throws `InsufficientStockException`.

- [x] **Step 2: Implement Checkout Preview calculation**
  Calculate individual shipping fees (`STANDARD` vs `EXPRESS_FRESH`), apply shop vouchers and platform discounts, return breakdown.

- [x] **Step 3: Implement `@Transactional` Order Creation with Atomic Stock Deduction**
  Execute `productRepository.deductStock(productId, quantity)`.
  Create sub-orders per shop with unique `orderCode` (e.g. `ORD-YYYYMMDD-XXXX`), create `order_items`, clear ordered items from user's cart.

- [x] **Step 4: Implement order cancellation with stock restitution**
  Allow buyer to cancel if `status == PENDING`, re-increment `stock_quantity += quantity`.

- [x] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=OrderSplittingTest`. Expected: PASS.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement atomic multi-vendor order splitting and concurrency stock deduction"`

---

### Task 7: Payment Gateway (COD & VNPay Sandbox QR Integration)

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/payment/VNPayConfig.java`
- Create: `backend/src/main/java/com/chopee/modules/payment/VNPayHelper.java`
- Create: `backend/src/main/java/com/chopee/modules/payment/PaymentService.java`
- Create: `backend/src/main/java/com/chopee/modules/payment/PaymentController.java`
- Test: `backend/src/test/java/com/chopee/modules/payment/VNPayPaymentTest.java`

**Interfaces:**
- Produces: `POST /api/v1/payment/vnpay/create-payment`, `GET /api/v1/payment/vnpay/ipn`, `GET /api/v1/payment/vnpay/callback`.

- [x] **Step 1: Write unit tests for VNPay checksum HMAC-SHA512**
  Verify correct hash generation, parameter sorting, and validation logic.

- [x] **Step 2: Implement VNPay URL Builder & Hash generator**
  Use test credentials (`vnp_TmnCode`, `vnp_HashSecret`, sandbox URL `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`).

- [x] **Step 3: Implement IPN Webhook handler**
  Verify signature, check order status, update `payment_status = 'PAID'` for all orders sharing `groupOrderCode` if `vnp_ResponseCode == "00"`.

- [x] **Step 4: Implement Return URL callback handler**
  Redirect or return status response for client display.

- [x] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=VNPayPaymentTest`. Expected: PASS.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement VNPay sandbox payment gateway with HMAC-SHA512 IPN webhook"`

---

### Task 8: Seller Center & Admin Management APIs

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/seller/dto/*.java`
- Create: `backend/src/main/java/com/chopee/modules/seller/SellerService.java`
- Create: `backend/src/main/java/com/chopee/modules/seller/SellerController.java`
- Create: `backend/src/main/java/com/chopee/modules/admin/AdminService.java`
- Create: `backend/src/main/java/com/chopee/modules/admin/AdminController.java`
- Test: `backend/src/test/java/com/chopee/modules/seller/SellerSecurityTest.java`

**Interfaces:**
- Produces: `/api/v1/seller/dashboard`, `/api/v1/seller/products`, `/api/v1/seller/orders`, `/api/v1/admin/dashboard`, `/api/v1/admin/shops`.

- [x] **Step 1: Write IDOR security tests for Seller operations**
  Verify Seller A receives HTTP 403 when updating Seller B's product or order status.

- [x] **Step 2: Implement Seller Service & Controller**
  Product CRUD with JSON attributes and image URLs; order state transitions (`CONFIRMED`, `SHIPPING`); dashboard revenue aggregation.

- [x] **Step 3: Implement Admin Service & Controller**
  Approve/Reject new shop registrations; lock violating shops; platform-wide revenue & order analytics.

- [x] **Step 4: Run tests and verify**
  Run: `mvn test -Dtest=SellerSecurityTest`. Expected: PASS.

- [x] **Step 5: Commit**
  `git add backend/ && git commit -m "feat: implement Seller Center and Admin Management APIs with IDOR protection"`

---

### Task 9: AI Shopping Copilot Backend Service

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/ai/dto/AIChatRequest.java`
- Create: `backend/src/main/java/com/chopee/modules/ai/dto/AIChatResponse.java`
- Create: `backend/src/main/java/com/chopee/modules/ai/AIShoppingCopilotService.java`
- Create: `backend/src/main/java/com/chopee/modules/ai/AIController.java`
- Test: `backend/src/test/java/com/chopee/modules/ai/AIServiceTest.java`

**Interfaces:**
- Produces: `POST /api/v1/ai/chat` (receives query, queries DB for matching products, prompts LLM, returns structured JSON response with text and product cards).

- [x] **Step 1: Write unit tests for context enrichment & response formatting**
  Verify that when user asks for "nấu canh chua", the service queries fresh vegetables and fish, and embeds them into the response payload.

- [x] **Step 2: Implement Intent Recognition & Context Enrichment**
  Parse keywords from user message (e.g. food ingredients, electronics specs, budget numbers).
  Query MySQL for top 5-8 matching in-stock products.

- [x] **Step 3: Implement LLM Prompt & Client**
  Format system prompt instructing the model to act as an all-in-one shopping advisor and output structured recommendations.

- [x] **Step 4: Implement `AIController`**
  Expose `POST /api/v1/ai/chat`.

- [x] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=AIServiceTest`. Expected: PASS.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement AI Shopping Copilot backend service with context enrichment"`

---

### Task 10: Realistic Vietnamese Marketplace Data Seeding

**Files:**
- Create: `backend/src/main/java/com/chopee/config/DataInitializer.java`
- Test: `backend/src/test/java/com/chopee/config/DataInitializerTest.java`

**Interfaces:**
- Produces: Pre-populated MySQL database on startup with 5 realistic shops, categories, 30+ products across all industries, and 4 test accounts.

- [x] **Step 1: Implement `DataInitializer` bean implementing `CommandLineRunner`**
  Add test users: `admin`, `seller_food`, `seller_drink`, `buyer1` (password: `123456`).
  Add 5 shops: *Nông Sản Sạch Đà Lạt*, *Đại Lý Đồ Uống Hùng Phát*, *Thế Giới Gia Dụng Philips*, *TechZone*, *UniStyle*.
  Add categories with parent-child hierarchy (Thực phẩm tươi sống, Nước giải khát, Thiết bị gia dụng, Phụ kiện công nghệ, Thời trang).
  Add products with real images, prices, units (`kg`, `thùng`, `chiếc`), and rich JSON attributes.

- [x] **Step 2: Run backend test verifying data is seeded without errors**
  Run: `mvn test -Dtest=DataInitializerTest`. Expected: PASS.

- [x] **Step 3: Commit**
  `git add backend/ && git commit -m "feat: add realistic Vietnamese multi-industry marketplace data initializer"`
---

### Task 10B: Advanced Backend APIs: Address Book, Product Reviews & Voucher Promotion System

**Files:**
- Create: `backend/src/main/java/com/chopee/modules/address/**/*.java`
- Create: `backend/src/main/java/com/chopee/modules/review/**/*.java`
- Create: `backend/src/main/java/com/chopee/modules/voucher/**/*.java`
- Test: `backend/src/test/java/com/chopee/modules/address/AddressControllerTest.java`
- Test: `backend/src/test/java/com/chopee/modules/review/ReviewControllerTest.java`
- Test: `backend/src/test/java/com/chopee/modules/voucher/VoucherControllerTest.java`

**Interfaces:**
- Produces: 14 RESTful endpoints covering Buyer Address Book (4), Product Reviews & Ratings with Seller Reply (4), Platform & Shop Vouchers (6). All 60/60 backend tests passing.

- [x] **Step 1: Implement Address Book Module (FR-AUTH-04)**
  `POST /api/v1/buyer/addresses`, `GET /api/v1/buyer/addresses`, `PUT /api/v1/buyer/addresses/{id}/default`, `DELETE /api/v1/buyer/addresses/{id}`.
  Supports default address re-assignment, phone/name validation, and strict user ownership.
  Run: `mvn test -Dtest=AddressControllerTest`. Result: PASS.

- [x] **Step 2: Implement Product Reviews & Ratings Module (FR-PROD-03)**
  `GET /api/v1/public/products/{productId}/reviews`, `POST /api/v1/buyer/reviews`, `GET /api/v1/seller/reviews`, `PUT /api/v1/seller/reviews/{id}/reply`.
  Reviews only permitted for verified purchases of `DELIVERED` orders; automatically updates `ratingAvg` and `reviewCount` on `Product`. Seller replies protected by IDOR validation.
  Run: `mvn test -Dtest=ReviewControllerTest`. Result: PASS.

- [x] **Step 3: Implement Voucher & Promotion System (FR-PAY-03)**
  `GET /api/v1/public/vouchers`, `GET /api/v1/public/shops/{shopId}/vouchers`, `GET /api/v1/public/vouchers/validate`, `GET /api/v1/seller/vouchers`, `POST /api/v1/seller/vouchers`, `POST /api/v1/admin/vouchers`.
  Supports `PERCENT` (with max discount amount cap) and `FIXED_AMOUNT`, min spend check, usage limits, date window validity, and shop vs platform scope.
  Run: `mvn test -Dtest=VoucherControllerTest`. Result: PASS.

- [x] **Step 4: Seed Vouchers and Test Addresses in `DataInitializer`**
  Pre-seed vouchers `CHOPEE10K`, `FREESHIPCHO`, `DALATFARM20`, `TECHSALE50` and sample buyer shipping addresses.

- [x] **Step 5: Run full backend test suite to verify 100% pass rate**
  Run: `mvn test`. Result: 60/60 tests PASS across 13 test suites.

- [x] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement Address Book, Product Reviews, and Voucher APIs with 60/60 passing tests"`

---

### Task 11: React Frontend - Architecture, Core API Client & State Stores

**Files:**
- Existing: `frontend/src/types/index.ts`
- Create: `frontend/src/services/api.ts`
- Create: `frontend/src/stores/useAuthStore.ts`
- Create: `frontend/src/stores/useCartStore.ts`
- Create: `frontend/src/stores/useAddressStore.ts`
- Create: `frontend/src/stores/useVoucherStore.ts`
- Create: `frontend/src/layouts/MarketLayout.tsx`
- Create: `frontend/src/layouts/SellerLayout.tsx`
- Create: `frontend/src/layouts/AdminLayout.tsx`
- Create: `frontend/src/components/Header.tsx`
- Create: `frontend/src/components/Footer.tsx`
- Create: `frontend/src/pages/auth/LoginPage.tsx`
- Create: `frontend/src/pages/auth/RegisterPage.tsx`
- Create: `frontend/src/pages/auth/RegisterSellerPage.tsx`
- Modify: `frontend/src/App.tsx`

**Interfaces:**
- Produces: Axios client with automatic Bearer token injection and error interceptors; Zustand stores for Auth, Cart, Address, and Vouchers; Shopee orange header with real-time cart badge; nested layout routing for Client (`/`), Seller (`/seller/*`), and Admin (`/admin/*`) portals.

- [x] **Step 1: Verify TypeScript DTO and Entity interfaces in `frontend/src/types/index.ts`**
  Verify complete types: `ApiResponse<T>`, `User`, `Shop`, `Category`, `ProductSummary`, `ProductDetail`, `CartItem`, `CartResponse`, `Order`, `OrderItem`, `UserAddress`, `Voucher`, `Review`, `AIChatMessage`, `DiscountType`, `PageResponse<T>`.

- [x] **Step 2: Implement Axios Client in `frontend/src/services/api.ts`**
  Configure Axios instance with `baseURL: '/api/v1'`, request interceptor adding `Authorization: Bearer ${token}`, and response interceptor extracting `response.data` and handling 401 unauthenticated redirect.

- [x] **Step 3: Implement `useAuthStore.ts` with Zustand & LocalStorage persistence**
  Manage `token`, `user`, `isAuthenticated`, `login(emailOrUsername, password)`, `register(...)`, `registerSeller(...)`, `logout()`, and role helpers (`isSeller()`, `isAdmin()`).

- [x] **Step 4: Implement `useCartStore.ts`, `useAddressStore.ts`, and `useVoucherStore.ts`**
  - `useCartStore`: Fetch cart from `/api/v1/buyer/cart`, optimistic `addToCart`, `updateQuantity` (supporting fractional steps), `removeFromCart`, `clearCart`.
  - `useAddressStore`: Fetch addresses from `/api/v1/buyer/addresses`, `addAddress`, `setDefaultAddress`, `deleteAddress`.
  - `useVoucherStore`: Fetch platform/shop vouchers, `validateVoucher(code, orderAmount, shopId)`.

- [x] **Step 5: Implement `Header.tsx` & `Footer.tsx`**
  Shopee orange header (`#EE4D2D`) with search input, category quick links, shopping cart icon with live item count badge, authentication dropdown (Login/Register or User name + Logout + "Kênh Người Bán" / "Quản Trị Sàn" shortcuts).
  Footer with store info, support hotline, and marketplace policy links.

- [x] **Step 6: Implement Layouts (`MarketLayout.tsx`, `SellerLayout.tsx`, `AdminLayout.tsx`)**
  - `MarketLayout`: Header + `<Outlet />` + Footer + floating AI Copilot widget.
  - `SellerLayout`: Seller sidebar (Dashboard, Quản lý sản phẩm, Đơn hàng, Voucher, Đánh giá), topbar, and main content.
  - `AdminLayout`: Admin sidebar (Tổng quan sàn, Duyệt gian hàng, Quản lý voucher), topbar, and main content.

- [x] **Step 7: Implement Auth Pages (`LoginPage.tsx`, `RegisterPage.tsx`, `RegisterSellerPage.tsx`)**
  Clean login/register forms with validation, error messages, and seamless redirect to intended pages.

- [x] **Step 8: Configure React Router in `frontend/src/App.tsx`**
  Setup `BrowserRouter` with routes: `/`, `/login`, `/register`, `/register-seller`, `/products/:id`, `/categories/:id`, `/cart`, `/checkout`, `/orders/success`, `/orders/my`, `/seller/*`, `/admin/*`.

- [x] **Step 9: Verify build & TypeScript compilation**
  Run: `cd frontend && npm run build`. Expected: BUILD SUCCESS with 0 errors (Verified in 36.43s).

- [x] **Step 10: Commit**
  `git add frontend/ && git commit -m "feat: implement frontend architecture, API client, Zustand stores, and navigation layouts"`

---

### Task 12: React Frontend - Marketplace Homepage, Product Catalog & Product Detail UI

**Files:**
- Create: `frontend/src/components/ProductCard.tsx`
- Create: `frontend/src/components/CategoryNav.tsx`
- Create: `frontend/src/components/ReviewList.tsx`
- Create: `frontend/src/pages/HomePage.tsx`
- Create: `frontend/src/pages/ProductDetailPage.tsx`
- Create: `frontend/src/pages/CategoryPage.tsx`

**Interfaces:**
- Produces: Dynamic Homepage with hero carousel banners, category quick-grid, "Chợ Thực Phẩm Tươi Sống Hôm Nay" highlight section, "Gia Dụng & Công Nghệ Hot", paginated product grid; Product Detail page with image gallery, seller badge, fractional quantity selector (`0.5kg` steps), technical JSON specifications table, and customer review list.

- [x] **Step 1: Implement `ProductCard.tsx`**
  Render product card with image thumbnail, discount percentage badge, product title, selling price, strike-through original price, sold count, fresh food badge (`Tươi sống`, `Bảo quản mát`), and unit badge (`kg`, `thùng`, `chiếc`).

- [x] **Step 2: Implement `CategoryNav.tsx`**
  Horizontal category carousel with category icons and links to `/categories/:id`.

- [x] **Step 3: Implement `HomePage.tsx`**
  Hero promotion banners, category navigation, flash sales / hot deals section, fresh grocery section with 2-hour express delivery tag, and paginated product grid with search filtering.

- [x] **Step 4: Implement `ReviewList.tsx`**
  Display star rating breakdown (5-star, 4-star, ...), list of verified customer reviews with buyer name, star rating, comment, date, review images, and seller reply badge.

- [x] **Step 5: Implement `ProductDetailPage.tsx`**
  Product image preview gallery, shop profile snippet (shop name, rating, address), unit selector with `minOrderQuantity` and `stepQuantity` increments, "Thêm Vào Giỏ Hàng" and "Mua Ngay" buttons, rich JSON attributes table (VietGAP, công suất, bảo hành, v.v.), and customer review list.

- [x] **Step 6: Implement `CategoryPage.tsx`**
  Sidebar filters (price range, storage type, min rating, sorting by price / newest / sold), product grid, and pagination.

- [x] **Step 7: Verify build & TypeScript compilation**
  Run: `cd frontend && npm run build`. Expected: BUILD SUCCESS with 0 errors (Verified in 27.48s).

- [x] **Step 8: Commit**
  `git add frontend/ && git commit -m "feat: implement Marketplace Homepage, Product Card, and Product Detail UI"`

---

### Task 13: React Frontend - Multi-Vendor Cart, Address Book, Vouchers & Checkout Flow

**Files:**
- Create: `frontend/src/pages/CartPage.tsx`
- Create: `frontend/src/pages/CheckoutPage.tsx`
- Create: `frontend/src/pages/OrderSuccessPage.tsx`
- Create: `frontend/src/pages/MyOrdersPage.tsx`
- Create: `frontend/src/components/AddressModal.tsx`
- Create: `frontend/src/components/VoucherModal.tsx`
- Create: `frontend/src/components/ReviewModal.tsx`

**Interfaces:**
- Produces: Multi-vendor cart grouped by shop with item checkboxes and fractional quantity updates; Checkout page with shipping address manager modal, shipping method selector (`STANDARD` vs `EXPRESS_FRESH`), platform and shop voucher applicator, checkout preview with live discount calculation, payment selector (COD vs VNPay); Order Success page; Order tracking page with order cancellation and review submission modals.

- [ ] **Step 1: Implement `CartPage.tsx`**
  Group cart items by `shop.name`. Checkbox per item and select-all per shop. Fractional quantity controls (`+` / `-` by `stepQuantity`). Delete item action. Shop subtotal calculation and bottom sticky checkout bar.

- [ ] **Step 2: Implement `AddressModal.tsx`**
  Modal to select existing delivery address or create new address (Receiver Name, Phone, Province, District, Ward, Detail Address, Default checkbox) calling `/api/v1/buyer/addresses`.

- [ ] **Step 3: Implement `VoucherModal.tsx`**
  Modal displaying available shop vouchers and platform vouchers with minimum spend requirements and discount amount, with one-click "Áp dụng" button.

- [ ] **Step 4: Implement `CheckoutPage.tsx`**
  Delivery address card (with change address trigger opening `AddressModal`), order breakdown per shop with shipping method options (`STANDARD` vs `EXPRESS_FRESH`), voucher selection per shop and platform voucher, order preview calculation, payment method options (COD vs VNPay Sandbox QR), and "Đặt Hàng" button triggering `/api/v1/buyer/orders`. If VNPay chosen, automatically redirect to VNPay payment URL.

- [ ] **Step 5: Implement `OrderSuccessPage.tsx`**
  Confirmation screen displaying `groupOrderCode`, summary of split orders created per shop, shipping addresses, payment status, and button to track orders in "Đơn Mua Của Tôi".

- [ ] **Step 6: Implement `MyOrdersPage.tsx` & `ReviewModal.tsx`**
  Tabs: *Tất cả*, *Chờ xác nhận (PENDING)*, *Đang giao (SHIPPING)*, *Đã giao (DELIVERED)*, *Đã hủy (CANCELLED)*.
  Each order card displays shop name, item list, total amount, order code, status badge.
  Action button: "Hủy đơn hàng" if `PENDING` (calling `/api/v1/buyer/orders/{code}/cancel`).
  Action button: "Đánh giá" if `DELIVERED` opening `ReviewModal` to submit 1-5 star rating and comment calling `/api/v1/buyer/reviews`.

- [ ] **Step 7: Verify build & TypeScript compilation**
  Run: `cd frontend && npm run build`. Expected: BUILD SUCCESS with 0 errors.

- [ ] **Step 8: Commit**
  `git add frontend/ && git commit -m "feat: implement multi-vendor cart, address book, vouchers, checkout flow and order review UI"`

---

### Task 14: React Frontend - Floating AI Shopping Copilot Widget

**Files:**
- Create: `frontend/src/components/AIChatWidget.tsx`
- Modify: `frontend/src/layouts/MarketLayout.tsx`

**Interfaces:**
- Produces: Floating animated button at bottom-right corner. Opens interactive chat window with Shopee orange header, typing indicator, suggested prompt chips (*"Gợi ý nguyên liệu nấu canh chua"*, *"Tìm sạc nhanh 65W cho laptop"*), AI natural language shopping recommendations, and interactive miniature product cards with direct "Thêm vào giỏ" button.

- [ ] **Step 1: Implement floating chat launcher and dialog window**
  Include Shopee orange chat header with robot avatar, minimize and close buttons, and smooth open/close animations.

- [ ] **Step 2: Add quick suggestion prompt chips**
  Buttons: *"Gợi ý nguyên liệu nấu canh chua 4 người"*, *"Tìm sạc nhanh 65W cho laptop"*, *"Lên thực đơn 150k mâm cơm gia đình"*, *"Thùng bia & nước ngọt tiệc 8 người"*. Clicking a chip sends the prompt immediately.

- [ ] **Step 3: Connect to Backend AI API (`/api/v1/ai/chat`)**
  Send user messages to backend, maintain conversation history, display typing indicator while awaiting Gemini response.

- [ ] **Step 4: Render rich message bubbles and product recommendation cards**
  Parse suggested products returned from backend API and render miniature product cards with image, price, shop name, and one-click "Thêm vào giỏ" action triggering `useCartStore.addToCart`.

- [ ] **Step 5: Embed `AIChatWidget` into `MarketLayout.tsx`**
  Ensure the widget appears seamlessly across all marketplace browsing pages.

- [ ] **Step 6: Verify build & TypeScript compilation**
  Run: `cd frontend && npm run build`. Expected: BUILD SUCCESS with 0 errors.

- [ ] **Step 7: Commit**
  `git add frontend/ && git commit -m "feat: implement floating AI Shopping Copilot chat widget with rich product cards"`

---

### Task 15: React Frontend - Seller Center & Admin Center Portals

**Files:**
- Create: `frontend/src/pages/seller/SellerDashboardPage.tsx`
- Create: `frontend/src/pages/seller/SellerProductsPage.tsx`
- Create: `frontend/src/pages/seller/SellerOrdersPage.tsx`
- Create: `frontend/src/pages/seller/SellerVouchersPage.tsx`
- Create: `frontend/src/pages/seller/SellerReviewsPage.tsx`
- Create: `frontend/src/pages/admin/AdminDashboardPage.tsx`
- Create: `frontend/src/pages/admin/AdminShopsPage.tsx`
- Create: `frontend/src/pages/admin/AdminVouchersPage.tsx`

**Interfaces:**
- Produces: Complete Seller Center (`/seller`) for merchants to manage products (with fresh food units & JSON attributes), fulfill incoming orders (`CONFIRMED` -> `SHIPPING`), create shop vouchers, and reply to buyer reviews; Complete Admin Center (`/admin`) for platform operators to monitor GMV, approve/reject shops, and manage platform-wide discount vouchers.

- [ ] **Step 1: Implement Seller Dashboard (`SellerDashboardPage.tsx`)**
  Revenue cards (Doanh thu hôm nay, Số đơn chờ xác nhận, Số sản phẩm hết hàng), order trend overview, recent order list.

- [ ] **Step 2: Implement Seller Product Management (`SellerProductsPage.tsx`)**
  Product table with search, category filter, and pagination. Modal form to add/edit product (name, price, stock, unit, storage type `NORMAL`/`FRESH`/`FROZEN_CHILLED`, shelf life, origin, and dynamic key-value attributes table).

- [ ] **Step 3: Implement Seller Order Management (`SellerOrdersPage.tsx`)**
  Filter orders by status (`PENDING`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`). Action buttons: "Xác nhận đơn" (`CONFIRMED`) and "Giao cho shipper" (`SHIPPING`).

- [ ] **Step 4: Implement Seller Vouchers & Reviews (`SellerVouchersPage.tsx`, `SellerReviewsPage.tsx`)**
  - Vouchers: List shop vouchers, modal form to create shop voucher (code, discount type, value, min order amount, max discount, start/end dates, usage limit).
  - Reviews: List buyer reviews for shop products, modal/inline form for seller to submit reply to customer review.

- [ ] **Step 5: Implement Admin Dashboard & Shop Management (`AdminDashboardPage.tsx`, `AdminShopsPage.tsx`)**
  - Admin Dashboard: Platform GMV, total active shops, total orders, total buyers.
  - Shop Management: Table of registered shops with status badges (`PENDING`, `APPROVED`, `REJECTED`, `LOCKED`). Action buttons to "Phê duyệt" (Approve), "Từ chối" (Reject), or "Khóa gian hàng" (Lock).

- [ ] **Step 6: Implement Admin Platform Vouchers (`AdminVouchersPage.tsx`)**
  Manage platform-wide vouchers applicable to all shops, toggle active/inactive status, and create new platform vouchers.

- [ ] **Step 7: Verify build & TypeScript compilation**
  Run: `cd frontend && npm run build`. Expected: BUILD SUCCESS with 0 errors.

- [ ] **Step 8: Commit**
  `git add frontend/ && git commit -m "feat: implement Seller Center and Admin Management frontend portals"`

---

### Task 16: End-to-End System Verification, Quality Gates & GitHub Repository Sync

**Files:**
- Modify: `README.md` (Add quickstart run instructions, credentials, API catalog, and architecture overview)
- Test: Full End-to-End integration test across Backend, Frontend, and Database.

- [ ] **Step 1: Run full backend test suite**
  Run: `cd backend && mvn clean test`. Expected: 60/60 unit & integration tests PASS across all 13 test suites.

- [ ] **Step 2: Run frontend production build**
  Run: `cd frontend && npm run build`. Expected: Vite build succeeds with 0 errors.

- [ ] **Step 3: Verify End-to-End user journeys**
  - **Journey 1 (Buyer Browsing & AI Copilot):** Login as `buyer1` (pass: `123456`), browse categories, ask AI Copilot for soup ingredients, click add to cart from recommendation card.
  - **Journey 2 (Multi-Vendor Cart & Vouchers):** Add 0.5kg cà chua (Shop Đà Lạt) and 1 thùng bia (Shop Hùng Phát) to cart. View cart grouped by shop.
  - **Journey 3 (Checkout & Order Splitting):** Open checkout, select/add delivery address via Address Book, apply voucher `CHOPEE10K`, checkout with COD. Verify 2 separate orders created with common `groupOrderCode`.
  - **Journey 4 (Seller Order Fulfillment):** Login as `seller_food`, verify Đà Lạt sub-order is visible, confirm order (`CONFIRMED`), dispatch order (`SHIPPING`).
  - **Journey 5 (Admin Platform Operations):** Login as `admin`, verify dashboard metrics, inspect registered shops, view platform vouchers.
  - **Journey 6 (Reviews & Rating Sync):** Mark order delivered, login as `buyer1`, submit 5-star review, verify seller receives notification and replies.

- [ ] **Step 4: Update `README.md` with Quickstart Guide**
  Document prerequisites, MySQL configuration, backend start command (`mvn spring-boot:run`), frontend start command (`npm run dev`), demo credentials table, and Swagger OpenAPI link (`http://localhost:8080/swagger-ui.html`).

- [ ] **Step 5: Git commit and push to GitHub repository `Tonyisme1/Chopee`**
  Run: `git add . && git commit -m "docs: complete implementation plan and full system synchronization"`
  Run: `git push origin main`.

