# Đặc Tả Kỹ Thuật: Hệ Thống Ma Trận Phân Loại Đa Ngành & Nhập Hàng Siêu Tốc Cho Người Bán
**Mã thiết kế:** `SPEC-20261009-VARIANT-MATRIX`  
**Ngày lập:** 2026-10-09  
**Tác giả:** Antigravity AI Assistant & Engineering Team  
**Trạng thái:** APPROVED FOR IMPLEMENTATION  

---

## 1. Bối Cảnh & Vấn Đề (Context & Problem Statement)

Trong thực tế vận hành sàn thương mại điện tử đa ngành (nông sản thực phẩm tươi sống, gạo, thịt cá, đồ điện tử công nghệ, thiết bị gia dụng):
1. **Sự tách biệt giữa Phân Loại (Loại/Mẫu mã/Quy cách đóng gói) và Số Lượng Mua (Quantity):**
   - **Phân loại (Variant / SKU):** Xác định thuộc tính riêng biệt (Loại tươi/khô, Màu sắc, Dung lượng RAM/ROM, Trọng lượng bao gạo 5kg/10kg/25kg, Khay thịt 300g/500g/1kg, Layout bàn phím và loại Switch). Mỗi biến thể có mã SKU, đơn giá và tồn kho riêng.
   - **Số lượng (Purchase Quantity):** Luôn luôn là số nguyên (`1, 2, 3, 4...`) biểu thị số lượng bịch, túi, bao, chiếc của phân loại mà người mua lựa chọn.
2. **Nỗi đau của người bán (Seller Pain Point):**
   - Người bán phải nhập hàng trăm mã sản phẩm. Form nhập liệu đơn lẻ hiện tại bắt người bán nhập từng biến thể thủ công rất chậm, tốn thời gian.
   - Thiếu bộ sinh ma trận 2 cấp tự động (Cartesian Product Generator), thiếu thanh áp dụng giá và tồn kho hàng loạt, và thiếu tính năng nhân bản nhanh sản phẩm (Product Duplicate).

---

## 2. Kiến Trúc CSDL & Dữ Liệu (Database Schema)

### 2.1. Cập nhật Bảng `products`
Bổ sung cột:
- `tier_variation` (`TEXT` / `JSON`): Định nghĩa cấu trúc các nhóm phân loại (Tối đa 2 nhóm, chuẩn Shopee).
  ```json
  [
    {
      "name": "Loại",
      "options": ["Tươi", "Khô"]
    },
    {
      "name": "Quy cách đóng gói",
      "options": ["Túi 500g", "Túi 1.0 kg", "Túi 2.0 kg"]
    }
  ]
  ```

### 2.2. Cập nhật Bảng `product_variants`
Bổ sung cột:
- `sku` (`VARCHAR(100)`): Mã quản lý kho hàng (ví dụ: `CACHUA-TUOI-500G`, `IP15-TITAN-256G`).
- `attributes` (`TEXT` / `JSON`): Ánh xạ chi tiết các thuộc tính của biến thể (ví dụ: `{"Loại": "Tươi", "Quy cách đóng gói": "Túi 500g"}`).

---

## 3. Kiến Trúc Backend REST APIs

### 3.1. DTO Mở Rộng
- `CreateProductRequest` & `UpdateProductRequest`:
  - Thêm `String tierVariation` (JSON format).
  - Cập nhật `CreateVariantRequest`: thêm `String sku`, `String attributes`.
- `ProductDetailResponse` & `ProductVariantResponse`:
  - Trả về `tierVariation` và trường `sku`, `attributes` của từng biến thể.

### 3.2. Endpoint Kênh Người Bán Mới
- `POST /api/v1/seller/products/{id}/duplicate`:
  - Kiểm tra quyền sở hữu IDOR của shop đối với `productId`.
  - Sao chép toàn bộ thông tin sản phẩm (tên + "[Bản sao]", danh mục, giá, mô tả, ảnh, `tierVariation`, các biến thể).
  - Tự động sinh `slug` duy nhất và lưu vào CSDL.
  - Trả về `ProductDetailResponse` của sản phẩm vừa nhân bản.

