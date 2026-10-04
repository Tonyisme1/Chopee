# Chopee Marketplace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, production-grade Multi-Vendor E-Commerce Marketplace platform ("Chopee") specializing in multi-industry commerce, fresh grocery foods, beverages, and household/tech goods, integrated with a floating AI Shopping Copilot, COD & VNPay Sandbox payments, and dedicated Buyer, Seller, and Admin portals.

**Architecture:** Modular Monolith using Spring Boot 3 (Java 17) for the REST backend with Spring Data JPA and Spring Security 6 (Stateless JWT). A single React (Vite + TypeScript + Tailwind CSS) frontend organized into 3 nested layout portals: Client Marketplace (`/`), Seller Center (`/seller`), and Admin Management (`/admin`). MySQL 8 serves as the relational data store with ACID transaction safety for multi-vendor order splitting and atomic stock deductions.

**Tech Stack:** Java 17, Spring Boot 3.3.x, Spring Data JPA, Hibernate, Spring Security 6, JJWT, Springdoc OpenAPI, MySQL 8, React 18/19, TypeScript, Vite, Tailwind CSS, Lucide Icons, Zustand, Axios, VNPay Sandbox, Google Gemini 1.5 Flash API.

**Spec:** [docs/superpowers/specs/2026-10-03-shopee-marketplace-design.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/superpowers/specs/2026-10-03-shopee-marketplace-design.md) & [docs/specs/00_index.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/00_index.md).

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
3. IDOR security: A seller authenticated for Shop A must receive HTTP 403 when attempting to mutate products or orders belonging to Shop B.
4. VNPay checksum verification: Tampered or invalid `vnp_SecureHash` in IPN webhook callbacks must be rejected with HTTP 400 and not mark orders as `PAID`.
5. AI Copilot grounding: AI chat must enrich context with real database product items so it never hallucinates non-existent products.

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


### Task 2: Core Data Model & JPA Entity Layer

**Files:**
- Create: `backend/src/main/java/com/chopee/entity/*.java`
- Create: `backend/src/main/java/com/chopee/repository/*.java`
- Test: `backend/src/test/java/com/chopee/repository/EntityMappingTest.java`

**Interfaces:**
- Produces: JPA entities (`User`, `UserAddress`, `Shop`, `Category`, `Product`, `ProductImage`, `ProductVariant`, `CartItem`, `Order`, `OrderItem`, `Voucher`, `Payment`, `Review`) and repositories.

- [ ] **Step 1: Write integration test for entity creation and relations**
  Test creating a user with `ROLE_SELLER`, a shop, a category, and a product with `attributes` JSON.

- [ ] **Step 2: Implement Enums and Base Entities**
  Define `Role`, `UserStatus`, `ShopType`, `ShopStatus`, `StorageType`, `ProductStatus`, `ShippingMethod`, `PaymentMethod`, `PaymentStatus`, `OrderStatus`.

- [ ] **Step 3: Implement User, Shop, Category, and Product Entities**
  Include fresh food fields: `unit`, `minOrderQuantity`, `stepQuantity`, `storageType`, `shelfLife`, `origin`, and JSON `attributes` string with converter.

- [ ] **Step 4: Implement CartItem, Order, OrderItem, Voucher, Payment Entities**
  Ensure `Order` contains `groupOrderCode`, `shop`, `user`, amounts, status, and shipping fields.

- [ ] **Step 5: Create Spring Data JPA Repositories**
  `UserRepository`, `ShopRepository`, `CategoryRepository`, `ProductRepository`, `CartItemRepository`, `OrderRepository`, `VoucherRepository`.

- [ ] **Step 6: Run tests to verify entity mappings**
  Run: `mvn test -Dtest=EntityMappingTest`. Expected: PASS.

- [ ] **Step 7: Commit**
  `git add backend/ && git commit -m "feat: implement JPA entities and repositories for Chopee marketplace"`

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

- [ ] **Step 1: Write tests for catalog browsing and filtering**
  Test category tree retrieval, product search by keyword, filter by storage type (`FRESH`), price range, and page sorting.

