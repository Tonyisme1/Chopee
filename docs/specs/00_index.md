# HỆ THỐNG TÀI LIỆU ĐẶC TẢ HỆ THỐNG SÀN CHỢ ĐIỆN TỬ (SHOPEE CLONE)
## Software Requirements & Architecture Specification (SRS)

- **Tên dự án:** Sàn Thương Mại Điện Tử Đa Ngành Hàng & Thực Phẩm Tươi Sống (Chợ Online)
- **Công nghệ nền tảng:** Java 17 + Spring Boot 3.x, React + Vite + TypeScript, MySQL 8
- **Phiên bản tài liệu:** v1.1.0
- **Trạng thái:** Đã phê duyệt (Approved)

---

## Danh mục Tài liệu Đặc tả Chi tiết

Tài liệu đặc tả hệ thống được phân rã thành các tập tin chuyên biệt theo chuẩn kỹ nghệ phần mềm (IEEE 830 / ISO 29148) để tiện theo dõi, bảo trì và kiểm soát thay đổi:

| STT | Tập tin tài liệu | Mô tả nội dung chính |
| :---: | :--- | :--- |
| **01** | [01_introduction.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/01_introduction.md) | Giới thiệu dự án, bối cảnh, mục tiêu, đối tượng sử dụng & nguồn tài liệu tham chiếu |
| **02** | [02_functional_requirements.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/02_functional_requirements.md) | Đặc tả chi tiết toàn bộ yêu cầu chức năng (FR) cho Buyer, Seller, Admin và AI Shopping Copilot |
| **03** | [03_non_functional_requirements.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/03_non_functional_requirements.md) | Yêu cầu phi chức năng (NFR): Hiệu năng, Bảo mật, Tính toàn vẹn ACID, Tính khả dụng & Mở rộng |
| **04** | [04_system_diagrams.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/04_system_diagrams.md) | Tổng hợp các loại sơ đồ hệ thống: Kiến trúc tổng thể, Use Case, Sequence Diagram, State Machine |
| **05** | [05_database_specification.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/05_database_specification.md) | Thiết kế CSDL, Data Dictionary, thuộc tính động JSON và Sơ đồ quan hệ thực thể (ERD) |
| **06** | [06_test_plan.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/06_test_plan.md) | Kế hoạch kiểm thử, ma trận kiểm thử (Test Matrix), Test Cases & Kịch bản nghiệm thu |
| **07** | [07_changelog.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/07_changelog.md) | Nhật ký thay đổi hệ thống (Change Log) được gán Change Key độc nhất (`CHG-xxx`) theo dõi lịch sử cập nhật |

---

## Quy Chuẩn Phát Triển & Luật Dự Án
Bắt buộc đọc và tuân thủ các quy tắc kiểm duyệt trước khi viết code và commit:
* 📜 [PROJECT_RULES.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/PROJECT_RULES.md): Quy định về Cổng kiểm duyệt pre-commit, kiểm tra IDOR, bảo vệ tồn kho và quy chuẩn đặt tên RESTful / React.
* 🤖 [AGENTS.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/AGENTS.md): Luật vận hành bắt buộc dành cho AI Assistant và automated workflows.

---

## Hướng dẫn cập nhật tài liệu
Mỗi khi có bất kỳ thay đổi nào liên quan đến kiến trúc, cơ sở dữ liệu hoặc luồng nghiệp vụ:
1. Cập nhật trực tiếp vào file tài liệu tương ứng (`01` đến `06`).
2. Ghi nhận một bản ghi thay đổi mới vào file [07_changelog.md](file:///d:/04_Code_Projects/Du_An/Demo_Quan_Ly/Quan_Ly_Cho_Online/docs/specs/07_changelog.md) kèm theo mã định danh `CHG-YYYYMMDD-XXX`.

