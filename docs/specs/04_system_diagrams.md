# 04. TỔNG HỢP CÁC SƠ ĐỒ HỆ THỐNG (SYSTEM DIAGRAMS)
## Architecture, Use Case, Sequence & State Diagrams

---

### 1. Sơ đồ Kiến trúc Tổng thể Hệ thống (System Architecture)
```mermaid
graph TD
    subgraph ClientLayer["TẦNG CLIENT (GIAO DIỆN NGƯỜI DÙNG)"]
        WebBuyer["Trang Mua Sắm Khách Hàng (React)"]
        WebSeller["Kênh Quản Lý Người Bán (/seller)"]
        WebAdmin["Kênh Quản Trị Sàn (/admin)"]
        AIChat["Widget Trợ Lý AI Mua Sắm"]
    end

    subgraph GatewaySecurity["TẦNG BẢO MẬT & ĐIỀU PHỐI (SPRING SECURITY 6)"]
        JwtFilter["JWT Authentication Filter"]
        CorsConfig["CORS & CSRF Handler"]
        RoleInterceptor["Role-Based Access Control (@PreAuthorize)"]
    end

    subgraph ServiceLayer["TẦNG NGHIỆP VỤ (MODULAR MONOLITH SERVICES)"]
        AuthService["Auth & User Service"]
        CatalogService["Product & Category Service"]
        CartService["Cart Service"]
        OrderService["Multi-Vendor Order Service"]
        PaymentService["VNPay & COD Payment Service"]
        AIService["AI Shopping Copilot Service"]
        AdminService["Admin Analytics & Approval Service"]
    end

    subgraph PersistenceLayer["TẦNG DỮ LIỆU & TÍCH HỢP NGOÀI"]
        MySQL[("MySQL 8 Database\n(InnoDB, utf8mb4)")]
        VNPayGateway["Cổng Thanh Toán VNPay Sandbox\n(HMAC-SHA512)"]
        LLMProvider["Google Gemini 1.5 / OpenAI API"]
    end

    ClientLayer -->|HTTP REST + Bearer Token| GatewaySecurity
    GatewaySecurity --> ServiceLayer
    ServiceLayer --> MySQL
    PaymentService -->|Tạo URL & Checksum SHA512| VNPayGateway
    VNPayGateway -->|Webhook IPN| PaymentService
    AIService -->|Context Enrichment + Function Calling| LLMProvider
```

---

### 2. Sơ đồ Use Case Tổng thể (Use Case Diagram)
```mermaid
graph LR
    subgraph Actors["Tác Nhân"]
        Buyer["Khách Hàng (Buyer)"]
        Seller["Người Bán (Seller)"]
        Admin["Quản Trị Viên (Admin)"]
    end

    subgraph BuyerUseCases["Chức Năng Khách Hàng"]
        UC_B1["Duyệt / Tìm kiếm sản phẩm theo ngành hàng"]
        UC_B2["Hỏi đáp Trợ lý AI (Gợi ý món, đồ công nghệ, gia dụng)"]
        UC_B3["Quản lý Giỏ hàng Đa Shop"]
        UC_B4["Đặt hàng & Tách đơn theo Shop"]
        UC_B5["Thanh toán COD / VNPay Sandbox"]
        UC_B6["Theo dõi & Đánh giá đơn hàng"]
    end

    subgraph SellerUseCases["Chức Năng Người Bán"]
        UC_S1["Đăng ký mở Gian hàng"]
        UC_S2["Đăng bán / Cập nhật sản phẩm (Thực phẩm/Gia dụng)"]
        UC_S3["Xác nhận & Cập nhật đơn hàng của Shop"]
        UC_S4["Xem báo cáo doanh thu của Shop"]
    end

    subgraph AdminUseCases["Chức Năng Quản Trị"]
        UC_A1["Phê duyệt / Tạm khóa Gian hàng"]
        UC_A2["Quản lý Danh mục Ngành hàng"]
        UC_A3["Khóa / Mở tài khoản người dùng"]
        UC_A4["Xem báo cáo tổng quan toàn sàn"]
    end

    Buyer --> UC_B1
    Buyer --> UC_B2
    Buyer --> UC_B3
    Buyer --> UC_B4
    Buyer --> UC_B5
    Buyer --> UC_B6

    Seller --> UC_S1
    Seller --> UC_S2
    Seller --> UC_S3
    Seller --> UC_S4

    Admin --> UC_A1
    Admin --> UC_A2
    Admin --> UC_A3
    Admin --> UC_A4
```

---

### 3. Sơ đồ Tuần tự: Tách Đơn Hàng Đa Shop (Multi-Vendor Order Splitting)
```mermaid
sequenceDiagram
    autonumber
    actor Khach as Khách Hàng
    participant FE as React Frontend
    participant OrderCtrl as OrderController
    participant OrderSvc as OrderService
    participant ProdRepo as ProductRepository
    participant OrderRepo as OrderRepository
    participant DB as MySQL Database

    Khach->>FE: Bấm "Tiến hành Đặt hàng" từ Giỏ
    FE->>OrderCtrl: POST /api/v1/buyer/orders (Danh sách CartItem, Địa chỉ, PTTT)
    OrderCtrl->>OrderSvc: createOrders(user, checkoutDTO)
    
    rect rgb(240, 248, 255)
        Note over OrderSvc, DB: Bắt đầu Transaction (@Transactional)
        OrderSvc->>OrderSvc: Nhóm CartItem theo shop_id
        OrderSvc->>OrderSvc: Sinh group_order_code (vd: GRP-20261003-8821)
        
        loop Với từng Shop
            OrderSvc->>ProdRepo: Kiểm tra & Trừ tồn kho nguyên tử (stock_quantity >= quantity)
            ProdRepo-->>OrderSvc: Cập nhật thành công (nếu thiếu hàng ném lỗi Rollback)
            OrderSvc->>OrderSvc: Tính phí ship shop + Áp dụng voucher
            OrderSvc->>OrderRepo: Lưu bản ghi đơn hàng con `orders`
            OrderSvc->>OrderRepo: Lưu các chi tiết món `order_items`
        end
        OrderSvc->>DB: Xóa các món đã mua khỏi `cart_items`
        Note over OrderSvc, DB: Commit Transaction thành công
    end

    OrderSvc-->>OrderCtrl: Trả về thông tin Đơn hàng & Group Order Code
    OrderCtrl-->>FE: 201 Created (Kèm payment_url nếu chọn VNPay)
    FE-->>Khach: Hiển thị màn hình thành công hoặc chuyển sang VNPay
```

