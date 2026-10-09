# Ma Trận Phân Loại Đa Ngành & Nhập Hàng Siêu Tốc Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Triển khai hoàn chỉnh hệ thống Ma trận Phân loại 2 cấp chuẩn Shopee và bộ công cụ nhập hàng siêu tốc (Mẫu 1-Click theo ngành, Áp dụng giá/kho hàng loạt, Nhân bản nhanh sản phẩm) trên cả Backend và Frontend Chopee.

**Architecture:** Mở rộng CSDL với `tier_variation` trên bảng `products`, `sku` và `attributes` trên `product_variants`. Cung cấp API `duplicateProduct` kèm bảo vệ IDOR. Xây dựng giao diện người bán với bộ sinh ma trận Cartesian Product và thanh Bulk Apply, đồng thời nâng cấp giao diện người mua chọn 2 tầng nút bấm trực quan.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, PostgreSQL/H2/MySQL, React 18, TypeScript, Tailwind CSS, Lucide Icons, Vite.

**Spec:** `docs/superpowers/specs/2026-10-09-multi-tier-variant-matrix-design.md`

## Global Constraints
- Purchase quantity (`quantity`) MUST ALWAYS be an integer (`1, 2, 3, 4...`).
- Enforce strict IDOR protection: Shop A cannot duplicate, update, or read unowned Shop B products.
- Pre-commit verification gates: `mvn test` and `npm run build` must pass with 0 errors.
- Mandatory change record in `docs/specs/07_changelog.md` with Change Key `CHG-20261009-002`.

## Review Focus
1. Backward compatibility: Sản phẩm cũ không có `tier_variation` vẫn hiển thị và mua hàng bình thường.
2. Cartesian Product Generator: Xử lý đúng khi người bán chỉ nhập 1 nhóm phân loại (1-tier) hoặc cả 2 nhóm (2-tier).
3. Bulk Apply: Cập nhật giá và tồn kho đồng loạt không làm mất tên hoặc cấu trúc ma trận.
4. Duplicate Product: Đảm bảo sinh `slug` mới không trùng lặp và sao chép đầy đủ các biến thể.
5. Buyer UI Combination Matching: Chọn kết hợp Nhóm 1 và Nhóm 2 phải ánh xạ chính xác đến `variantId` và đơn giá tương ứng.

---

### Task 1: Cập Nhật Thực Thể CSDL & Khởi Tạo Dữ Liệu Mẫu

**Files:**
- Modify: `backend/src/main/java/com/chopee/entity/Product.java`
- Modify: `backend/src/main/java/com/chopee/entity/ProductVariant.java`
- Modify: `backend/src/main/resources/db/chopee_schema_full.sql`
- Modify: `backend/src/main/java/com/chopee/config/DataInitializer.java`
- Test: `backend/src/test/java/com/chopee/repository/EntityMappingTest.java`

**Interfaces:**
- Produces: `product.getTierVariation()`, `variant.getSku()`, `variant.getAttributes()`

- [ ] **Step 1: Cập nhật `Product.java` và `ProductVariant.java`**
  - Thêm `@Column(columnDefinition = "TEXT") private String tierVariation;` vào `Product.java`.
  - Thêm `@Column(length = 100) private String sku;` và `@Column(columnDefinition = "TEXT") private String attributes;` vào `ProductVariant.java`.

- [ ] **Step 2: Cập nhật `chopee_schema_full.sql`**
  - Bổ sung `tier_variation TEXT NULL` vào `CREATE TABLE products`.
  - Bổ sung `sku VARCHAR(100) NULL` và `attributes TEXT NULL` vào `CREATE TABLE product_variants`.

- [ ] **Step 3: Cập nhật `DataInitializer.java`**
  - Cập nhật hàm `createProduct` để lưu `tierVariation`, sinh `sku` và `attributes` cho các biến thể thực tế (rau củ, điện thoại, bàn phím, thịt cá).

- [ ] **Step 4: Chạy kiểm thử xác nhận**
  - Chạy `mvn test -Dtest=EntityMappingTest`
  - Đảm bảo PASS 100%.

