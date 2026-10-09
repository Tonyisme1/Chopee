# 🛒 Chopee - Sàn Thương Mại Điện Tử Đa Ngành Hàng & Chợ Trực Tuyến

<p align="center">
  <b>Nền tảng Chợ Trực Tuyến Đa Người Bán (Multi-Vendor Marketplace) kết hợp Thực phẩm Tươi sống & Trợ lý AI Mua sắm Toàn năng.</b>
</p>

---

## 🌟 Điểm nổi bật của Dự án

1. **Mô hình Chợ Đa Người Bán (Multi-Vendor Marketplace):**
   - Khách hàng gom sản phẩm từ nhiều shop khác nhau vào 1 giỏ hàng chung.
   - Khi thanh toán, hệ thống tự động tách thành các đơn hàng độc lập theo từng Shop (`group_order_code`), bảo vệ toàn vẹn IDOR: người bán này không thể can thiệp hoặc xem đơn hàng của người bán khác.
2. **Hỗ trợ Nông sản, Thực phẩm Tươi Sống & Bán Lẻ Linh Hoạt:**
   - Đơn vị tính đa dạng: theo cân (`kg`, `g`, bước nhảy `0.5kg`), bó, vỉ, thùng, lốc, chiếc.
   - Phân loại điều kiện lưu kho: `NORMAL` (thường), `FRESH` (tươi mát), `FROZEN_CHILLED` (đông lạnh).
   - Tùy chọn phương thức vận chuyển hỏa tốc `EXPRESS_FRESH` (giao 2 giờ bảo quản lạnh).
3. **Trợ lý AI Mua sắm Toàn năng (Shopping Copilot):**
   - Tích hợp Google Gemini 1.5 Flash API với prompt nghiệp vụ sâu về ẩm thực và công nghệ.
   - Trả lời tự nhiên, gợi ý thực đơn món ăn (canh chua, lẩu, mâm cơm gia đình), tư vấn cấu hình sạc laptop, đồng thời nhúng Thẻ Sản phẩm trực quan có nút mua ngay 1-click vào giỏ hàng.
4. **Hệ thống Khuyến mãi & Vouchers Đa Cấp:**
   - Voucher toàn sàn do Admin tài trợ (`CHOPEE10K`, `FREESHIPCHO`).
   - Voucher do từng shop tự thiết lập (`DALATFARM20`, `TECHSALE50`).
   - Kiểm tra điều kiện áp dụng nghiêm ngặt: chi tiêu tối thiểu, trần giảm giá tối đa, thời hạn hiệu lực, giới hạn lượt dùng.
5. **Cổng Thanh toán Trực tuyến & Đánh giá Đơn hàng:**
   - Hỗ trợ thanh toán khi nhận hàng (COD) và Cổng thanh toán trực tuyến **VNPay Sandbox** (mã hóa chuẩn HMAC-SHA512).
   - Đánh giá sản phẩm kèm ảnh thực tế dành riêng cho khách hàng đã mua và nhận hàng thành công (`DELIVERED`), tự động cập nhật điểm sao trung bình của sản phẩm và gian hàng; hỗ trợ người bán phản hồi khách hàng.
6. **Kiến trúc CSDL Doanh nghiệp 3 Vùng (Hot / Warm / Cold):**
   - 24 bảng dữ liệu chuẩn hóa InnoDB ACID, Trigger tự động đồng bộ tồn kho và lịch sử trạng thái đơn hàng.
   - View thống kê báo cáo doanh thu (`v_shop_revenue_summary`, `v_order_fulfillment_sla`).
   - Stored Procedure & MySQL Event Scheduler tự động đóng băng đơn hàng cũ sang Cold Storage.

---

## 🛠️ Tech Stack & Kiến trúc Hệ thống

- **Backend:** Java 17, Spring Boot 3.3.x, Spring Data JPA, Spring Security 6 (Stateless JWT), JJWT 0.12.5, Springdoc OpenAPI 2.6.0 (Swagger 3), Lombok, MySQL Connector.
- **Frontend:** React 18, Vite 5.4, TypeScript, Tailwind CSS, Lucide Icons, Zustand (State Management with LocalStorage persistence), Axios with JWT interceptors.
- **Database:** MySQL 8.0+ (ACID InnoDB, UTF8MB4).
- **Trí tuệ nhân tạo (AI):** Google Gemini 1.5 Flash API qua `/api/v1/ai/chat`.
- **Cổng thanh toán:** VNPay Payment Gateway Sandbox.

---

## 👥 Tài khoản Dùng thử (Demo Credentials)

