/*
================================================================================
  ____  _   _    _    ____  _____ ___  ____  _____
 |  _ \| | | |  / \  / ___||_   _/ _ \|  _ \| ____|
 | | | | | | | / _ \ \___ \  | || | | | |_) |  _|
 | |_| | |_| |/ ___ \ ___) | | || |_| |  _ <| |___
 |____/ \___//_/   \_\____/  |_| \___/|_| \_\_____|
 
  DuaStore -- Do Thuy Tinh Decor, Hai Phong
================================================================================
  DANH SACH BANG (14 bang):
  [1]  Users            -- Tai khoan
  [2]  Categories       -- Danh muc (phan cap) [TK]
  [3]  Products         -- San pham goc [PLA]
  [4]  ProductImages    -- Bo suu tap anh san pham (Gallery Slider) [PLA]
  [5]  ProductVariants  -- Bien the: dung tich, gia, ton kho, hinh anh [PLA]
  [6]  Addresses        -- Dia chi giao hang [NHD]
  [7]  Promotions       -- Ma khuyen mai [BTM]
  [8]  Orders           -- Don hang [NHD]
  [9]  OrderItems       -- Chi tiet don hang [NHD]
  [10] Reviews          -- Danh gia [BTM]
  [11] CartItems        -- Gio hang [NXK]
  [12] Posts            -- Blog / Tin tuc [BTM]
  [13] Wishlists        -- Danh sach yeu thich [NXK]
  [14] banners          -- Banner trang chu
================================================================================
  HUONG DAN SU DUNG:
  1. Mo SQL Server Management Studio (SSMS)
  2. Chon "New Query"
  3. Dan toan bo noi dung file nay vao
  4. Nhan F5 hoac bam "Execute"
  5. Kiem tra khong co thong bao loi do
  6. Mat khau mac dinh: admin@123 (da BCrypt san)
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
DROP TABLE IF EXISTS Wishlists;
DROP TABLE IF EXISTS banners;
DROP TABLE IF EXISTS Posts;
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Promotions;
DROP TABLE IF EXISTS Addresses;
DROP TABLE IF EXISTS ProductVariants;
DROP TABLE IF EXISTS ProductImages;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Categories;
DROP TABLE IF EXISTS Users;
GO
 
-- ============================================================
-- [1] BANG: Users
-- ============================================================
-- Luu tru tai khoan nguoi dung va quan tri vien.
-- QUAN TRONG: Cot password KHONG luu plain text, chi luu BCrypt hash.
-- BCrypt hash cua "admin@123" = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyNM1Y5yK
-- Spring Security tu dong so sanh khi dang nhap.
-- Bang nay KHONG tinh diem (PLA phu trach nhung khong ghi diem rieng).
-- ============================================================
CREATE TABLE Users (
    id          INT            IDENTITY(1,1)  NOT NULL,
    username    NVARCHAR(50)                  NOT NULL,
    email       NVARCHAR(100)                 NOT NULL,
    password    NVARCHAR(255)                 NOT NULL,   -- BCrypt hash
    hoTen       NVARCHAR(100)                 NOT NULL,
    soDienThoai NVARCHAR(15)                  NULL,
    role        NVARCHAR(20)                  NOT NULL   DEFAULT 'USER',  -- 'USER' hoac 'ADMIN'
    isActive    BIT                           NOT NULL   DEFAULT 1,       -- 0 = khoa tai khoan
    ngayTao     DATETIME2(0)                  NOT NULL   DEFAULT GETDATE(),
    resetToken      NVARCHAR(255)                  NULL,       -- Token dat lai mat khau
    resetTokenExpiry DATETIME2(0)                   NULL,       -- Thoi gian het han token
    ngayCapNhat     DATETIME2(0)                   NULL,
  
    CONSTRAINT PK_Users          PRIMARY KEY (id),
    CONSTRAINT UQ_Users_Email    UNIQUE (email),       -- Khong cho 2 tai khoan trung email
    CONSTRAINT UQ_Users_Username UNIQUE (username),    -- Khong cho 2 tai khoan trung username
    CONSTRAINT CK_Users_Role     CHECK (role IN ('USER','ADMIN'))  -- Chi chap nhan 2 gia tri nay
);
GO
 
-- ============================================================
-- [2] BANG: Categories
-- ============================================================
-- Quan ly danh muc san pham theo cay phan cap.
-- Vi du: "Chai Thuy Tinh" (cha) → "Chai Ruou", "Chai Nuoc Hoa" (con)
-- Ky thuat: cot parentId tu tham chieu ve chinh bang nay (self-join).
-- Neu parentId = NULL → day la danh muc goc.
-- Phan cong: [NXK] Nguyen Xuan Khang
-- ============================================================
CREATE TABLE Categories (
    id              INT            IDENTITY(1,1)  NOT NULL,
    tenDanhMuc      NVARCHAR(100)                 NOT NULL,
    moTa            NVARCHAR(500)                 NULL,
    parentId        INT                           NULL,    -- NULL = danh muc goc (cap 1)
    thuTuHienThi    INT                           NOT NULL DEFAULT 0,  -- Sort order tren menu
    isActive        BIT                           NOT NULL DEFAULT 1,
 
    CONSTRAINT PK_Categories        PRIMARY KEY (id),
    CONSTRAINT FK_Categories_Parent FOREIGN KEY (parentId)
        REFERENCES Categories(id)   -- Self-join: tham chieu chinh no
);
GO
 
-- ============================================================
-- [3] BANG: Products
-- ============================================================
-- San pham GOC — KHONG luu gia, KHONG luu ton kho truc tiep.
-- Gia va ton kho luu o bang ProductVariants (tach biet theo tung bien the).
-- Vi du: "Chai Thuy Tinh Dung Ruou Tron" la 1 Product,
--         nhung "250ml - Nap Go" va "250ml - Nap Nhua" la 2 ProductVariant khac nhau.
-- Phan cong: [PLA] Phung Le Anh
-- ============================================================
CREATE TABLE Products (
    id                  INT             IDENTITY(1,1)  NOT NULL,
    tenSanPham          NVARCHAR(200)                  NOT NULL,
    moTa                NVARCHAR(MAX)                  NULL,
    chatLieu            NVARCHAR(100)                  NULL,   -- VD: Thuy tinh Borosilicate, Pha le
    xuatXu              NVARCHAR(100)                  NULL,   -- VD: Viet Nam, Tho Nhi Ky, Chau Au
    mucDichSuDung       NVARCHAR(100)                  NULL,   -- VD: Dung do uong, Trang tri, Qua tang
    thuongHieu          NVARCHAR(100)                  NULL,   -- VD: Bohemia, Libbey, Ocean
    kinhLoai            NVARCHAR(100)                  NULL,   -- VD: Soda-lime, Borosilicate, Pha le cat canh
 
    danhMucId           INT                            NOT NULL,   -- FK → Categories
 
    -- Anh chinh hien thi khi CHUA chon bien the cu the
    -- Khi khach chon bien the → se doi sang hinhAnh cua bien the do
    hinhAnhChinh        NVARCHAR(255)                  NULL,
 
    -- Trang thai san pham (bo sung tu phân tich Chalo Glass)
    -- DANG_BAN  : Co san, dat hang binh thuong
    -- DAT_TRUOC : Pre-order — hang chua ve kho, khach dat truoc
    -- NGUNG_BAN : An, khong hien thi tren website (soft delete)
    trangThaiSanPham    NVARCHAR(20)                   NOT NULL DEFAULT 'DANG_BAN',
 
    -- So ngay giao du kien khi DAT_TRUOC (NULL neu DANG_BAN)
    -- Vi du: 10 → "Du kien giao trong 10 ngay ke tu dat hang"
    -- Khac phuc han che cua Chalo Glass (ho chua co thong tin nay)
    leadTimeDays        INT                            NULL,
 
    isFeatured          BIT                            NOT NULL DEFAULT 0,  -- 1 = hien thi trang chu
    isActive            BIT                            NOT NULL DEFAULT 1,
    ngayTao             DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
    ngayCapNhat         DATETIME2(0)                   NULL,
 
    CONSTRAINT PK_Products           PRIMARY KEY (id),
    CONSTRAINT FK_Products_DanhMuc   FOREIGN KEY (danhMucId) REFERENCES Categories(id),
    CONSTRAINT CK_Products_TrangThai CHECK (trangThaiSanPham IN ('DANG_BAN','DAT_TRUOC','NGUNG_BAN'))
);
GO
 
-- ============================================================
-- [4] BANG: ProductImages (Bo sung tu yeu cau Gallery Slider)
-- ============================================================
-- Bo suu tap anh san pham. Admin upload nhieu anh cung luc.
-- Anh duoc hien thi trong Image Gallery Slider phia Client.
-- sortOrder: sap xep thu tu hien thi (tang dan).
-- Khi khong co anh nao trong bang nay → Frontend fallback sang
-- hinhAnhChinh cua Products + hinhAnh cua ProductVariants.
-- Phan cong: [PLA] Phung Le Anh
-- ============================================================
CREATE TABLE ProductImages (
    id          INT            IDENTITY(1,1)  NOT NULL,
    productId   INT                           NOT NULL,
    imageUrl    NVARCHAR(500)                 NOT NULL,   -- Duong dan /uploads/...
    sortOrder   INT                           NOT NULL DEFAULT 0,
    isActive    BIT                           NOT NULL DEFAULT 1,
    createdAt   DATETIME2(0)                  NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_ProductImages        PRIMARY KEY (id),
    CONSTRAINT FK_ProductImages_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE
);
GO

-- ============================================================
-- [5] BANG: ProductVariants
-- ============================================================
-- Moi bien the la 1 SKU (Stock Keeping Unit) cu the.
-- Vi du: Chai Tron 250ml - Nap Go la 1 bien the, co gia va ton kho rieng.
--
-- Cot hinhAnh:
--   Luu duong dan anh cua tung bien the cu the.
--   Vi du:  bien the "250ml - Nap Go"  → /uploads/chai-tron-nap-go.jpg
--           bien the "250ml - Nap Nhua" → /uploads/chai-tron-nap-nhua.jpg
--   Nap go va nap nhua nhin rat khac nhau ve hinh dang, mau sac.
--   Khi khach chon bien the → JavaScript se goi API lay hinhAnh nay
--   va doi anh chinh san pham ma KHONG reload trang.
--   Neu hinhAnh = NULL → giu nguyen hinhAnhChinh cua Products.
--   Tuong tu co che "Swatch" cua Coolmate (chon mau → anh doi).
--
-- Phan cong: [PLA] Phung Le Anh
-- ============================================================
CREATE TABLE ProductVariants (
    id              INT             IDENTITY(1,1)  NOT NULL,
    productId       INT                            NOT NULL,    -- FK → Products
 
    -- Ten bien the hien thi: "250ml - Nap Go", "500ml - Nap Nhua", "Xanh cobalt - Cao 25cm"
    tenBienThe      NVARCHAR(150)                  NOT NULL,
 
    -- Dung tich (ml). NULL neu bien the khong phan loai theo the tich
    -- Vi du: Binh hoa phan theo mau sac/chieu cao → dungTich = NULL
    dungTich        INT                            NULL,
 
    -- Gia goc (truoc khi giam)
    giaGoc          DECIMAL(12,0)                  NOT NULL,
 
    -- Gia khuyen mai (NULL = khong co KM). Nen < giaGoc.
    -- Frontend: if giaKhuyenMai != null → gach ngang giaGoc, hien thi giaKhuyenMai
    giaKhuyenMai    DECIMAL(12,0)                  NULL,
 
    -- Ton kho thuc te. Se tru khi khach dat hang thanh cong.
    -- OrderService.placeOrder() giam soLuongTon trong @Transactional
    soLuongTon      INT                            NOT NULL DEFAULT 0,
 
    -- Anh rieng cho tung bien the
    -- Duong dan: /uploads/ten-file.jpg
    -- Neu NULL → Frontend dung hinhAnhChinh cua Products thay the
    hinhAnh         NVARCHAR(255)                  NULL,
 
    -- Bien the mac dinh se hien thi khi vao trang chi tiet san pham lan dau
    isDefault       BIT                            NOT NULL DEFAULT 0,
 
    isActive        BIT                            NOT NULL DEFAULT 1,
 
    CONSTRAINT PK_ProductVariants         PRIMARY KEY (id),
    CONSTRAINT FK_ProductVariants_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE,  -- Xoa SP → tu dong xoa bien the
    CONSTRAINT CK_ProductVariants_Gia     CHECK (giaGoc >= 0),
    CONSTRAINT CK_ProductVariants_SoLuong CHECK (soLuongTon >= 0)
);
GO
 
-- ============================================================
-- [6] BANG: Addresses
-- ============================================================
-- Luu nhieu dia chi giao hang cua 1 nguoi dung.
-- isDefault = 1: dia chi mac dinh tu dong dien vao form checkout.
-- QUAN TRONG: Khi tao don hang, PHAI snapshot dia chi vao Orders
--             (snapTenNguoiNhan, snapDiaChi, snapSoDienThoai).
--             Neu khong snapshot, khi khach sua dia chi → lich su don hang bi sai.
-- Phan cong: [NXK] Nguyen Xuan Khang
-- ============================================================
CREATE TABLE Addresses (
    id              INT            IDENTITY(1,1)  NOT NULL,
    userId          INT                           NOT NULL,
    tenNguoiNhan    NVARCHAR(100)                 NOT NULL,
    soDienThoai     NVARCHAR(15)                  NOT NULL,
    tinhThanh       NVARCHAR(100)                 NOT NULL,
    quanHuyen       NVARCHAR(100)                 NOT NULL,
    phuongXa        NVARCHAR(100)                 NOT NULL,
    diaChiCuThe     NVARCHAR(200)                 NOT NULL,  -- So nha, ten duong
    isDefault       BIT                           NOT NULL DEFAULT 0,
 
    CONSTRAINT PK_Addresses      PRIMARY KEY (id),
    CONSTRAINT FK_Addresses_User FOREIGN KEY (userId)
        REFERENCES Users(id) ON DELETE CASCADE  -- Xoa User → tu dong xoa dia chi
);
GO
 
-- ============================================================
-- [7] BANG: Promotions
-- ============================================================
-- Quan ly ma voucher giam gia.
-- 2 loai giam: PHAN_TRAM (%) va SO_TIEN (dong co dinh).
-- Vi du PHAN_TRAM: ma KHAIHANG giam 15%, toi da 100.000d, don tu 200.000d.
-- Vi du SO_TIEN:   ma DECO50K giam thang 50.000d, don tu 300.000d.
-- Phan cong: [BTM] Bui Tran Minh
-- ============================================================
CREATE TABLE Promotions (
    id              INT             IDENTITY(1,1)  NOT NULL,
    maCode          NVARCHAR(50)                   NOT NULL,  -- Khach nhap ma nay
    tenChuongTrinh  NVARCHAR(200)                  NOT NULL,
    loaiGiam        NVARCHAR(15)                   NOT NULL,  -- 'PHAN_TRAM' hoac 'SO_TIEN'
    giaTriGiam      DECIMAL(10,2)                  NOT NULL,  -- 15 (%) hoac 50000 (dong)
    donHangToiThieu DECIMAL(12,0)                  NOT NULL DEFAULT 0,    -- Don tu bao nhieu moi ap duoc
    giamToiDa       DECIMAL(12,0)                  NULL,      -- Giam toi da bao nhieu (PHAN_TRAM)
    soLanDung       INT                            NULL,      -- Gioi han tong so lan su dung (NULL = khong gioi han)
    daDung          INT                            NOT NULL DEFAULT 0,    -- Da dung bao nhieu lan roi
    tuNgay          DATETIME2(0)                   NOT NULL,  -- Bat dau hieu luc
    denNgay         DATETIME2(0)                   NOT NULL,  -- Het hieu luc
    isActive        BIT                            NOT NULL DEFAULT 1,
 
    CONSTRAINT PK_Promotions        PRIMARY KEY (id),
    CONSTRAINT UQ_Promotions_Code   UNIQUE (maCode),          -- Ma voucher khong trung nhau
    CONSTRAINT CK_Promotions_Loai   CHECK (loaiGiam IN ('PHAN_TRAM','SO_TIEN')),
    CONSTRAINT CK_Promotions_NgayGT CHECK (tuNgay < denNgay)  -- Ngay bat dau phai truoc ngay ket thuc
);
GO
 
-- ============================================================
-- [8] BANG: Orders
-- ============================================================
-- Don hang. Co 2 trang thai rieng biet:
--   trangThaiDon: trang thai xu ly don (CHO_XAC_NHAN → DA_GIAO)
--   trangThaiTT:  trang thai thanh toan (CHUA_THANH_TOAN → DA_THANH_TOAN)
--
-- SNAPSHOT DIA CHI: Cac cot snapXxx luu lai thong tin nguoi nhan TAI THOI DIEM DAT HANG.
-- Neu khach sau nay sua dia chi → don cu van hien thi dia chi dung luc giao.
-- Day la thiet ke chuan cho moi he thong TMDT.
-- Phan cong: [NHD] Nguyen Huy Dung
-- ============================================================
CREATE TABLE Orders (
    id                  INT             IDENTITY(1,1)  NOT NULL,
    maDon               NVARCHAR(20)                   NOT NULL,  -- VD: DUA-20250001
    userId              INT                            NOT NULL,
    addressId           INT                            NULL,      -- FK → Addresses (co the NULL neu dia chi bi xoa)
 
    -- SNAPSHOT dia chi (bat bien, khong thay doi theo thoi gian)
    snapTenNguoiNhan    NVARCHAR(100)                  NOT NULL,
    snapSoDienThoai     NVARCHAR(15)                   NOT NULL,
    snapDiaChi          NVARCHAR(500)                  NOT NULL,  -- Dia chi day du da ghep san
 
    -- Tien
    tienHang            DECIMAL(12,0)                  NOT NULL,  -- Tong gia san pham (truoc giam)
    phiVanChuyen        DECIMAL(10,0)                  NOT NULL DEFAULT 0,
    tienGiam            DECIMAL(10,0)                  NOT NULL DEFAULT 0,  -- Tu voucher
    tongThanhToan       DECIMAL(12,0)                  NOT NULL,  -- = tienHang + phiVC - tienGiam
 
    -- Phuong thuc
    phuongThucTT        NVARCHAR(20)                   NOT NULL,  -- COD / CHUYEN_KHOAN / VNPAY
    phuongThucGiaoHang  NVARCHAR(20)                   NOT NULL DEFAULT 'SHIP',  -- SHIP / NHAN_TAI_CONG
 
    -- Trang thai
    trangThaiTT         NVARCHAR(25)                   NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    trangThaiDon        NVARCHAR(20)                   NOT NULL DEFAULT 'CHO_XAC_NHAN',
 
    promotionId         INT                            NULL,      -- Voucher duoc dung (neu co)
    ghiChu              NVARCHAR(500)                  NULL,      -- Ghi chu cua khach
    ngayDat             DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
    ngayCapNhat         DATETIME2(0)                   NULL,
 
    CONSTRAINT PK_Orders           PRIMARY KEY (id),
    CONSTRAINT UQ_Orders_MaDon     UNIQUE (maDon),
    CONSTRAINT FK_Orders_User      FOREIGN KEY (userId)      REFERENCES Users(id),
    CONSTRAINT FK_Orders_Address   FOREIGN KEY (addressId)   REFERENCES Addresses(id),
    CONSTRAINT FK_Orders_Promotion FOREIGN KEY (promotionId) REFERENCES Promotions(id),
    CONSTRAINT CK_Orders_TT        CHECK (phuongThucTT       IN ('CHUYEN_KHOAN','COD','VNPAY')),
    CONSTRAINT CK_Orders_GH        CHECK (phuongThucGiaoHang IN ('SHIP','NHAN_TAI_CONG')),
    CONSTRAINT CK_Orders_ThanhToan CHECK (trangThaiTT        IN ('CHUA_THANH_TOAN','DA_THANH_TOAN','HOAN_TIEN')),
    CONSTRAINT CK_Orders_TrangThai CHECK (trangThaiDon       IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HOAN_THANH','DA_HUY'))
);
GO
 
-- ============================================================
-- [9] BANG: OrderItems
-- ============================================================
-- Chi tiet tung san pham trong 1 don hang.
-- SNAPSHOT: Cac cot tenSanPham, tenBienThe, donGia la snapshot tai thoi diem dat.
-- Neu sau nay san pham bi sua gia hoac xoa → lich su don hang van hien thi dung.
-- Phan cong: [NHD] Nguyen Huy Dung
-- ============================================================
CREATE TABLE OrderItems (
    id              INT             IDENTITY(1,1)  NOT NULL,
    orderId         INT                            NOT NULL,
    productId       INT                            NULL,      -- NULL neu san pham bi xoa (ON DELETE SET NULL)
    variantId       INT                            NULL,      -- Giu tham chieu de bao toan lich su don hang
 
    -- SNAPSHOT (du lieu chot tai thoi diem dat hang)
    tenSanPham      NVARCHAR(200)                  NOT NULL,
    tenBienThe      NVARCHAR(150)                  NULL,      -- VD: "250ml - Nap Go"
    hinhAnhSP       NVARCHAR(255)                  NULL,      -- Anh bien the tai thoi diem dat (snapshot)
    donGia          DECIMAL(12,0)                  NOT NULL,  -- Gia ban thuc te luc do
    soLuong         INT                            NOT NULL,
    thanhTien       DECIMAL(12,0)                  NOT NULL,  -- = donGia × soLuong
 
    CONSTRAINT PK_OrderItems         PRIMARY KEY (id),
    CONSTRAINT FK_OrderItems_Order   FOREIGN KEY (orderId)
        REFERENCES Orders(id) ON DELETE CASCADE,              -- Xoa Order → xoa het OrderItems
    CONSTRAINT FK_OrderItems_Product FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE SET NULL,           -- SP bi xoa → giu OrderItem, set NULL
    CONSTRAINT FK_OrderItems_Variant FOREIGN KEY (variantId)
        REFERENCES ProductVariants(id) ON DELETE NO ACTION,  -- Tranh multiple cascade path tren SQL Server
    CONSTRAINT CK_OrderItems_Qty     CHECK (soLuong > 0)
);
GO
 
-- ============================================================
-- [10] BANG: Reviews
-- ============================================================
-- Danh gia san pham tu 1-5 sao kem binh luan.
-- isApproved = 0 mac dinh → Admin phai duyet moi hien thi tren website.
-- UNIQUE(userId, productId): moi khach chi duoc review 1 lan tren 1 san pham.
-- Phan cong: [BTM] Bui Tran Minh
-- ============================================================
CREATE TABLE Reviews (
    id          INT             IDENTITY(1,1)  NOT NULL,
    productId   INT                            NOT NULL,
    userId      INT                            NOT NULL,
    danhGia     TINYINT                        NOT NULL,       -- 1 den 5 sao
    binhLuan    NVARCHAR(1000)                 NULL,
    isApproved  BIT                            NOT NULL DEFAULT 0,  -- 0 = chua duyet, 1 = da duyet
    ngayTao     DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
 
    CONSTRAINT PK_Reviews             PRIMARY KEY (id),
    CONSTRAINT UQ_Reviews_UserProduct UNIQUE (userId, productId),  -- 1 khach = 1 review / SP
    CONSTRAINT FK_Reviews_Product     FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE,
    CONSTRAINT FK_Reviews_User        FOREIGN KEY (userId)
        REFERENCES Users(id),
    CONSTRAINT CK_Reviews_DanhGia     CHECK (danhGia BETWEEN 1 AND 5)
);
GO
 
-- ============================================================
-- [11] BANG: CartItems
-- ============================================================
-- Gio hang luu trong DB cho user da dang nhap.
-- UNIQUE(userId, variantId): dam bao moi bien the chi xuat hien 1 lan trong gio.
-- Neu khach them cung 1 bien the lan 2 → Service nen CONG them soLuong, khong INSERT moi.
-- variantId KHONG the NULL: khach phai chon cu the bien the nao truoc khi vao gio.
-- Phan cong: [NHD] Nguyen Huy Dung
-- ============================================================
CREATE TABLE CartItems (
    id          INT             IDENTITY(1,1)  NOT NULL,
    userId      INT                            NOT NULL,
    productId   INT                            NOT NULL,
    variantId   INT                            NOT NULL,    -- PHAI chon bien the cu the
    soLuong     INT                            NOT NULL DEFAULT 1,
    ngayThem    DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
 
    CONSTRAINT PK_CartItems             PRIMARY KEY (id),
    CONSTRAINT UQ_CartItems_UserVariant UNIQUE (userId, variantId),  -- 1 bien the = 1 dong
    CONSTRAINT FK_CartItems_User        FOREIGN KEY (userId)
        REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Product     FOREIGN KEY (productId) REFERENCES Products(id),
    CONSTRAINT FK_CartItems_Variant     FOREIGN KEY (variantId) REFERENCES ProductVariants(id),
    CONSTRAINT CK_CartItems_SoLuong     CHECK (soLuong > 0)
);
GO
 
-- ============================================================
-- [12] BANG: Banners (quan ly banner trang chu)
-- ============================================================
CREATE TABLE Banners (
    id              INT             IDENTITY(1,1) NOT NULL,
    title           NVARCHAR(200)                  NOT NULL,
    image_url       NVARCHAR(500)                  NOT NULL,
    link_url        NVARCHAR(1000)                 NULL,
    active          BIT                            NOT NULL DEFAULT 1,
    display_order   INT                            NOT NULL DEFAULT 0,
    start_date      DATETIME2(0)                   NULL,
    end_date        DATETIME2(0)                   NULL,
    description     NVARCHAR(500)                  NULL,
    created_at      DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Banners PRIMARY KEY (id),
    CONSTRAINT CK_Banners_DisplayOrder CHECK (display_order >= 0),
    CONSTRAINT CK_Banners_Period CHECK (end_date IS NULL OR start_date IS NULL OR end_date > start_date)
);
GO

INSERT INTO Banners (title, image_url, link_url, active, display_order, description)
VALUES (N'Banner DuaStore', N'/images/Banner 1 DuaStore.jpg', N'/san-pham', 1, 0,
        N'Banner chính trên trang chủ DuaStore');
GO

-- ============================================================
-- [13] BANG: Posts  (Bo sung tu bao cao khao sat: PC Market + Chalo Glass)
-- ============================================================
-- Bai viet Blog / Tin tuc / Huong dan chon thuy tinh.
-- 3 trang thai: NHAP (ban nhap) → XUAT_BAN (cong khai) → AN (an trang)
-- luotXem tang +1 moi lan khach vao GET /blog/{id}.
-- Phan cong: [BTM] Bui Tran Minh
-- ============================================================
CREATE TABLE Posts (
    id          INT             IDENTITY(1,1)  NOT NULL,
    tieuDe      NVARCHAR(300)                  NOT NULL,
    tomTat      NVARCHAR(500)                  NULL,       -- Hien thi tren trang danh sach /blog
    noiDung     NVARCHAR(MAX)                  NULL,       -- Noi dung day du (HTML)
    hinhAnh     NVARCHAR(255)                  NULL,       -- Anh thumbnail bai viet
    tacGiaId    INT                            NULL,       -- FK → Users (ON DELETE SET NULL)
    trangThai   NVARCHAR(15)                   NOT NULL DEFAULT 'NHAP',  -- NHAP / XUAT_BAN / AN
    luotXem     INT                            NOT NULL DEFAULT 0,
    ngayTao     DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
    ngayCapNhat DATETIME2(0)                   NULL,
 
    CONSTRAINT PK_Posts           PRIMARY KEY (id),
    CONSTRAINT FK_Posts_TacGia    FOREIGN KEY (tacGiaId)
        REFERENCES Users(id) ON DELETE SET NULL,  -- Xoa admin → giu bai viet, set tacGiaId = NULL
    CONSTRAINT CK_Posts_TrangThai CHECK (trangThai IN ('NHAP','XUAT_BAN','AN'))
);
GO
 
-- ============================================================
-- [14] BANG: Wishlists  (Bo sung tu bao cao khao sat: Chalo Glass)
-- ============================================================
-- Danh sach san pham yeu thich cua khach.
-- UNIQUE(userId, productId): moi SP chi luu 1 lan / user.
-- Luu productId (SP goc), KHONG luu variantId.
-- Ly do: Khi dat mua tu Wishlist, khach moi can chon bien the cu the.
-- Phan cong: [NXK] Nguyen Xuan Khang
-- ============================================================
CREATE TABLE Wishlists (
    id          INT             IDENTITY(1,1)  NOT NULL,
    userId      INT                            NOT NULL,
    productId   INT                            NOT NULL,
    ngayThem    DATETIME2(0)                   NOT NULL DEFAULT GETDATE(),
 
    CONSTRAINT PK_Wishlists             PRIMARY KEY (id),
    CONSTRAINT UQ_Wishlists_UserProduct UNIQUE (userId, productId),
    CONSTRAINT FK_Wishlists_User        FOREIGN KEY (userId)
        REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT FK_Wishlists_Product     FOREIGN KEY (productId)
        REFERENCES Products(id) ON DELETE CASCADE
);
GO
 
-- ============================================================
-- INDEXES (toi uu hoa toc do query)
-- ============================================================
-- Index giup SQL Server tim kiem nhanh hon, giong nhu muc luc sach.
-- Khong co Index → SQL phai doc toan bo bang (Table Scan → cham).
-- Co Index → SQL nhay thang den vi tri can doc (Index Seek → nhanh).
 
-- San pham: Tim theo danh muc, trang thai, muc dich, san pham noi bat
CREATE INDEX IX_Products_DanhMuc    ON Products        (danhMucId, isActive);
CREATE INDEX IX_Products_TrangThai  ON Products        (trangThaiSanPham, isActive);
CREATE INDEX IX_Products_MucDich    ON Products        (mucDichSuDung, isActive);
CREATE INDEX IX_Products_Featured   ON Products        (isFeatured, isActive);
CREATE INDEX IX_Banners_ActiveOrder ON Banners         (active, display_order, start_date, end_date);
 
-- Bien the: Bo loc the tich, lay bien the mac dinh
-- Index nay van hoat dong tot du da them cot hinhAnh (khong can INDEX rieng cho hinhAnh)
CREATE INDEX IX_Variants_DungTich   ON ProductVariants (productId, dungTich, isActive);
CREATE INDEX IX_Variants_Default    ON ProductVariants (productId, isDefault);
 
-- Don hang: Tim theo user, trang thai xu ly
CREATE INDEX IX_Orders_User         ON Orders          (userId, ngayDat DESC);
CREATE INDEX IX_Orders_TrangThai    ON Orders          (trangThaiDon, trangThaiTT);
 
-- Gio hang, Wishlist: Tim theo user
CREATE INDEX IX_CartItems_User      ON CartItems       (userId);
CREATE INDEX IX_Wishlists_User      ON Wishlists       (userId);
 
-- Danh gia: Tim danh gia da duyet cua 1 san pham
CREATE INDEX IX_Reviews_Product     ON Reviews         (productId, isApproved);

-- Hinh anh san pham: Tim gallery cua 1 san pham
CREATE INDEX IX_ProductImages_Product ON ProductImages (productId, isActive);

-- Chi tiet don hang: Kiem tra nguoi dung da mua san pham va thanh toan chua
CREATE INDEX IX_OrderItems_ProductUser ON OrderItems   (productId, orderId)
    INCLUDE (soLuong);

-- Blog: Tim bai viet da xuat ban, sap xep theo ngay moi nhat
CREATE INDEX IX_Posts_TrangThai     ON Posts           (trangThai, ngayTao DESC);
GO
 
-- ============================================================
-- DU LIEU MAU
-- ============================================================
 
-- Danh muc goc (cap 1)
INSERT INTO Categories (tenDanhMuc, moTa, thuTuHienThi) VALUES
    (N'Chai Thuy Tinh',  N'Cac loai chai thuy tinh dung ruou, nuoc hoa, thuc pham', 1),
    (N'Hu Thuy Tinh',    N'Hu dung do kho, thuc pham, gia vi',                      2),
    (N'Binh Trang Tri',  N'Binh hoa, binh decor, trung bay nha cua',               3),
    (N'Ly & Coc',        N'Cac loai ly coc thuy tinh cao cap',                      4),
    (N'Qua Tang',        N'Bo set qua tang thuy tinh sang trong',                   5);
 
-- Danh muc con (cap 2)
INSERT INTO Categories (tenDanhMuc, parentId, thuTuHienThi) VALUES
    (N'Chai Ruou',    1, 1), (N'Chai Nuoc Hoa', 1, 2), (N'Chai Thuc Pham', 1, 3),
    (N'Binh Hoa',     3, 1), (N'Binh Decor',    3, 2),
    (N'Ly Ruou Vang', 4, 1), (N'Ly Whisky',     4, 2), (N'Ly Nuoc',       4, 3),
    (N'Ly Champagne', 4, 4), (N'Ly Highball',   4, 5);
 
-- Tai khoan mac dinh
-- Mat khau "admin@123" da duoc BCrypt hash san
INSERT INTO Users (username, email, password, hoTen, soDienThoai, role) VALUES
    ('admin', 'admin@duastore.vn',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyNM1Y5yK',
     N'Quan Tri Vien', '0901234567', 'ADMIN');
INSERT INTO Users (username, email, password, hoTen, soDienThoai, role) VALUES
    ('nguyenvan', 'nguyen@gmail.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVyNM1Y5yK',
     N'Nguyen Van An', '0912345678', 'USER');
 
-- San pham mau
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
 
-- Dat leadTimeDays cho san pham Pre-order
UPDATE Products SET leadTimeDays = 10 WHERE tenSanPham LIKE N'%Pasabahce%';
 
-- Bien the san pham (them cot hinhAnh)
-- Ghi chu: Duong dan anh chi la vi du, thay bang duong dan anh thuc te khi upload
-- NULL = dung hinhAnhChinh cua bang Products thay the
 
-- SP1: Chai Thuy Tinh Dung Ruou Tron
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (1, N'50ml - Nap Go',    50,  15000, NULL,  20, N'/uploads/chai-tron-50ml-nap-go.jpg',    0),
    (1, N'50ml - Nap Nhua',  50,  13000, NULL,  15, N'/uploads/chai-tron-50ml-nap-nhua.jpg',  0),
    (1, N'100ml - Nap Go',  100,  23000, NULL,  50, N'/uploads/chai-tron-100ml-nap-go.jpg',   1),  -- Mac dinh
    (1, N'100ml - Nap Nhua',100,  20000, NULL,  40, N'/uploads/chai-tron-100ml-nap-nhua.jpg', 0),
    (1, N'250ml - Nap Go',  250,  38000, 34000, 30, N'/uploads/chai-tron-250ml-nap-go.jpg',   0),
    (1, N'250ml - Nap Nhua',250,  32000, NULL,  25, N'/uploads/chai-tron-250ml-nap-nhua.jpg', 0),
    (1, N'500ml - Nap Go',  500,  55000, 50000,  0, N'/uploads/chai-tron-500ml-nap-go.jpg',   0),  -- Het hang
    (1, N'500ml - Nap Nhua',500,  48000, NULL,  15, N'/uploads/chai-tron-500ml-nap-nhua.jpg', 0),
    (1, N'750ml - Nap Go',  750,  72000, NULL,  10, N'/uploads/chai-tron-750ml-nap-go.jpg',   0),
    (1, N'750ml - Nap Nhua',750,  65000, NULL,   8, N'/uploads/chai-tron-750ml-nap-nhua.jpg', 0);
 
-- SP2: Chai Thuy Tinh Vuong Co Lenh
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (2, N'250ml - Nap Bac', 250, 25000, NULL,  35, N'/uploads/chai-vuong-250ml-nap-bac.jpg', 1),
    (2, N'500ml - Nap Bac', 500, 42000, NULL,  20, N'/uploads/chai-vuong-500ml-nap-bac.jpg', 0),
    (2, N'750ml - Nap Bac', 750, 68000, 60000, 12, N'/uploads/chai-vuong-750ml-nap-bac.jpg', 0);
 
-- SP3: Binh Hoa Pha Le Cat Canh (phan theo mau sac va chieu cao)
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (3, N'Trong suot - Cao 25cm',  NULL, 450000, NULL,   15, N'/uploads/binh-hoa-trong-25cm.jpg',  1),
    (3, N'Xanh cobalt - Cao 25cm', NULL, 520000, 480000,  8, N'/uploads/binh-hoa-cobalt-25cm.jpg', 0),
    (3, N'Nau khoi - Cao 25cm',    NULL, 490000, NULL,   10, N'/uploads/binh-hoa-nau-25cm.jpg',    0),
    (3, N'Trong suot - Cao 35cm',  NULL, 620000, NULL,    6, N'/uploads/binh-hoa-trong-35cm.jpg',  0);
 
-- SP4: Ly Ruou Vang Pha Le Bohemia
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (4, N'200ml - Don chiec', NULL,  84000, NULL,   56, N'/uploads/ly-vang-200ml-don.jpg',  1),
    (4, N'350ml - Don chiec', NULL, 105000,  95000, 22, N'/uploads/ly-vang-350ml-don.jpg',  0),
    (4, N'Bo 6 cai - 200ml',  NULL, 480000, 430000, 15, N'/uploads/ly-vang-200ml-bo6.jpg',  0),
    (4, N'Bo 6 cai - 350ml',  NULL, 600000, 550000,  8, N'/uploads/ly-vang-350ml-bo6.jpg',  0);
 
-- SP5: Ly Highball
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (5, N'300ml - Don chiec', 300,  35000, NULL,   80, N'/uploads/ly-highball-300ml.jpg', 1),
    (5, N'450ml - Don chiec', 450,  45000,  40000, 60, N'/uploads/ly-highball-450ml.jpg', 0),
    (5, N'Bo 6 cai - 300ml',  300, 195000, 175000, 20, N'/uploads/ly-highball-bo6.jpg',   0);
 
-- SP6: Pre-order (soLuongTon = 0 vi hang chua ve kho)
INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (6, N'500ml - Pha le trang', 500, 850000, NULL,   0, N'/uploads/pasabahce-500ml.jpg', 1),
    (6, N'750ml - Pha le trang', 750, 980000, 890000, 0, N'/uploads/pasabahce-750ml.jpg', 0);
 
-- Dia chi mau
INSERT INTO Addresses (userId, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, isDefault) VALUES
    (2, N'Nguyen Van An', '0912345678', N'Hai Phong', N'Ngo Quyen', N'May To', N'123 Tran Hung Dao', 1);
 
-- Ma khuyen mai mau
INSERT INTO Promotions (maCode, tenChuongTrinh, loaiGiam, giaTriGiam, donHangToiThieu, giamToiDa, soLanDung, tuNgay, denNgay) VALUES
    ('KHAIHANG', N'Khai truong DuaStore Hai Phong', 'PHAN_TRAM', 15, 200000, 100000, 200, '2026-01-01', '2026-12-31'),
    ('FREESHIP',  N'Mien phi van chuyen don tu 500k', 'SO_TIEN',  30000, 500000, NULL, NULL, '2026-01-01', '2026-12-31'),
    ('DECO50K',   N'Giam 50k don tu 300k',           'SO_TIEN',  50000, 300000, NULL,  100, '2026-01-01', '2026-12-31');
 
-- Don hang mau
INSERT INTO Orders (maDon, userId, addressId, snapTenNguoiNhan, snapSoDienThoai, snapDiaChi,
    tienHang, phiVanChuyen, tienGiam, tongThanhToan,
    phuongThucTT, phuongThucGiaoHang, trangThaiTT, trangThaiDon, promotionId) VALUES
    ('DUA-20250001', 2, 1, N'Nguyen Van An', '0912345678',
     N'123 Tran Hung Dao, May To, Ngo Quyen, Hai Phong',
     354000, 30000, 30000, 354000, 'CHUYEN_KHOAN', 'SHIP', 'CHUA_THANH_TOAN', 'CHO_XAC_NHAN', 2);
 
-- Them hinhAnhSP vao OrderItems (snapshot anh luc dat hang)
INSERT INTO OrderItems (orderId, productId, variantId, tenSanPham, tenBienThe, hinhAnhSP, donGia, soLuong, thanhTien) VALUES
    (1, 1,  3, N'Chai Thuy Tinh Dung Ruou Tron', N'100ml - Nap Go',    N'/uploads/chai-tron-100ml-nap-go.jpg',  23000, 2,  46000),
    (1, 4, 14, N'Ly Ruou Vang Pha Le Bohemia',   N'200ml - Don chiec', N'/uploads/ly-vang-200ml-don.jpg',       84000, 3, 252000),
    (1, 5, 18, N'Ly Highball Thuy Tinh Cao Cap',  N'300ml - Don chiec', N'/uploads/ly-highball-300ml.jpg',       35000, 1,  35000);
 
-- Bai viet blog mau
INSERT INTO Posts (tieuDe, tomTat, noiDung, tacGiaId, trangThai) VALUES
    (N'Huong dan chon chai thuy tinh theo muc dich su dung',
     N'Phan biet chai dung ruou, nuoc hoa, thuc pham - diem khac biet ve mieng chai, kieu nap va chat lieu.',
     N'<p>Noi dung huong dan day du...</p>', 1, 'XUAT_BAN'),
    (N'Uu diem cua thuy tinh Borosilicate so voi thuy tinh thuong',
     N'Tai sao thuy tinh Borosilicate lai duoc ua chuong trong nganh thuc pham va duoc pham?',
     N'<p>Noi dung bai viet...</p>', 1, 'XUAT_BAN'),
    (N'Top 5 mau binh trang tri duoc ua chuong nhat 2025',
     N'Khao sat xu huong thi truong binh thuy tinh va pha le trang tri nam 2025.',
     N'<p>Noi dung bai viet...</p>', 1, 'NHAP');
 
-- Wishlist mau
INSERT INTO Wishlists (userId, productId) VALUES
    (2, 3),   -- Nguyen Van An yeu thich Binh Hoa Pha Le
    (2, 4);   -- Nguyen Van An yeu thich Ly Ruou Vang Bohemia
GO
 
-- ============================================================
-- VIEWS HO TRO
-- ============================================================
 
-- VIEW 1: Doanh thu theo thoi gian (dung cho Admin Dashboard)
-- Truy van: SELECT * FROM vw_DoanhThu WHERE thang = 6 AND nam = 2025
CREATE VIEW vw_DoanhThu AS
SELECT
    CAST(ngayDat AS DATE)      AS ngay,
    DATEPART(WEEK,  ngayDat)   AS tuan,
    DATEPART(MONTH, ngayDat)   AS thang,
    DATEPART(YEAR,  ngayDat)   AS nam,
    COUNT(*)                   AS soLuongDon,
    SUM(tongThanhToan)         AS tongDoanhThu
FROM Orders
WHERE trangThaiDon NOT IN ('DA_HUY')
  AND trangThaiTT = 'DA_THANH_TOAN'
GROUP BY CAST(ngayDat AS DATE),
         DATEPART(WEEK,  ngayDat),
         DATEPART(MONTH, ngayDat),
         DATEPART(YEAR,  ngayDat);
GO
 
-- VIEW 2: Gia va anh hien tai cua san pham (lay tu bien the mac dinh)
-- [Bo sung them hinhAnhBienThe de Frontend lay anh bien the default
-- Truy van: SELECT * FROM vw_ProductPrice WHERE isFeatured = 1 AND isActive = 1
CREATE VIEW vw_ProductPrice AS
SELECT
    p.id,
    p.tenSanPham,
    p.danhMucId,
    p.hinhAnhChinh,                                       -- Anh goc cua san pham
    p.trangThaiSanPham,
    p.leadTimeDays,
    p.isFeatured,
    p.isActive,
    pv.id                                  AS variantId,
    pv.tenBienThe,
    pv.dungTich,
    pv.giaGoc,
    ISNULL(pv.giaKhuyenMai, pv.giaGoc)    AS giaBan,      -- Gia ban thuc te
    pv.soLuongTon,
    ISNULL(pv.hinhAnh, p.hinhAnhChinh)    AS hinhAnhHienThi  -- Anh bien the, fallback sang anh chinh
FROM Products p
INNER JOIN ProductVariants pv
    ON pv.productId = p.id
   AND pv.isDefault = 1
   AND pv.isActive  = 1
WHERE p.isActive = 1
  AND p.trangThaiSanPham != 'NGUNG_BAN';
GO
 
-- VIEW 3: Bai viet da xuat ban (dung cho trang /blog phia client)
CREATE VIEW vw_PostsPublished AS
SELECT id, tieuDe, tomTat, hinhAnh, tacGiaId, luotXem, ngayTao
FROM   Posts
WHERE  trangThai = 'XUAT_BAN'
ORDER BY ngayTao DESC
OFFSET 0 ROWS;
GO

-- ============================================================
-- SEED DU LIEU SAN PHAM NOI BAT (isFeatured = 1)
-- Chay sau khi tao bang xong
-- ============================================================

IF NOT EXISTS (SELECT 1 FROM Categories WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT Categories ON;
    INSERT INTO Categories (id, tenDanhMuc, moTa, parentId, thuTuHienThi, isActive)
    VALUES
    (1, N'Chai thủy tinh', N'Chai đựng rượu, dầu ăn, nước hoa, tinh dầu', NULL, 1, 1),
    (2, N'Hũ thủy tinh', N'Hũ đựng thực phẩm, gia vị, đồ khô, mật ong', NULL, 2, 1),
    (3, N'Bình trang trí', N'Bình hoa, bình decor, bình pha lê cao cấp', NULL, 3, 1),
    (4, N'Ly & Cốc', N'Ly rượu vang, whisky, champagne, cốc nước', NULL, 4, 1),
    (5, N'Phụ kiện thủy tinh', N'Nắp, vòi, ống hút, dụng cụ pha chế', NULL, 5, 1),
    (6, N'Chai rượu',  N'Chai đựng rượu vang, rượu whisky',  1, 1, 1),
    (7, N'Chai tinh dầu', N'Chai nhỏ đựng tinh dầu, nước hoa', 1, 2, 1),
    (8, N'Hũ gia vị',  N'Hũ nhỏ đựng gia vị, muối, tiêu',     2, 1, 1),
    (9, N'Hũ bảo quản', N'Hũ lớn đựng thực phẩm khô, ngũ cốc', 2, 2, 1),
    (10, N'Bình hoa',  N'Bình cắm hoa các loại',              3, 1, 1),
    (11, N'Bình decor', N'Bình trang trí nội thất',           3, 2, 1),
    (12, N'Ly vang',   N'Ly uống rượu vang, champagne',       4, 1, 1),
    (13, N'Cốc nước',  N'Cốc uống nước hàng ngày',            4, 2, 1);
    SET IDENTITY_INSERT Categories OFF;
END
GO

IF NOT EXISTS (SELECT 1 FROM Products WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT Products ON;
    INSERT INTO Products (id, tenSanPham, moTa, chatLieu, xuatXu, mucDichSuDung, thuongHieu, danhMucId, hinhAnhChinh, trangThaiSanPham, leadTimeDays, isFeatured, isActive, ngayTao, ngayCapNhat)
    VALUES
    (1, N'Chai rượu vang thủy tinh cao cấp 750ml', N'Chai rượu vang thủy tinh cao cấp thiết kế sang trọng, thân chai dày, miệng chai gia công tỉ mỉ, nắp gỗ tự nhiên.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Đựng rượu vang, rượu whisky, trưng bày', N'DuaStore Premium', 6, '/images/products/chai-ruou-vang-750.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (2, N'Bộ 6 chai tinh dầu thủy tinh 10ml', N'Bộ 6 chai thủy tinh nhỏ 10ml có ống nhỏ giọt thủy tinh. Dùng đựng tinh dầu, nước hoa, serum.', N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng tinh dầu, nước hoa, mỹ phẩm handmade', N'DuaStore', 7, '/images/products/bo-chai-tinh-dau-10ml.jpg', 'DANG_BAN', 2, 1, 1, GETDATE(), GETDATE()),
    (3, N'Hũ mật ong thủy tinh 500ml có vòi', N'Hũ thủy tinh 500ml có vòi inox 304 và nắp gỗ. Dùng đựng mật ong, siro, nước trái cây.', N'Thủy tinh + Inox 304', N'Việt Nam', N'Đựng mật ong, siro, nước ép', N'DuaStore Home', 9, '/images/products/hu-mat-ong-500.jpg', 'DANG_BAN', 5, 1, 1, GETDATE(), GETDATE()),
    (4, N'Bộ 4 hũ gia vị thủy tinh 100ml', N'Bộ 4 hũ thủy tinh vuông 100ml nắp inox, kèm khay gỗ. Phù hợp đựng muối, tiêu, đường, bột ngọt.', N'Thủy tinh + Inox', N'Việt Nam', N'Đựng gia vị nhà bếp', N'DuaStore Home', 8, '/images/products/bo-hu-gia-vi.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (5, N'Bình hoa thủy tinh pha lê 30cm', N'Bình hoa thủy tinh pha lê cao cấp 30cm, mặt cắt kim cương. Phù hợp bàn ăn, phòng khách.', N'Pha lê K9', N'Trung Quốc', N'Cắm hoa tươi, hoa khô, trang trí nội thất', N'Crystal Lux', 10, '/images/products/binh-hoa-pha-le-30.jpg', 'DANG_BAN', 7, 1, 1, GETDATE(), GETDATE()),
    (6, N'Bình decor thủy tinh hình trụ 25cm', N'Bình thủy tinh hình trụ cao 25cm, nắp gỗ sồi. Trang trí bàn làm việc, kệ sách.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Trang trí nội thất, quà tặng', N'DuaStore Decor', 11, '/images/products/binh-tru-decor-25.jpg', 'DANG_BAN', 4, 1, 1, GETDATE(), GETDATE()),
    (7, N'Bộ 4 ly rượu vang thủy tinh 350ml', N'Bộ 4 ly rượu vang thủy tinh trong suốt 350ml, thân ly mỏng nhẹ, chân ly vững chãi.', N'Thủy tinh không chì', N'Việt Nam', N'Uống rượu vang, champagne', N'DuaStore Premium', 12, '/images/products/bo-4-ly-vang-350.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (8, N'Bộ 6 cốc nước thủy tinh 300ml', N'Bộ 6 cốc nước thủy tinh 300ml, thành dày chắc chắn, miệng cốc mài tròn đều.', N'Thủy tinh cường lực', N'Việt Nam', N'Uống nước hàng ngày', N'DuaStore Home', 13, '/images/products/bo-6-coc-nuoc-300.jpg', 'DANG_BAN', 2, 1, 1, GETDATE(), GETDATE()),
    (9, N'Chai nước hoa thủy tinh 50ml có vòi xịt', N'Chai nước hoa thủy tinh mini 50ml có vòi xịt inox và nắp từ tính.', N'Thủy tinh + Inox', N'Việt Nam', N'Đựng nước hoa, xịt khoáng', N'DuaStore', 7, '/images/products/chai-nuoc-hoa-50.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (10, N'Bộ 3 hũ thủy tinh bảo quản 1L', N'Bộ 3 hũ thủy tinh dung tích 1L, nắp kín silicone. Bảo quản gạo, ngũ cốc, mì ống.', N'Thủy tinh + Silicone', N'Việt Nam', N'Bảo quản thực phẩm khô', N'DuaStore Home', 9, '/images/products/bo-3-hu-bao-quan-1l.jpg', 'DANG_BAN', 5, 1, 1, GETDATE(), GETDATE()),
    -- Thêm 12 sản phẩm mới
    (11, N'Chai rượu whisky thủy tinh 1L cao cấp', N'Chai thủy tinh dung tích 1L thiết kế mạnh mẽ, nắp chai bằng gỗ sồi và niêm phong sáp ong.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Đựng rượu whisky, rượu mạnh, trưng bày', N'DuaStore Premium', 6, '/images/products/chai-whisky-1l.jpg', 'DANG_BAN', 4, 1, 1, GETDATE(), GETDATE()),
    (12, N'Hũ đựng trà thủy tinh 500ml', N'Hũ thủy tinh dung tích 500ml có nắp silicone kín khí, giữ trà luôn thơm mới.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Đựng trà, cafe, thảo mộc khô', N'DuaStore Home', 9, '/images/products/hu-dung-tra-500.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (13, N'Bộ 6 ly champagne thủy tinh 200ml', N'Bộ 6 ly champagne thủy tinh cao cấp 200ml, thân ly thon dài, chân thanh mảnh.', N'Thủy tinh không chì', N'Việt Nam', N'Uống champagne, rượu vang trắng', N'DuaStore Premium', 12, '/images/products/bo-6-ly-champagne-200.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (14, N'Bình hoa thủy tinh mini 15cm', N'Bình hoa thủy tinh mini cao 15cm, miệng loe, phù hợp cắm hoa nhỏ bàn làm việc.', N'Thủy tinh trong suốt', N'Việt Nam', N'Cắm hoa nhỏ, trang trí bàn', N'DuaStore Decor', 10, '/images/products/binh-hoa-mini-15.jpg', 'DANG_BAN', 2, 1, 1, GETDATE(), GETDATE()),
    (15, N'Chai dầu olive thủy tinh 250ml có vòi', N'Chai thủy tinh 250ml có vòi nhỏ giọt inox và nắp gỗ. Dùng đựng dầu olive, dầu ăn.', N'Thủy tinh + Inox 304', N'Việt Nam', N'Đựng dầu olive, giấm, nước sốt', N'DuaStore Home', 1, '/images/products/chai-dau-olive-250.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (16, N'Hũ đựng kẹo thủy tinh 300ml', N'Hũ thủy tinh 300ml nắp gỗ, thân hũ có gân nổi trang trí. Đựng kẹo, snack, đồ khô.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Đựng kẹo, snack, bánh quy', N'DuaStore Home', 9, '/images/products/hu-dung-keo-300.jpg', 'DANG_BAN', 2, 1, 1, GETDATE(), GETDATE()),
    (17, N'Bộ 4 cốc bia thủy tinh 500ml', N'Bộ 4 cốc bia thủy tinh dung tích 500ml, thành dày, tay cầm chắc chắn.', N'Thủy tinh cường lực', N'Việt Nam', N'Uống bia, nước giải khát', N'DuaStore Home', 13, '/images/products/bo-4-coc-bia-500.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (18, N'Bình decor thủy tinh màu hổ phách 20cm', N'Bình thủy tinh màu hổ phách 20cm, nắp gỗ, phong cách vintage.', N'Thủy tinh màu', N'Việt Nam', N'Trang trí nội thất, quà tặng', N'DuaStore Decor', 11, '/images/products/binh-ho-phach-20.jpg', 'DANG_BAN', 5, 1, 1, GETDATE(), GETDATE()),
    (19, N'Chai rượu sake thủy tinh 300ml', N'Chai rượu sake thủy tinh 300ml kiểu Nhật Bản, miệng rộng, nắp nhựa cao cấp.', N'Thủy tinh Borosilicate', N'Việt Nam', N'Đựng rượu sake, rượu trắng', N'DuaStore Premium', 6, '/images/products/chai-sake-300.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (20, N'Bộ 2 hũ đựng cafe thủy tinh 250ml', N'Bộ 2 hũ thủy tinh 250ml nắp kín silicone, nhãn cafe/trà trang trí.', N'Thủy tinh + Silicone', N'Việt Nam', N'Đựng cafe hạt, cafe bột, trà', N'DuaStore Home', 8, '/images/products/bo-2-hu-cafe-250.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE()),
    (21, N'Bộ 6 ly cocktail thủy tinh 250ml', N'Bộ 6 ly cocktail thủy tinh 250ml chân cao, phù hợp pha chế mocktail, cocktail.', N'Thủy tinh không chì', N'Việt Nam', N'Pha chế cocktail, nước ép, trái cây', N'DuaStore Premium', 12, '/images/products/bo-6-ly-cocktail-250.jpg', 'DANG_BAN', 4, 1, 1, GETDATE(), GETDATE()),
    (22, N'Bộ 5 chai thủy tinh đựng gia vị 50ml', N'Bộ 5 chai thủy tinh nhỏ 50ml kèm khay gỗ, nắp inox. Đựng tiêu, muối, ớt bột, nghệ, đường.', N'Thủy tinh + Inox', N'Việt Nam', N'Đựng gia vị khô nhà bếp', N'DuaStore Home', 8, '/images/products/bo-5-chai-gia-vi-50.jpg', 'DANG_BAN', 3, 1, 1, GETDATE(), GETDATE());
    SET IDENTITY_INSERT Products OFF;
END
GO

IF NOT EXISTS (SELECT 1 FROM ProductVariants WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT ProductVariants ON;
    INSERT INTO ProductVariants (id, productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault, isActive)
    VALUES
    (1, 1, N'750ml - Trong suốt', 750, 85000, NULL, 50, NULL, 1, 1),
    (2, 1, N'750ml - Xanh dương', 750, 95000, 85000, 30, NULL, 0, 1),
    (3, 1, N'750ml - Xanh lá',    750, 95000, 85000, 25, NULL, 0, 1),
    (4, 2, N'10ml - Trong suốt',  10,  45000, NULL, 100, NULL, 1, 1),
    (5, 2, N'10ml - Hổ phách',    10,  55000, NULL, 80,  NULL, 0, 1),
    (6, 3, N'500ml - Trong suốt', 500, 120000, 99000, 40, NULL, 1, 1),
    (7, 4, N'4x100ml - Vuông',    100, 135000, NULL, 60, NULL, 1, 1),
    (8, 5, N'30cm - Trong suốt',  0,   250000, NULL, 20, NULL, 1, 1),
    (9, 5, N'30cm - Màu khói',    0,   280000, 250000, 15, NULL, 0, 1),
    (10, 6, N'25cm - Trong suốt',  0,  110000, NULL, 35, NULL, 1, 1),
    (11, 7, N'350ml - Trong suốt', 350, 180000, NULL, 45, NULL, 1, 1),
    (12, 7, N'350ml - Màu khói',   350, 210000, 180000, 30, NULL, 0, 1),
    (13, 8, N'300ml - Trong suốt', 300, 95000, NULL, 70, NULL, 1, 1),
    (14, 9, N'50ml - Trong suốt',  50,  65000, NULL, 90, NULL, 1, 1),
    (15, 10, N'3x1L - Trong suốt', 1000, 165000, NULL, 40, NULL, 1, 1),
    -- Biến thể 12 sản phẩm mới
    (16, 11, N'1L - Trong suốt',     1000, 180000, NULL, 30, NULL, 1, 1),
    (17, 11, N'1L - Màu khói',       1000, 210000, 180000, 20, NULL, 0, 1),
    (18, 12, N'500ml - Trong suốt',  500,  135000, NULL, 40, NULL, 1, 1),
    (19, 12, N'500ml - Màu trà',     500,  155000, NULL, 25, NULL, 0, 1),
    (20, 13, N'200ml - Trong suốt',  200,  220000, NULL, 35, NULL, 1, 1),
    (21, 13, N'200ml - Màu khói',    200,  250000, 220000, 20, NULL, 0, 1),
    (22, 14, N'15cm - Trong suốt',   0,    75000,  NULL, 60, NULL, 1, 1),
    (23, 14, N'15cm - Xanh mint',    0,    90000,  75000,  40, NULL, 0, 1),
    (24, 15, N'250ml - Trong suốt',  250,  95000,  NULL, 45, NULL, 1, 1),
    (25, 16, N'300ml - Trong suốt',  300,  85000,  NULL, 50, NULL, 1, 1),
    (26, 16, N'300ml - Màu xanh',    300,  100000, 85000,  30, NULL, 0, 1),
    (27, 17, N'500ml - Trong suốt',  500,  130000, NULL, 40, NULL, 1, 1),
    (28, 18, N'20cm - Hổ phách',     0,    160000, NULL, 25, NULL, 1, 1),
    (29, 18, N'20cm - Xanh coban',   0,    180000, 160000, 15, NULL, 0, 1),
    (30, 19, N'300ml - Trong suốt',  300,  70000,  NULL, 55, NULL, 1, 1),
    (31, 20, N'2x250ml - Trong suốt',250,  125000, NULL, 35, NULL, 1, 1),
    (32, 21, N'250ml - Trong suốt',  250,  200000, NULL, 30, NULL, 1, 1),
    (33, 21, N'250ml - Màu khói',    250,  230000, 200000, 20, NULL, 0, 1),
    (34, 22, N'5x50ml - Trong suốt', 50,   110000, NULL, 60, NULL, 1, 1);
    SET IDENTITY_INSERT ProductVariants OFF;
END
GO

-- Chỉ seed bộ ảnh mở rộng khi bộ 22 sản phẩm mở rộng đã được tạo.
IF NOT EXISTS (SELECT 1 FROM ProductImages WHERE id = 1)
   AND EXISTS (SELECT 1 FROM Products WHERE id = 22)
BEGIN
    SET IDENTITY_INSERT ProductImages ON;
    INSERT INTO ProductImages (id, productId, imageUrl, sortOrder, isActive, createdAt)
    VALUES
    (1, 1, '/images/products/chai-ruou-vang-750.jpg',   1, 1, GETDATE()),
    (2, 1, '/images/products/chai-ruou-vang-750-2.jpg', 2, 1, GETDATE()),
    (3, 2, '/images/products/bo-chai-tinh-dau-10ml.jpg', 1, 1, GETDATE()),
    (4, 2, '/images/products/bo-chai-tinh-dau-10ml-2.jpg',2,1, GETDATE()),
    (5, 3, '/images/products/hu-mat-ong-500.jpg',       1, 1, GETDATE()),
    (6, 3, '/images/products/hu-mat-ong-500-2.jpg',     2, 1, GETDATE()),
    (7, 4, '/images/products/bo-hu-gia-vi.jpg',         1, 1, GETDATE()),
    (8, 4, '/images/products/bo-hu-gia-vi-2.jpg',       2, 1, GETDATE()),
    (9, 5, '/images/products/binh-hoa-pha-le-30.jpg',   1, 1, GETDATE()),
    (10, 5, '/images/products/binh-hoa-pha-le-30-2.jpg',2, 1, GETDATE()),
    (11, 6, '/images/products/binh-tru-decor-25.jpg',   1, 1, GETDATE()),
    (12, 6, '/images/products/binh-tru-decor-25-2.jpg', 2, 1, GETDATE()),
    (13, 7, '/images/products/bo-4-ly-vang-350.jpg',    1, 1, GETDATE()),
    (14, 7, '/images/products/bo-4-ly-vang-350-2.jpg',  2, 1, GETDATE()),
    (15, 8, '/images/products/bo-6-coc-nuoc-300.jpg',   1, 1, GETDATE()),
    (16, 8, '/images/products/bo-6-coc-nuoc-300-2.jpg', 2, 1, GETDATE()),
    (17, 9, '/images/products/chai-nuoc-hoa-50.jpg',    1, 1, GETDATE()),
    (18, 9, '/images/products/chai-nuoc-hoa-50-2.jpg',  2, 1, GETDATE()),
    (19, 10, '/images/products/bo-3-hu-bao-quan-1l.jpg',1, 1, GETDATE()),
    (20, 10, '/images/products/bo-3-hu-bao-quan-1l-2.jpg',2,1, GETDATE()),
    -- Hình ảnh 12 sản phẩm mới
    (21, 11, '/images/products/chai-whisky-1l.jpg',       1, 1, GETDATE()),
    (22, 11, '/images/products/chai-whisky-1l-2.jpg',     2, 1, GETDATE()),
    (23, 12, '/images/products/hu-dung-tra-500.jpg',      1, 1, GETDATE()),
    (24, 12, '/images/products/hu-dung-tra-500-2.jpg',    2, 1, GETDATE()),
    (25, 13, '/images/products/bo-6-ly-champagne-200.jpg', 1, 1, GETDATE()),
    (26, 13, '/images/products/bo-6-ly-champagne-200-2.jpg',2,1, GETDATE()),
    (27, 14, '/images/products/binh-hoa-mini-15.jpg',     1, 1, GETDATE()),
    (28, 14, '/images/products/binh-hoa-mini-15-2.jpg',   2, 1, GETDATE()),
    (29, 15, '/images/products/chai-dau-olive-250.jpg',  1, 1, GETDATE()),
    (30, 15, '/images/products/chai-dau-olive-250-2.jpg', 2, 1, GETDATE()),
    (31, 16, '/images/products/hu-dung-keo-300.jpg',      1, 1, GETDATE()),
    (32, 16, '/images/products/hu-dung-keo-300-2.jpg',    2, 1, GETDATE()),
    (33, 17, '/images/products/bo-4-coc-bia-500.jpg',     1, 1, GETDATE()),
    (34, 17, '/images/products/bo-4-coc-bia-500-2.jpg',   2, 1, GETDATE()),
    (35, 18, '/images/products/binh-ho-phach-20.jpg',     1, 1, GETDATE()),
    (36, 18, '/images/products/binh-ho-phach-20-2.jpg',   2, 1, GETDATE()),
    (37, 19, '/images/products/chai-sake-300.jpg',        1, 1, GETDATE()),
    (38, 19, '/images/products/chai-sake-300-2.jpg',      2, 1, GETDATE()),
    (39, 20, '/images/products/bo-2-hu-cafe-250.jpg',     1, 1, GETDATE()),
    (40, 20, '/images/products/bo-2-hu-cafe-250-2.jpg',   2, 1, GETDATE()),
    (41, 21, '/images/products/bo-6-ly-cocktail-250.jpg', 1, 1, GETDATE()),
    (42, 21, '/images/products/bo-6-ly-cocktail-250-2.jpg',2,1, GETDATE()),
    (43, 22, '/images/products/bo-5-chai-gia-vi-50.jpg',  1, 1, GETDATE()),
    (44, 22, '/images/products/bo-5-chai-gia-vi-50-2.jpg', 2, 1, GETDATE());
    SET IDENTITY_INSERT ProductImages OFF;
END
GO

PRINT '✅ Seed du lieu san pham noi bat hoan tat!';
GO
 
PRINT '====================================================';
PRINT ' DuaStore Database - San sang su dung!';
PRINT ' Tong so bang  : 14';
PRINT ' Views         : vw_DoanhThu, vw_ProductPrice, vw_PostsPublished';
PRINT ' Indexes       : 12';
PRINT ' Mat khau      : admin@123';
PRINT '====================================================';

-- ============================================================
-- KIEM TRA DU LIEU
-- ============================================================
-- SELECT * FROM Users;
-- SELECT * FROM Categories;
-- SELECT * FROM Products;
-- SELECT * FROM ProductImages;
-- SELECT * FROM ProductVariants;
-- SELECT * FROM Addresses;
-- SELECT * FROM Promotions;
-- SELECT * FROM Orders;
-- SELECT * FROM OrderItems;
-- SELECT * FROM Reviews;
-- SELECT * FROM CartItems;
-- SELECT * FROM Posts;
-- SELECT * FROM Wishlists;
-- SELECT * FROM banners;
-- GO
