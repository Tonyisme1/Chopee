# 📜 QUY CHUẨN & BỘ LUẬT PHÁT TRIỂN DỰ ÁN CHOPEE (PROJECT RULES)
## Project Development Governance, Review Gates & Change Control Guidelines

Tài liệu này quy định toàn bộ các nguyên tắc, tiêu chuẩn kỹ thuật và quy trình kiểm duyệt bắt buộc áp dụng cho mọi lập trình viên (Developer) và trợ lý AI (AI Assistant) khi tham gia phát triển, bảo trì hoặc mở rộng dự án **Chopee**.

---

## 1. NGUYÊN TẮC QUẢN LÝ THAY ĐỔI (CHANGE CONTROL & AUDIT)

### 1.1 Bắt buộc cấp mã Change Key cho mọi thay đổi
* Mọi sự thay đổi liên quan đến **Kiến trúc, Cơ sở dữ liệu (Schema), RESTful API, hoặc Luồng nghiệp vụ cốt lõi** đều bắt buộc phải được lập một bản ghi nhật ký mới trong file:
  📁 [`docs/specs/07_changelog.md`](./docs/specs/07_changelog.md)
* **Quy ước định dạng Change Key:**
  ```text
  CHG-YYYYMMDD-XXX
  ```
  *(Ví dụ: `CHG-20261003-001`, `CHG-20261003-002`)*
* **Cấu trúc bản ghi thay đổi bắt buộc gồm:**
  1. `Change Key`: Mã định danh duy nhất.
  2. `Date`: Ngày giờ thực hiện (`YYYY-MM-DD`).
  3. `Author`: Người/AI đề xuất hoặc thực hiện.
  4. `Type`: `ADDED` (Thêm mới) | `CHANGED` (Sửa đổi) | `FIXED` (Vá lỗi) | `REFACTORED` (Tái cấu trúc).
  5. `Component`: `ARCHITECTURE` | `DATABASE` | `BACKEND_API` | `FRONTEND_UI` | `AI_SERVICE` | `DOCS`.
  6. `Description`: Mô tả ngắn gọn sự thay đổi.
  7. `Rationale`: Lý do tại sao phải thay đổi? Giải quyết vấn đề gì?
  8. `Before vs After`: So sánh chi tiết trạng thái trước và sau khi đổi.
  9. `Impact`: Danh sách file/module bị ảnh hưởng.

### 1.2 Đồng bộ tài liệu song song với Code (Doc-Code Synchronization)
* **Luật bất di bất dịch:** Tuyệt đối không sửa code làm lệch thiết kế mà không cập nhật tài liệu đặc tả tương ứng trong thư mục [`docs/specs/`](./docs/specs/):
  * Đổi bảng/cột CSDL ➔ Phải cập nhật `05_database_specification.md`.
  * Đổi luồng/endpoint API ➔ Phải cập nhật `02_functional_requirements.md` và `04_system_diagrams.md`.
  * Thêm kịch bản test ➔ Phải cập nhật `06_test_plan.md`.

---

## 2. QUY TRÌNH KIỂM DUYỆT & NGHIỆM THU (REVIEW GATES)

### 2.1 Cổng kiểm duyệt bắt buộc trước khi Commit (Pre-Commit Hard Gate)
Trước khi thực hiện `git commit`, bắt buộc phải thỏa mãn 3 điều kiện:
1. **Kiểm tra biên dịch & Test Backend:**
   ```bash
   cd backend && mvn clean test
   ```
   *(Tất cả unit & integration tests phải PASS, 0 failure, 0 error)*.
2. **Kiểm tra biên dịch Frontend:**
   ```bash
   cd frontend && npm run build
   ```
   *(Bản build TypeScript và Vite phải thành công, không có lỗi kiểu dữ liệu type error)*.