- [ ] **Step 2: Implement category hierarchy service**
  Map parent-child category tree for fast display on navigation menus.

- [ ] **Step 3: Implement product search specification with JPA Criteria / QueryDSL**
  Support dynamic filter criteria: `keyword`, `categoryId`, `minPrice`, `maxPrice`, `storageType`, `unit`, `rating`.

- [ ] **Step 4: Implement `CatalogController` with OpenAPI annotations**
  Expose endpoints with documentation and pagination metadata (`page`, `size`, `totalElements`).

- [ ] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=CatalogControllerTest`. Expected: PASS.

- [ ] **Step 6: Commit**
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

- [ ] **Step 1: Write unit tests for Cart operations**
  Test adding fractional quantity (e.g., `0.5kg`), incrementing quantity by `stepQuantity`, grouping cart items by shop.

- [ ] **Step 2: Implement `CartService`**
  Validate product existence, stock availability, and enforce `minOrderQuantity`.
  Return `CartResponse` containing items grouped by `shopId` with subtotal per shop.

- [ ] **Step 3: Implement `CartController`**
  Annotate with `@PreAuthorize("hasRole('BUYER')")`.

- [ ] **Step 4: Run tests and verify**
  Run: `mvn test -Dtest=CartServiceTest`. Expected: PASS.

- [ ] **Step 5: Commit**
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

- [ ] **Step 1: Write integration tests for multi-shop order splitting and stock deduction**
  Test ordering items from Shop 1 and Shop 2 in a single checkout, verify 2 `Order` records created with matching `groupOrderCode`.
  Test overselling scenario: verify transaction rollbacks and throws `InsufficientStockException`.

- [ ] **Step 2: Implement Checkout Preview calculation**
  Calculate individual shipping fees (`STANDARD` vs `EXPRESS_FRESH`), apply shop vouchers and platform discounts, return breakdown.

- [ ] **Step 3: Implement `@Transactional` Order Creation with Atomic Stock Deduction**
  Execute `productRepository.deductStock(productId, quantity)`.
  Create sub-orders per shop with unique `orderCode` (e.g. `ORD-YYYYMMDD-XXXX`), create `order_items`, clear ordered items from user's cart.

- [ ] **Step 4: Implement order cancellation with stock restitution**
  Allow buyer to cancel if `status == PENDING`, re-increment `stock_quantity += quantity`.

- [ ] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=OrderSplittingTest`. Expected: PASS.

- [ ] **Step 6: Commit**
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

- [ ] **Step 1: Write unit tests for VNPay checksum HMAC-SHA512**
  Verify correct hash generation, parameter sorting, and validation logic.

- [ ] **Step 2: Implement VNPay URL Builder & Hash generator**
  Use test credentials (`vnp_TmnCode`, `vnp_HashSecret`, sandbox URL `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`).

- [ ] **Step 3: Implement IPN Webhook handler**
  Verify signature, check order status, update `payment_status = 'PAID'` for all orders sharing `groupOrderCode` if `vnp_ResponseCode == "00"`.

- [ ] **Step 4: Implement Return URL callback handler**
  Redirect or return status response for client display.

