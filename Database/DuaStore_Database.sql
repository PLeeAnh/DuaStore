/*
================================================================================
  ____  _   _    _    ____  _____ ___  ____  _____
 |  _ \| | | |  / \  / ___||_   _/ _ \|  _ \| ____|
 | | | | | | | / _ \ \___ \  | || | | | |_) |  _|
 | |_| | |_| |/ ___ \ ___) | | || |_| |  _ <| |___
 |____/ \___//_/   \_\____/  |_| \___/|_| \_\_____|
 
  DuaStore -- Do Thuy Tinh Decor, Hai Phong
================================================================================
  DANH SACH BANG (12 bang — khong thay doi so luong):
  [1]  Users            -- Tai khoan (khong tinh diem)
  [2]  Categories       -- Danh muc (phan cap) [NXK]
  [3]  Products         -- San pham goc [PLA]
  [4]  ProductVariants  -- Bien the: dung tich, gia, ton kho, hinh anh [PLA]
  [5]  Addresses        -- Dia chi giao hang [NXK]
  [6]  Promotions       -- Ma khuyen mai [BTM]
  [7]  Orders           -- Don hang [NHD]
  [8]  OrderItems       -- Chi tiet don hang [NHD]
  [9]  Reviews          -- Danh gia [BTM]
  [10] CartItems        -- Gio hang [NHD]
  [11] Posts            -- Blog / Tin tuc [BTM]
  [12] Wishlists        -- Danh sach yeu thich [NXK]
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
    ngayCapNhat DATETIME2(0)                  NULL,
 
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
    hinhAnh         NVARCHAR(255)                 NULL,    -- Anh dai dien cho danh muc
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
-- [4] BANG: ProductVariants
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
-- [5] BANG: Addresses
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
-- [6] BANG: Promotions
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
-- [7] BANG: Orders
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
    CONSTRAINT CK_Orders_TrangThai CHECK (trangThaiDon       IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HUY'))
);
GO
 
-- ============================================================
-- [8] BANG: OrderItems
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
    variantId       INT                            NULL,      -- NULL neu bien the bi xoa
 
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
        REFERENCES ProductVariants(id) ON DELETE SET NULL,
    CONSTRAINT CK_OrderItems_Qty     CHECK (soLuong > 0)
);
GO
 
-- ============================================================
-- [9] BANG: Reviews
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
-- [10] BANG: CartItems
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
-- [11] BANG: Posts  (Bo sung tu bao cao khao sat: PC Market + Chalo Glass)
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
-- [12] BANG: Wishlists  (Bo sung tu bao cao khao sat: Chalo Glass)
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
    ('KHAIHANG', N'Khai truong DuaStore Hai Phong', 'PHAN_TRAM', 15, 200000, 100000, 200, '2025-01-01', '2025-12-31'),
    ('FREESHIP',  N'Mien phi van chuyen don tu 500k', 'SO_TIEN',  30000, 500000, NULL, NULL, '2025-01-01', '2025-06-30'),
    ('DECO50K',   N'Giam 50k don tu 300k',           'SO_TIEN',  50000, 300000, NULL,  100, '2025-01-01', '2025-03-31');
 
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
-- KIEM TRA NHANH SAU KHI CHAY SCRIPT
-- ============================================================
-- Sau khi chay xong, chay cac lenh nay de kiem tra:
--   SELECT COUNT(*) FROM Users;           -- Ket qua: 2
--   SELECT COUNT(*) FROM Categories;      -- Ket qua: 15
--   SELECT COUNT(*) FROM Products;        -- Ket qua: 6
--   SELECT COUNT(*) FROM ProductVariants; -- Ket qua: 25
--   SELECT hinhAnh FROM ProductVariants WHERE hinhAnh IS NOT NULL; -- Xem anh bien the
 
PRINT '====================================================';
PRINT ' DuaStore Database - San sang su dung!';
PRINT ' Tong so bang  : 12';
PRINT ' Views         : vw_DoanhThu, vw_ProductPrice, vw_PostsPublished';
PRINT ' Indexes       : 12';
PRINT ' Mat khau      : admin@123';
PRINT '====================================================';