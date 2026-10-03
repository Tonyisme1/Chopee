# 01. GIỚI THIỆU, BỐI CẢNH & NGUỒN TÀI LIỆU THAM CHIẾU
## Project Introduction, Scope & References

---

### 1. Giới thiệu Dự án
Dự án **Chợ Online (Shopee Clone)** là một nền tảng thương mại điện tử đa người bán (Multi-vendor Marketplace) hoàn chỉnh. Hệ thống được xây dựng nhằm mô phỏng toàn diện mô hình kinh doanh sàn trực tuyến hiện đại, kết hợp đặc thù của một khu chợ truyền thống:
- Mua sắm hàng hóa tiêu dùng, đồ gia dụng, đồ công nghệ, thời trang.
- Đi chợ thực phẩm tươi sống, rau củ quả nông sản theo cân nặng (`kg`, `g`, `bó`), bảo quản lạnh, hạn sử dụng ngắn, giao hàng hỏa tốc trong ngày.
- Mua sắm nước uống giải khát, đóng thùng, lốc, chai lớn có trọng lượng cao.
- Trợ lý thông minh AI Shopping Copilot hỗ trợ lên thực đơn, tư vấn đồ công nghệ, đồ gia dụng và tìm kiếm theo ngân sách.

---

### 2. Mục tiêu Hệ thống
1. **Mô phỏng chân thực mô hình Multi-vendor:**
   - 1 người mua có thể chọn hàng từ nhiều shop khác nhau vào cùng 1 giỏ.
   - Khi thanh toán, hệ thống tự động tách thành nhiều đơn hàng con độc lập cho từng shop xử lý.
2. **Quản lý dữ liệu hàng hóa đa dạng:**
   - Hỗ trợ cả sản phẩm tiêu chuẩn (số lượng nguyên cái) và sản phẩm đặc thù nông sản (số lượng thập phân như `0.5kg`).
   - Cung cấp trường thuộc tính động `attributes` dạng JSON để lưu trữ thông số kỹ thuật (gia dụng/công nghệ) hoặc chứng nhận VietGAP, OCOP, nhiệt độ bảo quản (thực phẩm).
3. **Thanh toán tích hợp & Giao dịch an toàn:**
   - Hỗ trợ 2 phương thức: COD (Tiền mặt khi nhận hàng) và VNPay Sandbox (Cổng quét mã QR ngân hàng có mã hóa chữ ký số HMAC-SHA512).
   - Cơ chế trừ kho an toàn (ACID Transaction) chống bán vượt tồn kho (overselling).
4. **Trợ lý AI hỗ trợ toàn sàn:**
   - Chatbot AI thông minh đọc dữ liệu kho thực tế của sàn để tư vấn và hiển thị trực tiếp thẻ sản phẩm có nút mua hàng ngay trong tin nhắn.

---

### 3. Đối tượng Sử dụng (User Personas)
* **Khách hàng (Buyer):** Người tiêu dùng vào chợ tìm kiếm, duyệt hàng, lọc theo nhu cầu, chat với AI để nhờ tư vấn món ăn/đồ dùng, đặt hàng và đánh giá.
* **Người bán (Seller):** Các hộ kinh doanh, tiểu thương, cửa hàng đăng ký gian hàng, đăng tải sản phẩm, quản lý đơn hàng nhận được và theo dõi doanh thu.
* **Quản trị viên (Admin):** Người điều hành sàn, phê duyệt shop mới, kiểm duyệt sản phẩm vi phạm, quản lý danh mục và xem báo cáo tài chính toàn sàn.

---

### 4. Nguồn Tài liệu & Tiêu chuẩn Tham chiếu (References)
Hệ thống được thiết kế và xây dựng dựa trên các tiêu chuẩn công nghệ và tài liệu hướng dẫn chính thức sau:

1. **Chuẩn tài liệu phần mềm:**
   - IEEE 830-1998 / ISO/IEC/IEEE 29148: Systems and software engineering — Life cycle processes — Requirements engineering.
   - Architectural Decision Records (ADR) & Keep a Changelog Standard (v1.1.0).
2. **Mô hình Trải nghiệm Người dùng (UX/UI):**
   - Shopee Vietnam Web & Mobile Web Platform Patterns (Luồng giỏ hàng, Kênh người bán, Flash sale, Phí ship đa shop).
3. **Công nghệ Backend & Bảo mật:**
   - Spring Boot 3.3 Reference Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/
   - Spring Security 6 Architecture & JWT Stateless Authentication Guide.
   - Hibernate ORM 6.5 User Guide & Concurrency Control (Optimistic/Pessimistic Locking).
4. **Cổng thanh toán & Tích hợp:**
   - VNPay Payment Gateway Developer Integration Guide (VNPay Sandbox, HMAC-SHA512 checksum & IPN Webhook specifications).
5. **Trí tuệ nhân tạo (AI):**
   - Google Gemini 1.5 Flash API Documentation & Structured JSON Output Function Calling.
   - Spring AI Reference Documentation.