- [ ] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=VNPayPaymentTest`. Expected: PASS.

- [ ] **Step 6: Commit**
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

- [ ] **Step 1: Write IDOR security tests for Seller operations**
  Verify Seller A receives HTTP 403 when updating Seller B's product or order status.

- [ ] **Step 2: Implement Seller Service & Controller**
  Product CRUD with JSON attributes and image URLs; order state transitions (`CONFIRMED`, `SHIPPING`); dashboard revenue aggregation.

- [ ] **Step 3: Implement Admin Service & Controller**
  Approve/Reject new shop registrations; lock violating shops; platform-wide revenue & order analytics.

- [ ] **Step 4: Run tests and verify**
  Run: `mvn test -Dtest=SellerSecurityTest`. Expected: PASS.

- [ ] **Step 5: Commit**
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

- [ ] **Step 1: Write unit tests for context enrichment & response formatting**
  Verify that when user asks for "nấu canh chua", the service queries fresh vegetables and fish, and embeds them into the response payload.

- [ ] **Step 2: Implement Intent Recognition & Context Enrichment**
  Parse keywords from user message (e.g. food ingredients, electronics specs, budget numbers).
  Query MySQL for top 5-8 matching in-stock products.

- [ ] **Step 3: Implement LLM Prompt & Client**
  Format system prompt instructing the model to act as an all-in-one shopping advisor and output structured recommendations.

- [ ] **Step 4: Implement `AIController`**
  Expose `POST /api/v1/ai/chat`.

- [ ] **Step 5: Run tests and verify**
  Run: `mvn test -Dtest=AIServiceTest`. Expected: PASS.

- [ ] **Step 6: Commit**
  `git add backend/ && git commit -m "feat: implement AI Shopping Copilot backend service with context enrichment"`

---

### Task 10: Realistic Vietnamese Marketplace Data Seeding

**Files:**
- Create: `backend/src/main/java/com/chopee/config/DataInitializer.java`
- Test: `backend/src/test/java/com/chopee/config/DataInitializerTest.java`

**Interfaces:**
- Produces: Pre-populated MySQL database on startup with 5 realistic shops, categories, 30+ products across all industries, and 4 test accounts.

- [ ] **Step 1: Implement `DataInitializer` bean implementing `CommandLineRunner`**
  Add test users: `admin`, `seller_food`, `seller_drink`, `buyer1` (password: `123456`).
  Add 5 shops: *Nông Sản Sạch Đà Lạt*, *Đại Lý Đồ Uống Hùng Phát*, *Thế Giới Gia Dụng Philips*, *TechZone*, *UniStyle*.
  Add categories with parent-child hierarchy (Thực phẩm tươi sống, Nước giải khát, Thiết bị gia dụng, Phụ kiện công nghệ, Thời trang).
  Add products with real images, prices, units (`kg`, `thùng`, `chiếc`), and rich JSON attributes.

- [ ] **Step 2: Run backend test verifying data is seeded without errors**
  Run: `mvn test -Dtest=DataInitializerTest`. Expected: PASS.

- [ ] **Step 3: Commit**
  `git add backend/ && git commit -m "feat: add realistic Vietnamese multi-industry marketplace data initializer"`

---

### Task 11: React Frontend - Architecture, State Stores & Layouts

**Files:**
- Create: `frontend/src/types/*.ts`
- Create: `frontend/src/api/axiosClient.ts`
- Create: `frontend/src/store/useAuthStore.ts`
- Create: `frontend/src/store/useCartStore.ts`
- Create: `frontend/src/layouts/MarketLayout.tsx`
- Create: `frontend/src/layouts/SellerLayout.tsx`
- Create: `frontend/src/layouts/AdminLayout.tsx`
- Create: `frontend/src/components/Header.tsx`
- Create: `frontend/src/components/Footer.tsx`
- Modify: `frontend/src/App.tsx`

**Interfaces:**
- Produces: Navigation shell with Shopee-like top header, search bar, cart icon with live badge, user dropdown, and 3 layout routes.

- [ ] **Step 1: Define TypeScript interfaces**
  `User`, `Shop`, `Category`, `Product`, `CartItem`, `Order`, `OrderItem`, `AIChatMessage`.

- [ ] **Step 2: Configure Axios client with JWT interceptor**
  Attach `Authorization: Bearer <token>` automatically and handle 401 redirect to login.

- [ ] **Step 3: Create Zustand stores with LocalStorage persistence**
  `useAuthStore` (login, logout, token, user) and `useCartStore` (items, count, addItem, removeItem, fetchCart).

- [ ] **Step 4: Build MarketLayout, Header & Footer**
  Header includes Shopee orange banner, search bar with category suggestions, cart button with badge, and links to "Kênh Người Bán" and "Quản Trị Sàn".

- [ ] **Step 5: Setup React Router DOM routing**
  Configure routes: `/`, `/login`, `/register`, `/seller/*`, `/admin/*`.

- [ ] **Step 6: Build and verify frontend**
  Run: `npm run build`. Expected: SUCCESS.

- [ ] **Step 7: Commit**
  `git add frontend/ && git commit -m "feat: setup React frontend architecture, Zustand stores, and navigation layouts"`

---

### Task 12: React Frontend - Marketplace Homepage & Product Detail UI

**Files:**
- Create: `frontend/src/components/ProductCard.tsx`
- Create: `frontend/src/components/CategoryNav.tsx`
- Create: `frontend/src/pages/HomePage.tsx`
- Create: `frontend/src/pages/ProductDetailPage.tsx`
- Create: `frontend/src/pages/CategoryPage.tsx`

**Interfaces:**
- Produces: Homepage with banners, category carousel, flash sales, fresh food badge, product grid, and interactive Product Detail page with unit picker and dynamic attributes table.

- [ ] **Step 1: Implement `ProductCard.tsx`**
  Display thumbnail, product name, selling price, original price, sold count, fresh food badge (`Tươi sống`, `Bảo quản mát`), unit badge (`kg`, `thùng`, `chiếc`).

- [ ] **Step 2: Implement `HomePage.tsx`**
  Show hero banner, category icons, "Chợ Tươi Sống Hôm Nay" section, "Gia Dụng & Công Nghệ Hot" section, and infinite scroll / paginated product list.

- [ ] **Step 3: Implement `ProductDetailPage.tsx`**
  Image preview gallery, shop info card, quantity selector (supporting fractional steps like `0.5kg` for produce), "Thêm vào giỏ hàng" and "Mua ngay" buttons, and JSON specs table (Công suất, bảo hành, chứng nhận VietGAP...).

- [ ] **Step 4: Build and verify frontend**
  Run: `npm run build`. Expected: SUCCESS.

- [ ] **Step 5: Commit**
  `git add frontend/ && git commit -m "feat: implement Marketplace Homepage, Product Card, and Product Detail UI"`

---

### Task 13: React Frontend - Multi-Vendor Cart & Checkout Flow

**Files:**
- Create: `frontend/src/pages/CartPage.tsx`
- Create: `frontend/src/pages/CheckoutPage.tsx`
- Create: `frontend/src/pages/OrderSuccessPage.tsx`
- Create: `frontend/src/pages/MyOrdersPage.tsx`

**Interfaces:**
- Produces: Cart grouped by shop with item checkboxes, Checkout page with shipping method options (`STANDARD` vs `EXPRESS_FRESH`), payment selection (COD vs VNPay QR), and order tracking page.

- [ ] **Step 1: Implement `CartPage.tsx`**
  Group items by `shop.name`. Allow selecting all items from a shop or individual items. Support increasing/decreasing quantity by `stepQuantity`.

- [ ] **Step 2: Implement `CheckoutPage.tsx`**
  Preview sub-orders per shop with shipping fee calculation, shipping address form, payment method selector (COD, VNPay).
  Handle order creation and redirect to VNPay Sandbox URL if selected.

- [ ] **Step 3: Implement `OrderSuccessPage.tsx` & `MyOrdersPage.tsx`**
  Display `groupOrderCode` and breakdown of individual shop orders. Show live status pills (`PENDING`, `SHIPPING`, `DELIVERED`).

- [ ] **Step 4: Build and verify frontend**
  Run: `npm run build`. Expected: SUCCESS.

- [ ] **Step 5: Commit**
  `git add frontend/ && git commit -m "feat: implement multi-vendor cart, checkout, payment redirection, and order tracking"`

---

### Task 14: React Frontend - Floating AI Shopping Copilot Widget

**Files:**
- Create: `frontend/src/components/AIChatWidget.tsx`
- Modify: `frontend/src/layouts/MarketLayout.tsx`

**Interfaces:**
- Produces: Floating animated button at bottom-right corner. Opens chat window with typing indicator, suggested prompts, natural language advice, and interactive product cards with direct "Thêm vào giỏ" button.

- [ ] **Step 1: Implement floating chat launcher and dialog window**
  Include Shopee orange chat header with robot avatar, minimize and close buttons.

- [ ] **Step 2: Add quick suggestion prompt chips**
  Buttons: *"Gợi ý nguyên liệu nấu canh chua 4 người"*, *"Tìm sạc nhanh 65W cho laptop"*, *"Lên thực đơn 150k mâm cơm gia đình"*, *"Thùng bia & nước ngọt tiệc 8 người"*.

- [ ] **Step 3: Render rich message bubbles and product recommendation cards**
  Parse suggested products returned from backend API and render miniature product cards with image, price, and one-click "Thêm vào giỏ" action triggering `useCartStore.addItem`.

- [ ] **Step 4: Build and verify frontend**
  Run: `npm run build`. Expected: SUCCESS.

- [ ] **Step 5: Commit**
  `git add frontend/ && git commit -m "feat: implement floating AI Shopping Copilot chat widget with rich product cards"`

---

### Task 15: React Frontend - Seller Center & Admin Center Portals

**Files:**
- Create: `frontend/src/pages/seller/SellerDashboardPage.tsx`
- Create: `frontend/src/pages/seller/SellerProductsPage.tsx`
- Create: `frontend/src/pages/seller/SellerOrdersPage.tsx`
- Create: `frontend/src/pages/admin/AdminDashboardPage.tsx`
- Create: `frontend/src/pages/admin/AdminShopsPage.tsx`

**Interfaces:**
- Produces: Seller portal (`/seller`) to add/edit products with custom attributes and update order statuses; Admin portal (`/admin`) to approve shops and view platform statistics.

- [ ] **Step 1: Implement Seller Dashboard and Product Management**
  Data table with search and pagination, modal form to add/edit product (inputs for name, price, stock, unit, storage type, shelf life, and key-value JSON attributes).

- [ ] **Step 2: Implement Seller Order Management**
  View incoming orders for the seller's shop, action buttons to confirm order (`CONFIRMED`) and dispatch to shipper (`SHIPPING`).

- [ ] **Step 3: Implement Admin Portal**
  Dashboard metrics cards (total shops, total revenue, total orders), table to approve pending shop registrations or lock shops.

- [ ] **Step 4: Build and verify frontend**
  Run: `npm run build`. Expected: SUCCESS.

- [ ] **Step 5: Commit**
  `git add frontend/ && git commit -m "feat: implement Seller Center and Admin Management frontend portals"`

---

### Task 16: End-to-End System Verification & GitHub Repository Sync

**Files:**
- Modify: `README.md` (Add quickstart run instructions and credentials)
- Test: Full End-to-End integration test across Backend, Frontend, and Database.

- [ ] **Step 1: Run full backend test suite**
  Run: `cd backend && mvn clean test`. Expected: All unit & integration tests PASS.

- [ ] **Step 2: Run frontend production build**
  Run: `cd frontend && npm run build`. Expected: Vite build succeeds with 0 errors.

- [ ] **Step 3: Verify End-to-End flows**
  Test 1: Login as `buyer1`, browse categories, add 0.5kg tomato (Shop Đà Lạt) and 1 thùng bia (Shop Hùng Phát) to cart.
  Test 2: Open AI Chat widget, ask for recipe, click add to cart from recommendation.
  Test 3: Checkout with COD, verify 2 separate orders created with common `groupOrderCode`.
  Test 4: Login as `seller_food`, verify order from Shop Đà Lạt is visible, update status to `SHIPPING`.
  Test 5: Login as `admin`, verify dashboard metrics and shop listings.

- [ ] **Step 4: Update README.md with Quickstart Guide**
  Document how to run MySQL, start backend with `mvn spring-boot:run`, start frontend with `npm run dev`, and list default demo accounts.

- [ ] **Step 5: Push full implementation plan and code to GitHub `Tonyisme1/Chopee`**
  Run: `git add . && git commit -m "docs: complete detailed implementation plan for Chopee marketplace"`
  Run: `git push origin main`.
