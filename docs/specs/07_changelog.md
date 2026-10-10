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

---

### 📌 [CHG-20261004-013] Triển khai Task 4: Danh mục Đa ngành hàng, Tra cứu & Tìm kiếm Sản phẩm Động (Product Catalog & Dynamic Search)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Backend Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `CATALOG`, `SEARCH`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh phân hệ Catalog và Tìm kiếm sản phẩm phục vụ Chợ trực tuyến Chopee:
  1. Các DTO chuẩn hóa: `CategoryTreeResponse`, `ProductSearchCriteria`, `ProductSummaryResponse`, `ProductDetailResponse`, `ProductImageResponse`, `ProductVariantResponse`, `ShopPublicResponse`, `PageResponse<T>`.
  2. Dịch vụ phân cấp danh mục `CategoryService`: Lấy cây danh mục cha - con (`getCategoryTree`), tìm kiếm đệ quy ID danh mục cha và tất cả danh mục con (`getCategoryAndDescendantIds`).
  3. Tìm kiếm & lọc đa tiêu chí bằng JPA Specification `ProductSpecification` và mở rộng `JpaSpecificationExecutor` trong `ProductRepository`:
     - Chỉ tìm sản phẩm đang `ACTIVE`.
     - Tìm kiếm từ khóa không phân biệt hoa thường theo tên, mô tả, nguồn gốc xuất xứ, tên shop.
     - Lọc theo cây danh mục, khoảng giá (`minPrice` - `maxPrice`), điều kiện bảo quản thực phẩm (`StorageType`), đơn vị tính lẻ (`kg`, `chiếc`...), điểm đánh giá tối thiểu và lọc theo Shop.
     - Sắp xếp linh hoạt: mới nhất (`newest`), giá tăng dần (`price_asc`), giá giảm dần (`price_desc`), bán chạy nhất (`sales`), đánh giá cao nhất (`rating`).
  4. Bộ điều khiển REST `CatalogController` tại `/api/v1/public`:
     - `GET /api/v1/public/categories`: Lấy cây danh mục sản phẩm.
     - `GET /api/v1/public/categories/{id}`: Chi tiết danh mục theo ID.
     - `GET /api/v1/public/products`: Tra cứu, lọc sản phẩm phân trang với các bộ lọc chuyên sâu.
     - `GET /api/v1/public/products/{id}` & `/slug/{slug}`: Chi tiết sản phẩm kèm thông tin Shop, thuộc tính động JSON (VietGAP, OCOP, công suất), thư viện ảnh và danh sách biến thể.
     - `GET /api/v1/public/shops/{id}` & `/slug/{slug}`: Hồ sơ Shop công khai và thống kê tổng số sản phẩm mở bán.
  5. Bộ kiểm thử tích hợp `CatalogControllerTest` với 9 ca kiểm thử toàn diện đạt 100% PASS (Tổng 21/21 backend tests pass).
* **Lý do thay đổi:** Hoàn thành Task 4 theo kế hoạch triển khai, cung cấp đầy đủ các API tìm kiếm và duyệt danh mục cho người mua trên Chopee.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có API lấy danh mục hay tìm kiếm sản phẩm cho khách hàng.
  * *Sau:* Có đầy đủ 7 endpoint public phục vụ duyệt cây danh mục, tìm kiếm lọc thực phẩm tươi sống/đông lạnh và trang chi tiết sản phẩm.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/catalog/`, `backend/src/main/java/com/chopee/common/dto/PageResponse.java`, `backend/src/main/java/com/chopee/repository/ProductRepository.java`, `backend/src/test/java/com/chopee/modules/catalog/CatalogControllerTest.java`.

---

### 📌 [CHG-20261004-014] Triển khai Task 5: Quản lý Giỏ hàng Đa Người Bán & Bán Hàng Lẻ Thập Phân (Multi-Vendor Cart)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Backend Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `CART`, `MULTI_VENDOR`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh phân hệ Giỏ hàng cho Người mua (Buyer) hỗ trợ sàn thương mại đa người bán:
  1. Các DTO chuẩn hóa: `AddToCartRequest`, `UpdateCartItemRequest`, `CartItemResponse`, `ShopCartGroupResponse`, `CartResponse`.
  2. Xử lý nghiệp vụ tại `CartService`:
     - Tự động gom nhóm các món hàng theo từng gian hàng (`Shop`) riêng biệt trong giỏ hàng và tính subtotal cho từng shop.
     - Hỗ trợ số lượng lẻ dạng số thập phân (`BigDecimal`) phù hợp cho nông sản & thực phẩm tươi sống (ví dụ: `0.5kg`, `1.5kg`).
     - Kiểm tra nghiêm ngặt số lượng tối thiểu (`minOrderQuantity`) và bước nhảy số lượng (`stepQuantity`).
     - Tự động cộng dồn số lượng nếu sản phẩm/biến thể đã có trong giỏ hàng.
     - Bảo vệ chống IDOR: Chỉ cho phép người sở hữu giỏ hàng sửa hoặc xóa sản phẩm của chính mình.
  3. Bộ điều khiển REST `CartController` tại `/api/v1/buyer/cart`:
     - `GET /api/v1/buyer/cart`: Lấy giỏ hàng gom nhóm theo từng shop cùng tổng giá trị giỏ hàng.
     - `POST /api/v1/buyer/cart/items`: Thêm sản phẩm vào giỏ hàng.
     - `PUT /api/v1/buyer/cart/items/{id}`: Cập nhật số lượng sản phẩm.
     - `DELETE /api/v1/buyer/cart/items/{id}`: Xóa một món hàng khỏi giỏ.
     - `DELETE /api/v1/buyer/cart/clear`: Xóa sạch giỏ hàng.
  4. Bộ kiểm thử tích hợp: `CartServiceTest` (6 tests) và `CartControllerTest` (2 tests) đạt 100% PASS (Tổng 29/29 backend tests PASS).
* **Lý do thay đổi:** Hoàn thành Task 5 theo kế hoạch triển khai, tạo nền tảng vững chắc để người mua gom hàng từ nhiều shop trước khi bước vào quy trình Đặt hàng & Tách đơn đa Shop (Task 6).
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có phân hệ giỏ hàng, người dùng chưa thể lưu sản phẩm để thanh toán.
  * *Sau:* Có hệ thống giỏ hàng hoàn chỉnh, tự động tách nhóm theo shop và tính toán giá trị chuẩn xác.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/cart/`, `backend/src/main/java/com/chopee/repository/CartItemRepository.java`, `backend/src/test/java/com/chopee/modules/cart/`.

---

### 📌 [CHG-20261004-015] Triển khai Task 6: Tách Đơn Hàng Đa Cửa Hàng & Trừ Tồn Kho Nguyên Tử (Multi-Vendor Order Splitting & Atomic Stock Deduction)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Backend Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `ORDER`, `INVENTORY`, `MULTI_VENDOR`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh phân hệ Đặt hàng, Tách đơn đa Shop và Quản lý Tồn kho nguyên tử:
  1. Các DTO chuẩn hóa: `CheckoutPreviewRequest`, `OrderItemPreview`, `ShopCheckoutPreview`, `CheckoutPreviewResponse`, `CreateOrderRequest`, `OrderItemResponse`, `OrderResponse`, `CheckoutResultResponse`.
  2. Xử lý nghiệp vụ tại `OrderService`:
     - **Xem trước thanh toán (`previewCheckout`)**: Tính toán chi tiết phí ship và tiền hàng theo từng Shop riêng biệt (ví dụ: giao nhanh tươi sống `EXPRESS_FRESH` tính 25.000đ/shop, chuẩn `STANDARD` 15.000đ/shop).
     - **Tự động tách đơn hàng (Multi-Vendor Order Splitting)**: Khi người mua checkout giỏ hàng có sản phẩm từ N shop, hệ thống tự động sinh ra N đơn hàng độc lập (`orders`) tương ứng với từng shop để đảm bảo cô lập dữ liệu người bán, đồng thời gắn chung mã gom nhóm `groupOrderCode` (`GRP-...`) để người mua quản lý tổng thể.
     - **Trừ tồn kho nguyên tử & Chống overselling (Atomic Stock Deduction)**: Áp dụng `@Modifying deductStock` với điều kiện `stockQuantity >= qty`. Chạy trong giao dịch `@Transactional(rollbackFor = Exception.class)`. Nếu bất kỳ mặt hàng nào của bất kỳ shop nào không đủ tồn kho, toàn bộ giao dịch sẽ tự động Rollback hoàn toàn (không có đơn hàng nào được tạo, tồn kho các shop khác được hoàn nguyên).
     - **Dọn dẹp giỏ hàng**: Tự động xóa các mặt hàng đã đặt thành công khỏi giỏ hàng của người mua.
     - **Hủy đơn hàng & Hoàn trả tồn kho (`cancelOrder`)**: Chỉ cho phép hủy khi đơn ở trạng thái `PENDING`. Tự động hoàn lại số lượng tồn kho nguyên tử (`restoreStock`) cho tất cả sản phẩm trong đơn.
     - **Bảo mật IDOR**: Mọi thao tác truy vấn và hủy đơn đều kiểm tra chặt chẽ quyền sở hữu của `userId` thông qua `orderRepository.findByOrderCodeAndUserId(...)`.
  3. Bộ điều khiển REST `OrderController` tại `/api/v1/buyer/orders`:
     - `POST /api/v1/buyer/orders/checkout-preview`: Tính trước phí ship, tiền hàng và tổng thanh toán.
     - `POST /api/v1/buyer/orders`: Thực hiện đặt hàng và tách đơn.
     - `GET /api/v1/buyer/orders`: Danh sách đơn hàng phân trang theo trạng thái của người mua.
     - `GET /api/v1/buyer/orders/{orderCode}`: Chi tiết một đơn hàng cụ thể.
     - `GET /api/v1/buyer/orders/group/{groupOrderCode}`: Danh sách các đơn hàng con trong cùng lượt thanh toán.
     - `PUT /api/v1/buyer/orders/{orderCode}/cancel`: Hủy đơn hàng đang chờ xác nhận.
  4. Bộ kiểm thử tích hợp: `OrderSplittingTest` (5 tests) đạt 100% PASS (Toàn bộ 34/34 backend tests đạt 100% PASS, `npm run build` frontend đạt 100% PASS).
