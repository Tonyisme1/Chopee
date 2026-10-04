# 07. NHẬT KÝ THAY ĐỔI HỆ THỐNG & QUYẾT ĐỊNH THIẾT KẾ (SYSTEM CHANGELOG & ADR)
## Architecture Decision Records & Change Tracking Log

Tài liệu này theo dõi toàn bộ lịch sử các quyết định kiến trúc và thay đổi thiết kế hệ thống. Mỗi bản ghi thay đổi bắt buộc phải có một **Change Key độc nhất** theo định dạng `CHG-YYYYMMDD-XXX` để phục vụ việc tra cứu, kiểm soát phiên bản và đánh giá tác động.

---

### Quy Chuẩn Cấu Trúc Bản Ghi Nhật Ký (Change Record Standard)
Mỗi mục thay đổi bao gồm các trường bắt buộc sau:
* **Change Key:** Mã định danh duy nhất (VD: `CHG-20261003-001`).
* **Ngày thực hiện (Date):** `YYYY-MM-DD`.
* **Người đề xuất / Thực hiện (Author):** Tên người thay đổi.
* **Loại thay đổi (Change Type):** `ADDED` (Thêm mới) | `CHANGED` (Sửa đổi) | `FIXED` (Vá lỗi) | `REFACTORED` (Tái cấu trúc).
* **Phân hệ ảnh hưởng (Component):** `ARCHITECTURE` | `DATABASE` | `BACKEND_API` | `FRONTEND_UI` | `AI_SERVICE` | `DOCS`.
* **Mô tả thay đổi (Description):** Tóm tắt nội dung thay đổi.
* **Lý do thay đổi (Rationale):** Tại sao cần thay đổi? Giải quyết vấn đề gì?
* **Chi tiết Trước & Sau (Before vs After):**
  * *Trước (Before):* Trạng thái cũ của hệ thống/CSDL/nghiệp vụ.
  * *Sau (After):* Trạng thái mới sau khi áp dụng thay đổi.
* **Phạm vi tác động (Impact):** Các file, module hoặc API bị ảnh hưởng.

---

## Danh Sách Các Bản Ghi Nhật Ký Thay Đổi

### 📌 [CHG-20261003-001] Khởi tạo Kiến trúc Nền tảng Modular Monolith
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Technical Architect & Product Owner
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `ARCHITECTURE`, `BACKEND_API`, `FRONTEND_UI`
* **Mô tả thay đổi:** Thiết lập cấu trúc hệ thống sàn thương mại điện tử đa người bán (Shopee Clone) theo mô hình Modular Monolith sử dụng Java 17 + Spring Boot 3 và React + Vite + Tailwind CSS.
* **Lý do thay đổi:** Cần một kiến trúc chuẩn mực doanh nghiệp, dễ triển khai demo/đồ án, đảm bảo giao dịch ACID an toàn khi tách đơn hàng đa shop mà không tốn tài nguyên như microservices.
* **Chi tiết Trước & Sau:**
  * *Trước:* Thư mục dự án trống, chưa có thiết kế kiến trúc.
  * *Sau:* Chốt kiến trúc Spring Boot 3 phân tầng (`Controller` ➔ `Service` ➔ `Repository` ➔ `Entity/DTO`) kết hợp React Vite Monorepo phân chia 3 layout: Khách hàng (`/`), Kênh Người Bán (`/seller`), Quản trị sàn (`/admin`).
* **Phạm vi tác động:** `backend/`, `frontend/`, `docs/specs/01_introduction.md`.

---

