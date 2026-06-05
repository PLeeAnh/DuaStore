# DuaStore — Website Bán Đồ Thủy Tinh Decor

**FPT Polytechnic Hải Phòng** — Nhóm 404 Not Found  
GVHD: **Nguyễn Khánh Lâm** · Công nghệ: **Java Spring Boot + SQL Server**

---

## Yêu cầu

- Java 17+, SQL Server 2019+, Maven 3.8+
- Chạy script `DuaStore_Database.sql` trước lần đầu

## Chạy

```bash
git clone <repo-url>
# Sửa application.properties: username/password SQL Server
mvn spring-boot:run
# Client: http://localhost:8080/
# Admin:  http://localhost:8080/admin
```

## Cấu hình quan trọng

| Key | Giá trị | Ghi chú |
|-----|---------|---------|
| `ddl-auto` | `update` | Hibernate tự đồng bộ bảng |
| `spring.profiles.active` | `local` | Đọc `application-local.properties` nếu có |
| UTF-8 | 4 dòng `server.servlet.encoding.*` | Fix font tiếng Việt |
| Upload | `file:uploads/` | Ảnh lưu ở thư mục `uploads/` ngoài JAR |

## Nhóm

| TV | Vai trò | Phụ trách |
|----|---------|-----------|
| **PLA** | Nhóm trưởng | Products, Variants, Image, Security, Dashboard, UI |
| **NHD** | Backend | Orders, OrderItems |
| **TK** | Backend | Categories, CartItems, Checkout |
| **BTM** | Backend | Promotions, Reviews, Posts |
| **NXK** | Backend | Addresses, Wishlist |

## Video Review

<!-- Chèn link video review/ demo trang web ở đây -->
<!-- VD: [Xem video] (https://...) -->

## Lưu ý

- Spring Security thêm sau cùng, khi hoàn thành CRUD
- Không commit mật khẩu thật — dùng `.env` hoặc `application-local.properties`
- Database mẫu: `DuaStore_Database.sql` (12 bảng, 3 View, dữ liệu mẫu)