* **Lý do thay đổi:** Hoàn thành Task 6 theo kế hoạch triển khai, đảm bảo tính toàn vẹn đa người bán và tính nhất quán ACID của tồn kho khi thanh toán.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có quy trình đặt hàng, chưa tách đơn theo từng shop và chưa có cơ chế trừ tồn kho chống overselling.
  * *Sau:* Hệ thống đặt hàng hoàn chỉnh, tự động tách đơn theo từng gian hàng, đảm bảo tính nguyên tử ACID tuyệt đối và bảo mật chống IDOR.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/order/`, `backend/src/main/java/com/chopee/repository/OrderRepository.java`, `backend/src/main/java/com/chopee/repository/ProductRepository.java`, `backend/src/test/java/com/chopee/modules/order/OrderSplittingTest.java`.

---

### 📌 [CHG-20261004-016] Triển khai Task 7: Cổng Thanh toán Trực tuyến (Payment Gateway - VNPay Sandbox QR & IPN Webhook)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Backend Architect & Lead Developer
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `PAYMENT`, `SECURITY`, `WEBHOOK`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh tích hợp Cổng thanh toán trực tuyến VNPay Sandbox cho Chopee:
  1. Cấu hình bảo mật và môi trường `VNPayConfig`: Thiết lập các thông số kết nối sandbox (`payUrl`, `returnUrl`, `tmnCode`, `hashSecret`, `version = 2.1.0`, `command = pay`).
  2. Tiện ích mật mã học `VNPayHelper`:
     - Triển khai thuật toán băm HMAC-SHA512 sinh chuỗi hex 128 ký tự theo chuẩn VNPay.
     - Hàm sắp xếp tham số alphabetically (US-ASCII) và URL-encoding để chống tấn công giả mạo dữ liệu giao dịch.
     - Xác thực chữ ký số tự động (`verifySignature`) cho các yêu cầu từ Webhook IPN và Return URL.
  3. DTOs: `CreatePaymentRequest`, `VNPayPaymentResponse`, `VNPayIpnResponse`.
  4. Nghiệp vụ thanh toán `PaymentService`:
     - Khởi tạo thanh toán VNPay (`createVNPayPayment`): Tính tổng tiền đơn hàng nhóm đa Shop (`groupOrderCode`), nhân hệ số 100, tạo URL thanh toán VNPay kèm chữ ký số và lưu bản ghi `Payment` ở trạng thái `UNPAID`.
     - Xử lý Webhook IPN (`processIpn`): Kiểm tra checksum chữ ký số, kiểm tra tồn tại đơn hàng, kiểm tra khớp số tiền, chống xác nhận trùng lặp (Idempotency). Khi thanh toán thành công (`vnp_ResponseCode == "00"`), tự động cập nhật tất cả các đơn hàng con trong nhóm sang `paymentStatus = PAID` và lưu mã giao dịch `vnp_TransactionNo`.
     - Xử lý Return URL (`processCallback`): Tiếp nhận phản hồi khi trình duyệt người dùng quay lại từ cổng VNPay.
  5. Bộ điều khiển REST `PaymentController` tại `/api/v1/payment`:
     - `POST /api/v1/payment/vnpay/create-payment`: Khởi tạo liên kết thanh toán VNPay Sandbox.
     - `GET /api/v1/payment/vnpay/ipn`: Webhook Server-to-Server tiếp nhận IPN từ VNPay (Public).
     - `GET /api/v1/payment/vnpay/callback`: Tiếp nhận callback chuyển hướng người dùng (Public).
  6. Bộ kiểm thử tích hợp `VNPayPaymentTest` gồm 6 bài kiểm thử toàn diện (băm HMAC-SHA512, chữ ký hợp lệ/giả mạo, sinh URL thanh toán, xử lý IPN thành công cập nhật đơn sang PAID, phát hiện sai Checksum trả về RspCode 97, đơn không tồn tại trả về RspCode 01, và xử lý đơn đã thanh toán trước đó trả về RspCode 02) đạt 100% PASS (Tổng 40/40 backend tests PASS, `npm run build` frontend PASS).
* **Lý do thay đổi:** Hoàn thành Task 7 theo kế hoạch triển khai, cung cấp cổng thanh toán trực tuyến qua QR/thẻ ATM/Visa Sandbox cho Chopee.
* **Chi tiết Trước & Sau:**
  * *Trước:* Hệ thống chưa có cổng thanh toán trực tuyến, chỉ có mô hình dữ liệu Payment cơ bản.
  * *Sau:* Tích hợp hoàn chỉnh cổng VNPay Sandbox với xác thực HMAC-SHA512 và IPN webhook tự động cập nhật đơn nhóm.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/payment/`, `backend/src/main/java/com/chopee/config/SecurityConfig.java`, `backend/src/test/java/com/chopee/modules/payment/VNPayPaymentTest.java`.

---