### 📌 [CHG-20261003-002] Nâng cấp Schema CSDL cho Thực phẩm Tươi sống & Nông sản Chợ
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Database Specialist
* **Loại thay đổi:** `CHANGED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `BACKEND_API`, `FRONTEND_UI`
* **Mô tả thay đổi:** Bổ sung các trường đặc thù nông sản thực phẩm vào bảng `products`: `unit`, `storage_type`, `shelf_life`, `origin`, `min_order_quantity`, `step_quantity` và trường động `attributes` dạng JSON.
* **Lý do thay đổi:** Chợ không chỉ bán hàng đóng gói mà bán thực phẩm tươi sống, cần đơn vị tính lẻ (`kg`, `bó`), hạn sử dụng ngắn, nhiệt độ bảo quản và chứng nhận an toàn (VietGAP/OCOP).
* **Chi tiết Trước & Sau:**
  * *Trước:* Bảng `products` chỉ có các trường cơ bản như thương mại điện tử thông thường (quần áo/sách vở).
  * *Sau:* Hỗ trợ số lượng lẻ thập phân (vd: `0.5kg` rau), thêm phương thức vận chuyển hỏa tốc `EXPRESS_FRESH`, lưu chứng nhận và hướng dẫn bảo quản linh hoạt qua JSON.
* **Phạm vi tác động:** `docs/specs/02_functional_requirements.md`, `docs/specs/05_database_specification.md`.

---

### 📌 [CHG-20261003-003] Mở rộng Phân loại Ngành hàng Nước Uống & Đồ Giải Khát
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Technical Architect
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `BACKEND_API`
* **Mô tả thay đổi:** Tích hợp quy cách đóng gói và dung tích cho ngành đồ uống: quy cách `lon`, `chai`, `lốc 6`, `thùng 24`, dung tích `ml`/`lít`, và quản lý trọng lượng vận chuyển lớn.
* **Lý do thay đổi:** Nước uống giải khát là mặt hàng tiêu thụ thường xuyên trên chợ, có đặc thù đóng thùng nặng và yêu cầu quy cách đóng gói đa dạng.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có quy chuẩn cho các mặt hàng bán theo lốc, thùng và dung tích chất lỏng.
  * *Sau:* Trường `attributes` trong `products` lưu trữ `volume` (`330ml`, `1.5L`), `packaging` (`Thùng 24 lon`), hỗ trợ tính toán phí ship cho hàng cồng kềnh.
* **Phạm vi tác động:** `docs/specs/02_functional_requirements.md`, `docs/specs/05_database_specification.md`.

---

### 📌 [CHG-20261003-004] Tích hợp Trợ lý AI Mua sắm Toàn Năng (AI Shopping Copilot)
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** AI & Fullstack Engineer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `AI_SERVICE`, `BACKEND_API`, `FRONTEND_UI`
* **Mô tả thay đổi:** Tích hợp module AI kết nối LLM (Gemini / OpenAI API) với cơ sở dữ liệu sàn. Hỗ trợ khách hàng hỏi đáp thực đơn, tư vấn thông số công nghệ, đồ gia dụng, săn deal và hiển thị trực tiếp Thẻ Sản phẩm có nút mua trong chat.
* **Lý do thay đổi:** Tạo điểm nhấn đột phá (killer feature) cho dự án, nâng cao trải nghiệm mua sắm thông minh như có chuyên viên tư vấn riêng tại chợ.
* **Chi tiết Trước & Sau:**
  * *Trước:* Hệ thống chỉ hỗ trợ tìm kiếm từ khóa SQL truyền thống.
  * *Sau:* Bổ sung API `POST /api/v1/ai/chat`, Widget Chatbot nổi ở góc màn hình, thuật toán Context Enrichment kết hợp dữ liệu tồn kho thực tế vào câu trả lời của AI.
* **Phạm vi tác động:** `docs/specs/02_functional_requirements.md`, `docs/specs/04_system_diagrams.md`, `docs/specs/06_test_plan.md`.

---

### 📌 [CHG-20261003-005] Mô-đun hóa Bộ Tài liệu Đặc tả Hệ thống theo Chuẩn IEEE 830 / ISO 29148
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Technical Lead
* **Loại thay đổi:** `REFACTORED`
* **Phân hệ ảnh hưởng:** `DOCS`
* **Mô tả thay đổi:** Phân rã file đặc tả đơn lẻ thành bộ 8 tài liệu độc lập có cấu trúc: Mục lục (`00`), Giới thiệu & Tham chiếu (`01`), Yêu cầu chức năng (`02`), Yêu cầu phi chức năng (`03`), Sơ đồ hệ thống (`04`), Cơ sở dữ liệu & ERD (`05`), Kế hoạch kiểm thử (`06`), và Nhật ký thay đổi (`07`).
* **Lý do thay đổi:** Đáp ứng yêu cầu chuẩn hóa tài liệu kỹ nghệ phần mềm chuyên nghiệp, giúp dễ đọc, bảo trì từng phần độc lập và quản lý lịch sử thay đổi qua Change Key.
* **Chi tiết Trước & Sau:**
  * *Trước:* 1 file tài liệu duy nhất `2026-10-03-shopee-marketplace-design.md`.
  * *Sau:* Bộ tài liệu 8 file mô-đun trong thư mục `docs/specs/` kèm hệ thống đánh chỉ mục và liên kết chéo.
* **Phạm vi tác động:** Thư mục `docs/specs/`.

---

### 📌 [CHG-20261003-006] Ban hành Quy chuẩn Phát triển Dự án & Luật Vận hành AI (Project Governance & Agent Rules)
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Project Owner & Software Architect
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `DOCS`, `GOVERNANCE`
* **Mô tả thay đổi:** Thiết lập và ban hành tập luật phát triển dự án [`PROJECT_RULES.md`](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/PROJECT_RULES.md) và hướng dẫn bắt buộc cho AI Assistant [`AGENTS.md`](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/AGENTS.md).
* **Lý do thay đổi:** Đảm bảo mọi lập trình viên và trợ lý AI khi làm việc trên dự án phải tuân thủ nghiêm ngặt quy trình kiểm duyệt (Review Gates: `mvn test` & `npm run build` trước khi commit), nguyên tắc chống IDOR, bảo vệ tồn kho đồng thời và bắt buộc ghi nhận Change Key cho mọi thay đổi.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có văn bản quy định luật lệ phát triển và quy trình review cho người và agent.
  * *Sau:* Ban hành `PROJECT_RULES.md` và `AGENTS.md`, ràng buộc quy trình Evidence-First và quản lý phiên bản tài liệu.
* **Phạm vi tác động:** `PROJECT_RULES.md`, `AGENTS.md`, `docs/specs/00_index.md`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261003-007] Thiết lập Pipeline Tích hợp Liên tục (CI/CD) với GitHub Actions
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** DevOps & System Architect
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `CI_CD`, `BACKEND_API`, `FRONTEND_UI`
* **Mô tả thay đổi:** Thiết lập tệp cấu hình quy trình CI/CD tự động [`.github/workflows/ci.yml`](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/.github/workflows/ci.yml) với 2 jobs chạy song song: kiểm thử & đóng gói Backend (Java 17, Maven test & verify) và kiểm thử & build Frontend (Node 20, npm run build).
* **Lý do thay đổi:** Tự động hóa kiểm duyệt chất lượng code mỗi khi có commit hoặc pull request vào nhánh `main`, bảo đảm quy tắc kiểm duyệt không bao giờ bị vi phạm trên GitHub.
* **Chi tiết Trước & Sau:**
  * *Trước:* Dự án chưa có quy trình kiểm thử và build tự động (CI/CD) trên repository.
  * *Sau:* Mỗi lần push code lên `main`, GitHub Actions tự động dựng môi trường, cache dependencies và kiểm thử toàn bộ hệ thống.
* **Phạm vi tác động:** `.github/workflows/ci.yml`, `PROJECT_RULES.md`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261003-009] Triển khai Task 1: Khởi tạo Kiến trúc Khung Monorepo (Spring Boot 3 + React + Docker MySQL)
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Fullstack Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `ARCHITECTURE`, `BACKEND_API`, `FRONTEND_UI`, `DATABASE`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh bộ khung mã nguồn Monorepo gồm:
  1. `docker-compose.yml`: Khởi tạo dịch vụ MySQL 8.0 (`shopee_db`) và phpMyAdmin cổng 8081.
  2. `backend/`: Dự án Spring Boot 3.3.4 (Java 17, Maven, JPA, Security 6, JJWT 0.12.6, Springdoc OpenAPI 2.6.0, H2 test, `ApiResponse<T>`, `HealthController`).
  3. `frontend/`: Dự án React 18 (Vite, TypeScript, Tailwind CSS cấu hình màu cam Shopee `#EE4D2D`, Lucide Icons, Axios, Zustand, giao diện `App.tsx` kiểm tra kết nối API).
