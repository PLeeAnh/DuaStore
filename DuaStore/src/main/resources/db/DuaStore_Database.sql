/*
================================================================================
  ____  _   _    _    ____  _____ ___  ____  _____
 |  _ \| | | |  / \  / ___||_   _/ _ \|  _ \| ____|
 | | | | | | | / _ \ \___ \  | || | | | |_) |  _|
 | |_| | |_| |/ ___ \ ___) | | || |_| |  _ <| |___
 |____/ \___//_/   \_\____/  |_| \___/|_| \_\_____|

  DuaStore -- Do Thuy Tinh Decor, Hai Phong
================================================================================
  QUAN TRONG - doc truoc khi chay:
  1. "validate" la che do NGHIEM NGAT nhat cua Hibernate: no KHONG tu sua bang,
     chi kiem tra bang trong DB co dung 100% voi @Entity trong code khong.
     Neu lech (thieu cot, sai kieu, sai do dai...) -> app se KHONG khoi dong duoc.
  2. File nay duoc doi chieu thu cong tung dong voi 27 @Entity trong code
     (khong the chay Hibernate that de xuat DDL vi moi truong bien soan
     khong co mang internet toi Maven Central). Vi vay VAN CO XAC SUAT lech
     nho o vai cot ngay-gio (DATETIME2 precision) hoac do dai chuoi mac dinh
     (nhung cot khong ghi ro length trong @Column, JPA mac dinh la 255).
     -> Chay thu tren moi truong dev truoc, neu Hibernate bao loi
        "Schema-validation: wrong column type/length" o cot nao, gui lai
        thong bao loi do de sua chinh xac cot ay.
  3. Mat khau admin mac dinh: admin@123 (BCrypt hash co san).
  4. Khac voi ban cu: cot "role" (NVARCHAR) trong bang Users DA BI XOA.
     Toan bo phan quyen gio dung RBAC that: roles / permissions /
     role_permissions / user_roles. Xem phan SEED RBAC ben duoi.
  5. File duoc luu UTF-8 (khong phai UTF-16 nhu ban cu) de tranh loi vo
     tieng Viet (ban cu bi loi encoding o phan seed 22 san pham mo rong).
================================================================================
*/

-- ============================================================
-- BUOC 0: TAO DATABASE (neu chua co)
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'DuaStore')
    CREATE DATABASE DuaStore;
GO
USE DuaStore;
GO

-- ============================================================
-- BUOC 0.5: XOA BANG CU (drop theo thu tu dao nguoc de tranh loi FK)
-- ============================================================
DROP VIEW IF EXISTS vw_PostsPublished;
DROP VIEW IF EXISTS vw_ProductPrice;
DROP VIEW IF EXISTS vw_DoanhThu;
GO
DROP TABLE IF EXISTS admin_action_logs;
DROP TABLE IF EXISTS order_status_logs;
DROP TABLE IF EXISTS order_notes;
DROP TABLE IF EXISTS order_assignments;
DROP TABLE IF EXISTS Notifications;
DROP TABLE IF EXISTS UserVouchers;
DROP TABLE IF EXISTS Wishlists;
DROP TABLE IF EXISTS Posts;
DROP TABLE IF EXISTS PostCategories;
DROP TABLE IF EXISTS banners;
DROP TABLE IF EXISTS SavedCartItems;
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS FlashSales;
DROP TABLE IF EXISTS Promotions;
DROP TABLE IF EXISTS Addresses;
DROP TABLE IF EXISTS ProductVariants;
DROP TABLE IF EXISTS ProductImages;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Categories;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS linked_accounts;
DROP TABLE IF EXISTS user_auth_providers;
DROP TABLE IF EXISTS SiteSettings;
DROP TABLE IF EXISTS store_info;
DROP TABLE IF EXISTS users;
GO

-- ============================================================
-- [1] BANG: users  (User.java)
-- ============================================================
-- QUAN TRONG: KHONG con cot "role" nua. Phan quyen chuyen sang RBAC
-- (roles / permissions / role_permissions / user_roles) o ben duoi.
-- ============================================================
CREATE TABLE users (
    id                INT            IDENTITY(1,1)  NOT NULL,
    username          NVARCHAR(50)                  NOT NULL,
    email             NVARCHAR(100)                 NOT NULL,
    password          NVARCHAR(255)                 NOT NULL,   -- BCrypt hash
    hoTen             NVARCHAR(100)                 NOT NULL,
    soDienThoai       NVARCHAR(15)                  NULL,
    isActive          BIT                           NOT NULL DEFAULT 1,
    ngayTao           DATETIME2                     NOT NULL,
    ngayCapNhat       DATETIME2                     NULL,
    resetToken        NVARCHAR(255)                 NULL,
    resetTokenExpiry  DATETIME2                     NULL,

    CONSTRAINT PK_users          PRIMARY KEY (id),
    CONSTRAINT UQ_users_email    UNIQUE (email),
    CONSTRAINT UQ_users_username UNIQUE (username)
);
GO

-- ============================================================
-- [1b] ALTER users — them cot cho profile
-- ============================================================
ALTER TABLE users ADD avatar          NVARCHAR(255) NULL;
ALTER TABLE users ADD nickname        NVARCHAR(100) NULL;
ALTER TABLE users ADD status          NVARCHAR(20)  NOT NULL DEFAULT 'ONLINE';
ALTER TABLE users ADD email_visible   BIT           NOT NULL DEFAULT 0;
ALTER TABLE users ADD phone_visible   BIT           NOT NULL DEFAULT 0;
ALTER TABLE users ADD email_marketing BIT           NOT NULL DEFAULT 1;
GO

