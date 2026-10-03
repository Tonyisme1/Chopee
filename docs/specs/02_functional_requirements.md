# 02. ĐẶC TẢ YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)
## Functional Requirements Specification (FR)

---

### 1. Phân hệ Xác thực & Người dùng (FR-AUTH)
* **FR-AUTH-01 (Đăng ký tài khoản):** Cho phép người dùng đăng ký tài khoản với username, email, mật khẩu và họ tên. Mật khẩu được mã hóa bằng BCrypt. Vai trò mặc định: `ROLE_BUYER`.
* **FR-AUTH-02 (Đăng nhập & JWT):** Xác thực thông tin đăng nhập, sinh Bearer JWT Token có thời hạn sử dụng. Trả về thông tin hồ sơ và danh sách quyền hạn.
* **FR-AUTH-03 (Quản lý hồ sơ cá nhân):** Xem và cập nhật họ tên, số điện thoại, avatar, đổi mật khẩu.
* **FR-AUTH-04 (Sổ địa chỉ nhận hàng):** Người mua có thể lưu nhiều địa chỉ (Tỉnh/Thành phố, Quận/Huyện, Phường/Xã, Số nhà) và đặt 1 địa chỉ mặc định.
* **FR-AUTH-05 (Đăng ký mở Shop):** Khách hàng có tài khoản `ROLE_BUYER` có thể nộp đơn đăng ký trở thành Người Bán (nhập tên shop, số điện thoại, địa chỉ, loại hình kinh doanh).

---

### 2. Phân hệ Danh mục & Sản phẩm Chợ Đa Năng (FR-PROD)
* **FR-PROD-01 (Cây danh mục đa cấp):** Hiển thị danh mục ngành hàng theo dạng cha - con (Thực phẩm tươi sống ➔ Rau củ quả / Thịt cá; Đồ uống ➔ Bia / Nước ngọt; Đồ gia dụng ➔ Thiết bị nhà bếp...).
* **FR-PROD-02 (Tìm kiếm & Bộ lọc linh hoạt):**
  * Tìm kiếm theo từ khóa tên sản phẩm hoặc tên shop.
  * Lọc theo khoảng giá (`minPrice` - `maxPrice`).
  * Lọc theo điều kiện bảo quản thực phẩm: Hàng khô (`NORMAL`), Tươi sống (`FRESH`), Đông lạnh/Mát (`FROZEN_CHILLED`).
  * Lọc theo đơn vị tính (`kg`, `bó`, `lon`, `thùng`, `chiếc`).
  * Sắp xếp: Mới nhất, Bán chạy nhất, Giá tăng/giảm dần, Đánh giá cao nhất.
* **FR-PROD-03 (Trang chi tiết sản phẩm):**
  * Hiển thị thư viện ảnh sản phẩm, giá bán, giá gốc, tỷ lệ giảm giá %, tồn kho.
  * Hiển thị rõ các thuộc tính chuyên biệt cho Thực phẩm (Hạn sử dụng, Nơi xuất xứ, Đơn vị tính, Bước nhảy số lượng khi mua).
  * Hiển thị bảng thông số kỹ thuật động (Dynamic Attributes JSON): Công suất, bảo hành (gia dụng/công nghệ) hoặc chứng nhận VietGAP, OCOP (nông sản).
  * Hiển thị thông tin gian hàng và các đánh giá sao của khách đã mua.

---

### 3. Phân hệ Giỏ hàng & Đặt hàng Đa Shop (FR-ORDER)
* **FR-ORDER-01 (Thêm vào giỏ hàng):** 
  * Hỗ trợ số lượng lẻ đối với thực phẩm bán theo cân (vd: `0.5kg` cà chua, bước nhảy `0.5kg`).
  * Hỗ trợ số lượng nguyên cái/thùng đối với đồ gia dụng và đồ uống.
* **FR-ORDER-02 (Hiển thị giỏ hàng gom nhóm):** Giao diện tự động phân nhóm các mặt hàng theo từng gian hàng (`shop_id`), tương tự giao diện giỏ hàng của Shopee.
* **FR-ORDER-03 (Xem trước đơn hàng - Checkout Preview):**
  * Tự động tính phí vận chuyển riêng cho từng shop dựa trên phương thức giao hàng (`STANDARD` hay `EXPRESS_FRESH`).
  * Kiểm tra và áp dụng mã giảm giá (Voucher toàn sàn hoặc Voucher của từng shop).
* **FR-ORDER-04 (Tạo đơn hàng & Tách đơn tự động):**
  * Gom tất cả các mặt hàng được chọn và tạo thành một mã giao dịch chung (`group_order_code`).
  * Tự động phân tách thành các đơn hàng con (`orders`) độc lập gửi về từng shop.
  * Trừ tồn kho nguyên tử (`stock_quantity`) với bảo vệ giao dịch chống âm kho.