### 📌 [CHG-20261004-017] Triển khai Task 8: Phân hệ Kênh Người Bán & Quản Trị Hệ Thống (Seller Center & Admin Platform Management APIs)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Backend Architect & Security Specialist
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `SELLER_CENTER`, `ADMIN_PORTAL`, `SECURITY`, `MULTI_VENDOR`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh phân hệ Kênh Người Bán (Seller Center) và Quản trị Sàn (Admin Management):
  1. Module Kênh Người Bán (`SellerService`, `SellerController` tại `/api/v1/seller/**` bảo vệ bởi `@PreAuthorize("hasRole('SELLER')")`):
     - `GET /api/v1/seller/dashboard`: Thống kê doanh thu (loại trừ đơn `CANCELLED`), số lượng đơn theo từng trạng thái (`PENDING`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`), tổng số sản phẩm và đánh giá trung bình của shop.
     - `GET /api/v1/seller/products`: Danh sách sản phẩm của riêng gian hàng phân trang.
     - `POST /api/v1/seller/products`: Thêm mới sản phẩm kèm danh sách ảnh và các biến thể phân loại (`variants`).
     - `PUT /api/v1/seller/products/{id}`: Cập nhật thông tin sản phẩm, thuộc tính linh hoạt (`attributes`), ảnh và biến thể.
     - `DELETE /api/v1/seller/products/{id}`: Xóa mềm sản phẩm (chuyển sang `INACTIVE`).
     - `GET /api/v1/seller/orders`: Danh sách đơn hàng phân trang của riêng shop, hỗ trợ lọc theo `OrderStatus`.
     - `GET /api/v1/seller/orders/{orderCode}`: Chi tiết đơn hàng thuộc shop.
     - `PUT /api/v1/seller/orders/{orderCode}/status`: Cập nhật trạng thái đơn hàng theo luồng (`CONFIRMED -> SHIPPING -> DELIVERED` hoặc `CANCELLED`), tự động hoàn lại tồn kho nguyên tử (`restoreStock`) khi hủy đơn, chặn cập nhật trạng thái đơn đã kết thúc (`DELIVERED`/`CANCELLED`).
  2. Module Quản Trị Hệ Thống (`AdminService`, `AdminController` tại `/api/v1/admin/**` bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`):
     - `GET /api/v1/admin/dashboard`: Thống kê tổng số người dùng, người bán, người mua, tổng số gian hàng, gian hàng chờ duyệt (`PENDING`), gian hàng hoạt động (`APPROVED`), gian hàng bị khóa (`LOCKED`), tổng đơn hàng và tổng giá trị giao dịch GMV toàn sàn.
     - `GET /api/v1/admin/shops`: Danh sách gian hàng phân trang kèm bộ lọc trạng thái.
     - `PUT /api/v1/admin/shops/{id}/status`: Phê duyệt hoặc tạm khóa gian hàng vi phạm chính sách (`PENDING -> APPROVED -> LOCKED`).
  3. Bảo vệ IDOR Đa Người Bán Tuyệt Đối:
     - Mọi endpoint của Seller Center đều kiểm tra xác thực quyền sở hữu tài nguyên qua `Shop.user.id == seller.id` hoặc `Product.shop.id == seller.shop.id` hoặc `Order.shop.id == seller.shop.id`.
     - Bất kỳ nỗ lực đọc hoặc sửa sản phẩm/đơn hàng của gian hàng khác đều bị chặn ngay lập tức với mã HTTP 403 Forbidden.
  4. Bộ kiểm thử tích hợp & bảo mật `SellerSecurityTest` gồm 6 bài kiểm thử toàn diện đạt 100% PASS (Tổng 46/46 backend tests PASS, `npm run build` frontend PASS).
* **Lý do thay đổi:** Hoàn thành Task 8 theo kế hoạch triển khai, đảm bảo tính toàn vẹn đa người bán và bảo mật IDOR.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có API quản lý cho Kênh Người Bán và Quản trị viên Sàn.
  * *Sau:* Hệ thống Seller Center và Admin Portal hoàn chỉnh, bảo mật chống IDOR tuyệt đối, kiểm soát chặt chẽ luồng đơn hàng và vận hành gian hàng.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/seller/`, `backend/src/main/java/com/chopee/modules/admin/`, `backend/src/main/java/com/chopee/repository/`, `backend/src/test/java/com/chopee/modules/seller/`.

---

### 📌 [CHG-20261004-018] Triển khai Task 9: Trợ Lý Mua Sắm Toàn Năng Chopee (AI Shopping Copilot Backend Service with RAG & Context Enrichment)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** AI & Backend Architect
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `AI_COPILOT`, `BACKEND_API`, `RAG_CONTEXT_ENRICHMENT`, `PRODUCT_CATALOG`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh backend service Trợ Lý Mua Sắm AI (AI Shopping Copilot) tích hợp RAG:
  1. DTOs phân hệ AI Copilot (`AIChatRequest`, `AIChatResponse`, `ChatMessageDTO`):
     - Tiếp nhận tin nhắn người dùng (`message`), lịch sử hội thoại nhiều lượt (`history`), và ngân sách tối đa (`maxBudget`).
     - Trả về câu trả lời định dạng Markdown (`reply`), ý định được phân loại (`intent`), danh sách thẻ sản phẩm thực tế từ Database (`recommendedProducts` dạng `ProductSummaryResponse`), và 3 câu hỏi gợi ý tiếp theo (`suggestedQuestions`).
  2. Động cơ Nhận diện Ý định & Làm giàu Ngữ cảnh (`AIShoppingCopilotService`):
     - Nhận diện ý định thông minh: `COOKING_RECIPE` (ẩm thực / nấu ăn), `TECH_ADVICE` (gia dụng / công nghệ), `BUDGET_SHOPPING` (săn deal / tối ưu ngân sách), và `GENERAL_ASSISTANT` (chào hỏi / khám phá).
     - Ánh xạ món ăn đặc trưng Việt Nam sang từ khóa nguyên liệu tươi sống thực tế (ví dụ: `canh chua` -> `cá`, `cà chua`, `đậu bắp`, `thơm/dứa`, `bạc hà`, `giá`...).
     - Tự động trích xuất ngân sách qua Regex (ví dụ: `dưới 30k`, `ngân sách 500k`, `tầm 1 triệu`...).
     - Truy vấn RAG (Retrieval-Augmented Generation) trực tiếp từ MySQL: Tìm kiếm sản phẩm tồn kho (`status = ACTIVE`, `stockQuantity > 0`), lọc nghiêm ngặt theo trần ngân sách (`sellingPrice <= budget`), loại bỏ trùng lặp và giới hạn 6-8 sản phẩm phù hợp nhất.
     - Sinh nội dung tư vấn chuyên sâu theo miền nghiệp vụ bằng tiếng Việt tự nhiên kèm chỉ dẫn sử dụng nút "Thêm vào giỏ hàng" trực tiếp trên khung chat.
     - Sinh 3 câu hỏi gợi ý tương tác theo ngữ cảnh giúp người dùng tiếp tục khám phá sàn.
  3. REST Controller `AIController` tại `POST /api/v1/ai/chat`:
     - Phục vụ API công khai (được cấu hình `permitAll()` trong `SecurityConfig`) để cả khách vãng lai và người mua đã đăng nhập đều có thể trò chuyện với AI Shopping Copilot.
  4. Bộ kiểm thử tích hợp & nghiệp vụ `AIServiceTest` gồm 5 bài kiểm thử toàn diện đạt 100% PASS:
     - Nhận diện ý định nấu ăn canh chua và gắn thẻ nguyên liệu tươi sống (cá, cà chua, đậu bắp).
     - Tư vấn thiết bị công nghệ gia dụng (nồi chiên không dầu).
     - Lọc ràng buộc ngân sách khắt khe (chỉ trả về món <= 30k, loại trừ thịt bò Wagyu tiền triệu).
     - Dự phòng chào hỏi chung với câu hỏi gợi ý tương tác.
     - Endpoint REST `POST /api/v1/ai/chat` trả về HTTP 200 OK với cấu trúc ApiResponse chuẩn.
     - Tổng cộng 51/51 tests backend đạt 100% PASS, `npm run build` frontend đạt 100% PASS.
* **Lý do thay đổi:** Hoàn thành Task 9 theo kế hoạch triển khai, tạo tính năng đột phá (killer feature) AI Copilot cho sàn TMĐT Chopee.
* **Chi tiết Trước & Sau:**
  * *Trước:* Chưa có backend service AI, hệ thống chỉ hỗ trợ tìm kiếm từ khóa SQL truyền thống.
  * *Sau:* Hệ thống AI Copilot hoàn chỉnh kết hợp RAG với cơ sở dữ liệu thực, tự động đề xuất nguyên liệu tươi sống, đồ gia dụng và thẻ sản phẩm mua ngay trong chat.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/ai/`, `backend/src/main/java/com/chopee/config/SecurityConfig.java`, `backend/src/test/java/com/chopee/modules/ai/AIServiceTest.java`.

---

### 📌 [CHG-20261004-019] Triển khai Task 10: Khởi Tạo Dữ Liệu Thực Tế Chợ Việt Nam (Realistic Vietnamese Marketplace Multi-Industry Data Seeding)
* **Ngày thực hiện:** 2026-10-04
* **Người thực hiện:** Lead Database Engineer & QA Specialist
* **Loại thay đổi:** `ADDED`
* **Phân hệ ảnh hưởng:** `DATA_SEEDING`, `DATABASE`, `CATALOG`, `MULTI_VENDOR`
* **Mô tả thay đổi:** Xây dựng hoàn chỉnh cơ chế nạp dữ liệu mẫu khởi đầu (Data Seeding) chuẩn thương mại điện tử Việt Nam:
  1. Component Khởi tạo Dữ liệu `DataInitializer` (kế thừa `CommandLineRunner`):
     - Kiểm tra trạng thái Database: Tự động phát hiện cơ sở dữ liệu trống (`userRepository.count() == 0`) để nạp toàn bộ dữ liệu mẫu một cách an toàn và bỏ qua nếu dữ liệu đã tồn tại.
  2. Tạo 7 Tài khoản Người dùng chuẩn mật khẩu `123456` (đã mã hóa BCrypt):
     - 1 Quản trị viên (`admin@chopee.vn`, `ROLE_ADMIN`).
     - 5 Chủ gian hàng (`dalat_farm@chopee.vn`, `hungphat_beverage@chopee.vn`, `philips_mall@chopee.vn`, `techzone_official@chopee.vn`, `unistyle_fashion@chopee.vn`, `ROLE_SELLER`).
     - 1 Khách mua hàng (`buyer1@chopee.vn`, `ROLE_BUYER`).
  3. Tạo 5 Gian hàng (`Shop`) thuộc các ngành nghề kinh doanh trọng điểm:
     - *Nông Sản Sạch Đà Lạt* (`FOOD_FRESH`, rating 4.9).
     - *Đại Lý Đồ Uống Hùng Phát* (`GENERAL`, rating 4.8).
     - *Thế Giới Gia Dụng Philips* (`OFFICIAL_MALL`, rating 4.95).
     - *TechZone Official Store* (`OFFICIAL_MALL`, rating 4.85).
     - *UniStyle - Thời Trang & Phụ Kiện* (`GENERAL`, rating 4.75).
  4. Tạo Cây Danh mục Sản phẩm phân cấp cha - con 2 tầng gồm 14 danh mục (Thực phẩm tươi sống, Rau củ quả, Thịt hải sản, Đồ uống có cồn, Nước ngọt trà, Thiết bị gia dụng, Nồi chiên bếp điện, Máy xay ép, Phụ kiện công nghệ, Tai nghe loa, Bàn phím chuột, Pin sạc cáp, Thời trang).
  5. Tạo 31 Sản phẩm thực tế Việt Nam với đầy đủ thông số:
     - Nông sản tươi sống: Cà chua beef Đà Lạt, dưa leo baby, đậu bắp xanh, thơm mật, bắp cải trái tim, cá basa phi lê, thịt ba chỉ heo CP, tôm sú Cà Mau, nấm đùi gà, xà lách lolo (hỗ trợ mua lẻ bước nhảy 0.5kg, bảo quản `FRESH`, chứng nhận VietGAP, OCOP).
     - Đồ uống & Giải khát: Thùng bia Heineken Silver 330ml, Tiger Crystal, Coca-Cola 320ml, Sprite lốc 6 lon, Lavie thùng 24 chai, Trà xanh Không Độ (đầy đủ quy cách đóng thùng, lốc, thể tích).
     - Thiết bị gia dụng: Nồi chiên không dầu Philips HD9252, nồi cơm cao tần Tefal 1.5L, máy xay Philips HR2223, bếp từ đôi Inverter Sunhouse, ấm siêu tốc Lock&Lock, bàn là đứng Philips (thông số công suất, dung tích, bảo hành 24-36 tháng).
     - Phụ kiện công nghệ: Tai nghe Sony WH-1000XM5, Galaxy Buds2 Pro, chuột Logitech MX Master 3S, bàn phím cơ Keychron K2 Pro, sạc dự phòng Anker 65W, củ sạc GaN Ugreen 65W.
     - Thời trang & phụ kiện: Áo thun cotton trơn UniStyle, áo khoác gió thể thao trượt nước, balo laptop chống sốc Oxford.
     - Tạo biến thể phân loại (`variants`) theo kích cỡ, trọng lượng túi 500g / 1kg.
  6. Bộ kiểm thử tích hợp `DataInitializerTest` đạt 100% PASS (Tổng cộng 52/52 backend tests PASS, `npm run build` frontend PASS).
* **Lý do thay đổi:** Hoàn thành Task 10 theo kế hoạch triển khai, tạo bộ dữ liệu mẫu phong phú, sinh động chuẩn chợ TMĐT Việt Nam phục vụ chạy thử nghiệm, kiểm thử tự động và trình diễn demo.
* **Chi tiết Trước & Sau:**
  * *Trước:* Cơ sở dữ liệu trống, thiếu dữ liệu đa ngành để kiểm thử thực tế và trải nghiệm giao diện người dùng.
  * *Sau:* Hệ thống sở hữu 7 tài khoản mẫu, 5 shop, 14 danh mục và 31 sản phẩm chất lượng cao sẵn sàng đưa vào vận hành.
---

### 📌 [CHG-20261007-001] Tái Cấu Trúc & Tối Ưu Hóa Toàn Diện Cơ Sở Dữ Liệu Chopee (Database Hardening)
* **Ngày thực hiện:** 2026-10-07
* **Người thực hiện:** Lead Database Architect & Backend Specialist
* **Loại thay đổi:** `CHANGED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `CATALOG`, `ORDER`, `AUDIT`, `BACKEND_API`, `DOCS`
* **Mô tả thay đổi:**
  1. **Khắc phục xung đột Biến thể & Tồn kho (`products` vs `product_variants`):**
     - Bổ sung cột `has_variants BOOLEAN NOT NULL DEFAULT FALSE` vào bảng `products` để phân định dứt khoát hàng đơn lẻ và hàng có phân loại.
  2. **Chuẩn hóa Trọng lượng tính cước vận chuyển:**
     - Bổ sung cột `weight_grams INT NOT NULL DEFAULT 500` vào bảng `products` cho phép tính cước phí chính xác theo gram cho mọi loại hàng (nông sản, đồ uống, thiết bị).
  3. **Phân định Nguồn Giảm Giá & Đối Soát Tài Chính Đa Shop:**
     - Mở rộng bảng `orders`: tách `shop_discount_amount` (Shop chịu) và `platform_discount_amount` (Sàn trợ giá) phục vụ quyết toán Payout.
  4. **Kiểm soát Quy trình Hủy đơn:**
     - Bổ sung `cancelled_by ENUM('BUYER', 'SELLER', 'ADMIN', 'SYSTEM')` và `cancellation_reason VARCHAR(255)` trên bảng `orders`.
  5. **Bổ sung Thực thể Ghi vết Hành trình Đơn hàng (`order_status_history`):**
     - Tạo Entity `OrderStatusHistory`, Repository và tự động ghi log audit trail mỗi khi đơn đổi trạng thái.
  6. **Nâng cấp Đánh giá Khách hàng (`reviews`):**
     - Bổ sung cột `images_json TEXT` lưu trữ hình ảnh / video feedback thực tế của người mua.
  7. **Triển khai Triggers, Procedures và Functions Tầng Database:**
     - `trg_prevent_negative_stock`: Chống âm kho trực tiếp ở tầng DB.
     - `trg_order_status_audit`: Tự động ghi nhận lịch sử đơn hàng.
     - `trg_update_sold_count`: Tự động cộng dồn số lượng bán.
     - `trg_after_review_insert`: Tự động tính sao trung bình cho sản phẩm và shop.
     - `sp_cancel_order_and_restock`: Thủ tục hoàn kho nguyên tử có khóa bi quan `FOR UPDATE`.
     - `fn_calculate_shipping_fee`: Hàm tính cước phí lũy tiến theo trọng lượng.
  8. **Xuất bản Kịch bản SQL Hoàn chỉnh:**
     - `backend/src/main/resources/db/chopee_schema_full.sql` gồm 14 bảng, ràng buộc khóa ngoại chặt chẽ, Full-text index và DDL thủ tục.
  9. **Kiểm thử tự động:**
     - Toàn bộ 53/53 JUnit tests đạt 100% PASS, `npm run build` PASS.
* **Lý do thay đổi:** Giải quyết triệt để 5 lỗ hổng logic nghiệp vụ, giảm tải tính toán cho tầng ứng dụng và hoàn thiện thiết kế CSDL đạt chuẩn doanh nghiệp.
* **Chi tiết Trước & Sau:**
  * *Trước:* Bảng `products` thiếu trọng lượng và cờ biến thể; đơn hàng chưa phân định nguồn giảm giá; thiếu bảng ghi vết lịch sử và trigger tự động tính sao.
  * *Sau:* CSDL chuẩn hóa 3NF, bảo vệ toàn vẹn dữ liệu tự động bằng Triggers và Transactions, sẵn sàng cho tải cao.
* **Phạm vi tác động:** `Product.java`, `Order.java`, `Review.java`, `OrderStatusHistory.java`, `OrderService.java`, `SellerService.java`, `EntityMappingTest.java`, `05_database_specification.md`, `chopee_schema_full.sql`.

---

### 📌 [CHG-20261007-002] Mở Rộng Kiến Trúc CSDL Doanh Nghiệp 24 Bảng: Chu Kỳ Sống 3 Tầng, Xóa Mềm, Ví Ký Quỹ Escrow, Thẻ Kho & Flash Sale
* **Ngày thực hiện:** 2026-10-07
* **Người thực hiện:** Principal Database Architect & System Governance Engineer
* **Loại thay đổi:** `ADDED` / `CHANGED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `CATALOG`, `ORDER`, `FINANCE_WALLET`, `INVENTORY`, `DOCS`
* **Mô tả thay đổi:**
  1. **Quy chuẩn Xóa Mềm Toàn diện (Soft Delete & Data Immutability):**
     - Bổ sung `is_deleted BOOLEAN NOT NULL DEFAULT FALSE` và `deleted_at DATETIME NULL` trên tất cả các thực thể nghiệp vụ: `users`, `shops`, `categories`, `products`, `product_variants`, `vouchers`, `reviews`.
     - Tuyệt đối loại bỏ thao tác `DELETE FROM` gây đứt gãy lịch sử mua hàng hoặc báo cáo tài chính.
  2. **Chuẩn hóa Phân Tầng Dữ liệu 3 Lớp (3-Tier Hot - Warm - Cold Lifecycle):**
     - Bổ sung cột `storage_tier ENUM('HOT', 'WARM', 'COLD') DEFAULT 'HOT'` trên bảng `orders` kèm chỉ mục tối ưu `idx_orders_tier_created (storage_tier, created_at)`.
     - Phân định rõ ranh giới lưu trữ: Hot (RAM/Redis + SSD NVMe), Warm (MySQL Partitioning / Read-Replicas), Cold (Data Lake / S3 Parquet nén).
     - Bổ sung Stored Procedure `sp_archive_cold_orders` tự động nén và dời dữ liệu lịch sử trên 365 ngày sang Cold Tier.
  3. **Mô hình Tài chính Ví Ký Quỹ Đa Gian Hàng (Multi-Vendor Escrow & Wallets):**
     - Bổ sung bảng `shop_wallets`: Quản lý số dư khả dụng (`available_balance`), số dư chờ đối soát (`pending_balance`), và số dư đóng băng (`locked_balance`).
     - Bổ sung bảng `wallet_transactions`: Nhật ký biến động số dư theo dõi chi tiết từng khoản thu/chi (doanh thu đơn hàng, phí sàn, hoàn tiền, rút tiền).
     - Bổ sung bảng `payout_requests`: Quản lý lệnh rút tiền của người bán về ngân hàng thụ hưởng kèm trạng thái phê duyệt.
     - Bổ sung Stored Procedure `sp_settle_order_payout`: Tự động quyết toán tiền ký quỹ sang ví của Shop khi đơn hàng hoàn tất giao nhận (`DELIVERED`), tự động khấu trừ phí hoa hồng sàn.
  4. **Kiểm Soát Đổi Trả & Khiếu Nại (Return & Refund Disputes):**
     - Bổ sung bảng `refund_requests`: Tiếp nhận yêu cầu trả hàng, lý do hư hại/hết hạn, ảnh/video khui hộp làm bằng chứng, luồng hòa giải giữa Người mua, Người bán và Ban quản trị Sàn.
  5. **Thẻ Kho & Kiểm Soát Biến Động Xuất Nhập Tồn (Inventory Audit Logs):**
     - Bổ sung bảng `inventory_logs`: Lưu vết từng biến động kho (xuất bán đơn hàng, hoàn kho hủy đơn, nhập hàng, hao hụt hư hỏng).
     - Mở rộng Trigger `trg_update_sold_count` và Procedure `sp_cancel_order_and_restock` tự động ghi nhận thẻ kho minh bạch theo thời gian thực.
  6. **Đặc Tả Động Danh Mục & Thương Hiệu ("Gọn từ gốc"):**
     - Bổ sung bảng `brands`: Quản lý thương hiệu chính hãng toàn sàn (Apple, Samsung, Vinamilk, Philips...).
     - Bổ sung bảng `category_attributes`: Định nghĩa danh mục thông số kỹ thuật chuẩn hóa theo ngành hàng phục vụ bộ lọc tìm kiếm trên giao diện.
     - Bổ sung `brand_id` trong bảng `products`.
  7. **Chiến Dịch Khuyến Mãi Flash Sale:**
     - Bổ sung bảng `flash_sales` và `flash_sale_items`: Hỗ trợ bán hàng giá sốc theo khung giờ có giới hạn số lượng, tự động chuyển về giá gốc khi hết giờ/hết suất mà không cần update bảng `products`.
  8. **Hệ Thống Thông Báo Thời Gian Thực (In-App Notification Hub):**
     - Bổ sung bảng `notifications`: Quản lý thông báo trạng thái đơn hàng, ưu đãi và biến động số dư ví cho cả Buyer và Seller.
  9. **Đồng bộ hóa & Kiểm chứng Toàn vẹn:**
     - Cập nhật các Java Entity (`User`, `Shop`, `Category`, `Product`, `ProductVariant`, `OrderItem`, `Order`, `Voucher`, `Review`) tương thích 100% với schema mới.
     - Cập nhật `docs/specs/05_database_specification.md` đạt chuẩn 24 bảng.
     - Cập nhật kịch bản DDL `backend/src/main/resources/db/chopee_schema_full.sql`.
     - Kiểm thử tự động: 53/53 JUnit tests PASS, Vite build PASS.
* **Lý do thay đổi:** Hoàn thành đợt thẩm định và tăng cường cơ sở dữ liệu chuyên sâu nhiều lượt (`/goal`), đảm bảo hệ thống có đầy đủ nền tảng vận hành tài chính, quản lý tồn kho, đổi trả, phân tầng lưu trữ và chống mất dữ liệu đạt chuẩn doanh nghiệp quy mô lớn.
* **Chi tiết Trước & Sau:**
  * *Trước:* Hệ thống có 14 bảng, chưa có phân hệ ví tiền người bán (dòng tiền bị treo), chưa có thẻ kho ghi nhận biến động, thiếu bảng khiếu nại hoàn tiền, chưa áp dụng cơ chế xóa mềm và phân tầng lưu trữ.
  * *Sau:* Hệ thống hoàn thiện 24 bảng chuẩn hóa 3NF, tích hợp đầy đủ ví ký quỹ Escrow, thẻ kho tự động, đổi trả tranh chấp, flash sale giờ vàng, đặc tả thuộc tính động theo danh mục và cơ chế xóa mềm an toàn tuyệt đối.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/entity/`, `backend/src/main/resources/db/chopee_schema_full.sql`, `docs/specs/05_database_specification.md`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261007-003] Tối Ưu Hóa & Khai Thác Triệt Để Năng Lực Nội Tại MySQL 8.0+ (Native Database Hardening)
* **Ngày thực hiện:** 2026-10-07
* **Người thực hiện:** Principal Database Architect & System Governance Engineer
* **Loại thay đổi:** `CHANGED` / `OPTIMIZED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `CATALOG`, `FINANCE_WALLET`, `DOCS`
* **Mô tả thay đổi:**
  1. **Triển khai Ràng buộc Toàn vẹn Dữ liệu Cấp Engine (Native CHECK Constraints):**
     - `chk_shops_rating`: Điểm đánh giá shop bắt buộc nằm trong khoảng `[1.0, 5.0]`.
     - `chk_products_prices`: Chặn logic sai lệch giá (`selling_price >= 0 AND original_price >= selling_price`).
     - `chk_products_stock`: Tồn kho và số lượng đã bán không âm (`stock_quantity >= 0 AND sold_quantity >= 0`).
     - `chk_products_weight` & `chk_products_moq`: Trọng lượng và số lượng đặt tối thiểu bắt buộc $> 0`.
     - `chk_orders_amounts` & `chk_orders_discounts`: Tổng tiền, tiền ship và giảm giá $\ge 0$.
     - `chk_vouchers_dates` & `chk_vouchers_usage`: `start_date < end_date` và `used_count <= usage_limit`.
     - `chk_wallet_available`, `pending`, `locked`: Chặn âm số dư ví người bán ở cấp độ engine InnoDB.
     - `chk_flash_sales_time` & `chk_fsi_stock`: Thời gian và giới hạn tồn kho flash sale chặt chẽ.
  2. **Cột Sinh Ảo Tự Động (Generated Virtual Columns):**
     - Bổ sung `products.discount_percentage`: Tính tự động phần trăm giảm giá `ROUND(((original_price - selling_price) / original_price) * 100)` tại nội tại DB, không tốn ổ đĩa và có index hỗ trợ lọc nhanh sản phẩm giảm sâu.
  3. **Mở rộng Triggers Bảo Vệ Toàn Vẹn Tài Chính & Danh Mục:**
     - `trg_prevent_negative_wallet`: Chặn tuyệt đối số âm trên `shop_wallets.available_balance`.
     - `trg_auto_sync_has_variants`: Tự động đồng bộ cờ `has_variants = TRUE` khi có phân loại hàng được thêm vào.
  4. **Khung Nhìn Tối Ưu Hiệu Năng (Native Database Views):**
     - `vw_active_products`: Khung nhìn sản phẩm đang mở bán kết hợp Shop, Thương hiệu, Danh mục và % giảm giá, triệt tiêu gánh nặng câu lệnh `JOIN` phức tạp ở backend.
     - `vw_seller_financial_summary`: Khung nhìn tóm tắt số dư, doanh thu và đơn hàng đã giao cho từng người bán.
  5. **Bộ Lập Lịch Tác Vụ Nội Tại (MySQL Event Scheduler):**
     - `evt_daily_cold_archive`: Tự động dời đơn cũ > 365 ngày sang Cold Tier vào 02:00 sáng mỗi ngày.
     - `evt_auto_update_flash_sale_status`: Quét mỗi phút tự động kích hoạt/đóng chiến dịch flash sale đúng giờ.
  6. **Hàm Tính Toán Nội Tại:**
     - Bổ sung `fn_is_product_in_stock`: Kiểm tra nhanh tồn kho khả dụng ngay trong câu query SQL.
* **Lý do thay đổi:** Tận dụng tối đa sức mạnh sẵn có của Hệ quản trị CSDL MySQL 8.0+, đưa các quy tắc kiểm tra tính toàn vẹn xuống tận tầng lưu trữ để bảo vệ dữ liệu chống lại cả các câu lệnh SQL chạy sai hoặc lỗi từ ứng dụng, đồng thời giảm tải tính toán cho CPU của Backend Server.
* **Chi tiết Trước & Sau:**
  * *Trước:* Việc kiểm tra giá âm, ví âm, tính phần trăm giảm giá và kiểm tra tồn kho phụ thuộc hoàn toàn vào tầng code Java Backend; thiếu Event Scheduler và Views nội tại.
  * *Sau:* MySQL tự bảo vệ toàn vẹn dữ liệu ở cấp độ engine InnoDB qua CHECK constraints, Triggers, Virtual Columns, Views và Events tự động hóa 100%.
* **Phạm vi tác động:** `backend/src/main/resources/db/chopee_schema_full.sql`, `docs/specs/05_database_specification.md`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261007-004] Tối Ưu Hóa Chỉ Mục Bao Phủ, Tìm Kiếm Tiếng Việt N-Gram & Tham Số Máy Chủ Sản Xuất
* **Ngày thực hiện:** 2026-10-07
* **Người thực hiện:** Principal Database Architect & Performance Engineer
* **Loại thay đổi:** `OPTIMIZED` / `ADDED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `CATALOG`, `ORDER`, `DOCS`
* **Mô tả thay đổi:**
  1. **Thiết kế Hệ thống Chỉ mục Bao phủ (Covering Indexes) Siêu Tốc:**
     - `idx_products_cat_sold (category_id, status, is_deleted, sold_quantity DESC)`: Cho phép truy vấn danh sách sản phẩm bán chạy theo danh mục phục vụ trực tiếp từ B-Tree index trong RAM, loại bỏ hoàn toàn việc đọc disk block.
     - `idx_products_shop_created (shop_id, status, is_deleted, created_at DESC)`: Tối ưu trang chi tiết gian hàng của người bán.
     - `idx_orders_user_created (user_id, created_at DESC)`: Đạt độ trễ $< 1\text{ms}$ khi khách phân trang xem lịch sử đơn.
     - `idx_orders_shop_status_created (shop_id, status, created_at DESC)`: Tăng tốc luồng lọc đơn hàng theo trạng thái của Seller.
     - `idx_vouchers_lookup (code, is_active, is_deleted, start_date, end_date)`: Hỗ trợ xác thực mã giảm giá tức thì trong 1 lần tra cứu.
     - `idx_cart_items_lookup (user_id, product_id, variant_id)`: Tối ưu kiểm tra trùng lặp món hàng trong giỏ.
  2. **Tối ưu Hóa Tìm Kiếm Tiếng Việt N-Gram (Vietnamese Full-Text Search):**
     - Cấu hình `/*!50100 WITH PARSER ngram */` trên chỉ mục `ft_products_search (name, description)` hỗ trợ phân tích từ ghép tiếng Việt có dấu/không dấu chuẩn xác mà không cần phụ thuộc vào ElasticSearch ở quy mô khởi đầu.
  3. **Thủ tục Bảo Trì & Dọn Dẹp Dữ Liệu Rác (Database Maintenance & Hygiene):**
     - Bổ sung `sp_cleanup_abandoned_carts(IN p_days_old INT)`: Tự động dọn dẹp các mục giỏ hàng bị bỏ quên quá hạn, bảo vệ Tầng Nóng (Hot Tier) không bị phình to.
     - Bổ sung `sp_defragment_and_analyze_tables()`: Tự động chạy `ANALYZE TABLE` chống phân mảnh index và cập nhật số liệu thống kê cho Cost-Based Optimizer.
  4. **Bổ sung Hàm & Khung Nhìn Nghiệp Vụ:**
     - `fn_calculate_voucher_discount`: Hàm nội tại tính toán chính xác tiền chiết khấu voucher theo % hoặc tiền cố định.
     - `vw_platform_daily_metrics`: Khung nhìn báo cáo chỉ số kinh doanh hàng ngày toàn sàn (GMV, số đơn, phí sàn).
  5. **Ban Hành File Cấu Hình Máy Chủ Sản Xuất (`my_production.cnf`):**
     - Xuất bản file cấu hình tinh chỉnh nhân MySQL 8.0 chuyên sâu tại `backend/src/main/resources/db/my_production.cnf` (Buffer pool 75% RAM, 1GB Redo log, `innodb_flush_log_at_trx_commit = 2`, NVMe IOPS 5000-10000).
  6. **Cung Cấp Kịch Bản Mẫu Phân Vùng Bảng `orders` Theo Tháng (Big Data Partitioning):**
     - Đưa mẫu DDL `PARTITION BY RANGE` theo tháng vào tài liệu để sẵn sàng kích hoạt khi dữ liệu sàn vượt mốc 10 triệu đơn hàng.
* **Lý do thay đổi:** Tối ưu hóa đến từng micro-giây cho các câu truy vấn có tần suất cao nhất của sàn TMĐT, đảm bảo hệ thống duy trì độ trễ cực thấp ngay cả trong các dịp bão sale lưu lượng lớn.
* **Chi tiết Trước & Sau:**
  * *Trước:* Các câu truy vấn lịch sử đơn hàng, danh mục bán chạy phải duyệt qua nhiều block dữ liệu đĩa; tìm kiếm từ khóa tiếng Việt cơ bản; thiếu thủ tục dọn dẹp giỏ hàng bỏ quên; chưa có file cấu hình production riêng.
  * *Sau:* Có hệ thống Covering Index đầy đủ, tìm kiếm N-gram tiếng Việt tối ưu, có thủ tục dọn dẹp định kỳ và file cấu hình máy chủ MySQL chuẩn doanh nghiệp.
* **Phạm vi tác động:** `backend/src/main/resources/db/chopee_schema_full.sql`, `backend/src/main/resources/db/my_production.cnf`, `docs/specs/05_database_specification.md`, `docs/specs/07_changelog.md`.

---

## [CHG-20261007-005] - Hoàn thiện Hệ thống API Backend: Sổ Địa Chỉ, Đánh Giá Sản Phẩm & Voucher Khuyến Mãi

* **Mã thay đổi:** `CHG-20261007-005`
* **Ngày thực hiện:** 07/10/2026
* **Người thực hiện:** Antigravity AI Assistant & Engineering Team
* **Loại thay đổi:** Feature / Architecture / API Expansion
* **Thành phần tác động:** `backend` (Address, Review, Voucher Modules, DataInitializer, Security & Testing)
* **Mô tả chi tiết:**
  1. **Phân hệ Sổ Địa Chỉ Nhận Hàng (FR-AUTH-04):**
     - Bổ sung `AddressController` (`/api/v1/buyer/addresses`) và `AddressService`.
     - Hỗ trợ thêm mới địa chỉ, xem danh sách địa chỉ sắp xếp mặc định lên đầu, đặt địa chỉ mặc định và xóa địa chỉ với cơ chế tự động chuyển mặc định.
  2. **Phân hệ Đánh Giá & Phản Hồi Sản Phẩm (FR-PROD-03):**
     - Bổ sung `ReviewController` và `ReviewService`.
     - `GET /api/v1/public/products/{productId}/reviews`: Xem đánh giá công khai kèm phân trang.
     - `POST /api/v1/buyer/reviews`: Người mua gửi đánh giá 1-5 sao kèm hình ảnh cho món hàng đã giao thành công (`DELIVERED`), tự động tính lại điểm `ratingAvg` và `reviewCount` của sản phẩm.
     - `GET /api/v1/seller/reviews`: Gian hàng xem danh sách đánh giá sản phẩm của shop.
     - `PUT /api/v1/seller/reviews/{id}/reply`: Gian hàng phản hồi đánh giá khách hàng với bảo vệ IDOR nghiêm ngặt.
  3. **Phân hệ Mã Giảm Giá & Khuyến Mãi (FR-PAY-03):**
     - Bổ sung `VoucherController` và `VoucherService`.
     - `GET /api/v1/public/vouchers`: Tra cứu danh sách voucher toàn sàn Chopee đang có hiệu lực.
     - `GET /api/v1/public/shops/{shopId}/vouchers`: Tra cứu voucher riêng của từng Shop.
     - `GET /api/v1/public/vouchers/validate`: API công khai kiểm tra tính hợp lệ và tính số tiền giảm giá tức thì.
     - `GET /api/v1/seller/vouchers` & `POST /api/v1/seller/vouchers`: Kênh người bán quản lý và tạo voucher riêng của shop.
     - `POST /api/v1/admin/vouchers`: Quản trị viên sàn tạo voucher toàn sàn.
  4. **Nâng Cấp Dữ Liệu Khởi Tạo Mẫu (`DataInitializer`):**
     - Tự động gieo mầm các mã giảm giá thực tế (`CHOPEE10K`, `FREESHIPCHO`, `DALATFARM20`, `TECHSALE50`) và địa chỉ nhận hàng mẫu.
  5. **Bảo Đảm Chất Lượng (Quality Gates):**
     - Bổ sung bộ kiểm thử tự động `AddressControllerTest`, `ReviewControllerTest`, `VoucherControllerTest`.
     - 60/60 bài kiểm thử backend (`mvn test`) vượt qua thành công 100%.
* **Lý do thay đổi:** Đồng bộ đầy đủ 100% các yêu cầu chức năng (FR-AUTH đến FR-AI) thành các endpoint RESTful chuẩn xác, sẵn sàng khởi chạy ứng dụng kết nối MySQL WAMP để người dùng kiểm thử trực tiếp.
* **Chi tiết Trước & Sau:**
  * *Trước:* Các entity `Review`, `Voucher`, `UserAddress` đã có trong database nhưng chưa có tầng Controller & Service RESTful tương ứng.
  * *Sau:* Hệ thống API hoàn chỉnh, bảo mật phân quyền Role rõ ràng, tích hợp Swagger OpenAPI UI chi tiết.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/**`, `backend/src/test/java/com/chopee/**`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261009-001] Chuẩn hóa Số Lượng Đặt Mua Số Nguyên & Mô hình Quy Cách Đóng Gói (Packaging Specifications)

