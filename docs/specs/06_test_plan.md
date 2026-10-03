# 06. KẾ HOẠCH KIỂM THỬ & NGHIỆM THU (TEST PLAN)
## Software Test Plan & Acceptance Scenarios

---

### 1. Mục tiêu Kiểm thử (Test Objectives)
Đảm bảo hệ thống Chợ Online hoạt động chính xác, ổn định, bảo mật và đáp ứng đầy đủ các yêu cầu chức năng lẫn phi chức năng trước khi nghiệm thu báo cáo đồ án / demo sản phẩm.

---

### 2. Ma trận Kiểm thử (Test Matrix)
| Tầng kiểm thử | Công cụ áp dụng | Đối tượng kiểm thử |
| :--- | :--- | :--- |
| **Unit Test** | JUnit 5, Mockito | Logic tính phí ship, tách đơn hàng theo shop, trừ tồn kho, mã hóa HMAC-SHA512 |
| **Integration Test** | Spring Boot Test, MockMvc | Luồng xác thực JWT, REST API Endpoints, Transaction Rollback khi lỗi |
| **Concurrency Test** | JMeter / Script Đa luồng | 50 luồng đồng thời mua cùng 1 sản phẩm còn lại 1 suất duy nhất |
| **End-to-End Test (E2E)** | Kiểm thử giao diện tương tác | Quy trình từ Đăng ký ➔ Chọn hàng đa shop ➔ Chat với AI ➔ Thanh toán VNPay ➔ Seller nhận đơn |

---

### 3. Kịch bản Kiểm thử Chi tiết (Detailed Test Cases)

#### TC-AUTH: Xác thực & Phân quyền
* **TC-AUTH-01 (Đăng ký tài khoản hợp lệ):**
  * *Đầu vào:* `username: "testuser"`, `email: "test@gmail.com"`, `password: "123456"`.
  * *Kỳ vọng:* Trả về HTTP 201 Created, mật khẩu trong database được mã hóa hash BCrypt.
* **TC-AUTH-02 (Đăng nhập sai mật khẩu):**
  * *Đầu vào:* `username: "testuser"`, `password: "sai_mat_khau"`.
  * *Kỳ vọng:* Trả về HTTP 401 Unauthorized, thông báo lỗi *"Tên đăng nhập hoặc mật khẩu không đúng"*.
* **TC-AUTH-03 (Chống truy cập chéo - IDOR giữa 2 Shop):**
  * *Đầu vào:* Chủ Shop A dùng JWT của mình gọi API cập nhật trạng thái đơn hàng của Shop B.
  * *Kỳ vọng:* Trả về HTTP 403 Forbidden, hệ thống từ chối can thiệp đơn của shop khác.

#### TC-CART-ORDER: Giỏ hàng & Tách Đơn Hàng Đa Shop
* **TC-ORDER-01 (Thêm sản phẩm lẻ theo cân nặng):**
  * *Đầu vào:* Thêm `0.5kg` cà chua, sau đó bấm tăng thêm 1 bước nhảy (`+0.5kg`).
  * *Kỳ vọng:* Giỏ hàng cập nhật số lượng là `1.0kg`, giá tiền nhân đúng theo tỷ lệ.
* **TC-ORDER-02 (Tách đơn tự động khi giỏ hàng có nhiều shop):**
  * *Đầu vào:* Giỏ hàng chứa 1 thùng bia (Shop Hùng Phát) và 1kg rau (Shop Đà Lạt). Bấm thanh toán COD.
  * *Kỳ vọng:* 
    * Tạo 1 `group_order_code`.
    * Sinh ra đúng 2 bản ghi `orders` riêng biệt với `shop_id` tương ứng.
    * Mỗi shop chỉ nhìn thấy đơn hàng của shop mình trong trang Quản lý Người bán.
* **TC-ORDER-03 (Trừ kho an toàn & Chống âm kho):**
  * *Đầu vào:* Sản phẩm có tồn kho là `2 chiếc`. Khách đặt mua `3 chiếc`.
  * *Kỳ vọng:* Trả về HTTP 400 Bad Request kèm thông báo lỗi sản phẩm không đủ tồn kho. Cơ sở dữ liệu không bị thay đổi (Rollback).

#### TC-PAY: Thanh toán VNPay Sandbox
* **TC-PAY-01 (Tạo URL thanh toán VNPay chuẩn):**
  * *Đầu vào:* Gọi API tạo thanh toán cho nhóm đơn hàng `groupOrderCode`.
  * *Kỳ vọng:* Trả về URL hợp lệ chứa đầy đủ các tham số `vnp_Amount`, `vnp_TxnRef`, và chữ ký số `vnp_SecureHash` đúng định dạng SHA512.
* **TC-PAY-02 (Xử lý Webhook IPN từ VNPay):**
  * *Đầu vào:* Giả lập VNPay gửi request IPN với `vnp_ResponseCode = '00'` và chữ ký số hợp lệ.
  * *Kỳ vọng:* Trả về `{"RspCode": "00", "Message": "Confirm Success"}`, đồng thời chuyển `payment_status` của toàn bộ đơn con thành `PAID`.

#### TC-AI: Trợ lý AI Mua sắm (AI Copilot)
* **TC-AI-01 (Gợi ý thực đơn đi chợ & Nguyên liệu tươi sống):**
  * *Đầu vào:* Khách nhắn *"Tối nay nhà 4 người ăn lẩu cá, gợi ý nguyên liệu giúp mình"*.
  * *Kỳ vọng:* AI trả lời công thức nấu lẩu ngắn gọn và trích xuất danh sách các mặt hàng (cá, nấm, rau bổi, gia vị lẩu) đang có sẵn trong database kèm giá tiền và ảnh đại diện.
* **TC-AI-02 (Tư vấn Đồ công nghệ & Gia dụng):**
  * *Đầu vào:* Khách nhắn *"Củ sạc 65W GaN có sạc được cho laptop ThinkPad không?"*.
  * *Kỳ vọng:* AI giải thích chuẩn chuẩn Power Delivery và giới thiệu sản phẩm củ sạc 65W đang bán trên sàn.
* **TC-AI-03 (Thêm vào giỏ từ khung chat):**
  * *Đầu vào:* Khách bấm nút "Thêm vào giỏ" ngay trên thẻ sản phẩm trong khung chat AI.
  * *Kỳ vọng:* Giỏ hàng tăng thêm món đó, badge giỏ hàng trên Header nhảy số tức thì.
