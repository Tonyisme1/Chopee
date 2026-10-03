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