* **Mã thay đổi:** `CHG-20261009-001`
* **Ngày thực hiện:** 2026-10-09
* **Người thực hiện:** Antigravity AI Assistant & Engineering Team
* **Loại thay đổi:** `REFACTORED` / `CHANGED`
* **Phân hệ ảnh hưởng:** `FRONTEND_UI`, `BACKEND_API`, `DATABASE`
* **Mô tả thay đổi:** Chuẩn hóa toàn bộ quy ước mua hàng trên sàn Chopee: Số lượng đặt mua luôn luôn là số nguyên (`1, 2, 3, 4...`). Các mặt hàng theo trọng lượng (`kg`), thể tích (`lít`), hay nông sản (`bó`, `khay`) được tổ chức thành các Quy cách đóng gói cụ thể (Túi 500g, Túi 1kg, Túi 2kg, Thùng 5kg; Chai 500ml, Can 2L, Can 5L; Bó tiêu chuẩn; Combo...) để khách hàng chọn quy cách và mua theo số lượng nguyên món.
* **Lý do thay đổi:**
  - Đáp ứng yêu cầu chuẩn mực từ Product Owner: Loại bỏ việc người dùng phải nhập hoặc bấm chọn số lượng lẻ thập phân (`0.5kg`, `1.5kg`).
  - Triệt tiêu hoàn toàn rủi ro sai số dấu phẩy động (IEEE-754 floating point arithmetic) khi tính toán tổng tiền, chiết khấu voucher sàn và phân tách đơn hàng đa người bán.
  - Phù hợp với thực tế vận hành thương mại điện tử thực phẩm: Thực phẩm tươi sống được đóng gói sẵn theo bịch/túi/hộp cố định để dán tem mã vạch và giao hỏa tốc.