* **Lý do thay đổi:** Hoàn thành Task 1 theo Kế hoạch triển khai, tạo nền tảng vững chắc để phát triển các module CSDL và nghiệp vụ.
* **Chi tiết Trước & Sau:**
  * *Trước:* Thư mục dự án chưa có mã nguồn backend và frontend, chưa có cấu hình container CSDL.
  * *Sau:* Cả backend (`mvn clean test` PASS 100%) và frontend (`npm run build` PASS) đều biên dịch thành công và kết nối thông suốt.
---

### 📌 [CHG-20261003-010] Triển khai Task 2: Core Data Model, 11 Enums, 13 JPA Entities & Repositories
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Fullstack Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `BACKEND_API`, `CORE_MODEL`
* **Mô tả thay đổi:** Xây dựng toàn bộ hệ thống thực thể dữ liệu (Data Access Layer) cho Chopee Marketplace:
  1. 11 Enums nghiệp vụ (`Role`, `UserStatus`, `ShopType`, `ShopStatus`, `StorageType`, `ProductStatus`, `ShippingMethod`, `PaymentMethod`, `PaymentStatus`, `OrderStatus`, `DiscountType`).
  2. 13 JPA Entities: `User`, `UserAddress`, `Shop`, `Category`, `Product`, `ProductImage`, `ProductVariant`, `CartItem`, `Order`, `OrderItem`, `Voucher`, `Payment`, `Review` với các thuộc tính cho thực phẩm tươi sống (`unit`, `stepQuantity`, `minOrderQuantity`, `storageType`, `shelfLife`) và JSON specs `attributes`.
  3. 13 Spring Data JPA Repositories tương ứng, đặc biệt là `ProductRepository` với truy vấn nguyên tử chống overselling `@Modifying(clearAutomatically = true, flushAutomatically = true) deductStock(...)`.
  4. Bộ kiểm thử tích hợp `EntityMappingTest` với 5 ca kiểm thử thực tế (tạo User, Shop, Product tươi sống với JSON attributes, Order splitting theo nhóm shop `groupOrderCode`, và atomic stock deduction) đạt 100% PASS.