-- ============================================================
-- [1c] BANG: user_auth_providers  (UserAuthProvider.java)
-- Luu phuong thuc dang nhap (PASSWORD, GOOGLE) cho 1 user.
-- Cung email chi co 1 user, nhung co the co nhieu phuong thuc.
-- ============================================================
CREATE TABLE user_auth_providers (
    id              INT IDENTITY(1,1) NOT NULL,
    user_id         INT               NOT NULL,
    provider        NVARCHAR(20)      NOT NULL,  -- 'PASSWORD', 'GOOGLE'
    provider_sub    NVARCHAR(255)     NULL,       -- Google 'sub', NULL for PASSWORD
    linked_at       DATETIME2         NOT NULL,
    CONSTRAINT PK_user_auth_providers PRIMARY KEY (id),
    CONSTRAINT FK_uap_user FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- ============================================================
-- [1d] BANG: linked_accounts  (LinkedAccount.java)
-- Danh sach tai khoan da lien ket de chuc nang "Doi tai khoan".
-- ============================================================
CREATE TABLE linked_accounts (
    id              INT IDENTITY(1,1) NOT NULL,
    user_id         INT               NOT NULL,
    linked_user_id  INT               NOT NULL,
    created_at      DATETIME2         NOT NULL,
    CONSTRAINT PK_linked_accounts PRIMARY KEY (id),
    CONSTRAINT FK_la_user   FOREIGN KEY (user_id)       REFERENCES users(id),
    CONSTRAINT FK_la_linked FOREIGN KEY (linked_user_id) REFERENCES users(id),
    CONSTRAINT UQ_la_pair   UNIQUE (user_id, linked_user_id)
);
GO

-- ============================================================
-- [1e] BANG: user_settings  (UserSetting.java)
-- Key-value settings cho tung user (thong bao, giao dien, v.v.)
-- ============================================================
CREATE TABLE user_settings (
    user_id       INT              NOT NULL,
    setting_key   NVARCHAR(50)     NOT NULL,
    setting_value NVARCHAR(500)    NULL,
    CONSTRAINT PK_user_settings PRIMARY KEY (user_id, setting_key),
    CONSTRAINT FK_us_user FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- ============================================================
-- [2] BANG: roles  (Role.java)
-- ============================================================
-- Vai tro "SUPER_ADMIN" duoc code (SecurityService.hasPermission) coi la
-- bypass toan bo kiem tra quyen -> khong can gan permission le cho vai tro nay.
-- ============================================================
CREATE TABLE roles (
    id       INT            IDENTITY(1,1)  NOT NULL,
    name     NVARCHAR(50)                  NOT NULL,
    moTa     NVARCHAR(200)                 NULL,
    ngayTao  DATETIME2                     NOT NULL,

    CONSTRAINT PK_roles     PRIMARY KEY (id),
    CONSTRAINT UQ_roles_name UNIQUE (name)
);
GO

-- ============================================================
-- [3] BANG: permissions  (Permission.java)
-- ============================================================
-- Moi quyen la 1 cap (module, action). GrantedAuthority duoc code ghep
-- thanh "MODULE_ACTION" (vd PRODUCT + CREATE -> "PRODUCT_CREATE"),
-- phai khop 100% voi cac hang so trong PermissionEnum.java.
-- ============================================================
CREATE TABLE permissions (
    id       INT            IDENTITY(1,1)  NOT NULL,
    module   NVARCHAR(50)                  NOT NULL,
    action   NVARCHAR(50)                  NOT NULL,
    moTa     NVARCHAR(200)                 NULL,
    ngayTao  DATETIME2                     NOT NULL,

    CONSTRAINT PK_permissions PRIMARY KEY (id)
);
GO

-- ============================================================
-- [4] BANG: role_permissions  (Role.permissions @ManyToMany)
-- ============================================================
CREATE TABLE role_permissions (
    role_id        INT NOT NULL,
    permission_id  INT NOT NULL,

    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_Role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT FK_role_permissions_Permission FOREIGN KEY (permission_id)
        REFERENCES permissions(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [5] BANG: user_roles  (User.roles @ManyToMany)
-- ============================================================
-- QUAN TRONG: CustomUserDetailsService se CHAN dang nhap neu user khong co
-- it nhat 1 role (throw DisabledException). Moi user PHAI duoc gan role.
-- ============================================================
CREATE TABLE user_roles (
    user_id  INT NOT NULL,
    role_id  INT NOT NULL,

    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_User FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_user_roles_Role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON DELETE NO ACTION
);
GO

-- ============================================================
-- [6] BANG: Categories  (Category.java)
-- ============================================================
CREATE TABLE Categories (
    id              INT            IDENTITY(1,1)  NOT NULL,
    tenDanhMuc      NVARCHAR(255)                 NOT NULL,
    moTa            NVARCHAR(255)                 NULL,
    parentId        INT                           NULL,
    thuTuHienThi    INT                           NOT NULL DEFAULT 0,
    isActive        BIT                           NOT NULL DEFAULT 1,
    imageUrl        NVARCHAR(500)                 NULL,
    ngayTao         DATETIME2                     NULL,
    ngayCapNhat     DATETIME2                     NULL,

    CONSTRAINT PK_Categories        PRIMARY KEY (id),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (parentId)
        REFERENCES Categories(id)
);
GO

-- ============================================================
-- [7] BANG: Products  (Product.java)
-- ============================================================
CREATE TABLE Products (
    id                  INT             IDENTITY(1,1)  NOT NULL,
    tenSanPham          NVARCHAR(255)                  NOT NULL,
    moTa                NVARCHAR(MAX)                  NULL,
    chatLieu            NVARCHAR(255)                  NULL,
    xuatXu              NVARCHAR(255)                  NULL,
    mucDichSuDung       NVARCHAR(255)                  NULL,
    thuongHieu          NVARCHAR(255)                  NULL,
    kinhLoai            NVARCHAR(255)                  NULL,
    hinhDang            NVARCHAR(255)                  NULL,
    danhMucId           INT                            NOT NULL,
    hinhAnhChinh        NVARCHAR(255)                  NULL,
    trangThaiSanPham    NVARCHAR(255)                  NOT NULL DEFAULT 'DANG_BAN',
    leadTimeDays        INT                            NULL,
    isFeatured          BIT                            NOT NULL DEFAULT 0,
    isActive            BIT                            NOT NULL DEFAULT 1,
    ngayPhatHanh        DATE                           NULL,
    ngayTao             DATETIME2                      NULL,
    ngayCapNhat         DATETIME2                      NULL,

    CONSTRAINT PK_Products           PRIMARY KEY (id),
    CONSTRAINT FK_Products_DanhMuc   FOREIGN KEY (danhMucId) REFERENCES Categories(id),
    CONSTRAINT CK_Products_TrangThai CHECK (trangThaiSanPham IN ('DANG_BAN','DAT_TRUOC','NGUNG_BAN'))
);
GO

-- ============================================================
-- [8] BANG: ProductImages  (ProductImage.java)
-- ============================================================
CREATE TABLE ProductImages (
    id          INT            IDENTITY(1,1)  NOT NULL,
    productId   INT                           NOT NULL,
    imageUrl    NVARCHAR(255)                 NOT NULL,
    sortOrder   INT                           NOT NULL DEFAULT 0,
    isActive    BIT                           NOT NULL DEFAULT 1,
    createdAt   DATETIME2                     NULL,

    CONSTRAINT PK_ProductImages         PRIMARY KEY (id),
    CONSTRAINT FK_ProductImages_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [9] BANG: ProductVariants  (ProductVariant.java)
-- ============================================================
CREATE TABLE ProductVariants (
    id              INT             IDENTITY(1,1)  NOT NULL,
    productId       INT                            NOT NULL,
    tenBienThe      NVARCHAR(255)                  NOT NULL,
    dungTich        INT                            NULL,
    giaGoc          DECIMAL(12,0)                  NOT NULL,
    giaKhuyenMai    DECIMAL(12,0)                  NULL,
    soLuongTon      INT                            NOT NULL DEFAULT 0,
    hinhAnh         NVARCHAR(255)                  NULL,
    isDefault       BIT                            NOT NULL DEFAULT 0,
    isActive        BIT                            NOT NULL DEFAULT 1,

    CONSTRAINT PK_ProductVariants         PRIMARY KEY (id),
    CONSTRAINT FK_ProductVariants_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE,
    CONSTRAINT CK_ProductVariants_Gia     CHECK (giaGoc >= 0),
    CONSTRAINT CK_ProductVariants_SoLuong CHECK (soLuongTon >= 0)
);
GO

-- ============================================================
-- [10] BANG: Addresses  (Address.java)
-- ============================================================
-- Da them latitude/longitude (ban cu chua co) de phuc vu tinh phi ship
-- theo khoang cach thuc te.
-- ============================================================
CREATE TABLE Addresses (
    id              INT            IDENTITY(1,1)  NOT NULL,
    userId          INT                           NOT NULL,
    tenNguoiNhan    NVARCHAR(100)                 NOT NULL,
    soDienThoai     NVARCHAR(15)                  NOT NULL,
    tinhThanh       NVARCHAR(100)                 NOT NULL,
    quanHuyen       NVARCHAR(100)                 NOT NULL,
    phuongXa        NVARCHAR(100)                 NOT NULL,
    diaChiCuThe     NVARCHAR(200)                 NOT NULL,
    isDefault       BIT                           NOT NULL DEFAULT 0,
    latitude        FLOAT                         NULL,
    longitude       FLOAT                         NULL,

    CONSTRAINT PK_Addresses      PRIMARY KEY (id),
    CONSTRAINT FK_Addresses_User FOREIGN KEY (userId)
        REFERENCES users(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [11] BANG: Promotions  (Promotion.java)
-- ============================================================
-- Da mo rong rat nhieu so voi ban cu: voucherType, priority, stackable,
-- budget/usedBudget (ngan sach voucher), maxClaims(PerUser), targetType/Ids,
-- savedCount (vi da co tinh nang "luu voucher ve vi" - UserVouchers).
-- ============================================================
CREATE TABLE Promotions (
    id                  INT             IDENTITY(1,1)  NOT NULL,
    maCode              NVARCHAR(50)                   NOT NULL,
    tenChuongTrinh      NVARCHAR(200)                  NOT NULL,
    loaiGiam            NVARCHAR(15)                   NOT NULL,
    giaTriGiam          DECIMAL(10,2)                  NOT NULL,
    donHangToiThieu     DECIMAL(12,0)                  NOT NULL DEFAULT 0,
    giamToiDa           DECIMAL(12,0)                  NULL,
    soLanDung           INT                            NULL,
    daDung              INT                            NOT NULL DEFAULT 0,
    tuNgay              DATETIME2                      NOT NULL,
    denNgay             DATETIME2                      NOT NULL,
    isActive            BIT                            NOT NULL DEFAULT 1,
    voucherType         NVARCHAR(20)                   NULL DEFAULT 'VOUCHER',
    priority            INT                            NULL DEFAULT 0,
    stackable           BIT                            NULL DEFAULT 0,
    budget              DECIMAL(12,0)                  NULL,
    usedBudget          DECIMAL(12,0)                  NULL DEFAULT 0,
    maxClaims           INT                            NULL,
    maxClaimsPerUser    INT                            NULL,
    targetType          NVARCHAR(20)                   NULL,
    targetIds           NVARCHAR(500)                  NULL,
    savedCount          INT                            NULL DEFAULT 0,

    CONSTRAINT PK_Promotions        PRIMARY KEY (id),
    CONSTRAINT UQ_Promotions_Code   UNIQUE (maCode),
    CONSTRAINT CK_Promotions_Loai   CHECK (loaiGiam IN ('PHAN_TRAM','SO_TIEN')),
    CONSTRAINT CK_Promotions_NgayGT CHECK (tuNgay < denNgay),
    CONSTRAINT CK_Promotions_VType  CHECK (voucherType IS NULL OR voucherType IN ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))
);
GO

-- ============================================================
-- [12] BANG: FlashSales  (FlashSale.java)
-- ============================================================
CREATE TABLE FlashSales (
    id           INT            IDENTITY(1,1)  NOT NULL,
    productId    INT                           NOT NULL,
    giaTriGiam   DECIMAL(5,2)                  NOT NULL,
    ngayBatDau   DATETIME2                     NOT NULL,
    ngayKetThuc  DATETIME2                     NOT NULL,
    isActive     BIT                           NOT NULL DEFAULT 1,

    CONSTRAINT PK_FlashSales         PRIMARY KEY (id),
    CONSTRAINT FK_FlashSales_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [13] BANG: orders  (Order.java)
-- ============================================================
CREATE TABLE orders (
    id                  INT             IDENTITY(1,1)  NOT NULL,
    maDon               NVARCHAR(20)                   NOT NULL,
    userId              INT                            NOT NULL,
    addressId           INT                            NULL,
    snapTenNguoiNhan    NVARCHAR(100)                  NOT NULL,
    snapSoDienThoai     NVARCHAR(15)                   NOT NULL,
    snapDiaChi          NVARCHAR(500)                  NOT NULL,
    tienHang            DECIMAL(12,0)                  NOT NULL,
    phiVanChuyen        DECIMAL(10,0)                  NOT NULL DEFAULT 0,
    tienGiam            DECIMAL(10,0)                  NOT NULL DEFAULT 0,
    tongThanhToan       DECIMAL(12,0)                  NOT NULL,
    phuongThucTT        NVARCHAR(20)                   NOT NULL,
    phuongThucGiaoHang  NVARCHAR(20)                   NOT NULL DEFAULT 'SHIP',
    trangThaiTT         NVARCHAR(25)                   NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    trangThaiDon        NVARCHAR(20)                   NOT NULL DEFAULT 'CHO_XAC_NHAN',
    promotionId         INT                            NULL,
    ghiChu              NVARCHAR(500)                  NULL,
    maVanDon            NVARCHAR(50)                   NULL,
    ngayDat             DATETIME2                      NULL,
    ngayCapNhat         DATETIME2                      NULL,

    CONSTRAINT PK_orders           PRIMARY KEY (id),
    CONSTRAINT UQ_orders_MaDon     UNIQUE (maDon),
    CONSTRAINT FK_orders_User      FOREIGN KEY (userId)      REFERENCES users(id),
    CONSTRAINT FK_orders_Address   FOREIGN KEY (addressId)   REFERENCES Addresses(id),
    CONSTRAINT FK_orders_Promotion FOREIGN KEY (promotionId) REFERENCES Promotions(id),
    CONSTRAINT CK_orders_TT        CHECK (phuongThucTT       IN ('CHUYEN_KHOAN','COD','VNPAY')),
    CONSTRAINT CK_orders_GH        CHECK (phuongThucGiaoHang IN ('SHIP','NHAN_TAI_CONG')),
    CONSTRAINT CK_orders_ThanhToan CHECK (trangThaiTT        IN ('CHUA_THANH_TOAN','DA_THANH_TOAN')),
    CONSTRAINT CK_orders_TrangThai CHECK (trangThaiDon       IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HOAN_THANH','DA_HUY'))
);
GO
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='maVanDon')
BEGIN
    ALTER TABLE orders ADD maVanDon NVARCHAR(50) NULL;
END
GO

-- ============================================================
-- [14] BANG: OrderItems  (OrderItem.java, table order_items)
-- ============================================================
CREATE TABLE order_items (
    id              INT             IDENTITY(1,1)  NOT NULL,
    orderId         INT                            NOT NULL,
    productId       INT                            NULL,
    variantId       INT                            NULL,
    tenSanPham      NVARCHAR(200)                  NOT NULL,
    tenBienThe      NVARCHAR(150)                  NULL,
    hinhAnhSP       NVARCHAR(255)                  NULL,
    donGia          DECIMAL(12,0)                  NOT NULL,
    soLuong         INT                            NOT NULL,
    thanhTien       DECIMAL(12,0)                  NOT NULL,

    CONSTRAINT PK_order_items         PRIMARY KEY (id),
    CONSTRAINT FK_order_items_Order   FOREIGN KEY (orderId)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_order_items_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE SET NULL,
    CONSTRAINT FK_order_items_Variant FOREIGN KEY (variantId)
        REFERENCES ProductVariants(id) ON DELETE NO ACTION,
    CONSTRAINT CK_order_items_Qty     CHECK (soLuong > 0)
);
GO

-- ============================================================
-- [15] BANG: order_assignments  (OrderAssignment.java)
-- ============================================================
-- Admin nao dang phu trach xu ly don hang nao.
-- ============================================================
CREATE TABLE order_assignments (
    id         INT            IDENTITY(1,1)  NOT NULL,
    orderId    INT                           NOT NULL,
    adminId    INT                           NOT NULL,
    ngayPhan   DATETIME2                     NOT NULL,
    trangThai  NVARCHAR(20)                  NULL,

    CONSTRAINT PK_order_assignments PRIMARY KEY (id),
    CONSTRAINT FK_order_assignments_Order FOREIGN KEY (orderId)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_order_assignments_Admin FOREIGN KEY (adminId)
        REFERENCES users(id) ON DELETE NO ACTION
);
GO

-- ============================================================
-- [16] BANG: order_notes  (OrderNote.java)
-- ============================================================
-- Ghi chu noi bo cua Admin tren 1 don hang (khac voi ghiChu cua khach).
-- ============================================================
CREATE TABLE order_notes (
    id         INT            IDENTITY(1,1)  NOT NULL,
    order_id   INT                           NOT NULL,
    admin_id   INT                           NOT NULL,
    noiDung    NVARCHAR(1000)                NOT NULL,
    ngayTao    DATETIME2                     NOT NULL,

    CONSTRAINT PK_order_notes PRIMARY KEY (id),
    CONSTRAINT FK_order_notes_Order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_order_notes_Admin FOREIGN KEY (admin_id)
        REFERENCES users(id) ON DELETE NO ACTION
);
GO

-- ============================================================
-- [17] BANG: order_status_logs  (OrderStatusLog.java)
-- ============================================================
-- Nhat ky moi thay doi trang thai / su kien cua 1 don hang (audit trail).
-- ============================================================
CREATE TABLE order_status_logs (
    id                  INT            IDENTITY(1,1)  NOT NULL,
    order_id            INT                           NOT NULL,
    loai_su_kien        NVARCHAR(50)                  NOT NULL,
    trang_thai_cu       NVARCHAR(50)                  NULL,
    trang_thai_moi      NVARCHAR(50)                  NULL,
    nguoi_thuc_hien_id  INT                           NULL,
    ghiChu              NVARCHAR(500)                 NULL,
    thoi_gian           DATETIME2                     NOT NULL,

    CONSTRAINT PK_order_status_logs PRIMARY KEY (id),
    CONSTRAINT FK_order_status_logs_Order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_order_status_logs_User FOREIGN KEY (nguoi_thuc_hien_id)
        REFERENCES users(id) ON DELETE NO ACTION,
    CONSTRAINT CK_order_status_logs_Loai CHECK (loai_su_kien IN
        ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED'))
);
GO

-- ============================================================
-- [18] BANG: Reviews  (Review.java)
-- ============================================================
-- Luu y: danhGia gio la INT (khong con TINYINT nhu ban cu) va them
-- cot hinhAnh (khach co the dinh kem anh khi danh gia).
-- ============================================================
CREATE TABLE Reviews (
    id          INT             IDENTITY(1,1)  NOT NULL,
    productId   INT                            NOT NULL,
    userId      INT                            NOT NULL,
    danhGia     INT                            NOT NULL,
    binhLuan    NVARCHAR(1000)                 NULL,
    hinhAnh     NVARCHAR(500)                  NULL,
    isApproved  BIT                            NOT NULL DEFAULT 0,
    ngayTao     DATETIME2                      NULL,

    CONSTRAINT PK_Reviews             PRIMARY KEY (id),
    CONSTRAINT UQ_Reviews_UserProduct UNIQUE (userId, productId),
    CONSTRAINT FK_Reviews_Product     FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE,
    CONSTRAINT FK_Reviews_User        FOREIGN KEY (userId)
        REFERENCES users(id),
    CONSTRAINT CK_Reviews_DanhGia     CHECK (danhGia BETWEEN 1 AND 5)
);
GO

-- ============================================================
-- [19] BANG: CartItems  (CartItem.java)
-- ============================================================
-- Da gop them cot giaLucThem (tu migration__add_gialucthem.sql) de
-- phat hien khi gia san pham thay doi so voi luc khach them vao gio.
-- ============================================================
CREATE TABLE CartItems (
    id           INT             IDENTITY(1,1)  NOT NULL,
    userId       INT                            NOT NULL,
    productId    INT                            NOT NULL,
    variantId    INT                            NOT NULL,
    soLuong      INT                            NOT NULL DEFAULT 1,
    giaLucThem   DECIMAL(12,0)                  NULL,
    ngayThem     DATETIME2                      NULL,

    CONSTRAINT PK_CartItems             PRIMARY KEY (id),
    CONSTRAINT UQ_CartItems_UserVariant UNIQUE (userId, variantId),
    CONSTRAINT FK_CartItems_User        FOREIGN KEY (userId)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Product     FOREIGN KEY (productId) REFERENCES Products(id),
    CONSTRAINT FK_CartItems_Variant     FOREIGN KEY (variantId) REFERENCES ProductVariants(id),
    CONSTRAINT CK_CartItems_SoLuong     CHECK (soLuong > 0)
);
GO

-- ============================================================
-- [20] BANG: SavedCartItems  (SavedCartItem.java)
-- ============================================================
-- Gop tu migration__saved_cart_items.sql. San pham khach bam "de mua sau".
-- Ghi chu: file migration goc dung IDENTITY/DATETIME2/GETDATE kieu T-SQL
-- thuan, o day viet lai dung dung kieu Hibernate sinh ra cho khop entity.
-- ============================================================
CREATE TABLE SavedCartItems (
    id          INT             IDENTITY(1,1)  NOT NULL,
    userId      INT                            NOT NULL,
    productId   INT                            NOT NULL,
    variantId   INT                            NOT NULL,
    soLuong     INT                            NOT NULL DEFAULT 1,
    giaLuu      DECIMAL(19,2)                  NOT NULL,
    ngayLuu     DATETIME2                      NULL,

    CONSTRAINT PK_SavedCartItems         PRIMARY KEY (id),
    CONSTRAINT FK_SavedCartItems_User    FOREIGN KEY (userId)    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_SavedCartItems_Product FOREIGN KEY (productId) REFERENCES Products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_SavedCartItems_Variant FOREIGN KEY (variantId) REFERENCES ProductVariants(id) ON DELETE NO ACTION
);
GO

-- ============================================================
-- [21] BANG: banners  (Banner.java)
-- ============================================================
CREATE TABLE banners (
    id              INT             IDENTITY(1,1) NOT NULL,
    title           NVARCHAR(200)                  NOT NULL,
    image_url       NVARCHAR(500)                  NOT NULL,
    link_url        NVARCHAR(1000)                 NULL,
    active          BIT                            NOT NULL DEFAULT 1,
    display_order   INT                            NOT NULL DEFAULT 0,
    start_date      DATETIME2                      NULL,
    end_date        DATETIME2                      NULL,
    description     NVARCHAR(500)                  NULL,
    created_at      DATETIME2                      NOT NULL,
    updated_at      DATETIME2                      NOT NULL,

    CONSTRAINT PK_banners PRIMARY KEY (id),
    CONSTRAINT CK_banners_DisplayOrder CHECK (display_order >= 0),
    CONSTRAINT CK_banners_Period CHECK (end_date IS NULL OR start_date IS NULL OR end_date > start_date)
);
GO

-- ============================================================
-- [22] BANG: PostCategories  (PostCategory.java)
-- ============================================================
CREATE TABLE PostCategories (
    id          INT            IDENTITY(1,1)  NOT NULL,
    tenDanhMuc  NVARCHAR(200)                 NOT NULL,
    moTa        NVARCHAR(500)                 NULL,
    slug        NVARCHAR(300)                 NULL,
    thuTu       INT                           NULL DEFAULT 0,
    ngayTao     DATETIME2                     NOT NULL,

    CONSTRAINT PK_PostCategories PRIMARY KEY (id)
);
GO

-- ============================================================
-- [24] BANG: Posts  (Post.java)
-- ============================================================
-- Khac ban cu: danhMucId gio la FK toi PostCategories. Them metaDescription,
-- slug, isFeatured.
-- ============================================================
CREATE TABLE Posts (
    id                INT             IDENTITY(1,1)  NOT NULL,
    tieuDe            NVARCHAR(300)                  NOT NULL,
    slug              NVARCHAR(500)                  NULL,
    metaDescription   NVARCHAR(500)                  NULL,
    tomTat            NVARCHAR(500)                  NULL,
    noiDung           NVARCHAR(MAX)                  NULL,
    hinhAnh           NVARCHAR(255)                  NULL,
    tacGiaId          INT                            NULL,
    danhMucId         INT                            NULL,
    trangThai         NVARCHAR(15)                   NOT NULL DEFAULT 'NHAP',
    luotXem           INT                            NOT NULL DEFAULT 0,
    isFeatured        BIT                            NULL DEFAULT 0,
    ngayXuatBan       DATETIME2                      NULL,
    ngayTao           DATETIME2                      NULL,
    ngayCapNhat       DATETIME2                      NULL,

    CONSTRAINT PK_Posts           PRIMARY KEY (id),
    CONSTRAINT FK_Posts_TacGia    FOREIGN KEY (tacGiaId)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT FK_Posts_DanhMuc   FOREIGN KEY (danhMucId)
        REFERENCES PostCategories(id) ON DELETE SET NULL,
    CONSTRAINT CK_Posts_TrangThai CHECK (trangThai IN ('NHAP','XUAT_BAN','AN'))
);
GO

-- ============================================================
-- [26] BANG: Wishlists  (Wishlist.java)
-- ============================================================
CREATE TABLE Wishlists (
    id          INT             IDENTITY(1,1)  NOT NULL,
    userId      INT                            NOT NULL,
    productId   INT                            NOT NULL,
    ngayThem    DATETIME2                      NULL,

    CONSTRAINT PK_Wishlists             PRIMARY KEY (id),
    CONSTRAINT UQ_Wishlists_UserProduct UNIQUE (userId, productId),
    CONSTRAINT FK_Wishlists_User        FOREIGN KEY (userId)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_Wishlists_Product     FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [27] BANG: UserVouchers  (UserVoucher.java)
-- ============================================================
-- Voucher khach da "luu ve vi" (khac voi Promotions la kho voucher chung).
-- ============================================================
CREATE TABLE UserVouchers (
    id               INT             IDENTITY(1,1)  NOT NULL,
    userId           INT                            NOT NULL,
    promotionId      INT                            NOT NULL,
    voucherCode      NVARCHAR(50)                   NULL,
    remainingUses    INT                            NULL,
    savedAt          DATETIME2                      NULL,
    usedAt           DATETIME2                      NULL,
    expiredAt        DATETIME2                      NULL,
    status           NVARCHAR(15)                   NOT NULL DEFAULT 'AVAILABLE',
    totalSaved       DECIMAL(19,2)                  NULL,

    CONSTRAINT PK_UserVouchers PRIMARY KEY (id),
    CONSTRAINT UQ_UserVouchers_UserPromo UNIQUE (userId, promotionId),
    CONSTRAINT FK_UserVouchers_User FOREIGN KEY (userId)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_UserVouchers_Promotion FOREIGN KEY (promotionId)
        REFERENCES Promotions(id) ON DELETE NO ACTION,
    CONSTRAINT CK_UserVouchers_Status CHECK (status IN ('AVAILABLE','USED','EXPIRED'))
);
GO

-- ============================================================
-- [28] BANG: Notifications  (Notification.java)
-- ============================================================
-- userId = NULL nghia la thong bao chung (vd targetRole = 'ADMIN' gui cho
-- toan bo admin thay vi 1 nguoi cu the).
-- ============================================================
CREATE TABLE Notifications (
    id          INT             IDENTITY(1,1)  NOT NULL,
    content     NVARCHAR(MAX)                  NOT NULL,
    linkType    NVARCHAR(20)                   NULL,
    linkId      INT                            NULL,
    linkUrl     NVARCHAR(500)                  NULL,
    linkLabel   NVARCHAR(255)                  NULL,
    userId      INT                            NULL,
    targetRole  NVARCHAR(20)                   NULL,
    isActive    BIT                            NOT NULL DEFAULT 1,
    createdAt   DATETIME2                      NOT NULL,

    CONSTRAINT PK_Notifications PRIMARY KEY (id),
    CONSTRAINT FK_Notifications_User FOREIGN KEY (userId)
        REFERENCES users(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [29] BANG: admin_action_logs  (AdminActionLog.java)
-- ============================================================
-- Nhat ky moi thao tac cua Admin (sua san pham, xoa danh muc...) - audit trail.
-- ============================================================
CREATE TABLE admin_action_logs (
    id           INT            IDENTITY(1,1)  NOT NULL,
    adminId      INT                           NOT NULL,
    hanhDong     NVARCHAR(50)                  NOT NULL,
    loaiEntity   NVARCHAR(50)                  NOT NULL,
    entityId     INT                           NOT NULL,
    giaTriCu     NVARCHAR(MAX)                 NULL,
    giaTriMoi    NVARCHAR(MAX)                 NULL,
    moTa         NVARCHAR(MAX)                 NULL,
    ngayTao      DATETIME2                     NOT NULL,
    ipAddress    NVARCHAR(50)                  NULL,

    CONSTRAINT PK_admin_action_logs PRIMARY KEY (id),
    CONSTRAINT FK_admin_action_logs_Admin FOREIGN KEY (adminId)
        REFERENCES users(id) ON DELETE NO ACTION
);
GO

-- ============================================================
-- [30] BANG: SiteSettings  (SiteSetting.java)
-- ============================================================
CREATE TABLE SiteSettings (
    id            INT            IDENTITY(1,1)  NOT NULL,
    settingKey    NVARCHAR(100)                 NOT NULL,
    settingValue  NVARCHAR(MAX)                 NULL,
    settingGroup  NVARCHAR(50)                  NULL,
    createdAt     DATETIME2                     NULL,
    updatedAt     DATETIME2                     NULL,

    CONSTRAINT PK_SiteSettings PRIMARY KEY (id),
    CONSTRAINT UQ_SiteSettings_Key UNIQUE (settingKey)
);
GO

-- ============================================================
-- [31] BANG: store_info  (StoreInfo.java)
-- ============================================================
-- Thong tin cua hang (dung tinh khoang cach ship + hien thi lien he).
-- ============================================================
CREATE TABLE store_info (
    id            INT            IDENTITY(1,1)  NOT NULL,
    tenCuaHang    NVARCHAR(200)                 NOT NULL,
    soNha         NVARCHAR(100)                 NULL,
    duong         NVARCHAR(200)                 NULL,
    phuongXa      NVARCHAR(100)                 NULL,
    quanHuyen     NVARCHAR(100)                 NULL,
    tinhThanh     NVARCHAR(100)                 NULL,
    soDienThoai   NVARCHAR(20)                  NULL,
    email         NVARCHAR(100)                 NULL,
    latitude      FLOAT                         NULL,
    longitude     FLOAT                         NULL,
    isActive      BIT                           NOT NULL DEFAULT 1,
    isDefault     BIT                           NOT NULL DEFAULT 0,
    createdAt     DATETIME2                     NULL,
    updatedAt     DATETIME2                     NULL,

    CONSTRAINT PK_store_info PRIMARY KEY (id)
);
GO

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IX_Products_DanhMuc      ON Products        (danhMucId, isActive);
CREATE INDEX IX_Products_TrangThai    ON Products        (trangThaiSanPham, isActive);
CREATE INDEX IX_Products_MucDich      ON Products        (mucDichSuDung, isActive);
CREATE INDEX IX_Products_Featured     ON Products        (isFeatured, isActive);
CREATE INDEX IX_banners_ActiveOrder   ON banners         (active, display_order, start_date, end_date);
CREATE INDEX IX_Variants_DungTich     ON ProductVariants (productId, dungTich, isActive);
CREATE INDEX IX_Variants_Default      ON ProductVariants (productId, isDefault);
CREATE INDEX IX_orders_User           ON orders          (userId, ngayDat DESC);
CREATE INDEX IX_orders_TrangThai      ON orders          (trangThaiDon, trangThaiTT);
CREATE INDEX IX_CartItems_User        ON CartItems       (userId);
CREATE INDEX IX_Wishlists_User        ON Wishlists       (userId);
CREATE INDEX IX_Reviews_Product       ON Reviews         (productId, isApproved);
CREATE INDEX IX_ProductImages_Product ON ProductImages   (productId, isActive);
CREATE INDEX IX_order_items_ProductUser ON order_items   (productId, orderId) INCLUDE (soLuong);
CREATE INDEX IX_Posts_TrangThai       ON Posts           (trangThai, ngayTao DESC);
-- Gop tu migration__voucher_indexes.sql (sua ten cot is_active -> isActive cho dung schema thuc te)
CREATE INDEX idx_promotions_active_dates    ON Promotions   (isActive, tuNgay, denNgay);
CREATE INDEX idx_user_vouchers_user_status  ON UserVouchers (userId, status);
CREATE INDEX idx_user_vouchers_expired_at   ON UserVouchers (expiredAt);
CREATE INDEX idx_user_vouchers_promotion_id ON UserVouchers (promotionId);
CREATE INDEX idx_promotions_code            ON Promotions   (maCode);
GO

-- ============================================================
-- SEED: RBAC (roles / permissions / role_permissions / user_roles)
-- ============================================================
-- Danh sach permission khop CHINH XAC voi PermissionEnum.java trong code.
-- ============================================================
INSERT INTO roles (name, moTa, ngayTao) VALUES
    (N'SUPER_ADMIN', N'Toan quyen he thong (bypass moi kiem tra quyen)', GETDATE()),
    (N'ADMIN',       N'Quan tri vien duoc gan quyen cu the', GETDATE()),
    (N'USER',        N'Khach hang', GETDATE());
GO

INSERT INTO permissions (module, action, moTa, ngayTao) VALUES
    (N'DASHBOARD', N'READ', N'Xem trang tong quan', GETDATE()),
    (N'PRODUCT', N'CREATE', N'Them san pham', GETDATE()),
    (N'PRODUCT', N'READ',   N'Xem san pham', GETDATE()),
    (N'PRODUCT', N'UPDATE', N'Sua san pham', GETDATE()),
    (N'PRODUCT', N'DELETE', N'Xoa san pham', GETDATE()),
    (N'ORDER', N'READ',   N'Xem don hang', GETDATE()),
    (N'ORDER', N'UPDATE', N'Cap nhat don hang', GETDATE()),
    (N'USER', N'READ',   N'Xem nguoi dung', GETDATE()),
    (N'USER', N'UPDATE', N'Sua nguoi dung', GETDATE()),
    (N'CATEGORY', N'CREATE', N'Them danh muc', GETDATE()),
    (N'CATEGORY', N'READ',   N'Xem danh muc', GETDATE()),
    (N'CATEGORY', N'UPDATE', N'Sua danh muc', GETDATE()),
    (N'CATEGORY', N'DELETE', N'Xoa danh muc', GETDATE()),
    (N'PROMOTION', N'CREATE', N'Them khuyen mai', GETDATE()),
    (N'PROMOTION', N'READ',   N'Xem khuyen mai', GETDATE()),
    (N'PROMOTION', N'UPDATE', N'Sua khuyen mai', GETDATE()),
    (N'PROMOTION', N'DELETE', N'Xoa khuyen mai', GETDATE()),
    (N'REVIEW', N'READ',    N'Xem danh gia', GETDATE()),
    (N'REVIEW', N'APPROVE', N'Duyet danh gia', GETDATE()),
    (N'REVIEW', N'HIDE',    N'An danh gia', GETDATE()),
    (N'REVIEW', N'DELETE',  N'Xoa danh gia', GETDATE()),
    (N'POST', N'CREATE', N'Them bai viet', GETDATE()),
    (N'POST', N'READ',   N'Xem bai viet', GETDATE()),
    (N'POST', N'UPDATE', N'Sua bai viet', GETDATE()),
    (N'POST', N'DELETE', N'Xoa bai viet', GETDATE()),
    (N'VARIANT', N'CREATE', N'Them bien the', GETDATE()),
    (N'VARIANT', N'READ',   N'Xem bien the', GETDATE()),
    (N'VARIANT', N'UPDATE', N'Sua bien the', GETDATE()),
    (N'VARIANT', N'DELETE', N'Xoa bien the', GETDATE()),
    (N'ROLE', N'CREATE', N'Them vai tro', GETDATE()),
    (N'ROLE', N'READ',   N'Xem vai tro', GETDATE()),
    (N'ROLE', N'UPDATE', N'Sua vai tro', GETDATE()),
    (N'ROLE', N'DELETE', N'Xoa vai tro', GETDATE()),
    (N'NOTIFICATION', N'CREATE', N'Tao thong bao', GETDATE()),
    (N'NOTIFICATION', N'READ',   N'Xem thong bao', GETDATE()),
    (N'NOTIFICATION', N'UPDATE', N'Sua thong bao', GETDATE()),
    (N'NOTIFICATION', N'DELETE', N'Xoa thong bao', GETDATE()),
    (N'AUDIT_LOG', N'READ', N'Xem nhat ky he thong', GETDATE()),
    (N'STORE', N'CREATE', N'Them dia chi cua hang', GETDATE()),
    (N'STORE', N'READ',   N'Xem dia chi cua hang', GETDATE()),
    (N'STORE', N'UPDATE', N'Sua dia chi cua hang', GETDATE()),
    (N'STORE', N'DELETE', N'Xoa dia chi cua hang', GETDATE()),
    (N'BANNER', N'CREATE', N'Them banner', GETDATE()),
    (N'BANNER', N'READ',   N'Xem banner', GETDATE()),
    (N'BANNER', N'UPDATE', N'Sua banner', GETDATE()),
    (N'BANNER', N'DELETE', N'Xoa banner', GETDATE()),
    (N'CUSTOMER', N'READ',   N'Xem khach hang', GETDATE()),
    (N'CUSTOMER', N'UPDATE', N'Sua khach hang', GETDATE()),
    (N'HOMEPAGE', N'READ',   N'Xem cau hinh trang chu', GETDATE()),
    (N'HOMEPAGE', N'UPDATE', N'Sua cau hinh trang chu', GETDATE()),
    (N'APPEARANCE', N'READ',   N'Xem giao dien', GETDATE()),
    (N'APPEARANCE', N'UPDATE', N'Sua giao dien', GETDATE()),
    (N'ANALYTICS', N'READ', N'Xem phan tich', GETDATE()),
    (N'EMAIL_SETTING', N'READ',   N'Xem cau hinh email', GETDATE()),
    (N'EMAIL_SETTING', N'UPDATE', N'Sua cau hinh email', GETDATE()),
    (N'PAYMENT_SETTING', N'READ',   N'Xem cau hinh thanh toan', GETDATE()),
    (N'PAYMENT_SETTING', N'UPDATE', N'Sua cau hinh thanh toan', GETDATE()),
    (N'SHIPPING_SETTING', N'READ',   N'Xem cau hinh van chuyen', GETDATE()),
    (N'SHIPPING_SETTING', N'UPDATE', N'Sua cau hinh van chuyen', GETDATE());
GO

-- ADMIN (vai tro thuong, khong bypass) duoc gan TOAN BO permission ben tren
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = N'ADMIN'), id FROM permissions;
GO

-- Tai khoan mac dinh
-- Mat khau "admin@123" - hash BCrypt duoi day DA duoc verify thuc te (bcrypt.checkpw)
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('admin', 'admin@duastore.vn',
     '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO',
     N'Quan Tri Vien', '0901234567', 1, GETDATE());
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('nguyenvan', 'nguyen@gmail.com',
     '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO',
     N'Nguyen Van An', '0912345678', 1, GETDATE());
GO

-- Gan vai tro: admin -> SUPER_ADMIN (toan quyen), nguyenvan -> USER
INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE username = 'admin'),     (SELECT id FROM roles WHERE name = N'SUPER_ADMIN')),
    ((SELECT id FROM users WHERE username = 'nguyenvan'), (SELECT id FROM roles WHERE name = N'USER'));
GO

-- Seed: mac dinh moi user co phuong thuc dang nhap PASSWORD
INSERT INTO user_auth_providers (user_id, provider, linked_at)
SELECT id, 'PASSWORD', ngayTao FROM users;
GO

-- ============================================================
-- SEED: Danh muc + San pham mau
-- ============================================================
INSERT INTO Categories (tenDanhMuc, moTa, thuTuHienThi) VALUES
    (N'Chai Thuy Tinh',  N'Cac loai chai thuy tinh dung ruou, nuoc hoa, thuc pham', 1),
    (N'Hu Thuy Tinh',    N'Hu dung do kho, thuc pham, gia vi',                      2),
    (N'Binh Trang Tri',  N'Binh hoa, binh decor, trung bay nha cua',               3),
    (N'Ly & Coc',        N'Cac loai ly coc thuy tinh cao cap',                      4),
    (N'Qua Tang',        N'Bo set qua tang thuy tinh sang trong',                   5);

INSERT INTO Categories (tenDanhMuc, parentId, thuTuHienThi) VALUES
    (N'Chai Ruou',    1, 1), (N'Chai Nuoc Hoa', 1, 2), (N'Chai Thuc Pham', 1, 3),
    (N'Binh Hoa',     3, 1), (N'Binh Decor',    3, 2),
    (N'Ly Ruou Vang', 4, 1), (N'Ly Whisky',     4, 2), (N'Ly Nuoc',       4, 3),
    (N'Ly Champagne', 4, 4), (N'Ly Highball',   4, 5);
GO

INSERT INTO Products (tenSanPham, moTa, chatLieu, xuatXu, mucDichSuDung, danhMucId, trangThaiSanPham, isFeatured) VALUES
    (N'Chai Thuy Tinh Dung Ruou Tron',
     N'Chai thuy tinh hinh tron, mieng rong, phu hop dung ruou, nuoc ep, siro. Chat lieu thuy tinh trong suot, an toan thuc pham.',
     N'Thuy tinh trong suot', N'Viet Nam', N'Dung do uong', 6, 'DANG_BAN', 1),
    (N'Chai Thuy Tinh Vuong Co Lenh',
     N'Chai hinh vuong co lenh, thiet ke sang trong, dung ruou vang, nuoc hoa cao cap.',
     N'Thuy tinh trong suot', N'Viet Nam', N'Dung do uong', 6, 'DANG_BAN', 1),
    (N'Binh Hoa Pha Le Cat Canh',
     N'Binh cam hoa pha le cat canh thu cong, sang trong, la qua tang y nghia cho dip sinh nhat, cuoi hoi.',
     N'Pha le cat canh', N'Chau Au', N'Trang tri', 9, 'DANG_BAN', 1),
    (N'Ly Ruou Vang Pha Le Bohemia',
     N'Ly ruou vang pha le Bohemia chinh hang, trong suot tuyet doi, thanh lich. Nha may Bohemia - Sec.',
     N'Pha le Bohemia', N'Sec', N'Dung do uong', 11, 'DANG_BAN', 1),
    (N'Ly Highball Thuy Tinh Cao Cap',
     N'Ly Highball thuy tinh cao, thanh mong, phu hop pha cocktail, nuoc co ga, whisky on the rocks.',
     N'Thuy tinh cuong luc', N'Viet Nam', N'Dung do uong', 15, 'DANG_BAN', 0),
    (N'Binh Pha Le Pasabahce Nhap Khau',
     N'Binh pha le cao cap nhap khau tu Tho Nhi Ky, thuong hieu Pasabahce. Dat truoc 7-10 ngay lam viec.',
     N'Pha le cao cap', N'Tho Nhi Ky', N'Trang tri', 9, 'DAT_TRUOC', 1);

UPDATE Products SET leadTimeDays = 10 WHERE tenSanPham LIKE N'%Pasabahce%';
GO

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (1, N'50ml - Nap Go',    50,  15000, NULL,  20, N'/uploads/chai-tron-50ml-nap-go.jpg',    0),
    (1, N'50ml - Nap Nhua',  50,  13000, NULL,  15, N'/uploads/chai-tron-50ml-nap-nhua.jpg',  0),
    (1, N'100ml - Nap Go',  100,  23000, NULL,  50, N'/uploads/chai-tron-100ml-nap-go.jpg',   1),
    (1, N'100ml - Nap Nhua',100,  20000, NULL,  40, N'/uploads/chai-tron-100ml-nap-nhua.jpg', 0),
    (1, N'250ml - Nap Go',  250,  38000, 34000, 30, N'/uploads/chai-tron-250ml-nap-go.jpg',   0),
    (1, N'250ml - Nap Nhua',250,  32000, NULL,  25, N'/uploads/chai-tron-250ml-nap-nhua.jpg', 0),
    (1, N'500ml - Nap Go',  500,  55000, 50000,  0, N'/uploads/chai-tron-500ml-nap-go.jpg',   0),
    (1, N'500ml - Nap Nhua',500,  48000, NULL,  15, N'/uploads/chai-tron-500ml-nap-nhua.jpg', 0),
    (1, N'750ml - Nap Go',  750,  72000, NULL,  10, N'/uploads/chai-tron-750ml-nap-go.jpg',   0),
    (1, N'750ml - Nap Nhua',750,  65000, NULL,   8, N'/uploads/chai-tron-750ml-nap-nhua.jpg', 0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (2, N'250ml - Nap Bac', 250, 25000, NULL,  35, N'/uploads/chai-vuong-250ml-nap-bac.jpg', 1),
    (2, N'500ml - Nap Bac', 500, 42000, NULL,  20, N'/uploads/chai-vuong-500ml-nap-bac.jpg', 0),
    (2, N'750ml - Nap Bac', 750, 68000, 60000, 12, N'/uploads/chai-vuong-750ml-nap-bac.jpg', 0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (3, N'Trong suot - Cao 25cm',  NULL, 450000, NULL,   15, N'/uploads/binh-hoa-trong-25cm.jpg',  1),
    (3, N'Xanh cobalt - Cao 25cm', NULL, 520000, 480000,  8, N'/uploads/binh-hoa-cobalt-25cm.jpg', 0),
    (3, N'Nau khoi - Cao 25cm',    NULL, 490000, NULL,   10, N'/uploads/binh-hoa-nau-25cm.jpg',    0),
    (3, N'Trong suot - Cao 35cm',  NULL, 620000, NULL,    6, N'/uploads/binh-hoa-trong-35cm.jpg',  0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (4, N'200ml - Don chiec', NULL,  84000, NULL,   56, N'/uploads/ly-vang-200ml-don.jpg',  1),
    (4, N'350ml - Don chiec', NULL, 105000,  95000, 22, N'/uploads/ly-vang-350ml-don.jpg',  0),
    (4, N'Bo 6 cai - 200ml',  NULL, 480000, 430000, 15, N'/uploads/ly-vang-200ml-bo6.jpg',  0),
    (4, N'Bo 6 cai - 350ml',  NULL, 600000, 550000,  8, N'/uploads/ly-vang-350ml-bo6.jpg',  0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (5, N'300ml - Don chiec', 300,  35000, NULL,   80, N'/uploads/ly-highball-300ml.jpg', 1),
    (5, N'450ml - Don chiec', 450,  45000,  40000, 60, N'/uploads/ly-highball-450ml.jpg', 0),
    (5, N'Bo 6 cai - 300ml',  300, 195000, 175000, 20, N'/uploads/ly-highball-bo6.jpg',   0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (6, N'500ml - Pha le trang', 500, 850000, NULL,   0, N'/uploads/pasabahce-500ml.jpg', 1),
    (6, N'750ml - Pha le trang', 750, 980000, 890000, 0, N'/uploads/pasabahce-750ml.jpg', 0);
GO

INSERT INTO Addresses (userId, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, isDefault) VALUES
    (2, N'Nguyen Van An', '0912345678', N'Hai Phong', N'Ngo Quyen', N'May To', N'123 Tran Hung Dao', 1);

INSERT INTO Promotions (maCode, tenChuongTrinh, loaiGiam, giaTriGiam, donHangToiThieu, giamToiDa, soLanDung, tuNgay, denNgay) VALUES
    ('KHAIHANG', N'Khai truong DuaStore Hai Phong', 'PHAN_TRAM', 15, 200000, 100000, 200, '2026-01-01', '2026-12-31'),
    ('FREESHIP',  N'Mien phi van chuyen don tu 500k', 'SO_TIEN',  30000, 500000, NULL, NULL, '2026-01-01', '2026-12-31'),
    ('DECO50K',   N'Giam 50k don tu 300k',           'SO_TIEN',  50000, 300000, NULL,  100, '2026-01-01', '2026-12-31');

INSERT INTO orders (maDon, userId, addressId, snapTenNguoiNhan, snapSoDienThoai, snapDiaChi,
    tienHang, phiVanChuyen, tienGiam, tongThanhToan,
    phuongThucTT, phuongThucGiaoHang, trangThaiTT, trangThaiDon, promotionId, ngayDat) VALUES
    ('DUA-20260001', 2, 1, N'Nguyen Van An', '0912345678',
     N'123 Tran Hung Dao, May To, Ngo Quyen, Hai Phong',
     354000, 30000, 30000, 354000, 'CHUYEN_KHOAN', 'SHIP', 'CHUA_THANH_TOAN', 'CHO_XAC_NHAN', 2, GETDATE());

INSERT INTO order_items (orderId, productId, variantId, tenSanPham, tenBienThe, hinhAnhSP, donGia, soLuong, thanhTien) VALUES
    (1, 1,  3, N'Chai Thuy Tinh Dung Ruou Tron', N'100ml - Nap Go',    N'/uploads/chai-tron-100ml-nap-go.jpg',  23000, 2,  46000),
    (1, 4, 14, N'Ly Ruou Vang Pha Le Bohemia',   N'200ml - Don chiec', N'/uploads/ly-vang-200ml-don.jpg',       84000, 3, 252000),
    (1, 5, 18, N'Ly Highball Thuy Tinh Cao Cap',  N'300ml - Don chiec', N'/uploads/ly-highball-300ml.jpg',       35000, 1,  35000);

INSERT INTO PostCategories (tenDanhMuc, moTa, thuTu, ngayTao) VALUES
    (N'Huong Dan', N'Cac bai huong dan chon va bao quan do thuy tinh', 1, GETDATE()),
    (N'Xu Huong',  N'Xu huong trang tri va qua tang', 2, GETDATE());

INSERT INTO Posts (tieuDe, tomTat, noiDung, tacGiaId, danhMucId, trangThai, ngayTao, ngayCapNhat) VALUES
    (N'Huong dan chon chai thuy tinh theo muc dich su dung',
     N'Phan biet chai dung ruou, nuoc hoa, thuc pham - diem khac biet ve mieng chai, kieu nap va chat lieu.',
     N'<p>Noi dung huong dan day du...</p>', 1, 1, 'XUAT_BAN', GETDATE(), GETDATE()),
    (N'Uu diem cua thuy tinh Borosilicate so voi thuy tinh thuong',
     N'Tai sao thuy tinh Borosilicate lai duoc ua chuong trong nganh thuc pham va duoc pham?',
     N'<p>Noi dung bai viet...</p>', 1, 1, 'XUAT_BAN', GETDATE(), GETDATE()),
    (N'Top 5 mau binh trang tri duoc ua chuong nhat nam nay',
     N'Khao sat xu huong thi truong binh thuy tinh va pha le trang tri.',
     N'<p>Noi dung bai viet...</p>', 1, 2, 'NHAP', GETDATE(), GETDATE());

INSERT INTO Wishlists (userId, productId, ngayThem) VALUES
    (2, 3, GETDATE()),
    (2, 4, GETDATE());

INSERT INTO banners (title, image_url, link_url, active, display_order, description, created_at, updated_at)
VALUES (N'Banner DuaStore', N'/images/Banner 1 DuaStore.jpg', N'/san-pham', 1, 0,
        N'Banner chinh tren trang chu DuaStore', GETDATE(), GETDATE());

INSERT INTO store_info (tenCuaHang, soNha, duong, phuongXa, quanHuyen, tinhThanh, soDienThoai, email, isActive, isDefault, createdAt, updatedAt)
VALUES (N'DuaStore Hai Phong', N'123', N'Tran Hung Dao', N'May To', N'Ngo Quyen', N'Hai Phong',
        '0225.123.4567', 'contact@duastore.vn', 1, 1, GETDATE(), GETDATE());
GO

PRINT 'Seed du lieu co ban hoan tat!';
GO

-- ============================================================
-- VIEWS HO TRO
-- ============================================================
CREATE VIEW vw_DoanhThu AS
SELECT
    CAST(ngayDat AS DATE)      AS ngay,
    DATEPART(WEEK,  ngayDat)   AS tuan,
    DATEPART(MONTH, ngayDat)   AS thang,
    DATEPART(YEAR,  ngayDat)   AS nam,
    COUNT(*)                   AS soLuongDon,
    SUM(tongThanhToan)         AS tongDoanhThu
FROM orders
WHERE trangThaiDon NOT IN ('DA_HUY')
  AND trangThaiTT = 'DA_THANH_TOAN'
GROUP BY CAST(ngayDat AS DATE),
         DATEPART(WEEK,  ngayDat),
         DATEPART(MONTH, ngayDat),
         DATEPART(YEAR,  ngayDat);
GO

CREATE VIEW vw_ProductPrice AS
SELECT
    p.id,
    p.tenSanPham,
    p.danhMucId,
    p.hinhAnhChinh,
    p.trangThaiSanPham,
    p.leadTimeDays,
    p.isFeatured,
    p.isActive,
    pv.id                                  AS variantId,
    pv.tenBienThe,
    pv.dungTich,
    pv.giaGoc,
    ISNULL(pv.giaKhuyenMai, pv.giaGoc)    AS giaBan,
    pv.soLuongTon,
    ISNULL(pv.hinhAnh, p.hinhAnhChinh)    AS hinhAnhHienThi
FROM Products p
INNER JOIN ProductVariants pv
    ON pv.productId = p.id
   AND pv.isDefault = 1
   AND pv.isActive  = 1
WHERE p.isActive = 1
  AND p.trangThaiSanPham != 'NGUNG_BAN';
GO

CREATE VIEW vw_PostsPublished AS
SELECT id, tieuDe, tomTat, hinhAnh, tacGiaId, luotXem, ngayTao
FROM   Posts
WHERE  trangThai = 'XUAT_BAN'
ORDER BY ngayTao DESC
OFFSET 0 ROWS;
GO

PRINT '====================================================';
PRINT ' DuaStore Database - San sang su dung!';
PRINT ' Tong so bang  : 30 (gom 2 bang join: role_permissions, user_roles)';
PRINT ' Views         : vw_DoanhThu, vw_ProductPrice, vw_PostsPublished';
PRINT ' Tai khoan admin: admin / admin@123 (vai tro SUPER_ADMIN)';
PRINT ' File nay thay the hoan toan cho:';
PRINT '   - migration__voucher_indexes.sql';
PRINT '   - migration__saved_cart_items.sql';
PRINT '   - migration__add_gialucthem.sql';
PRINT '====================================================';
GO