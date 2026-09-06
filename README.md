# DuaStore

**DuaStore** là hệ thống thương mại điện tử chuyên bán các sản phẩm thủy tinh (chai, hũ, bình trang trí, ly & cốc, quà tặng thủy tinh) — xây dựng bằng **Spring Boot 4** (Java 21), giao diện Thymeleaf, cơ sở dữ liệu **SQL Server**.

Dự án gồm 2 phần trong cùng một ứng dụng:

- **Client** — trang bán hàng cho khách (duyệt sản phẩm, giỏ hàng, đặt hàng, thanh toán VNPay/COD, đánh giá, yêu thích, voucher, blog...).
- **Admin** — trang quản trị cho nhân viên/cửa hàng (quản lý sản phẩm, đơn hàng, khuyến mãi, hoàn trả, người dùng & phân quyền, thống kê...).

---

## Mục lục

- [Tính năng chính](#tính-năng-chính)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Hướng dẫn cài đặt & chạy dự án](#hướng-dẫn-cài-đặt--chạy-dự-án)
- [Tài khoản demo](#tài-khoản-demo)
- [Tài liệu liên quan](#tài-liệu-liên-quan)
- [Ghi chú quan trọng](#ghi-chú-quan-trọng)

---

## Tính năng chính

### Phía khách hàng (Client)

- Đăng ký / đăng nhập (mật khẩu, xác thực 2 lớp 2FA, đăng nhập nhanh qua Google OAuth2)
- Tìm kiếm, lọc và xem chi tiết sản phẩm theo danh mục, biến thể (dung tích, kiểu nắp...)
- Giỏ hàng, lưu giỏ hàng, danh sách yêu thích (wishlist)
- Đặt hàng, thanh toán qua **VNPay** hoặc COD, theo dõi trạng thái đơn hàng theo thời gian thực
- Áp dụng mã khuyến mãi / voucher, tích điểm thành viên (loyalty points)
- Yêu cầu hoàn trả / đổi hàng kèm hình ảnh, video minh chứng (quy trình riêng cho hàng thủy tinh dễ vỡ)
- Đánh giá sản phẩm kèm hình ảnh
- Đọc bài viết / blog hướng dẫn, tin tức
- Gửi tin nhắn liên hệ tới cửa hàng

### Phía quản trị (Admin)

- Quản lý sản phẩm, biến thể, danh mục, hình ảnh; lịch sử thay đổi giá
- Quản lý đơn hàng: xác nhận, phân công xử lý, cập nhật trạng thái, phát hiện gian lận (fraud detection)
- Quản lý khuyến mãi, voucher, chương trình **Flash Sale**
- Xử lý yêu cầu hoàn trả / đổi hàng
- Quản lý người dùng, vai trò (Role) và **phân quyền chi tiết theo module** (RBAC — 83 quyền hạn)
- Kiểm duyệt đánh giá sản phẩm
- Quản lý nội dung: bài viết, banner trang chủ, banner popup, liên kết chân trang
- Thống kê doanh thu, dashboard, phân tích hành vi người dùng
- Nhật ký thao tác quản trị (audit log), thông báo realtime qua WebSocket
- Cấu hình cửa hàng, chính sách hoàn trả, cấu hình hệ thống (SiteSettings)

---

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ / Runtime | Java 21 |
| Framework | Spring Boot 4.0.6 (Spring MVC, Spring Data JPA, Spring Security, Spring WebSocket) |
| Giao diện | Thymeleaf + Thymeleaf Layout Dialect |
| Cơ sở dữ liệu | Microsoft SQL Server (chính thức) — có driver H2/PostgreSQL cho dev/preview |
| Xác thực | Spring Security, OAuth2 Client (Google Login), 2FA (TOTP + mã QR qua ZXing) |
| Thanh toán | VNPay (sandbox), chuyển khoản VietQR |
| Vận chuyển | Tích hợp API Giao Hàng Nhanh (GHN), hỗ trợ đa đơn vị vận chuyển |
| Email | Gmail SMTP (xác nhận đơn hàng, OTP, thông báo) |
| Bảo mật nội dung | Jsoup (HTML Sanitizer chống XSS) |
| Giám sát / Docs | Spring Boot Actuator, springdoc-openapi (Swagger UI) |
| Build tool | Maven (kèm Maven Wrapper `mvnw`) |
| Khác | Lombok, Jackson, Spring Cache |

---

## Cấu trúc thư mục

```
DuaStore/
├── pom.xml                                # Cấu hình Maven, khai báo dependency
├── mvnw, mvnw.cmd                         # Maven Wrapper (không cần cài Maven riêng)
├── DuaStore_TaiLieu_DB_UseCase.docx       # Tài liệu thiết kế DB + đặc tả Use Case
├── src/main/java/com/duastore/
│   ├── DuaStoreApplication.java           # Điểm khởi động ứng dụng
│   ├── config/                            # Cấu hình Spring (Security, WebSocket, CORS, Cache...)
│   │   └── security/                      # Cấu hình xác thực, phân quyền, OAuth2, rate limiting
│   ├── controller/
│   │   ├── admin/                         # Controller trang quản trị
│   │   ├── client/                        # Controller trang khách hàng
│   │   └── api/                           # REST API nội bộ (AJAX, tìm kiếm, gợi ý...)
│   ├── dto/                               # Data Transfer Object (form, API request/response)
│   ├── model/                             # JPA Entity (ánh xạ trực tiếp với DB)
│   │   └── enums/                         # Enum dùng chung
│   ├── repository/                        # Spring Data JPA Repository
│   ├── scheduler/                         # Tác vụ định kỳ (tự huỷ đơn, hết hạn voucher, điểm...)
│   ├── service/                           # Nghiệp vụ dùng chung
│   │   ├── admin/                         # Nghiệp vụ riêng cho quản trị
│   │   └── client/                        # Nghiệp vụ riêng cho khách hàng
│   └── util/                              # Tiện ích dùng chung (format tiền, sanitize HTML...)
├── src/main/resources/
│   ├── application.properties             # Cấu hình chính (đọc secrets qua biến môi trường)
│   ├── application-secrets.properties.example   # Mẫu file secrets — copy thành application-secrets.properties
│   ├── application-h2preview.properties   # Profile chạy nhanh bằng H2 in-memory (không cần cài SQL Server)
│   ├── db/DuaStore_Database.sql           # ⭐ Script DUY NHẤT tạo schema + seed dữ liệu mẫu cho SQL Server
│   ├── static/                            # CSS, JS, ảnh tĩnh
│   └── templates/                         # View Thymeleaf (layout/, fragments/, view/client/, view/admin/, error/)
└── uploads/                                # Nơi lưu ảnh người dùng tải lên (sản phẩm, đánh giá, banner...)
```

---

## Yêu cầu hệ thống

- **JDK 21** trở lên
- **Microsoft SQL Server** (2019+ khuyến nghị) — hoặc dùng profile `h2preview` để chạy thử nhanh không cần cài đặt
- Không bắt buộc cài Maven — dự án đã kèm sẵn Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## Hướng dẫn cài đặt & chạy dự án

### Cách 1 — Chạy đầy đủ với SQL Server (khuyến nghị)

**Bước 1: Tạo cơ sở dữ liệu**

Mở SQL Server Management Studio (hoặc `sqlcmd`) và chạy toàn bộ nội dung file:

```
src/main/resources/db/DuaStore_Database.sql
```

Script này sẽ tự tạo database `DuaStore`, toàn bộ 44 bảng, các index, 3 view hỗ trợ và **dữ liệu mẫu ban đầu** (vai trò, quyền hạn, tài khoản demo, danh mục, sản phẩm, khuyến mãi, bài viết...). Đây là **nguồn schema duy nhất** của dự án — ứng dụng chạy với `spring.jpa.hibernate.ddl-auto=validate` nên bắt buộc schema phải khớp 100% với script này.

**Bước 2: Khai báo cấu hình local (cách đơn giản nhất — khuyến nghị)**

Tạo file `src/main/resources/application-local.properties` từ file mẫu:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Mở file vừa tạo và sửa `spring.datasource.username`/`spring.datasource.password` theo tài khoản SQL Server thật của máy bạn, điền `duastore.remember-me.key` bất kỳ. File này khớp với `spring.profiles.active=local` khai báo ở đầu `application.properties` nên Spring Boot sẽ tự nạp — **không cần set biến môi trường nào cả**, chỉ cần copy + sửa vài dòng là chạy được. File đã có trong `.gitignore` nên sẽ không bị đưa lên Git.

> ⚠️ Nếu bỏ qua bước này, ứng dụng sẽ báo lỗi `Could not resolve placeholder 'DB_USERNAME'` (hoặc `REMEMBER_ME_KEY`) khi khởi động và không chạy được — đây là lỗi phổ biến nhất khi lần đầu setup dự án.

**Cách khác (thay vì dùng file trên):** set trực tiếp các biến môi trường sau qua OS hoặc IDE run configuration — dùng khi deploy hoặc không muốn dùng file `application-local.properties`:

| Biến môi trường | Ý nghĩa |
|---|---|
| `DB_USERNAME` | Tên đăng nhập SQL Server |
| `DB_PASSWORD` | Mật khẩu SQL Server |
| `REMEMBER_ME_KEY` | Chuỗi bí mật bất kỳ dùng cho "Ghi nhớ đăng nhập" |
| `DB_URL` *(tuỳ chọn)* | JDBC URL, mặc định `jdbc:sqlserver://localhost:1433;databaseName=DuaStore;...` |

Ngoài ra, nếu muốn dùng đăng nhập Google / gửi email / thanh toán VietQR với thông tin thật, có thể tạo thêm file `src/main/resources/application-secrets.properties` từ `application-secrets.properties.example` (tùy chọn, không bắt buộc để chạy được app).

**Bước 3: Chạy ứng dụng**

```bash
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows
```

Ứng dụng mặc định chạy tại **http://localhost:8080**.

### Cách 2 — Chạy nhanh xem giao diện với H2 (không cần cài SQL Server)

Dùng để xem nhanh giao diện, **không có sẵn dữ liệu mẫu** (vì dữ liệu mẫu chỉ được seed qua script SQL Server ở trên, còn profile này tạo schema rỗng bằng `ddl-auto=create`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2preview
```

---

## Tài khoản demo

Sau khi chạy script SQL (Cách 1), hệ thống có sẵn 2 tài khoản mẫu:

| Vai trò | Tài khoản đăng nhập | Mật khẩu |
|---|---|---|
| SUPER_ADMIN (toàn quyền quản trị) | `admin` | `admin@123` |
| USER (khách hàng) | `nguyenvan` | `admin@123` |

> Mật khẩu seed sẵn trong script là hash BCrypt của `admin@123` cho cả 2 tài khoản demo — **nên đổi ngay** nếu triển khai ra môi trường thật.

---

## Tài liệu liên quan

- **`src/main/resources/db/DuaStore_Database.sql`** — script schema + seed dữ liệu SQL Server (nguồn duy nhất, bắt buộc chạy trước khi khởi động app)
- **`DuaStore_TaiLieu_DB_UseCase.docx`** — tài liệu mô tả chi tiết 44 bảng dữ liệu (thuộc tính, kiểu dữ liệu, khóa) và đặc tả 20 Use Case chính của hệ thống

---

## Ghi chú quan trọng

- Ứng dụng chạy với `spring.jpa.hibernate.ddl-auto=validate`: **schema không được Hibernate tự sinh**. Nếu sau này thêm/sửa field trong `@Entity`, phải cập nhật thủ công file `DuaStore_Database.sql` theo đúng tên cột (camelCase, không tự chuyển snake_case do dùng `PhysicalNamingStrategyStandardImpl`) — nếu không ứng dụng sẽ báo lỗi validate ngay khi khởi động.
- Dữ liệu mẫu (roles, permissions, tài khoản admin, danh mục, sản phẩm, khuyến mãi, bài viết...) **chỉ được seed qua file SQL** ở trên — không còn seed bằng code Java (`CommandLineRunner`) như phiên bản cũ, giúp khởi động ứng dụng nhẹ và nhanh hơn.
- Không commit file `application-secrets.properties` (chứa thông tin nhạy cảm thật) lên Git — chỉ commit file `.example`/`.template`.