* **Lý do thay đổi:** Hoàn thành toàn diện Task 2 theo kế hoạch, chuẩn bị cơ sở dữ liệu vững chắc cho Authentication và Catalog APIs.
* **Chi tiết Trước & Sau:**
  * *Trước:* Backend mới chỉ có khung cấu hình ban đầu, chưa có tầng Entity hay Repository.
  * *Sau:* Toàn bộ mô hình dữ liệu quan hệ được sinh chuẩn xác, hỗ trợ đầy đủ sàn đa người bán và thực phẩm tươi sống.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/entity/`, `backend/src/main/java/com/chopee/repository/`, `backend/src/test/java/com/chopee/repository/EntityMappingTest.java`.

---

### 📌 [CHG-20261003-011] Triển khai Task 3: Xác thực Người dùng, Bảo mật & Phân quyền RBAC (Spring Security 6 + JJWT)
* **Ngày thực hiện:** 2026-10-03
* **Người thực hiện:** Security Specialist & Backend Lead
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `SECURITY`, `AUTHENTICATION`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh phân hệ Xác thực và Phân quyền người dùng:
  1. Cấu hình Spring Security 6 Stateless Session với `SecurityConfig`, tích hợp `JwtAuthenticationEntryPoint` trả về cấu trúc lỗi chuẩn JSON 401 `ApiResponse`.
  2. Bộ sinh và xác thực mã bí mật `JwtTokenProvider` trên chuẩn bảo mật JJWT 0.12.x HMAC-SHA256, đóng gói claims `userId`, `username`, `email`, `role`, `fullName`, `shopId`.
  3. Bộ lọc `JwtAuthenticationFilter` (OncePerRequestFilter) tự động trích xuất token Bearer từ header `Authorization`, xác thực và gán quyền vào `SecurityContextHolder`.
  4. Dịch vụ người dùng `CustomUserDetailsService` và đối tượng `UserPrincipal` tải người dùng linh hoạt qua username hoặc email, hỗ trợ kiểm tra trạng thái khóa tài khoản (`BLOCKED`).
  5. Các DTO yêu cầu & phản hồi: `RegisterRequest`, `LoginRequest`, `RegisterSellerRequest`, `UserProfileResponse`, `AuthResponse`.
  6. Xử lý nghiệp vụ tại `AuthService` và bộ điều khiển REST `AuthController`:
     - `POST /api/v1/auth/register`: Đăng ký tài khoản Người mua (`ROLE_BUYER`), mã hóa mật khẩu bằng BCrypt.
     - `POST /api/v1/auth/login`: Đăng nhập bằng username hoặc email, cấp phát Token JWT.
     - `GET /api/v1/auth/me`: Lấy thông tin tài khoản hiện tại từ token đang đăng nhập.
     - `POST /api/v1/auth/register-seller`: Nâng cấp Người mua thành Người bán (`ROLE_SELLER`), khởi tạo Shop với slug chuẩn hóa tiếng Việt, sinh token mới mang quyền SELLER và `shopId`.
  7. Bộ xử lý ngoại lệ toàn cục `GlobalExceptionHandler` bắt trọn các lỗi validation (400), BadCredentials (401), AccessDenied (403), ResponseStatusException, trả về format chuẩn hóa `ApiResponse<T>`.
  8. Bộ kiểm thử tích hợp `AuthControllerTest` kiểm tra toàn bộ luồng Auth, 12/12 bài test hệ thống PASS 100%.
* **Lý do thay đổi:** Hoàn thành Task 3 theo kế hoạch, cung cấp cơ chế bảo mật và phân quyền cho sàn thương mại điện tử đa người bán.
* **Chi tiết Trước & Sau:**
  * *Trước:* Các API mở tự do (`permitAll`), chưa có cơ chế cấp phát token JWT và phân quyền Buyer/Seller/Admin.
  * *Sau:* Toàn bộ các endpoint được bảo vệ nghiêm ngặt bằng JWT và RBAC; có API cho phép người dùng đăng ký, đăng nhập và tự nâng cấp lên Seller.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/security/`, `backend/src/main/java/com/chopee/modules/auth/`, `backend/src/main/java/com/chopee/config/SecurityConfig.java`, `backend/src/main/java/com/chopee/common/GlobalExceptionHandler.java`, `backend/src/test/java/com/chopee/modules/auth/AuthControllerTest.java`.