- [ ] **Step 5: Commit**
  - `git commit -m "feat: add tierVariation, sku and attributes to Product and ProductVariant entities"`

---

### Task 2: Backend DTOs, API Nhân Bản Sản Phẩm & Mở Rộng Seller Service

**Files:**
- Modify: `backend/src/main/java/com/chopee/modules/seller/dto/CreateProductRequest.java`
- Modify: `backend/src/main/java/com/chopee/modules/seller/dto/UpdateProductRequest.java`
- Modify: `backend/src/main/java/com/chopee/modules/seller/dto/CreateVariantRequest.java`
- Modify: `backend/src/main/java/com/chopee/modules/catalog/dto/ProductDetailResponse.java`
- Modify: `backend/src/main/java/com/chopee/modules/catalog/dto/ProductVariantResponse.java`
- Modify: `backend/src/main/java/com/chopee/modules/catalog/ProductService.java`
- Modify: `backend/src/main/java/com/chopee/modules/seller/SellerService.java`
- Modify: `backend/src/main/java/com/chopee/modules/seller/SellerController.java`
- Test: `backend/src/test/java/com/chopee/modules/seller/SellerControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/seller/products/{id}/duplicate` trả về `ProductDetailResponse`.

- [ ] **Step 1: Cập nhật các DTO Requests và Responses**
  - `CreateProductRequest` & `UpdateProductRequest`: thêm `String tierVariation`.
  - `CreateVariantRequest`: thêm `String sku`, `String attributes`.
  - `ProductDetailResponse`: thêm `String tierVariation`.
  - `ProductVariantResponse`: thêm `String sku`, `String attributes`.

- [ ] **Step 2: Cập nhật `ProductService.mapToDetailResponse`**
  - Map `tierVariation` và các trường `sku`, `attributes` của từng variant.

- [ ] **Step 3: Cập nhật `SellerService.createProduct`, `updateProduct` và thêm `duplicateProduct`**
  - Xử lý lưu `tierVariation`, `sku`, `attributes` trong `createProduct` và `updateProduct`.
  - Viết hàm `duplicateProduct(Long sellerId, Long productId)`: kiểm tra IDOR sở hữu shop, sao chép sản phẩm với tên `[Bản sao] + name`, sinh slug ngẫu nhiên, sao chép variants.

- [ ] **Step 4: Thêm endpoint vào `SellerController.java`**
  - Thêm `@PostMapping("/products/{id}/duplicate")`.

- [ ] **Step 5: Chạy toàn bộ backend test**
  - Chạy `mvn test`.
  - Đảm bảo 60/60 tests PASS.

- [ ] **Step 6: Commit**
  - `git commit -m "feat: support variant matrix attributes and product duplication in seller API"`

---

### Task 3: Frontend Seller UI - Ma Trận Phân Loại & Nhập Hàng Siêu Tốc