* **Chi tiết Trước & Sau:**
  - *Trước:* Giao diện chi tiết sản phẩm và giỏ hàng có bộ cân bước nhảy `0.5kg`, khách chọn các số lẻ thập phân; giỏ hàng nhảy bước `0.5`; kênh người bán cấu hình `stepQuantity = 0.5`.
  - *Sau:*
    1. **Trang chi tiết sản phẩm (`ProductDetailPage.tsx`):**
       - Lưới chọn "Quy cách đóng gói & Phân loại" trực quan, liên kết trực tiếp với các biến thể thực tế trong CSDL (`Túi 500g (0.5kg)`, `Túi 1.0 kg (1 ký)`, `Túi 2.0 kg`, v.v.).
       - Khung giá cập nhật tức thì theo quy cách được chọn (`/ Túi 500g`, `/ Túi 1.0 kg`, `/ Chai 1L`...).
       - Bộ tăng giảm số lượng (`handleIncrease`, `handleDecrease`) luôn là số nguyên thuần túy (+1, -1, tối thiểu 1).
       - Nút chọn số lượng nhanh: 1, 2, 3, 5, 10 bịch/gói.
       - Tạm tính hiển thị chi tiết: `Tạm tính: X đ (Y × Tên quy cách)`.
    2. **Trang giỏ hàng (`CartPage.tsx`):**
       - Hiển thị nổi bật nhãn `Quy cách: [Tên biến thể]` (VD: `Quy cách: Túi 500g (0.5kg)`).
       - Nút tăng giảm số lượng strictly integer (+1, -1).
    3. **Kênh người bán (`SellerProductsPage.tsx`):**
       - Mặc định `stepQuantity = 1` và `minOrderQuantity = 1` cho mọi đơn vị tính.
       - Hướng dẫn rõ quy ước sàn cho người bán khi cấu hình đơn vị sản phẩm.
    4. **Dữ liệu mẫu (`DataInitializer.java`):**
       - Bổ sung biến thể đóng gói `Túi 2.0 kg` bên cạnh `Túi 500g` và `Túi 1.0 kg` cho toàn bộ thực phẩm tươi sống tính ký.