3. **Bằng chứng thực nghiệm (Evidence Before Assertion):**
   * Không bao giờ được phép khẳng định "tính năng đã hoạt động" khi chưa chạy lệnh kiểm thử và kiểm tra output thực tế trên terminal.

### 2.2 Quy chuẩn bảo mật & Chống truy cập chéo (Security Review Checklist)
* **Kiểm tra IDOR (Insecure Direct Object References):** Mọi API liên quan đến Seller (`/api/v1/seller/*`) bắt buộc phải kiểm tra quyền sở hữu `shopId == principal.shopId`. Seller của Shop A tuyệt đối không được xem/sửa đơn hàng hoặc sản phẩm của Shop B.
* **Kiểm tra Concurrency (Chống âm kho):** Thao tác trừ tồn kho phải dùng câu lệnh điều kiện `WHERE stock_quantity >= :qty` trong giao dịch `@Transactional`.
* **Kiểm tra Checksum thanh toán:** API Webhook nhận thanh toán VNPay bắt buộc phải xác thực mã băm HMAC-SHA512 trước khi cập nhật trạng thái đơn thành `PAID`.

---

## 3. QUY CHUẨN LẬP TRÌNH & ĐẶT TÊN (CODING CONVENTIONS)

### 3.1 Backend (Spring Boot 3 + Java 17)
* **Mô hình phân tầng (Clean Layering):**
  * `Controller`: Chỉ tiếp nhận HTTP request, validate DTO đầu vào bằng `@Valid`, bọc phản hồi trong `ApiResponse<T>`. Tuyệt đối không viết logic tính toán hay gọi trực tiếp Repository tại Controller.
  * `Service`: Xử lý nghiệp vụ, quản lý ranh giới giao dịch `@Transactional`.
  * `Repository`: Kế thừa `JpaRepository`, viết truy vấn tối ưu.
  * `Entity / DTO`: Phân tách rành mạch Entity CSDL và Request/Response DTO.
* **Quy chuẩn đặt tên URI RESTful:**
  * Sử dụng danh từ số nhiều, chữ thường, cách nhau bằng dấu gạch ngang (kebab-case):
    * Đúng: `GET /api/v1/products`, `POST /api/v1/cart/items`
    * Sai: `GET /api/v1/getProductList`, `POST /api/v1/add_to_cart`

### 3.2 Frontend (React + TypeScript + Tailwind CSS)
* **Cấu trúc Component:**
  * Tên file Component dùng PascalCase: `ProductCard.tsx`, `AIChatWidget.tsx`.
  * Tên file hook dùng camelCase bắt đầu bằng `use`: `useAuthStore.ts`, `useCartStore.ts`.
* **Quản trị State:**
  * Dùng Zustand cho toàn cục (Auth, Cart). Không lạm dụng prop-drilling quá 2 cấp.
  * Dùng Axios Interceptor để tự động gắn Bearer Token và điều hướng khi token hết hạn.

---

## 4. QUY CHUẨN COMMIT MESSAGE (CONVENTIONAL COMMITS)
Mọi commit trên Git phải tuân theo cấu trúc:
```text
<type>(<scope>): <mô tả ngắn gọn bằng tiếng Anh hoặc tiếng Việt>
```
* `feat`: Thêm tính năng mới (vd: `feat(order): implement multi-vendor order splitting`)
* `fix`: Sửa lỗi (vd: `fix(cart): prevent negative quantity for fresh food`)
* `docs`: Cập nhật tài liệu, đặc tả hoặc nhật ký (vd: `docs(specs): add CHG-20261003-006 project rules`)
* `test`: Thêm hoặc cập nhật test (vd: `test(payment): add VNPay HMAC-SHA512 checksum test`)
* `refactor`: Tái cấu trúc code mà không đổi hành vi (vd: `refactor(auth): extract JwtTokenProvider`)
* `chore`: Cấu hình, cài đặt thư viện (vd: `chore: setup Tailwind CSS and Vite config`)
