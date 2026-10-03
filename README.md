# 🛒 Chopee - Sàn Thương Mại Điện Tử Đa Ngành Hàng & Chợ Trực Tuyến

<p align="center">
  <b>Nền tảng Chợ Trực Tuyến Đa Người Bán (Multi-Vendor Marketplace) kết hợp Thực phẩm Tươi sống & Trợ lý AI Mua sắm Toàn năng.</b>
</p>

---

## 🌟 Điểm nổi bật của Dự án
- **Mô hình Chợ Đa Người Bán (Multi-vendor Shopee Clone):** Hỗ trợ khách hàng mua sắm từ nhiều shop khác nhau trên cùng 1 giỏ hàng, tự động tách đơn độc lập theo từng shop khi checkout.
- **Đa ngành hàng & Thực phẩm tươi sống:** Hỗ trợ sản phẩm bán lẻ theo cân (`kg`, `g`, `bó`), bảo quản lạnh (`FRESH`, `FROZEN`), đơn hàng giao hỏa tốc 2h (`EXPRESS_FRESH`), và quy cách đóng thùng/lốc đồ uống lớn.
- **Trợ lý AI Mua sắm (AI Copilot):** Tích hợp AI tư vấn thực đơn đi chợ, so sánh thông số kỹ thuật công nghệ/gia dụng và hiển thị Thẻ Sản phẩm có nút mua trực tiếp trong chat.
- **Thanh toán tích hợp:** Hỗ trợ COD và Cổng thanh toán VNPay Sandbox (mã hóa HMAC-SHA512).
- **Kiến trúc chuẩn Doanh nghiệp:** Spring Boot 3 + Java 17, Spring Security 6 Stateless JWT, React Vite TypeScript, MySQL 8 InnoDB ACID.

---

## 📚 Hệ thống Tài liệu Đặc tả (Specifications)
Hệ thống tài liệu được phân rã chi tiết theo chuẩn IEEE 830 tại thư mục [`docs/specs/`](./docs/specs/):
1. [00_index.md](./docs/specs/00_index.md) - Mục lục tài liệu đặc tả
2. [01_introduction.md](./docs/specs/01_introduction.md) - Giới thiệu, bối cảnh & tài liệu tham chiếu
3. [02_functional_requirements.md](./docs/specs/02_functional_requirements.md) - Yêu cầu chức năng (FR)
4. [03_non_functional_requirements.md](./docs/specs/03_non_functional_requirements.md) - Yêu cầu phi chức năng (NFR)
5. [04_system_diagrams.md](./docs/specs/04_system_diagrams.md) - Tổng hợp các sơ đồ hệ thống (Use Case, Sequence, State Machine)
6. [05_database_specification.md](./docs/specs/05_database_specification.md) - Đặc tả CSDL & Sơ đồ ERD
7. [06_test_plan.md](./docs/specs/06_test_plan.md) - Kế hoạch kiểm thử & nghiệm thu
8. [07_changelog.md](./docs/specs/07_changelog.md) - Nhật ký thay đổi (Change Keys)

---

## 🛠️ Tech Stack
- **Backend:** Java 17, Spring Boot 3.3.x, Spring Data JPA, Spring Security 6, JJWT, Springdoc OpenAPI (Swagger), MySQL 8.
- **Frontend:** React 18/19, Vite, TypeScript, Tailwind CSS, Lucide Icons, Zustand, Axios.
- **Third-party Services:** VNPay Sandbox, Google Gemini 1.5 Flash API.