---

### 4. Sơ đồ Tuần tự: Thanh toán VNPay Sandbox & Webhook IPN
```mermaid
sequenceDiagram
    autonumber
    actor Khach as Khách Hàng
    participant FE as React Frontend
    participant PayCtrl as PaymentController
    participant VNPaySvc as VNPayService
    participant VNPayGW as Cổng VNPay Sandbox
    participant DB as MySQL Database

    Khach->>FE: Chọn phương thức VNPay & Xác nhận
    FE->>PayCtrl: POST /api/v1/payment/vnpay/create-payment (groupOrderCode)
    PayCtrl->>VNPaySvc: generatePaymentUrl(groupOrderCode, totalAmount)
    VNPaySvc->>VNPaySvc: Ký HMAC-SHA512 với HashSecret
    VNPaySvc-->>PayCtrl: Trả về URL cổng VNPay
    PayCtrl-->>FE: Trả về redirect_url
    FE->>VNPayGW: Chuyển hướng trình duyệt sang cổng thanh toán VNPay
    
    Khach->>VNPayGW: Quét mã QR hoặc nhập thẻ ATM test
    VNPayGW->>VNPayGW: Xử lý giao dịch thành công (Mã 00)
    
    par Kênh Webhook ngầm (IPN)
        VNPayGW->>PayCtrl: GET /api/v1/payment/vnpay/ipn (Kèm mã phản hồi & Checksum SHA512)
        PayCtrl->>VNPaySvc: Xác thực chữ ký số HMAC-SHA512
        VNPaySvc->>DB: Cập nhật payment_status = 'PAID' cho tất cả đơn thuộc groupOrderCode
        PayCtrl-->>VNPayGW: Trả về RspCode: 00 (Confirm Success)
    and Kênh Trả về trình duyệt (Return URL)
        VNPayGW->>FE: Chuyển hướng về /checkout/success?groupCode=...
        FE-->>Khach: Thông báo: "Thanh toán thành công! Đơn hàng đang được gửi đến các shop."
    end
```

---

### 5. Sơ đồ Tuần tự: Trợ lý AI Mua sắm (AI Shopping Copilot)
```mermaid
sequenceDiagram
    autonumber
    actor Khach as Khách Hàng
    participant Widget as Floating AI Chat Widget
    participant AICtrl as AIController
    participant AISvc as AIService
    participant ProdRepo as ProductRepository
    participant LLM as Google Gemini / OpenAI API

    Khach->>Widget: Nhập câu hỏi (VD: "Gợi ý nguyên liệu nấu canh chua cho 4 người")
    Widget->>AICtrl: POST /api/v1/ai/chat (Message, Context)
    AICtrl->>AISvc: processShoppingQuery(message)
    
    AISvc->>ProdRepo: Tìm kiếm sản phẩm liên quan (Theo từ khóa, danh mục, tồn kho > 0)
    ProdRepo-->>AISvc: Trả về 5-8 sản phẩm khớp nhất kèm Giá, Ảnh, Tồn kho
    
    AISvc->>LLM: Gửi Prompt chuyên gia đi chợ + Danh sách sản phẩm thực tế từ Database
    LLM-->>AISvc: Trả về phản hồi tự nhiên + JSON mảng ID sản phẩm được đề xuất
    
    AISvc-->>AICtrl: Đóng gói Response (Text tư vấn + Thông tin thẻ sản phẩm)
    AICtrl-->>Widget: Trả về JSON cho Client
    Widget-->>Khach: Hiển thị câu trả lời + Các Thẻ Sản Phẩm có nút "Thêm vào giỏ"
```

---

### 6. Sơ đồ Vòng đời Trạng thái Đơn hàng (Order State Machine)
```mermaid
stateDiagram-v2
    [*] --> PENDING: Khách đặt hàng thành công

    PENDING --> CONFIRMED: Shop xác nhận chuẩn bị hàng
    PENDING --> CANCELLED: Khách hủy đơn (Hoàn lại tồn kho)
    PENDING --> CANCELLED: Shop từ chối / Hết hàng (Hoàn lại tồn kho)

    CONFIRMED --> SHIPPING: Shop bàn giao đơn vị vận chuyển
    CONFIRMED --> CANCELLED: Trường hợp đặc biệt (Shop báo sự cố)

    SHIPPING --> DELIVERED: Giao hàng thành công đến tay khách
    SHIPPING --> CANCELLED: Giao thất bại / Khách từ chối nhận (Hoàn tồn kho)

    DELIVERED --> [*]: Đơn hàng hoàn tất (Khách có thể đánh giá 1-5 sao)
    CANCELLED --> [*]: Kết thúc chu trình đơn hủy
```