---

### 📌 [CHG-20261004-012] Đổi tên Database mặc định từ `shopee_db` thành `chopee_db`
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Fullstack Architect
* **Loại thay đổi:** `CHANGED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `CONFIG`, `DOCKER`
* **Mô tả thay đổi:** Chuẩn hóa tên cơ sở dữ liệu mặc định thành `chopee_db` đồng bộ với thương hiệu dự án Chopee:
  1. `backend/src/main/resources/application.yml`: Thay đổi chuỗi JDBC kết nối mặc định thành `${DB_NAME:chopee_db}`.
  2. `docker-compose.yml`: Cập nhật biến môi trường container MySQL `MYSQL_DATABASE: chopee_db`.
  3. Cập nhật tài liệu thiết kế và kế hoạch triển khai đồng bộ tên CSDL `chopee_db`.
* **Lý do thay đổi:** Đồng bộ nhận diện thương hiệu Chopee Marketplace và đáp ứng yêu cầu người dùng cấu hình CSDL `chopee_db` trên WampServer và Docker.
* **Chi tiết Trước & Sau:**
  * *Trước:* Tên database mặc định là `shopee_db`.
  * *Sau:* Tên database mặc định là `chopee_db` trên cả môi trường Spring Boot và Docker Compose.
* **Phạm vi tác động:** `backend/src/main/resources/application.yml`, `docker-compose.yml`, `docs/superpowers/specs/2026-10-03-shopee-marketplace-design.md`, `docs/superpowers/plans/2026-10-03-chopee-marketplace-implementation.md`.