* **FR-ORDER-05 (Theo dõi trạng thái đơn hàng):** Người mua có thể theo dõi tiến độ từng đơn con: `PENDING` (Chờ duyệt) ➔ `CONFIRMED` (Chuẩn bị hàng) ➔ `SHIPPING` (Đang giao) ➔ `DELIVERED` (Đã giao) ➔ `CANCELLED` (Đã hủy).
* **FR-ORDER-06 (Hủy đơn hàng):** Người mua được phép hủy đơn khi đơn còn ở trạng thái `PENDING`. Hệ thống tự động hoàn lại tồn kho cho sản phẩm.

---

### 4. Phân hệ Thanh toán & Khuyến mãi (FR-PAY)
* **FR-PAY-01 (Thanh toán COD):** Thanh toán tiền mặt khi nhận hàng. Đơn hàng chuyển sang trạng thái chờ shop xác nhận, `payment_status = 'UNPAID'`.
* **FR-PAY-02 (Thanh toán VNPay Sandbox QR):**
  * Tạo đường link thanh toán có chữ ký HMAC-SHA512 chuyển hướng sang cổng VNPay.
  * Khách quét mã QR bằng ứng dụng ngân hàng test hoặc nhập thông tin thẻ ATM nội địa test.
  * Webhook IPN tự động xác thực chữ ký và cập nhật `payment_status = 'PAID'` cho toàn bộ các đơn hàng thuộc mã giao dịch chung.
* **FR-PAY-03 (Hệ thống Voucher & Mã giảm giá):**
  * Quản lý mã voucher giảm theo phần trăm hoặc số tiền cố định.
  * Giới hạn giá trị đơn hàng tối thiểu và số lượt sử dụng tối đa.
  * Phân biệt voucher toàn sàn và voucher phát hành bởi từng shop.

---

### 5. Phân hệ Kênh Người Bán (FR-SELLER)
* **FR-SELLER-01 (Bảng điều khiển - Dashboard):** Thống kê nhanh doanh thu theo ngày/tháng, số lượng đơn hàng mới, số lượng sản phẩm sắp hết hàng.
* **FR-SELLER-02 (Quản lý sản phẩm của Shop):**
  * Thêm sản phẩm mới: chọn danh mục, tải ảnh, nhập giá, tồn kho, đơn vị tính, điều kiện bảo quản, xuất xứ và thuộc tính động JSON.
  * Sửa/Xóa sản phẩm hoặc tạm ẩn khỏi sàn (`INACTIVE`).
* **FR-SELLER-03 (Xử lý đơn hàng):** Xem danh sách các đơn hàng khách đặt tại shop mình, xác nhận đơn (`CONFIRMED`), chuyển giao shipper (`SHIPPING`).
* **FR-SELLER-04 (Hồ sơ shop):** Cập nhật logo, ảnh bìa, mô tả giới thiệu shop.

---

### 6. Phân hệ Quản trị Sàn (FR-ADMIN)
* **FR-ADMIN-01 (Thống kê toàn sàn):** Biểu đồ doanh số toàn bộ chợ, tổng số đơn hàng phát sinh, số lượng shop đang hoạt động.
* **FR-ADMIN-02 (Phê duyệt Gian hàng):** Xét duyệt các yêu cầu đăng ký mở shop mới (`APPROVED` hoặc `REJECTED`), tạm khóa các shop có hành vi gian lận (`LOCKED`).
* **FR-ADMIN-03 (Quản lý Danh mục):** Thêm mới, đổi tên, phân cấp danh mục cha - con.
* **FR-ADMIN-04 (Quản lý Người dùng):** Tra cứu thông tin người dùng, khóa/mở khóa tài khoản (`ACTIVE` / `BLOCKED`).

---

### 7. Phân hệ Trợ lý AI Mua sắm (FR-AI)
* **FR-AI-01 (Giao diện Chatbot nổi - Floating Widget):** Nút tròn icon AI nằm ở góc phải màn hình, bấm vào để mở hộp thoại trò chuyện mọi lúc.
* **FR-AI-02 (Tư vấn Thực đơn & Đi chợ):** Khách gõ tên món ăn hoặc số người ăn ➔ AI hướng dẫn công thức vắn tắt và tìm kiếm các nguyên liệu tươi sống trên chợ để hiển thị dạng thẻ.
* **FR-AI-03 (Tư vấn Đồ Công nghệ & Gia dụng):** Giải đáp thắc mắc về thông số kỹ thuật (công suất, kích thước, độ tương thích sạc cáp) và đề xuất sản phẩm tốt nhất trên sàn.
* **FR-AI-04 (Tư vấn Ngân sách & Săn Deal):** Gợi ý giỏ hàng tối ưu trong tầm giá của khách hàng.
* **FR-AI-05 (Tương tác Thẻ Sản phẩm trực tiếp):** Mỗi sản phẩm được gợi ý trong chat hiển thị đầy đủ ảnh, tên, giá bán và nút **"Thêm vào giỏ hàng"** để khách có thể mua ngay lập tức mà không cần thoát khỏi khung chat.