* **Phạm vi tác động:** `frontend/src/pages/ProductDetailPage.tsx`, `frontend/src/pages/CartPage.tsx`, `frontend/src/pages/seller/SellerProductsPage.tsx`, `backend/src/main/java/com/chopee/config/DataInitializer.java`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261009-002] Hệ Thống Ma Trận Phân Loại 2 Cấp (Multi-Tier Variant Matrix), Nhập Hàng Siêu Tốc & Nhân Bản Sản Phẩm

* **Mã thay đổi:** `CHG-20261009-002`
* **Ngày thực hiện:** 2026-10-09
* **Người thực hiện:** Antigravity AI Assistant & Engineering Team
* **Loại thay đổi:** `ADDED` / `CHANGED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `BACKEND_API`, `FRONTEND_UI`, `DOCS`
* **Mô tả thay đổi:**
  Triển khai toàn diện Hệ thống Ma trận Phân loại Đa ngành 2 cấp (Multi-tier Variant Matrix), Bộ sinh biến thể Cartesian tự động, Thanh áp dụng giá & tồn kho hàng loạt (Bulk Apply Bar), Bộ mẫu 1-click theo ngành hàng (Industry Presets), Tính năng nhân bản sản phẩm 1-click (Duplicate Product kèm bảo vệ IDOR), và Giao diện người mua chọn 2 tầng nút bấm chuẩn Shopee.
* **Lý do thay đổi:**
  - Giải quyết triệt để yêu cầu và nỗi đau của người bán: Phân biệt rõ ràng giữa Phân loại (Loại tươi/khô, Màu sắc, RAM/ROM, Trọng lượng bao 5kg/10kg, Layout bàn phím, Switch...) và Số lượng mua (luôn là số nguyên 1, 2, 3... bịch/gói/chiếc).
  - Khắc phục triệt để tình trạng người bán phải nhập hàng trăm mã hàng thủ công: Giúp người bán tạo hàng chục/hàng trăm biến thể trong vài giây nhờ bộ sinh ma trận Cartesian và thanh điền giá/tồn kho đồng loạt.
  - Cho phép người bán nhân bản sản phẩm có cấu trúc tương tự chỉ với 1 click (sao chép toàn bộ thuộc tính, biến thể và bảo vệ chống tấn công IDOR giữa các Shop).
* **Chi tiết Trước & Sau:**
  - *Trước:*
    - Bảng `products` chỉ có các trường đơn lẻ, `product_variants` chỉ có `variant_name`, `price`, `stock_quantity`.
    - Người bán phải nhập từng biến thể một cách thủ công, không có mẫu ngành hàng, không có áp dụng giá/kho hàng loạt, không có nhân bản sản phẩm.
    - Khách hàng xem sản phẩm chỉ thấy 1 hàng nút biến thể phẳng dài, khó phân biệt giữa các nhóm thuộc tính (ví dụ Loại vs Kích cỡ đóng gói).
  - *Sau:*
    1. **CSDL & Entity Backend:**
       - `products`: Bổ sung cột `tier_variation` (`TEXT` JSON) lưu cấu trúc 2 tầng phân loại (chuẩn Shopee).
       - `product_variants`: Bổ sung `sku` (`VARCHAR(100)`) và `attributes` (`TEXT` JSON).
       - Cập nhật `chopee_schema_full.sql` và `DataInitializer.java` đồng bộ dữ liệu mẫu đa ngành (Rau củ VietGAP, Bàn phím cơ Keychron, Áo thun thời trang).
    2. **Backend API & Bảo mật:**
       - `CreateProductRequest`, `UpdateProductRequest`, `ProductDetailResponse`, `ProductVariantResponse` hỗ trợ `tierVariation`, `sku`, `attributes`.
       - Endpoint mới `POST /api/v1/seller/products/{id}/duplicate`: Nhân bản toàn bộ thông tin sản phẩm và biến thể kèm kiểm tra quyền sở hữu IDOR nghiêm ngặt.
       - Bộ test `SellerSecurityTest` kiểm thử đầy đủ kịch bản nhân bản thành công và chặn IDOR trái phép (403 Forbidden).
    3. **Giao diện Người Bán (`SellerProductsPage.tsx`):**
       - Mẫu 1-Click theo 8 ngành hàng: 🥬 Rau củ, 🥩 Thịt cá tươi, 🌾 Gạo / Nông sản, 📱 Điện thoại, ⌨️ Bàn phím cơ, 🎧 Tai nghe, 🧃 Đồ uống, 👕 Thời trang.
       - Bộ sinh ma trận Cartesian 2 cấp tự động ($N \times M$) khi thêm/bớt tùy chọn.
       - Thanh áp dụng hàng loạt (Bulk Apply Bar): 1-click áp dụng Giá bán và Tồn kho cho toàn bộ ma trận biến thể.
       - Nút Nhân bản (Copy) và Chỉnh sửa (Edit) trực tiếp trong danh sách sản phẩm.
    4. **Giao diện Người Mua (`ProductDetailPage.tsx`):**
       - Render 2 tầng nút bấm độc lập (Tầng 1: Loại/Màu sắc; Tầng 2: Quy cách/Cấu hình).
       - Tự động bắt cặp biến thể, cập nhật giá bán, tồn kho tối đa, mã SKU tức thì.
       - Bảo toàn quy ước số lượng đặt mua luôn là số nguyên (`1, 2, 3...`).
* **Phạm vi tác động:**
  - `backend/src/main/java/com/chopee/entity/Product.java`
  - `backend/src/main/java/com/chopee/entity/ProductVariant.java`
  - `backend/src/main/resources/db/chopee_schema_full.sql`
  - `backend/src/main/java/com/chopee/config/DataInitializer.java`
  - `backend/src/main/java/com/chopee/modules/seller/**`
  - `backend/src/main/java/com/chopee/modules/catalog/**`
  - `frontend/src/types/index.ts`
  - `frontend/src/services/api.ts`
  - `frontend/src/pages/seller/SellerProductsPage.tsx`
  - `frontend/src/pages/ProductDetailPage.tsx`
  - `docs/specs/07_changelog.md`

---

### 📌 [CHG-20261009-003] Hoàn tất Khởi tạo & Làm mới Dữ liệu Mẫu Ma Trận Biến Thể Đa Tầng Toàn Diện (Full Multi-Tier Variant Seeding)
* **Ngày thực hiện:** 2026-10-09
* **Người thực hiện:** Lead Architect & Fullstack Engineer
* **Loại thay đổi:** `UPDATED`
* **Phân hệ ảnh hưởng:** `DATABASE`, `SEED_DATA`, `CATALOG`
* **Mô tả thay đổi:**
  1. Cập nhật và kích hoạt bộ dữ liệu mẫu đa tầng (Multi-tier Variant Matrix) chuẩn mã hóa UTF-8 tiếng Việt hoàn chỉnh trong `DataInitializer.java`:
     - 🥬 **Rau củ quả:** Cà chua beef Đà Lạt (Tươi / Sấy khô × Túi 500g / 1kg / 2kg), Dưa leo baby VietGAP (Tươi giòn / Ngâm chua ngọt × Túi 500g / 1kg / 2kg).
     - 🥩 **Thịt sạch:** Thịt ba chỉ CP (Ba chỉ rút sườn / Sườn non heo / Nạc dăm × Khay 300g / 500g / 1kg).
     - 🦐 **Hải sản tươi sống:** Tôm sú Cà Mau (Sống bơi oxy / Cấp đông nguyên con × Hộp 500g / Hộp 1kg).
     - 🌾 **Lương thực:** Gạo ST25 Ông Cua Thượng Hạng (ST25 Lúa Tôm / Gạo Lứt Đỏ × Túi 1kg / Bao 5kg / Bao 10kg / Bao 25kg).
     - 📱 **Điện thoại:** iPhone 15 Pro Max VN/A (Titan Tự Nhiên / Đen Midnight / Trắng Starlight × 256GB / 512GB / 1TB).
     - ⌨️ **Bàn phím cơ:** Keychron K2 Pro (Layout 75% / TKL 87 phím × Red Switch / Brown Switch / Blue Switch).
     - 🖱️ **Chuột công thái học:** Logitech MX Master 3S (Đen Xám Graphite / Trắng Xám Pale Grey).
     - 🎧 **Tai nghe cao cấp:** Sony WH-1000XM5 (Đen Nhám / Bạc Ánh Kim / Xanh Navy).
     - 🍳 **Gia dụng thông minh:** Nồi chiên không dầu Philips HD9252 (Bản 4.1L / Bản 6.2L XXL × Đen bóng / Trắng ngọc trai).
     - 👕 **Thời trang:** Áo thun nam UniStyle (Trắng Basic / Đen Tuyền / Xám Tiêu × Size M / L / XL), Balo laptop chống sốc Oxford (Đen / Xám / Xanh × Bản 14 inch / 16 inch).
     - 🥤 **Đồ uống & Giải khát:** Thùng Bia Tiger Crystal & Coca-Cola (Lon lẻ 320ml / Lốc 6 lon / Thùng 24 lon).
  2. Bổ sung cơ chế an toàn dọn dẹp ràng buộc khóa ngoại (foreign key decoupling) với `cart_items` và `order_items` khi làm mới biến thể nhằm chống xung đột toàn vẹn dữ liệu.
  3. Xử lý triệt để hiện tượng sai mã ký tự console (CP1258/ANSI vs UTF-8) thông qua kết nối Hibernate Session JPA nội bộ.
  4. Đạt 100% kiểm thử: `mvn test` (62/62 PASS) và `npm run build` (PASS).
* **Lý do thay đổi:** Cung cấp trải nghiệm thực tế, sinh động và đầy đủ cho người mua lẫn người bán theo đúng yêu cầu nghiệp vụ về tách bạch giữa thuộc tính phân loại (loại, bao bì, dung lượng, màu sắc...) và số lượng mua dạng số nguyên.
* **Chi tiết Trước & Sau:**
  * *Trước:* Dữ liệu biến thể mẫu chỉ có dạng 1 tầng hoặc một số sản phẩm chưa có ma trận biến thể.
  * *Sau:* 100% danh mục trọng điểm đều sở hữu ma trận biến thể 2 cấp thực tế chuẩn hóa theo ngành hàng.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/config/DataInitializer.java`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261009-004] Tinh Chỉnh Giao Diện Chi Tiết Sản Phẩm Chuẩn Shopee (Standard Variation Buttons & Clean Stepper)
* **Ngày thực hiện:** 2026-10-09
* **Người thực hiện:** Frontend Lead & UI/UX Specialist
* **Loại thay đổi:** `IMPROVED`
* **Phân hệ ảnh hưởng:** `FRONTEND_UI`, `PRODUCT_DETAIL`
* **Mô tả thay đổi:**
  1. Loại bỏ toàn bộ các chuỗi đơn vị rườm rà ("1 chiếc", "1 túi", v.v.) đính kèm lộn xộn trong hộp giá bán và các nút bấm.
  2. Xóa bỏ hoàn toàn thanh chọn nhanh số lượng nhân tạo ("Chọn nhanh số lượng: 1 túi, 2 túi, 3 túi...") và dòng "Tạm tính (1 × 1 túi)" không đúng thiết kế sàn Shopee.
  3. Tái cấu trúc giao diện theo chuẩn Shopee Marketplace:
     - Khung giá tinh giản: Hiển thị giá bán chính xác dạng tiền tệ, giá gốc gạch ngang và huy hiệu giảm giá phần trăm.
     - Phân loại hàng (Mẫu / Màu sắc / Kích cỡ / Quy cách): Thiết kế hàng ngang với nhãn căn trái cố định (`w-24 md:w-28 text-gray-500`), các nút bấm biến thể dạng thẻ hình chữ nhật bo góc với góc đánh dấu tam giác đỏ và dấu tích trắng đặc trưng của Shopee khi được chọn.
     - Ô chọn số lượng chuẩn: Gồm nút trừ `[-]`, ô nhập số nguyên `[ 1 ]`, nút cộng `[+]` và số lượng tồn kho kế bên ("xxx sản phẩm có sẵn").
     - Hàng nút thao tác chuẩn sàn: Nút `Thêm Vào Giỏ Hàng` (nền cam nhạt, viền cam, icon giỏ hàng) và nút `Mua Ngay` (nền cam đậm đặc trưng Shopee).
     - Hàng huy hiệu đảm bảo: "15 Ngày Đổi Trả Miễn Phí", "Chính Hãng 100%", "Miễn Phí Vận Chuyển".
* **Lý do thay đổi:** Phản hồi người dùng về việc giao diện hiển thị đơn vị số lượng lộn xộn, khác biệt so với trải nghiệm mua hàng chuẩn của Shopee.
* **Chi tiết Trước & Sau:**
  * *Trước:* Hiển thị đơn vị ghép chuỗi "1 chiếc", "1 túi" trong ô giá và nút bấm, có thanh chọn nhanh 1, 2, 3 túi làm rối mắt.
  * *Sau:* Giao diện chuẩn xác 100% theo giao diện Shopee như hình ảnh tham chiếu của người dùng.
* **Phạm vi tác động:** `frontend/src/pages/ProductDetailPage.tsx`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261009-005] Đồng Bộ Dữ Liệu Giỏ Hàng Đa Cửa Hàng & Khắc Phục Lỗi Hiển Thị Giỏ Hàng Rỗng (Cart State Sync & Multi-Shop Response Normalization)
* **Ngày thực hiện:** 2026-10-09
* **Người thực hiện:** Fullstack Lead & Frontend Specialist
* **Loại thay đổi:** `FIXED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `FRONTEND_UI`, `CART_MODULE`
* **Mô tả thay đổi:**
  1. **Khắc phục lỗi phân rã dữ liệu giỏ hàng trong `useCartStore.ts`**:
     - Cập nhật hàm xử lý phản hồi giỏ hàng (`parseCartResponse`) giải nén chính xác danh sách các nhóm gian hàng `response.data.shops` thành mảng `flatItems: CartItem[]` kèm đầy đủ thông tin cửa hàng (`shopId`, `shopName`, `shopSlug`), đơn giá (`unitPrice` / `sellingPrice`), thành tiền (`itemSubtotal` / `subtotal`), và biến thể sản phẩm.
     - Đồng bộ hóa các thao tác `addToCart`, `updateQuantity`, `removeItem` bằng cách gọi lại `await get().fetchCart()` sau mỗi tác vụ thành công nhằm đảm bảo trạng thái giỏ hàng ở Client và CSDL luôn đồng bộ 100%.
  2. **Nâng cấp tính tương thích kép cho Backend API Cart DTOs**:
     - `CartResponse.java`: Bổ sung danh sách phẳng `items` (`List<CartItemResponse>`) song song với nhóm cửa hàng `shops` (`List<ShopCartGroupResponse>`), kèm getter bí danh `@JsonProperty("totalAmount")` trỏ về `grandTotal`.
     - `CartItemResponse.java`: Bổ sung các trường `shopId`, `shopName`, `shopSlug` cùng các getter bí danh `@JsonProperty("sellingPrice")` và `@JsonProperty("subtotal")`.
     - `CartService.java`: Nạp đầy đủ thông tin cửa hàng và danh sách tổng `allItems` vào `CartResponse` trong cả `getCart` và `mapToItemResponse`.
  3. **Khắc phục logic chọn sản phẩm thanh toán trong `CartPage.tsx`**:
     - Sử dụng biến cờ `hasInitializedSelection` để chỉ tự động chọn toàn bộ sản phẩm trong lần tải đầu tiên, duy trì trạng thái chọn của người dùng khi cập nhật số lượng thay vì bị chọn lại hoặc mất dấu.
* **Lý do thay đổi:** Khắc phục triệt để lỗi người dùng thêm sản phẩm vào giỏ hàng thành công nhưng khi vào trang `/cart` lại thấy giỏ trống rỗng do cấu trúc DTO backend (`shops`) không khớp với logic bóc tách cũ (`items`) ở frontend.
* **Chi tiết Trước & Sau:**
  * *Trước:* Khi gọi `addToCart` hoặc `fetchCart`, frontend truy cập `response.data.items` dẫn đến `undefined`, gán `items: []` làm sạch giỏ hàng trên giao diện mặc dù CSDL backend đã lưu bản ghi.
  * *Sau:* Thêm vào giỏ hàng hiển thị ngay lập tức trên badge thanh điều hướng (`Header`), trang `/cart` hiển thị đầy đủ từng món hàng gom nhóm chuẩn xác theo từng cửa hàng, cho phép tăng giảm số lượng số nguyên `[-] [1] [+]` và tính thành tiền mượt mà.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/cart/dto/CartResponse.java`, `backend/src/main/java/com/chopee/modules/cart/dto/CartItemResponse.java`, `backend/src/main/java/com/chopee/modules/cart/CartService.java`, `frontend/src/stores/useCartStore.ts`, `frontend/src/pages/CartPage.tsx`, `frontend/src/services/api.ts`, `frontend/src/types/index.ts`, `docs/specs/07_changelog.md`.

---

### 📌 [CHG-20261010-001] Đồng Bộ Dữ Liệu Thanh Toán & Khắc Phục Lỗi Tổng Tiền 0đ (Checkout Cost Normalization & Selected Items Flow)
* **Ngày thực hiện:** 2026-10-10
* **Người thực hiện:** Fullstack Lead & Frontend Specialist
* **Loại thay đổi:** `FIXED`
* **Phân hệ ảnh hưởng:** `BACKEND_API`, `FRONTEND_UI`, `ORDER_MODULE`
* **Mô tả thay đổi:**
  1. **Khắc phục lỗi lệch tên thuộc tính DTO trong Checkout Preview**:
     - `CheckoutPreviewResponse.java` và `ShopCheckoutPreview.java`: Bổ sung Jackson getter bí danh (`@JsonProperty("groupSubtotal")`, `@JsonProperty("finalTotalAmount")`, `@JsonProperty("totalDiscount")`, `@JsonProperty("subOrders")`, `@JsonProperty("shopSubtotal")`, `@JsonProperty("shippingFee")`, `@JsonProperty("shopTotal")`) tương thích kép với toàn bộ các tên trường mà frontend gọi.
     - `frontend/src/types/index.ts`: Bổ sung các trường bí danh (`totalItemsAmount`, `grandFinalAmount`, `totalDiscountAmount`, `shops`) cho `CheckoutPreviewResponse` và `SubOrderPreview`.
  2. **Tái thiết kế cơ chế tính toán chi phí trang Thanh toán (`CheckoutPage.tsx`)**:
     - Triển khai công thức tính toán dự phòng thông minh (Reactive Fallback): Tiền hàng, phí ship (15k tiêu chuẩn / 25k hỏa tốc), giảm giá voucher shop, giảm giá voucher sàn và tổng thanh toán luôn được tính toán tự động dựa trên các món hàng được chọn ngay cả khi preview máy chủ đang tải hoặc có độ trễ, đảm bảo giao diện **tuyệt đối không bao giờ hiển thị 0đ**.
     - Nhận danh sách các món hàng được chọn (`selectedItemIds`) từ trang Giỏ hàng (`CartPage.tsx`), lọc chính xác các món hàng và nhóm shop tương ứng để thanh toán thay vì gom toàn bộ giỏ.
     - Sửa lỗi truyền `orderAmount` vào modal Voucher sàn: Truyền đúng tổng tiền hàng thay vì `0đ`, cho phép áp dụng voucher sàn thành công.
  3. **Đồng bộ API Tạo Đơn Hàng & Dọn Giỏ Hàng Thông Minh**:
     - `OrderService.java`: Thêm cơ chế an toàn trong `fetchEligibleCartItems` (tự động fallback về toàn bộ giỏ nếu danh sách ID lọc bị rỗng).
     - Khi đặt hàng thành công: Truyền `cartItemIds` chính xác để backend chỉ trừ tồn kho và xóa các món đã đặt khỏi giỏ, giữ lại các món chưa được chọn mua.
* **Lý do thay đổi:** Khắc phục lỗi người mua vào trang thanh toán chọn đủ các phương thức và voucher nhưng tổng thanh toán hiển thị 0đ do lệch tên trường dữ liệu giữa backend (`totalItemsAmount`, `grandFinalAmount`) và frontend (`groupSubtotal`, `finalTotalAmount`).
* **Chi tiết Trước & Sau:**
  * *Trước:* Trang thanh toán hiển thị "Tổng tiền hàng: 0đ", "Tổng thanh toán: 0đ", modal voucher sàn báo lỗi không đủ giá trị đơn tối thiểu.
  * *Sau:* Trang thanh toán hiển thị chính xác 100% tiền hàng, tiền ship theo từng shop và phương thức giao hàng, chiết khấu voucher hiển thị rõ ràng, tổng thanh toán cập nhật tức thì và chuyển hướng thanh toán VNPay/COD chuẩn xác.
* **Phạm vi tác động:** `backend/src/main/java/com/chopee/modules/order/dto/CheckoutPreviewResponse.java`, `backend/src/main/java/com/chopee/modules/order/dto/ShopCheckoutPreview.java`, `backend/src/main/java/com/chopee/modules/order/OrderService.java`, `frontend/src/types/index.ts`, `frontend/src/services/api.ts`, `frontend/src/pages/CartPage.tsx`, `frontend/src/pages/CheckoutPage.tsx`, `docs/specs/07_changelog.md`.















