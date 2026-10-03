# 03. ĐẶC TẢ YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)
## Non-Functional Requirements Specification (NFR)

---

### 1. Hiệu năng & Tốc độ Phản hồi (NFR-PERF)
* **NFR-PERF-01 (Thời gian phản hồi API):** 
  * 95% các truy vấn đọc dữ liệu (danh sách sản phẩm, chi tiết mặt hàng, danh mục) phải hoàn thành dưới **200ms**.
  * Các giao dịch ghi (tạo đơn hàng, thanh toán) phải hoàn thành dưới **500ms** (không tính thời gian chờ người dùng tương tác trên cổng VNPay bên ngoài).
* **NFR-PERF-02 (Thời gian tải trang Frontend):** Trang chủ và trang danh mục tải lần đầu (First Contentful Paint) dưới **1.5 giây** trên đường truyền internet tiêu chuẩn.
* **NFR-PERF-03 (Phản hồi Trợ lý AI):** Thời gian phản hồi cuộc hội thoại AI trung bình từ **1.0 - 2.5 giây** (bao gồm thời gian truy vấn dữ liệu sản phẩm trong DB và gọi qua LLM API).

---

### 2. An toàn & Bảo mật Hệ thống (NFR-SEC)
* **NFR-SEC-01 (Mã hóa Mật khẩu):** Mật khẩu người dùng bắt buộc phải được băm bằng thuật toán **BCrypt** với hệ số `strength = 10` trước khi lưu vào cơ sở dữ liệu.
* **NFR-SEC-02 (Xác thực Stateless JWT):**
  * Sử dụng chữ ký HMAC-SHA256 với Secret Key tối thiểu 256 bits.
  * Token có thời gian sống (TTL) xác định (mặc định 24 giờ).
  * Kiểm tra tính hợp lệ và thu hồi quyền hạn ngay lập tức khi phát hiện token hết hạn hoặc bị giả mạo.
* **NFR-SEC-03 (Phân quyền truy cập tài nguyên - RBAC):**
  * Chặn triệt để hiện tượng IDOR (Insecure Direct Object References): Người bán Shop A tuyệt đối không được xem/sửa đơn hàng hoặc sản phẩm của Shop B.
  * Sử dụng `@PreAuthorize` ở tầng Service/Controller để kiểm tra chủ sở hữu dữ liệu.
* **NFR-SEC-04 (Bảo mật Giao dịch Thanh toán):**
  * Mã hóa toàn bộ dữ liệu gọi sang cổng VNPay bằng thuật toán **HMAC-SHA512**.
  * Kiểm tra tính toàn vẹn của chữ ký số (Checksum) khi nhận webhook IPN để chống hành vi giả mạo kết quả thanh toán.
* **NFR-SEC-05 (Chống Injection & XSS):**
  * 100% truy vấn cơ sở dữ liệu sử dụng Spring Data JPA / Hibernate PreparedStatement để ngăn chặn SQL Injection.
  * React tự động escape HTML khi render dữ liệu để chống Cross-Site Scripting (XSS).

---

### 3. Tính Toàn vẹn Dữ liệu & Quản lý Xung đột Đồng thời (NFR-ACID)
* **NFR-ACID-01 (Giao dịch Nguyên tử - Atomicity):** Toàn bộ thao tác tách đơn hàng và trừ tồn kho cho nhiều sản phẩm từ nhiều shop khác nhau phải nằm trong cùng một Transaction (`@Transactional`). Nếu có bất kỳ lỗi nào xảy ra (ví dụ một sản phẩm bị hết hàng giữa chừng), toàn bộ giao dịch phải được Rollback 100%.
* **NFR-ACID-02 (Chống Bán Vượt Tồn Kho - Concurrency Control):**
  * Sử dụng cơ chế khóa có điều kiện ở tầng Database:
    ```sql
    UPDATE products SET stock_quantity = stock_quantity - :qty WHERE id = :id AND stock_quantity >= :qty;
    ```
  * Đảm bảo ngay cả khi 100 người dùng cùng bấm mua 1 sản phẩm cuối cùng tại cùng 1 mili-giây, chỉ duy nhất người đầu tiên mua thành công, 99 người còn lại nhận được thông báo hết hàng chính xác.

---

### 4. Khả năng Mở rộng & Dễ Bảo trì (NFR-MAINT)
* **NFR-MAINT-01 (Kiến trúc phân tầng sạch - Clean Layering):**
  * Tách biệt rành mạch: `Controller` (Tiếp nhận request, validation) ➔ `Service` (Nghiệp vụ, transaction) ➔ `Repository` (Giao tiếp DB) ➔ `Entity / DTO`.
  * Không viết logic nghiệp vụ trực tiếp trong Controller hay Repository.
* **NFR-MAINT-02 (Chuẩn hóa RESTful & Mã lỗi):**
  * Sử dụng đúng HTTP Status Codes: `200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`.
  * Xử lý lỗi tập trung thông qua `@RestControllerAdvice` và bọc trong cấu trúc `ApiResponse<T>` thống nhất.
* **NFR-MAINT-03 (Tài liệu hóa API tự động):** Cung cấp giao diện tương tác Swagger UI (`springdoc-openapi`) phục vụ việc kiểm thử nhanh API và trích xuất tài liệu phục vụ báo cáo đồ án.