Hệ thống đã nạp sẵn dữ liệu mẫu (`DataInitializer` & SQL Seed scripts). Mật khẩu mặc định cho toàn bộ tài khoản thử nghiệm là: **`123456`**

| Vai trò (Role) | Tên đăng nhập (Username) | Email | Gian hàng sở hữu / Quyền hạn |
|:---|:---|:---|:---|
| **Người Mua (Buyer)** | `buyer1` | `buyer1@chopee.vn` | Khách mua hàng, giỏ hàng, sổ địa chỉ, đánh giá sản phẩm |
| **Người Bán (Food Seller)** | `seller_food` | `seller.food@chopee.vn` | Gian hàng **Đà Lạt Farm - Nông Sản Tươi** (Rau củ quả tươi sống) |
| **Người Bán (Tech Seller)** | `seller_tech` | `seller.tech@chopee.vn` | Gian hàng **TechZone Store** (Phụ kiện & Đồ gia dụng công nghệ) |
| **Quản Trị Viên (Admin)** | `admin` | `admin@chopee.vn` | Kiểm duyệt gian hàng, giám sát GMV toàn sàn, tạo Voucher sàn |

---

## ⚡ Hướng dẫn Cài đặt & Khởi chạy (Quickstart)

### 1. Yêu cầu Môi trường
- Java Development Kit (JDK) 17 trở lên
- Apache Maven 3.8+
- Node.js 18+ & npm
- MySQL Server 8.0+ (hoặc WampServer / XAMPP đang chạy cổng `3306`)

### 2. Thiết lập Cơ sở Dữ liệu
1. Mở MySQL Client / phpMyAdmin và tạo database `chopee_db`:
   ```sql
   CREATE DATABASE IF NOT EXISTS chopee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Thực thi file master schema và seed data:
   ```bash
   mysql -u root -p chopee_db < database/00_master_run_all.sql
   ```

### 3. Khởi chạy Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```
- Backend REST API sẽ lắng nghe tại cổng: `http://localhost:8080`
- **Swagger OpenAPI Documentation:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Chạy bộ kiểm thử tự động:
  ```bash
  mvn test
  ```
  *(Kết quả: 60/60 tests PASS trên 13 test suites)*

### 4. Khởi chạy Frontend (React Vite)
Mở cửa sổ terminal mới:
```bash
cd frontend
npm install
npm run dev
```
- Cổng Marketplace sẽ mở tại: `http://localhost:5173`
- Vite tự động cấu hình Reverse Proxy `/api` tới backend `http://localhost:8080`.
- Kiểm tra biên dịch production:
  ```bash
  npm run build
  ```
  *(Kết quả: 0 lỗi TypeScript, 0 lỗi Vite)*

---

## 🌐 Bản đồ Cổng Truy cập (Portals & Routes)

### 1. Kênh Người Mua & Khách Hàng (Client Marketplace)
- `/`: Trang chủ Chopee (Hero banners, ngành hàng, Chợ Thực Phẩm Tươi Sống 2H, Flash Sale, danh mục sản phẩm).
- `/products/:id`: Chi tiết sản phẩm (Bộ chọn số lượng thập phân 0.5kg, ảnh thư viện, thông số kỹ thuật, đánh giá sao).
- `/categories/:id`: Lọc sản phẩm theo khoảng giá, điều kiện bảo quản, rating.
- `/cart`: Giỏ hàng đa shop nhóm trực quan theo gian hàng, điều chỉnh số lượng realtime.
- `/checkout`: Trang đặt hàng, chọn sổ địa chỉ, chọn đơn vị vận chuyển (`STANDARD` vs `EXPRESS_FRESH`), áp dụng voucher shop & sàn, chọn COD / VNPay.
- `/orders/success`: Màn hình hoàn tất thông báo tách đơn theo từng shop (`groupOrderCode`).
- `/orders/my`: Quản lý đơn mua cá nhân theo trạng thái (`PENDING`, `SHIPPING`, `DELIVERED`, `CANCELLED`), hủy đơn, gửi đánh giá 5 sao.
- **AI Shopping Copilot:** Nút robot nổi góc phải màn hình mọi trang, mở hộp chat AI thông minh tư vấn món ăn và mua đồ 1-click.