---

## 4. Giao Diện Người Bán: Nhập Hàng Siêu Tốc (`SellerProductsPage.tsx`)

### 4.1. Bộ Mẫu 1-Click Theo Ngành Hàng (Industry Presets)
Người bán bấm 1 nút mẫu ngành hàng, hệ thống tự động thiết lập Nhóm 1 và Nhóm 2:
1. 🥬 **Rau củ / Trái cây**: Nhóm 1: `Tình trạng` (Tươi, Khô) × Nhóm 2: `Quy cách` (Túi 500g, Túi 1kg, Túi 2kg, Thùng 5kg).
2. 🥩 **Thịt tươi / Thủy hải sản**: Nhóm 1: `Phân loại thịt` (Ba chỉ, Sườn non, Nạc dăm) × Nhóm 2: `Khối lượng khay` (Khay 300g, Khay 500g, Khay 1kg).
3. 🌾 **Gạo & Nông sản khô**: Nhóm 1: `Loại hạt` (ST25 thượng hạng, Thơm lài) × Nhóm 2: `Đóng gói` (Túi 1kg, Bao 5kg, Bao 10kg, Bao 25kg).
4. 📱 **Điện thoại / Tablet**: Nhóm 1: `Màu sắc` (Titan Tự Nhiên, Đen Midnight, Trắng Starlight) × Nhóm 2: `Cấu hình` (8GB/128GB, 8GB/256GB, 12GB/512GB).
5. ⌨️ **Bàn phím cơ**: Nhóm 1: `Layout` (Layout 75%, TKL 87 phím, Fullsize) × Nhóm 2: `Switch` (Red Switch, Blue Switch, Brown Switch).
6. 🎧 **Tai nghe / Loa**: Nhóm 1: `Màu sắc` (Đen, Trắng, Xanh Navy).
7. 🧃 **Đồ uống / Bia nước ngọt**: Nhóm 1: `Quy cách` (Lon lẻ 330ml, Lốc 6 lon, Thùng 24 lon).

### 4.2. Bộ Sinh Ma Trận 2 Cấp (Cartesian Product Generator)
- Khi nhập tùy chọn ở Nhóm 1 và Nhóm 2, bảng danh sách biến thể được tự động render với tên biến thể ghép: `[Tùy chọn 1] - [Tùy chọn 2]`.
- **Thanh Áp dụng Hàng Loạt (Bulk Apply Bar):**
  - Nhập **Giá chung** và **Tồn kho chung** ➔ Bấm nút **"Áp dụng cho tất cả"**.
  - Tất cả các dòng ma trận được cập nhật đồng thời chỉ trong 1 click.
  - Người bán vẫn có thể tinh chỉnh giá/kho riêng cho từng dòng cụ thể nếu muốn.

### 4.3. Tính Năng Nhân Bản Nhanh (1-Click Quick Duplicate)
- Nút "Nhân bản" trong bảng danh sách sản phẩm của gian hàng.
- Bấm vào sẽ nạp ngay dữ liệu sang form tạo mới để người bán chỉ việc thay đổi tên/ảnh là hoàn tất tạo sản phẩm mới trong 5 giây.

---

## 5. Giao Diện Người Mua (`ProductDetailPage.tsx`)

- Đối với sản phẩm có `tierVariation`:
  - Hiển thị 2 nhóm nút bấm trực quan (Tầng 1: Loại/Màu sắc; Tầng 2: Quy cách/Cấu hình).
  - Khi người mua chọn đủ 2 tầng, hệ thống tự động tìm và kích hoạt SKU/Variant tương ứng:
    - Cập nhật đơn giá tức thì theo SKU đó.
    - Cập nhật tồn kho tối đa.
  - Bộ chọn số lượng đặt mua: Luôn là **Số nguyên** (`1, 2, 3...` gói/bịch/chiếc).
- Khi thêm vào giỏ hàng: Gửi `variantId` của SKU đã chọn kèm số lượng nguyên.