**Files:**
- Modify: `frontend/src/types/index.ts`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/pages/seller/SellerProductsPage.tsx`

**Interfaces:**
- Consumes: `sellerApi.duplicateProduct(id)`
- Produces: Form nhập liệu ma trận 2 cấp, thanh áp dụng hàng loạt, nút mẫu ngành hàng.

- [ ] **Step 1: Cập nhật TypeScript Types & API Client**
  - Thêm `tierVariation?: string`, `sku?: string`, `attributes?: string` vào `ProductDetail`, `ProductVariant`, `CreateProductPayload`.
  - Thêm `sellerApi.duplicateProduct(id: number)` trong `frontend/src/services/api.ts`.

- [ ] **Step 2: Tích hợp Bộ Mẫu 1-Click Theo Ngành Hàng (Industry Presets)**
  - Thêm các nút mẫu: `🥬 Rau củ`, `🥩 Thịt tươi`, `🌾 Gạo / Nông sản`, `📱 Điện thoại`, `⌨️ Bàn phím cơ`, `🎧 Tai nghe`, `🧃 Đồ uống`.
  - Khi click: tự động điền nhóm 1 và nhóm 2 tương ứng.

- [ ] **Step 3: Tích hợp Bộ Sinh Ma Trận 2 Cấp & Thanh Áp Dụng Hàng Loạt (Bulk Apply Bar)**
  - Tự động sinh danh sách phân loại `Nhóm 1 × Nhóm 2`.
  - Khung "Áp dụng cho tất cả phân loại": Điền 1 giá + 1 tồn kho ➔ Click là toàn bộ các dòng ma trận nhận giá và kho ngay lập tức.
  - Cho phép sửa tay từng dòng.

- [ ] **Step 4: Tích hợp Nút Nhân Bản Sản Phẩm (Quick Duplicate)**
  - Thêm nút Copy cạnh nút Sửa/Xóa trong bảng danh sách sản phẩm.
  - Bấm vào sẽ gọi API duplicate và mở modal chỉnh sửa nhanh.

- [ ] **Step 5: Kiểm tra build TypeScript**
  - Chạy `npm run build` trong `frontend/`.
  - Đảm bảo 0 lỗi TypeScript.

- [ ] **Step 6: Commit**
  - `git commit -m "feat: add multi-tier variant matrix, industry presets and bulk apply to seller UI"`

---

### Task 4: Frontend Buyer UI - Chọn Mua 2 Tầng Phân Loại Chuẩn Shopee

**Files:**
- Modify: `frontend/src/pages/ProductDetailPage.tsx`

**Interfaces:**
- Hiển thị 2 nhóm phân loại độc lập khi sản phẩm có `tierVariation`.

- [ ] **Step 1: Phân tích `tierVariation` trên `ProductDetailPage.tsx`**
  - Parse JSON `product.tierVariation` nếu có để lấy danh sách 2 nhóm: `group1: { name, options }`, `group2: { name, options }`.
  - Quản lý state `selectedTier1: string` và `selectedTier2: string`.

- [ ] **Step 2: Render 2 Tầng Nút Bấm Phân Loại**
  - Tầng 1: Hiển thị các nút của Nhóm 1 (ví dụ: [Tươi] [Khô] hoặc [Đen] [Titan]).
  - Tầng 2: Hiển thị các nút của Nhóm 2 (ví dụ: [Túi 500g] [Túi 1kg] [Túi 2kg] hoặc [128GB] [256GB]).

- [ ] **Step 3: Ánh Xạ Biến Thể Tương Ứng & Cập Nhật Giá**
  - Khi chọn đủ 2 tầng: Tìm biến thể khớp với cặp thuộc tính để lấy `variantId`, giá bán và tồn kho.
  - Cập nhật Price Box và số lượng tối đa.

- [ ] **Step 4: Giữ Vững Quy Ước Số Lượng Nguyên (`Quantity Stepper`)**
  - Nút tăng giảm số lượng strictly integer (+1, -1, tối thiểu 1).
  - Tạm tính = Giá biến thể × Số lượng nguyên.

- [ ] **Step 5: Kiểm tra build TypeScript**
  - Chạy `npm run build` trong `frontend/`.
  - Đảm bảo 0 lỗi TypeScript.

- [ ] **Step 6: Commit**
  - `git commit -m "feat: add 2-tier variant selection UI for buyers on ProductDetailPage"`

---

### Task 5: Xác Thực Toàn Diện, Ghi Nhật Ký Hệ Thống & Hoàn Tất

**Files:**
- Modify: `docs/specs/07_changelog.md`

- [ ] **Step 1: Chạy kiểm thử tự động toàn diện**
  - Chạy `mvn test` (đảm bảo 60/60 tests backend PASS).
  - Chạy `npm run build` (đảm bảo frontend đóng gói thành công).

- [ ] **Step 2: Ghi nhận nhật ký thay đổi `CHG-20261009-002`**
  - Thêm bản ghi chi tiết vào `docs/specs/07_changelog.md`.

- [ ] **Step 3: Commit và đẩy code lên GitHub**
  - `git push origin main`