### 2. Kênh Người Bán (Seller Center)
- `/seller`: Bảng điều khiển doanh thu, đơn hàng chờ xác nhận, sản phẩm đang bán.
- `/seller/products`: Thêm mới, chỉnh sửa thông số JSON, cập nhật tồn kho, quy định điều kiện bảo quản thực phẩm.
- `/seller/orders`: Quản lý đơn hàng, đóng gói xác nhận (`CONFIRMED`), giao bên vận chuyển (`SHIPPING`).
- `/seller/vouchers`: Thiết lập mã giảm giá riêng của shop (theo % hoặc tiền mặt).
- `/seller/reviews`: Xem đánh giá người mua và gửi phản hồi chăm sóc khách hàng.

### 3. Kênh Quản Trị Sàn (Admin Center)
- `/admin`: Dashboard giám sát GMV toàn sàn, phân bổ người dùng, số lượng đơn hàng.
- `/admin/shops`: Kiểm duyệt hồ sơ đối tác, phê duyệt (`APPROVED`), từ chối (`REJECTED`) hoặc khóa gian hàng vi phạm (`LOCKED`).
- `/admin/vouchers`: Tạo và điều hành các chiến dịch mã giảm giá trợ giá toàn sàn.

---

## 📋 Danh mục 52 REST API Endpoints

Hệ thống cung cấp đầy đủ 52 REST API chuẩn Enterprise được tổ chức theo 12 module nghiệp vụ:
1. **Auth Module (`/api/v1/auth`):** Đăng nhập, đăng ký buyer, đăng ký shop, thông tin user hiện tại.
2. **Catalog & Categories (`/api/v1/public/categories`, `/api/v1/public/products`):** Cây danh mục, tìm kiếm, lọc đa thuộc tính, chi tiết sản phẩm.
3. **Cart Module (`/api/v1/buyer/cart`):** Lấy giỏ hàng, thêm sản phẩm, cập nhật số lượng bước nhảy thập phân, xóa sản phẩm.
4. **Address Book (`/api/v1/buyer/addresses`):** Danh sách địa chỉ, tạo mới, đặt làm mặc định, xóa địa chỉ.
5. **Orders Module (`/api/v1/buyer/orders`):** Đặt hàng đa shop tách tự động, xem danh sách đơn hàng, chi tiết đơn theo mã, hủy đơn.
6. **Vouchers Module (`/api/v1/public/vouchers`, `/api/v1/seller/vouchers`, `/api/v1/admin/vouchers`):** Tra cứu, thẩm định điều kiện voucher, tạo voucher shop, tạo voucher sàn.
7. **Reviews Module (`/api/v1/public/products/{id}/reviews`, `/api/v1/buyer/reviews`, `/api/v1/seller/reviews`):** Đánh giá verified purchase, tính điểm trung bình sản phẩm, người bán phản hồi review.
8. **Seller Management (`/api/v1/seller`):** Dashboard thống kê, CRUD sản phẩm, cập nhật trạng thái đơn hàng.
9. **Admin Platform (`/api/v1/admin`):** Dashboard GMV, duyệt gian hàng, khóa/mở khóa gian hàng.
10. **AI Copilot (`/api/v1/ai/chat`):** Chatbot tư vấn mua sắm ngữ cảnh thực phẩm & công nghệ.
11. **Payment Gateway (`/api/v1/payment/vnpay`):** Tạo URL thanh toán VNPay HMAC-SHA512, tiếp nhận IPN callback.

---

## 📚 Hệ thống Tài liệu Đặc tả (Specifications)
Hệ thống tài liệu được duy trì đồng bộ 100% tại thư mục [`docs/specs/`](./docs/specs/):
1. [00_index.md](./docs/specs/00_index.md) - Mục lục tài liệu đặc tả
2. [01_introduction.md](./docs/specs/01_introduction.md) - Giới thiệu, bối cảnh & tài liệu tham chiếu
3. [02_functional_requirements.md](./docs/specs/02_functional_requirements.md) - Yêu cầu chức năng (FR)
4. [03_non_functional_requirements.md](./docs/specs/03_non_functional_requirements.md) - Yêu cầu phi chức năng (NFR)
5. [04_system_diagrams.md](./docs/specs/04_system_diagrams.md) - Tổng hợp các sơ đồ hệ thống (Use Case, Sequence, State Machine)
6. [05_database_specification.md](./docs/specs/05_database_specification.md) - Đặc tả CSDL & Sơ đồ ERD
7. [06_test_plan.md](./docs/specs/06_test_plan.md) - Kế hoạch kiểm thử & nghiệm thu
8. [07_changelog.md](./docs/specs/07_changelog.md) - Nhật ký thay đổi (Change Keys)

---

<p align="center">
  <b>© 2026 Chopee Marketplace - Developed with Excellence.</b>
</p>
