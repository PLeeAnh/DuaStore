/*
================================================================================
 DuaStore_Database.sql
 Script tao du lieu SQL Server cho du an DuaStore (Spring Boot)
================================================================================
 FIX SO VOI BAN CU:
 Ban cu (viet tay) bi LECH voi @Entity trong code -> Hibernate "ddl-auto=validate"
 bao loi khong khoi dong duoc app. Cac loi da sua trong ban nay:

   1. THIEU BANG "RefundRequests" (entity RefundRequest.java) -> da them day du.
   2. Toan bo cot kieu chuoi da dung NVARCHAR de ho tro tieng Viet co dau day du.
   3. 3 bang "linked_accounts", "user_settings", "user_auth_providers" dat sai
     ten cot kieu snake_case (user_id, created_at, setting_key...) trong khi
     entity dung PhysicalNamingStrategyStandardImpl (khong tu convert) nen cot
     that su phai la camelCase (userId, createdAt, settingKey...). Da sua.
  4. Bang "roles" thieu cot "isActive" (co trong entity Role.java). Da them.
  5. Bang "users" thieu 7 cot moi trong entity: hoTen, nickname, avatar,
     emailVisible, phoneVisible, emailMarketing, status. Da them.
  6. Bang "Addresses" thieu 2 cot GHN (ghnDistrictId, ghnWardCode). Da them.
  7. Bang "FlashSales" thieu soLuongToiDa, soLuongDaBan. Da them.
  8. Bang "order_items" thieu cot "loaiGia". Bang "order_notes" thieu "tag". Da them.
  9. Cac cot NOT NULL (isActive, trangThai...) van giu DEFAULT (nhu ban cu) de
     insert seed du lieu khong bi loi - Hibernate validate KHONG kiem tra
     DEFAULT constraint nen an toan 100%.

  V10: Them 3 bang moi (StockMovements, ReviewReplies, ContactReplies) va cot
       severity cho CustomerNotes. Xoa bang RefundRequests (tinh nang refund da bo).


File nay la NGUON SCRIPT DUY NHAT cho DB san pham (khong con schema.sql /
  application-ddlgen.properties). App runtime co
  spring.jpa.hibernate.ddl-auto=validate nen script phai khop 100% voi @Entity.

  QUAN TRONG: Neu sau nay ban SUA/THEM entity (@Column moi, doi kieu du lieu...),
  file nay se LAI BI LECH -> app se bao loi validate khi chay. Luc do nhom lai
  cac column entity them moi (dung dung camelCase cua entity) va sua script tay.

 Tai khoan mac dinh sau khi seed: admin / admin@123 (vai tro PRODUCT_OWNER)
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
-- BUOC 0.5: XOA BANG/VIEW CU (de chay lai script nhieu lan an toan)
-- ============================================================
DROP VIEW IF EXISTS vw_PostsPublished;
DROP VIEW IF EXISTS vw_ProductPrice;
DROP VIEW IF EXISTS vw_DoanhThu;
GO
DROP TABLE IF EXISTS StockMovements;
DROP TABLE IF EXISTS ReviewReplies;
DROP TABLE IF EXISTS ContactReplies;
DROP TABLE IF EXISTS admin_action_logs;
DROP TABLE IF EXISTS order_status_logs;
DROP TABLE IF EXISTS order_notes;
DROP TABLE IF EXISTS order_assignments;
DROP TABLE IF EXISTS Notifications;
DROP TABLE IF EXISTS UserVouchers;
DROP TABLE IF EXISTS ReviewImages;
DROP TABLE IF EXISTS LoyaltyTransactions;
DROP TABLE IF EXISTS footer_links;
DROP TABLE IF EXISTS PriceHistory;
DROP TABLE IF EXISTS CustomerTags;
DROP TABLE IF EXISTS CustomerNotes;
DROP TABLE IF EXISTS Wishlists;
DROP TABLE IF EXISTS Posts;
DROP TABLE IF EXISTS PostCategories;
DROP TABLE IF EXISTS banners;
DROP TABLE IF EXISTS SavedCartItems;
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS FlashSaleItems;
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
DROP TABLE IF EXISTS contact_messages;
DROP TABLE IF EXISTS popup_banners;
DROP TABLE IF EXISTS UserActivityLogs;
DROP TABLE IF EXISTS ProductViews;
DROP TABLE IF EXISTS users;
GO

-- ============================================================
-- BUOC 1: TAO BANG (sinh tu Hibernate @Entity - khop 100% voi code)
-- ============================================================
    create table Addresses (
        ghnDistrictId int,
        id int identity not null,
        isDefault bit default 0 not null,
        latitude float(53),
        longitude float(53),
        userId int not null,
        soDienThoai nvarchar(15) not null,
        ghnWardCode nvarchar(20),
        phuongXa nvarchar(100) not null,
        quanHuyen nvarchar(100) not null,
        tenNguoiNhan nvarchar(100) not null,
        tinhThanh nvarchar(100) not null,
        diaChiCuThe nvarchar(200) not null,
        primary key (id)
    );

    create table admin_action_logs (
        adminId int not null,
        entityId int not null,
        id int identity not null,
        ngayTao datetime2(7) not null,
        hanhDong nvarchar(50) not null,
        ipAddress nvarchar(50),
        loaiEntity nvarchar(50) not null,
        giaTriCu nvarchar(max),
        giaTriMoi nvarchar(max),
        moTa nvarchar(max),
        primary key (id)
    );

    create table banners (
        active bit default 1 not null,
        display_order int default 0 not null,
        id int identity not null,
        created_at datetime2(7) not null,
        end_date datetime2(7),
        start_date datetime2(7),
        updated_at datetime2(7) not null,
        title nvarchar(200) not null,
        description nvarchar(500),
        image_url nvarchar(500) not null,
        link_url nvarchar(1000),
        primary key (id)
    );

    create table CartItems (
        giaLucThem numeric(12,0),
        id int identity not null,
        productId int not null,
        soLuong int default 1 not null,
        userId int not null,
        variantId int not null,
        ngayThem datetime2(7),
        primary key (id)
    );

    create table Categories (
        id int identity not null,
        isActive bit default 1 not null,
        parentId int,
        thuTuHienThi int default 0,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7),
        imageUrl nvarchar(500),
        moTa nvarchar(255),
        tenDanhMuc nvarchar(255) not null,
        slug nvarchar(500),
        primary key (id)
    );

    create table FlashSales (
        id int identity not null,
        isActive bit default 1 not null,
        priority int default 0,
        ngayBatDau datetime2(7) not null,
        ngayKetThuc datetime2(7) not null,
        tenChuongTrinh nvarchar(200) not null,
        moTa nvarchar(500),
        primary key (id)
    );

    create table FlashSaleItems (
        id int identity not null,
        flashSaleId int not null,
        variantId int not null,
        giaGoc numeric(12,0) not null,
        giaSale numeric(12,0) not null,
        soLuongToiDa int default 0 not null,
        soLuongDaBan int default 0 not null,
        isActive bit default 1 not null,
        primary key (id)
    );

    create table linked_accounts (
        id int identity not null,
        linkedUserId int not null,
        userId int not null,
        createdAt datetime2(7) not null,
        primary key (id)
    );

    create table Notifications (
        id int identity not null,
        isActive bit default 1 not null,
        linkId int,
        userId int,
        createdAt datetime2(7) not null,
        linkType nvarchar(20),
        targetRole nvarchar(20),
        linkUrl nvarchar(500),
        content NVARCHAR(MAX) not null,
        linkLabel nvarchar(255),
        requiredPermission nvarchar(40),
        primary key (id)
    );

    create table order_assignments (
        adminId int not null,
        id int identity not null,
        orderId int not null,
        ngayPhan datetime2(7) not null,
        trangThai nvarchar(20),
        primary key (id)
    );

    create table order_items (
        donGia numeric(12,0) not null,
        giaVon numeric(12,0),
        id int identity not null,
        orderId int not null,
        productId int,
        soLuong int not null,
        thanhTien numeric(12,0) not null,
        variantId int,
        loaiGia nvarchar(20),
        tenBienThe nvarchar(150),
        tenSanPham nvarchar(200) not null,
        hinhAnhSP nvarchar(255),
        primary key (id)
    );

    create table order_notes (
        admin_id int not null,
        id int identity not null,
        order_id int not null,
        ngayTao datetime2(7) not null,
        tag nvarchar(50),
        noiDung nvarchar(1000) not null,
        primary key (id)
    );

    create table order_status_logs (
        id int identity not null,
        nguoi_thuc_hien_id int,
        order_id int not null,
        thoi_gian datetime2(7) not null,
        loai_su_kien nvarchar(50) not null check ((loai_su_kien in ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED'))),
        trang_thai_cu nvarchar(50),
        trang_thai_moi nvarchar(50),
        ghiChu nvarchar(500),
        primary key (id)
    );

    create table orders (
        addressId int,
        id int identity not null,
        phiVanChuyen numeric(10,0) default 0 not null,
        promotionId int,
        tienGiam numeric(10,0) default 0 not null,
        tienHang numeric(12,0) not null,
        tongThanhToan numeric(12,0) not null,
        userId int not null,
        createdBy int,
        lastModifiedBy int,
        ngayCapNhat datetime2(7),
        ngayDat datetime2(7) not null,
        ngayGiao datetime2(7),
        snapSoDienThoai nvarchar(15) not null,
        maDon nvarchar(20) not null,
        phuongThucGiaoHang nvarchar(20) default 'SHIP' not null,
        phuongThucTT nvarchar(20) not null,
        trangThaiDon nvarchar(20) default 'CHO_XAC_NHAN' not null,
        trangThaiTT nvarchar(25) default 'CHUA_THANH_TOAN' not null,
        maVanDon nvarchar(50),
        sepayTransactionId nvarchar(100),
        shippingCarrier nvarchar(30),
        snapTenNguoiNhan nvarchar(100) not null,
        ghiChu nvarchar(500),
        snapDiaChi nvarchar(500) not null,
        fraudWarning nvarchar(1000),
        primary key (id)
    );

    create table permissions (
        id int identity not null,
        ngayTao datetime2(7) not null,
        action nvarchar(50) not null,
        module nvarchar(50) not null,
        moTa nvarchar(200),
        primary key (id)
    );

    create table PostCategories (
        id int identity not null,
        thuTu int default 0,
        ngayTao datetime2(7) not null,
        tenDanhMuc nvarchar(200) not null,
        slug nvarchar(300),
        moTa nvarchar(500),
        primary key (id)
    );

    create table Posts (
        danhMucId int,
        id int identity not null,
        isFeatured bit default 0,
        luotXem int default 0 not null,
        tacGiaId int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        ngayXuatBan datetime2(7),
        trangThai nvarchar(15) default 'NHAP' not null,
        tieuDe nvarchar(300) not null,
        metaDescription nvarchar(500),
        slug nvarchar(500),
        tomTat nvarchar(500),
        hinhAnh nvarchar(255),
        noiDung NVARCHAR(MAX),
        primary key (id)
    );

    create table ProductImages (
        id int identity not null,
        isActive bit default 1 not null,
        productId int not null,
        sortOrder int default 0,
        createdAt datetime2(7),
        imageUrl nvarchar(255) not null,
        primary key (id)
    );

    create table Products (
        danhMucId int not null,
        id int identity not null,
        isActive bit default 1 not null,
        isFeatured bit default 0 not null,
        leadTimeDays int,
        minPrice numeric(12,0),
        ngayPhatHanh date,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7),
        chatLieu nvarchar(255),
        hinhAnhChinh nvarchar(255),
        hinhDang nvarchar(255),
        kinhLoai nvarchar(255),
        moTa NVARCHAR(MAX),
        mucDichSuDung nvarchar(255),
        tenSanPham nvarchar(255) not null,
        thuongHieu nvarchar(255),
        trangThaiSanPham nvarchar(255) default 'DANG_BAN' not null,
        xuatXu nvarchar(255),
        primary key (id)
    );

    create table ProductVariants (
        dungTich int,
        giaGoc numeric(12,0) not null,
        giaKhuyenMai numeric(12,0),
        giaVon numeric(12,0),
        id int identity not null,
        isActive bit default 1 not null,
        isDefault bit default 0 not null,
        isCustom bit default 0 not null,
        lowStockThreshold int default 20 not null,
        productId int not null,
        soLuongTon int default 0 not null,
        version int default 0 not null,
        hinhAnh nvarchar(255),
        tenBienThe nvarchar(255) not null,
        primary key (id)
    );

    create table Promotions (
        budget numeric(12,0),
        daDung int default 0 not null,
        donHangToiThieu numeric(12,0) default 0 not null,
        giaTriGiam numeric(10,2) not null,
        giamToiDa numeric(12,0),
        id int identity not null,
        isActive bit default 1 not null,
        maxClaims int,
        maxClaimsPerUser int,
        priority int default 0,
        savedCount int default 0,
        soLanDung int,
        stackable bit default 0,
        usedBudget numeric(12,0) default 0,
        denNgay datetime2(7) not null,
        tuNgay datetime2(7) not null,
        loaiGiam nvarchar(15) not null,
        targetType nvarchar(20),
        voucherType nvarchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))) default 'VOUCHER',
        maCode nvarchar(50) not null,
        tenChuongTrinh nvarchar(200) not null,
        targetIds nvarchar(500),
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit default 0 not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        binhLuan nvarchar(1000),
        primary key (id),
        constraint UK_reviews_user_product unique (userId, productId)
    );

    create table role_permissions (
        permission_id int not null,
        role_id int not null,
        primary key (permission_id, role_id)
    );

    create table roles (
        id int identity not null,
        isActive bit default 1 not null,
        ngayTao datetime2(7) not null,
        name nvarchar(50) not null,
        moTa nvarchar(200),
        primary key (id)
    );

    create table SavedCartItems (
        giaLuu numeric(38,2) not null,
        id int identity not null,
        productId int not null,
        soLuong int default 1 not null,
        userId int not null,
        variantId int not null,
        ngayLuu datetime2(7),
        primary key (id)
    );

    create table SiteSettings (
        id int identity not null,
        createdAt datetime2(7),
        updatedAt datetime2(7),
        settingGroup nvarchar(50),
        settingKey nvarchar(100) not null,
        settingValue NVARCHAR(MAX),
        primary key (id)
    );

    create table store_info (
        id int identity not null,
        isActive bit default 1 not null,
        isDefault bit default 0 not null,
        latitude float(53),
        longitude float(53),
        createdAt datetime2(7),
        updatedAt datetime2(7),
        soDienThoai nvarchar(20),
        email nvarchar(100),
        phuongXa nvarchar(100),
        quanHuyen nvarchar(100),
        soNha nvarchar(100),
        tinhThanh nvarchar(100),
        duong nvarchar(200),
        tenCuaHang nvarchar(200) not null,
        primary key (id)
    );

    create table user_auth_providers (
        id int identity not null,
        userId int,
        linkedAt datetime2(7) not null,
        provider nvarchar(20) not null,
        provider_sub nvarchar(255),
        primary key (id)
    );

    create table ProductViews (
        productId int not null,
        userId int not null,
        id bigint identity not null,
        viewedAt datetime2(7) not null,
        primary key (id)
    );

    create table UserActivityLogs (
        userId int not null,
        activityAt datetime2(7) not null,
        id bigint identity not null,
        ipAddress nvarchar(45),
        activityType nvarchar(50) not null,
        description nvarchar(500),
        primary key (id)
    );

    create table contact_messages (
        id int identity not null,
        is_read bit not null,
        is_spam bit not null,
        is_resolved bit not null default 0,
        created_at datetime2(7) not null,
        phan_loai nvarchar(30) not null,
        hoTen nvarchar(150) not null,
        email nvarchar(200) not null,
        noiDung nvarchar(2000) not null,
        primary key (id)
    );

    create table popup_banners (
        active bit not null,
        id int identity not null,
        interval_minutes int,
        created_at datetime2(7) not null,
        updated_at datetime2(7) not null,
        display_mode nvarchar(20) not null,
        title nvarchar(200) not null,
        image_url nvarchar(500) not null,
        link_url nvarchar(1000),
        primary key (id)
    );

    create table user_roles (
        role_id int not null,
        user_id int not null,
        primary key (role_id, user_id)
    );

    create table user_settings (
        userId int not null,
        settingKey nvarchar(50) not null,
        settingValue nvarchar(500),
        primary key (userId, settingKey)
    );

    create table users (
        emailMarketing bit,
        emailVisible bit,
        id int identity not null,
        isActive bit default 1 not null,
        phoneVisible bit,
        ngaySinh date,
        twoFactorEnabled bit,
        twoFactorSecret nvarchar(64),
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        resetTokenExpiry datetime2(7),
        failedAttempts int not null default 0,
        lockedUntil datetime2(7),
        soDienThoai nvarchar(15),
        status nvarchar(20),
        username nvarchar(50) not null,
        email nvarchar(100) not null,
        hoTen nvarchar(100) not null,
        nickname nvarchar(100),
        avatar nvarchar(255),
        password nvarchar(255) not null,
        resetToken nvarchar(255),
        lockReason nvarchar(500),
        primary key (id)
    );

    -- Yeu cau khoa tai khoan khach hang: ADMIN/STAFF khoa tai khoan that (khong phai
    -- bot nghi van gian lan) phai cho PRODUCT_OWNER duyet moi thuc su co hieu luc.
    create table account_lock_requests (
        id int identity not null,
        userId int not null,
        requestedBy int not null,
        decidedBy int,
        reason nvarchar(500) not null,
        decisionNote nvarchar(500),
        status nvarchar(20) not null default 'PENDING' check ((status in ('PENDING','APPROVED','REJECTED'))),
        requestedAt datetime2(7) not null,
        decidedAt datetime2(7),
        primary key (id),
        constraint FK_alr_user foreign key (userId) references users(id),
        constraint FK_alr_requestedBy foreign key (requestedBy) references users(id),
        constraint FK_alr_decidedBy foreign key (decidedBy) references users(id)
    );

    create table UserVouchers (
        id int identity not null,
        promotionId int not null,
        remainingUses int,
        totalSaved numeric(38,2),
        userId int not null,
        expiredAt datetime2(7),
        savedAt datetime2(7) not null,
        usedAt datetime2(7),
        status nvarchar(15) default 'AVAILABLE' not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
        voucherCode nvarchar(50),
        primary key (id)
    );

    create table Wishlists (
        id int identity not null,
        productId int not null,
        userId int not null,
        ngayThem datetime2(7),
        primary key (id)
    );

    create table CustomerNotes (
        id int identity not null,
        userId int not null,
        content nvarchar(max) not null,
        createdBy nvarchar(100) not null,
        createdAt datetime2(7) not null,
        primary key (id)
    );

    create table CustomerTags (
        id int identity not null,
        userId int not null,
        tag nvarchar(50) not null,
        createdAt datetime2(7) not null,
        primary key (id),
        constraint UK_CustomerTags unique (userId, tag)
    );

    create table PriceHistory (
        id int identity not null,
        variantId int,
        variantName nvarchar(255),
        productId int,
        productName nvarchar(255),
        giaCu numeric(18,2),
        giaMoi numeric(18,2),
        nguoiThayDoiId int,
        ngayThayDoi datetime2(7) not null,
        nguon nvarchar(255),
        primary key (id)
    );

    create table footer_links (
        id int identity not null,
        title nvarchar(200) not null,
        url nvarchar(500) not null,
        display_order int not null,
        is_active bit not null,
        columnIndex int not null,
        created_at datetime2(7) not null,
        primary key (id)
    );

    create table LoyaltyTransactions (
        id int identity not null,
        userId int not null,
        points int not null,
        balance int not null,
        type nvarchar(20) not null,
        referenceId int,
        note nvarchar(500),
        createdAt datetime2(7) not null,
        primary key (id)
    );

    -- So du diem tich luy HIEN TAI, 1 dong/user — cot dem cap nhat NGUYEN TU (UPDATE...WHERE),
    -- tach rieng khoi LoyaltyTransactions (lich su append-only). Xem LoyaltyBalance.java.
    create table LoyaltyBalances (
        userId int not null,
        balance int not null default 0,
        primary key (userId)
    );

    create table ReviewImages (
        id int identity not null,
        reviewId int not null,
        imageUrl nvarchar(500) not null,
        sortOrder int not null,
        primary key (id)
    );
GO

-- ============================================================
-- BUOC 2: KHOA NGOAI (FOREIGN KEY) - sinh tu Hibernate @Entity
-- ============================================================
    alter table orders 
       add constraint UKkdjgqq60gdh45821e0iqp357q unique (maDon);

    alter table Promotions 
       add constraint UKjb4yn746ot7vi7ltwkoggcyik unique (maCode);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table SiteSettings 
       add constraint UK6fllodnub8qh92fkirlt3rjhr unique (settingKey);

    alter table users 
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table UserVouchers 
       add constraint UKg1q4hrfehhwey62vcyt9tj3ge unique (userId, promotionId);

    alter table Wishlists
       add constraint UKnyiaslokuixrb7h9fpmo6j4nc unique (userId, productId);

    alter table CartItems 
       add constraint UK7wqcpmx1ycp3a1sfcdrjnwibr unique (userId, variantId);

    alter table CustomerNotes
       add constraint FK_CustomerNotes_userId
       foreign key (userId)
       references users;

    alter table CustomerTags
       add constraint FK_CustomerTags_userId
       foreign key (userId)
       references users;

    alter table admin_action_logs
       add constraint FKb2noouv518ekq5ffcxosgdj4g 
       foreign key (adminId) 
       references users;

    alter table CartItems 
       add constraint FK3j8oshhm6rclt8i57qr0lesxb 
       foreign key (productId) 
       references Products;

    alter table CartItems 
       add constraint FKqdx0vb6alnqltskjqw8nhpl9n 
       foreign key (variantId) 
       references ProductVariants;

    alter table Categories 
       add constraint FKom1a8i2mg4xhf6ktacsh1vogp 
       foreign key (parentId) 
       references Categories;

    alter table order_assignments 
       add constraint FKd32o2ndn8s6dv1i1yajxao80a 
       foreign key (adminId) 
       references users;

    alter table order_assignments 
       add constraint FKcm6mruj1t58wjglpnfsd6xgcd 
       foreign key (orderId) 
       references orders;

    alter table order_items 
       add constraint FK5dledqxrq55xmpqy9fr4cpbsu 
       foreign key (orderId) 
       references orders;

    alter table order_notes 
       add constraint FKov5hr2bsjgqbc4mgc40bmdoin 
       foreign key (admin_id) 
       references users;

    alter table order_notes 
       add constraint FKgl7kbn92v2whrvmco2ygu3cdt 
       foreign key (order_id) 
       references orders;

    alter table order_status_logs 
       add constraint FKmr8kbxx88motp36uk5jqlwwi2 
       foreign key (nguoi_thuc_hien_id) 
       references users;

    alter table order_status_logs 
       add constraint FKpoehv8fptppd81oysnw7l44by 
       foreign key (order_id) 
       references orders;

    alter table orders 
       add constraint FKg960mua4eodibuhrm6gokmn6i 
       foreign key (promotionId) 
       references Promotions;

    alter table orders 
       add constraint FK6co8q7ko456baksb6tdjq2dfv 
       foreign key (userId) 
       references users;

    alter table Posts 
       add constraint FKh5leuxac6k9g6eh7i8tjuhfp1 
       foreign key (danhMucId) 
       references PostCategories;

    alter table ProductImages 
       add constraint FK3bsgj9dw8f36hb7p8s3c8sj96 
       foreign key (productId) 
       references Products;

    alter table ProductVariants 
       add constraint FKnrqu92gwc9ue8usxv9dov5cn7 
       foreign key (productId) 
       references Products;

    alter table role_permissions 
       add constraint FKegdk29eiy7mdtefy5c7eirr6e 
       foreign key (permission_id) 
       references permissions;

    alter table role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles;

    alter table SavedCartItems 
       add constraint FKgy3yiei9ahvjudjm37tp11ll5 
       foreign key (productId) 
       references Products;

    alter table SavedCartItems 
       add constraint FKsx0f2p008q6e0pwb9bipq8opu 
       foreign key (variantId) 
       references ProductVariants;

    alter table user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles;

    alter table user_roles 
       add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
       foreign key (user_id) 
       references users;

    alter table UserVouchers 
       add constraint FK78mpv1easxbi2d20hfixypd82 
       foreign key (promotionId) 
       references Promotions;

    alter table Wishlists
       add constraint FKl8me5k171y8fskc8x4r5ht3nc
       foreign key (productId)
       references Products;

    alter table user_auth_providers
       add constraint FK_user_auth_providers_userId
       foreign key (userId)
       references users;

    alter table PriceHistory
       add constraint FK_PriceHistory_variantId
       foreign key (variantId)
       references ProductVariants;

    alter table PriceHistory
       add constraint FK_PriceHistory_productId
       foreign key (productId)
       references Products;

    alter table PriceHistory
       add constraint FK_PriceHistory_nguoiThayDoiId
       foreign key (nguoiThayDoiId)
       references users;

    alter table LoyaltyTransactions
       add constraint FK_LoyaltyTransactions_userId
       foreign key (userId)
       references users;

    alter table LoyaltyBalances
       add constraint FK_LoyaltyBalances_userId
       foreign key (userId)
       references users;

    alter table ReviewImages
       add constraint FK_ReviewImages_reviewId
       foreign key (reviewId)
       references Reviews;

    alter table FlashSaleItems
       add constraint FK_FlashSaleItems_flashSaleId
       foreign key (flashSaleId)
       references FlashSales;
GO

-- ============================================================
-- BUOC 3: INDEX HO TRO TRUY VAN NHANH
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
CREATE INDEX idx_promotions_active_dates    ON Promotions   (isActive, tuNgay, denNgay);
CREATE INDEX idx_user_vouchers_user_status  ON UserVouchers (userId, status);
CREATE INDEX idx_user_vouchers_expired_at   ON UserVouchers (expiredAt);
CREATE INDEX idx_user_vouchers_promotion_id ON UserVouchers (promotionId);
CREATE INDEX idx_promotions_code            ON Promotions   (maCode);
-- V2: Index for custom variants
CREATE INDEX IX_Variants_Custom ON ProductVariants (isCustom, isActive);
-- V4: Indexes for admin_action_logs
CREATE INDEX IX_admin_action_logs_entity_lookup ON admin_action_logs (loaiEntity, entityId, ngayTao DESC);
CREATE INDEX IX_admin_action_logs_admin_lookup ON admin_action_logs (adminId, ngayTao DESC);
CREATE INDEX IX_admin_action_logs_ngay_tao ON admin_action_logs (ngayTao DESC);
CREATE INDEX IX_admin_action_logs_admin_ngay_tao ON admin_action_logs (adminId, ngayTao DESC);
-- Flash sale items
CREATE INDEX IX_FlashSaleItems_FlashSale ON FlashSaleItems (flashSaleId, isActive);
CREATE INDEX IX_FlashSaleItems_Variant ON FlashSaleItems (variantId);
GO

-- ============================================================
-- BUOC 4: SEED DU LIEU MAU
-- ============================================================
-- SEED: RBAC (roles / permissions / role_permissions / user_roles)
-- ============================================================
-- Danh sach permission khop CHINH XAC 1:1 voi PermissionEnum.java trong code
-- (83 quyen: moi module/action trong enum deu co dong INSERT tuong ung).
-- ============================================================
INSERT INTO roles (name, moTa, ngayTao) VALUES
    (N'PRODUCT_OWNER', N'Chủ sở hữu sản phẩm - toàn quyền hệ thống', GETDATE()),
    (N'ADMIN',       N'Quản trị viên được gán quyền cụ thể', GETDATE()),
    (N'STAFF',       N'Nhân viên xử lý đơn hàng, sản phẩm, đánh giá', GETDATE()),
    (N'USER',        N'Khách hàng', GETDATE());
GO

INSERT INTO permissions (module, action, moTa, ngayTao) VALUES
    (N'DASHBOARD', N'READ', N'Xem trang tổng quan', GETDATE()),
    (N'PRODUCT', N'CREATE', N'Thêm sản phẩm', GETDATE()),
    (N'PRODUCT', N'READ',   N'Xem sản phẩm', GETDATE()),
    (N'PRODUCT', N'UPDATE', N'Sửa sản phẩm', GETDATE()),
    (N'PRODUCT', N'DELETE', N'Xóa sản phẩm', GETDATE()),
    (N'ORDER', N'READ',   N'Xem đơn hàng', GETDATE()),
    (N'ORDER', N'UPDATE', N'Cập nhật đơn hàng', GETDATE()),
    (N'USER', N'READ',   N'Xem người dùng', GETDATE()),
    (N'USER', N'UPDATE', N'Sửa người dùng', GETDATE()),
    (N'USER', N'CREATE', N'Thêm người dùng', GETDATE()),
    (N'CATEGORY', N'CREATE', N'Thêm danh mục', GETDATE()),
    (N'CATEGORY', N'READ',   N'Xem danh mục', GETDATE()),
    (N'CATEGORY', N'UPDATE', N'Sửa danh mục', GETDATE()),
    (N'CATEGORY', N'DELETE', N'Xóa danh mục', GETDATE()),
    (N'PROMOTION', N'CREATE', N'Thêm khuyến mãi', GETDATE()),
    (N'PROMOTION', N'READ',   N'Xem khuyến mãi', GETDATE()),
    (N'PROMOTION', N'UPDATE', N'Sửa khuyến mãi', GETDATE()),
    (N'PROMOTION', N'DELETE', N'Xóa khuyến mãi', GETDATE()),
    (N'REVIEW', N'READ',    N'Xem đánh giá', GETDATE()),
    (N'REVIEW', N'APPROVE', N'Duyệt đánh giá', GETDATE()),
    (N'REVIEW', N'HIDE',    N'Ẩn đánh giá', GETDATE()),
    (N'REVIEW', N'DELETE',  N'Xóa đánh giá', GETDATE()),
    (N'POST', N'CREATE', N'Thêm bài viết', GETDATE()),
    (N'POST', N'READ',   N'Xem bài viết', GETDATE()),
    (N'POST', N'UPDATE', N'Sửa bài viết', GETDATE()),
    (N'POST', N'DELETE', N'Xóa bài viết', GETDATE()),
    (N'VARIANT', N'CREATE', N'Thêm biến thể', GETDATE()),
    (N'VARIANT', N'READ',   N'Xem biến thể', GETDATE()),
    (N'VARIANT', N'UPDATE', N'Sửa biến thể', GETDATE()),
    (N'VARIANT', N'DELETE', N'Xóa biến thể', GETDATE()),
    (N'ROLE', N'CREATE', N'Thêm vai trò', GETDATE()),
    (N'ROLE', N'READ',   N'Xem vai trò', GETDATE()),
    (N'ROLE', N'UPDATE', N'Sửa vai trò', GETDATE()),
    (N'ROLE', N'DELETE', N'Xóa vai trò', GETDATE()),
    (N'NOTIFICATION', N'CREATE', N'Tạo thông báo', GETDATE()),
    (N'NOTIFICATION', N'READ',   N'Xem thông báo', GETDATE()),
    (N'NOTIFICATION', N'UPDATE', N'Sửa thông báo', GETDATE()),
    (N'NOTIFICATION', N'DELETE', N'Xóa thông báo', GETDATE()),
    (N'AUDIT_LOG', N'READ', N'Xem nhật ký hệ thống', GETDATE()),
    (N'STORE', N'CREATE', N'Thêm địa chỉ cửa hàng', GETDATE()),
    (N'STORE', N'READ',   N'Xem địa chỉ cửa hàng', GETDATE()),
    (N'STORE', N'UPDATE', N'Sửa địa chỉ cửa hàng', GETDATE()),
    (N'STORE', N'DELETE', N'Xóa địa chỉ cửa hàng', GETDATE()),
    (N'BANNER', N'CREATE', N'Thêm banner', GETDATE()),
    (N'BANNER', N'READ',   N'Xem banner', GETDATE()),
    (N'BANNER', N'UPDATE', N'Sửa banner', GETDATE()),
    (N'BANNER', N'DELETE', N'Xóa banner', GETDATE()),
    (N'CUSTOMER', N'READ',   N'Xem khách hàng', GETDATE()),
    (N'CUSTOMER', N'UPDATE', N'Sửa khách hàng', GETDATE()),
    (N'HOMEPAGE', N'READ',   N'Xem cấu hình trang chủ', GETDATE()),
    (N'HOMEPAGE', N'UPDATE', N'Sửa cấu hình trang chủ', GETDATE()),
    (N'APPEARANCE', N'READ',   N'Xem giao diện', GETDATE()),
    (N'APPEARANCE', N'UPDATE', N'Sửa giao diện', GETDATE()),
    (N'ANALYTICS', N'READ', N'Xem phân tích', GETDATE()),
    (N'EMAIL_SETTING', N'READ',   N'Xem cấu hình email', GETDATE()),
    (N'EMAIL_SETTING', N'UPDATE', N'Sửa cấu hình email', GETDATE()),
    (N'PAYMENT_SETTING', N'READ',   N'Xem cấu hình thanh toán', GETDATE()),
    (N'PAYMENT_SETTING', N'UPDATE', N'Sửa cấu hình thanh toán', GETDATE()),
    (N'SHIPPING_SETTING', N'READ',   N'Xem cấu hình vận chuyển', GETDATE()),
    (N'SHIPPING_SETTING', N'UPDATE', N'Sửa cấu hình vận chuyển', GETDATE()),
    (N'POST_CATEGORY', N'CREATE', N'Thêm danh mục bài viết', GETDATE()),
    (N'POST_CATEGORY', N'READ',   N'Xem danh mục bài viết', GETDATE()),
    (N'POST_CATEGORY', N'UPDATE', N'Sửa danh mục bài viết', GETDATE()),
    (N'POST_CATEGORY', N'DELETE', N'Xóa danh mục bài viết', GETDATE()),
    (N'FLASH_SALE', N'CREATE', N'Thêm chương trình flash sale', GETDATE()),
    (N'FLASH_SALE', N'READ',   N'Xem chương trình flash sale', GETDATE()),
    (N'FLASH_SALE', N'UPDATE', N'Sửa chương trình flash sale', GETDATE()),
    (N'FLASH_SALE', N'DELETE', N'Xóa chương trình flash sale', GETDATE()),
    (N'FOOTER_LINK', N'CREATE', N'Thêm link chân trang', GETDATE()),
    (N'FOOTER_LINK', N'READ',   N'Xem link chân trang', GETDATE()),
    (N'FOOTER_LINK', N'UPDATE', N'Sửa link chân trang', GETDATE()),
    (N'FOOTER_LINK', N'DELETE', N'Xóa link chân trang', GETDATE()),
    (N'PRICE_HISTORY', N'READ', N'Xem lịch sử giá', GETDATE()),
    (N'LOYALTY', N'READ',   N'Xem tích điểm khách hàng', GETDATE()),
    (N'LOYALTY', N'UPDATE', N'Sửa tích điểm khách hàng', GETDATE()),
    (N'ALERT', N'READ',   N'Xem cảnh báo', GETDATE()),
    (N'ALERT', N'UPDATE', N'Sửa cảnh báo', GETDATE()),
    (N'CONTACT_MESSAGE', N'READ',   N'Xem tin nhắn liên hệ', GETDATE()),
    (N'CONTACT_MESSAGE', N'UPDATE', N'Sửa tin nhắn liên hệ', GETDATE()),
    (N'CONTACT_MESSAGE', N'DELETE', N'Xóa tin nhắn liên hệ', GETDATE());
GO

-- PRODUCT_OWNER (chu so huu) duoc gan TOAN BO permission (bypass)
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = N'PRODUCT_OWNER'), id FROM permissions;
GO

-- ADMIN duoc gan TOAN BO permission (khong bypass)
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = N'ADMIN'), id FROM permissions;
GO

-- STAFF duoc gan quyen xu ly don hang, san pham, danh gia, khach hang, tin nhan
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT id FROM roles WHERE name = N'STAFF'), id FROM permissions
WHERE (module IN ('DASHBOARD','ORDER','PRODUCT','VARIANT','REVIEW','CUSTOMER','CONTACT_MESSAGE','CATEGORY')
       AND action IN ('READ','UPDATE'))
   OR (module = 'PRODUCT' AND action = 'CREATE')
   OR (module = 'VARIANT' AND action = 'CREATE')
   OR (module = 'REVIEW' AND action IN ('APPROVE','HIDE'))
   OR (module = 'ORDER' AND action = 'UPDATE');
GO

-- Tai khoan mac dinh
-- Mat khau "admin@123" - hash BCrypt duoi day DA duoc verify thuc te (bcrypt.checkpw)
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('admin', 'admin@duastore.vn',
     '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO',
     N'Quản Trị Viên', '0901234567', 1, GETDATE());
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('nguyenvan', 'nguyen@gmail.com',
     '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO',
     N'Nguyễn Văn An', '0912345678', 1, GETDATE());
GO

-- ============================================================
-- SEED: Tai khoan test cho ca 4 vai tro (de kiem thu phan quyen)
-- Mat khau tat ca deu la "admin@123" (dung chung hash da verify o tren)
-- ============================================================
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('owner2', 'owner2@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Phạm Văn Chu', '0930000001', 1, GETDATE()),
    ('owner3', 'owner3@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Lê Thị Hằng', '0930000002', 1, GETDATE()),
    ('owner4', 'owner4@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Trần Văn Kiệt', '0930000003', 1, GETDATE()),
    ('admin2', 'admin2@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Nguyễn Thị Quyên', '0931000001', 1, GETDATE()),
    ('admin3', 'admin3@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Trần Văn Quang', '0931000002', 1, GETDATE()),
    ('admin4', 'admin4@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Lê Thị Huệ', '0931000003', 1, GETDATE()),
    ('admin5', 'admin5@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Phạm Văn Tài', '0931000004', 1, GETDATE()),
    ('admin6', 'admin6@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Võ Thị Ngân', '0931000005', 1, GETDATE()),
    ('staff1', 'staff1@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Bùi Văn Nhân', '0932000001', 1, GETDATE()),
    ('staff2', 'staff2@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Đỗ Thị Thảo', '0932000002', 1, GETDATE()),
    ('staff3', 'staff3@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Ngô Văn Lộc', '0932000003', 1, GETDATE()),
    ('staff4', 'staff4@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Đặng Thị Yến', '0932000004', 1, GETDATE()),
    ('staff5', 'staff5@duastore.vn', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Hoàng Văn Phong', '0932000005', 1, GETDATE()),
    ('tranthib', 'tranthib@gmail.com', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Trần Thị Bình', '0913456789', 1, GETDATE()),
    ('lehoangc', 'lehoangc@gmail.com', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Lê Hoàng Cường', '0914567890', 1, GETDATE()),
    ('phamthid', 'phamthid@gmail.com', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Phạm Thị Dung', '0915678901', 1, GETDATE()),
    ('vominhe', 'vominhe@gmail.com', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Võ Minh Đức', '0916789012', 1, GETDATE()),
    ('dangthif', 'dangthif@gmail.com', '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO', N'Đặng Thị Phương', '0917890123', 1, GETDATE());
GO

-- Gan vai tro: admin -> PRODUCT_OWNER (toan quyen), nguyenvan -> USER
INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE username = 'admin'),     (SELECT id FROM roles WHERE name = N'PRODUCT_OWNER')),
    ((SELECT id FROM users WHERE username = 'nguyenvan'), (SELECT id FROM roles WHERE name = N'USER'));
GO

-- Gan vai tro cho cac tai khoan test moi (4 vai tro, de kiem thu phan quyen)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE name = N'PRODUCT_OWNER')
FROM users u WHERE u.username IN ('owner2','owner3','owner4');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE name = N'ADMIN')
FROM users u WHERE u.username IN ('admin2','admin3','admin4','admin5','admin6');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE name = N'STAFF')
FROM users u WHERE u.username IN ('staff1','staff2','staff3','staff4','staff5');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE name = N'USER')
FROM users u WHERE u.username IN ('tranthib','lehoangc','phamthid','vominhe','dangthif');
GO

-- Seed: mac dinh moi user co phuong thuc dang nhap PASSWORD
INSERT INTO user_auth_providers (userId, provider, linkedAt)
SELECT id, 'PASSWORD', ngayTao FROM users;
GO

-- ============================================================
-- SEED: Danh muc + San pham mau
-- ============================================================
INSERT INTO Categories (tenDanhMuc, moTa, thuTuHienThi) VALUES
    (N'Chai Thủy Tinh',  N'Các loại chai thủy tinh đựng rượu, nước hoa, thực phẩm', 1),
    (N'Hũ Thủy Tinh',    N'Hũ đựng đồ khô, thực phẩm, gia vị',                      2),
    (N'Bình Trang Trí',  N'Bình hoa, bình decor, trưng bày nhà cửa',               3),
    (N'Ly & Cốc',        N'Các loại ly cốc thủy tinh cao cấp',                      4),
    (N'Quà Tặng',        N'Bộ set quà tặng thủy tinh sang trọng',                   5);

INSERT INTO Categories (tenDanhMuc, parentId, thuTuHienThi) VALUES
    (N'Chai Rượu',    1, 1), (N'Chai Nước Hoa', 1, 2), (N'Chai Thực Phẩm', 1, 3),
    (N'Bình Hoa',     3, 1), (N'Bình Decor',    3, 2),
    (N'Ly Rượu Vang', 4, 1), (N'Ly Whisky',     4, 2), (N'Ly Nước',       4, 3),
    (N'Ly Champagne', 4, 4), (N'Ly Highball',   4, 5);
GO

-- Danh muc con bo sung (danh muc goc con it, tach nho de de duyet hon)
INSERT INTO Categories (tenDanhMuc, parentId, thuTuHienThi)
SELECT N'Bình Hoa Pha Lê', id, 3 FROM Categories WHERE tenDanhMuc = N'Bình Trang Trí'
UNION ALL
SELECT N'Bình Decanter Rượu', id, 4 FROM Categories WHERE tenDanhMuc = N'Bình Trang Trí'
UNION ALL
SELECT N'Hũ Đựng Thực Phẩm', id, 1 FROM Categories WHERE tenDanhMuc = N'Hũ Thủy Tinh'
UNION ALL
SELECT N'Hũ Ngâm Rượu', id, 2 FROM Categories WHERE tenDanhMuc = N'Hũ Thủy Tinh'
UNION ALL
SELECT N'Ly Màu Nghệ Thuật', id, 6 FROM Categories WHERE tenDanhMuc = N'Ly & Cốc';
GO

-- Anh dai dien danh muc (hien tren trang chu / mega-menu)
UPDATE Categories SET imageUrl = '/images/products/chai-tron-250ml-nap-go.jpg' WHERE tenDanhMuc = N'Chai Thủy Tinh';
UPDATE Categories SET imageUrl = '/images/products/hu-hinh-trai-bi-500ml.jpg' WHERE tenDanhMuc = N'Hũ Thủy Tinh';
UPDATE Categories SET imageUrl = '/images/products/binh-hoa-trong-25cm.jpg' WHERE tenDanhMuc = N'Bình Trang Trí';
UPDATE Categories SET imageUrl = '/images/products/ly-highball-300ml.jpg' WHERE tenDanhMuc = N'Ly & Cốc';
UPDATE Categories SET imageUrl = '/images/products/bo-tach-tra-200ml.jpg' WHERE tenDanhMuc = N'Quà Tặng';
UPDATE Categories SET imageUrl = '/images/products/chai-vodka-tron-500ml.jpg' WHERE tenDanhMuc = N'Chai Rượu';
UPDATE Categories SET imageUrl = '/images/products/chai-nuoc-hoa-vuong-100ml.jpg' WHERE tenDanhMuc = N'Chai Nước Hoa';
UPDATE Categories SET imageUrl = '/images/products/chai-tron-500ml-nap-nhua.jpg' WHERE tenDanhMuc = N'Chai Thực Phẩm';
UPDATE Categories SET imageUrl = '/images/products/bo-binh-hoa-mau-800ml.jpg' WHERE tenDanhMuc = N'Bình Hoa';
UPDATE Categories SET imageUrl = '/images/products/binh-hoa-cobalt-25cm.jpg' WHERE tenDanhMuc = N'Bình Decor';
UPDATE Categories SET imageUrl = '/images/products/ly-vang-350ml-don.jpg' WHERE tenDanhMuc = N'Ly Rượu Vang';
UPDATE Categories SET imageUrl = '/images/products/ly-highball-450ml.jpg' WHERE tenDanhMuc = N'Ly Whisky';
UPDATE Categories SET imageUrl = '/images/products/coc-co-quai-250ml.jpg' WHERE tenDanhMuc = N'Ly Nước';
UPDATE Categories SET imageUrl = '/images/products/ly-vang-200ml-don.jpg' WHERE tenDanhMuc = N'Ly Champagne';
UPDATE Categories SET imageUrl = '/images/products/ly-highball-bo6.jpg' WHERE tenDanhMuc = N'Ly Highball';
UPDATE Categories SET imageUrl = '/images/products/binh-hoa-pha-le-1000ml.jpg' WHERE tenDanhMuc = N'Bình Hoa Pha Lê';
UPDATE Categories SET imageUrl = '/images/products/binh-decanter-hario-400ml.jpg' WHERE tenDanhMuc = N'Bình Decanter Rượu';
UPDATE Categories SET imageUrl = '/images/products/hu-hinh-trai-bi-500ml.jpg' WHERE tenDanhMuc = N'Hũ Đựng Thực Phẩm';
UPDATE Categories SET imageUrl = '/images/products/binh-ngam-ruou-5000ml.jpg' WHERE tenDanhMuc = N'Hũ Ngâm Rượu';
UPDATE Categories SET imageUrl = '/images/products/bo-ly-mau-1-350ml.jpg' WHERE tenDanhMuc = N'Ly Màu Nghệ Thuật';
GO

INSERT INTO Products (tenSanPham, moTa, chatLieu, xuatXu, mucDichSuDung, danhMucId, trangThaiSanPham, isFeatured) VALUES
    (N'Chai Thủy Tinh Đựng Rượu Tròn',
     N'Chai thủy tinh hình tròn, miệng rộng, phù hợp đựng rượu, nước ép, si-rô. Chất liệu thủy tinh trong suốt, an toàn thực phẩm.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng đồ uống', 6, 'DANG_BAN', 1),
    (N'Chai Thủy Tinh Vuông Cổ Lãng',
     N'Chai hình vuông cổ lãng, thiết kế sang trọng, đựng rượu vang, nước hoa cao cấp.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng đồ uống', 6, 'DANG_BAN', 1),
    (N'Bình Hoa Pha Lê Cắt Cạnh',
     N'Bình cắm hoa pha lê cắt cạnh thủ công, sang trọng, là quà tặng ý nghĩa cho dịp sinh nhật, cưới hỏi.',
     N'Pha lê cắt cạnh', N'Châu Âu', N'Trang trí', 9, 'DANG_BAN', 1),
    (N'Ly Rượu Vang Pha Lê Bohemia',
     N'Ly rượu vang pha lê Bohemia chính hãng, trong suốt tuyệt đối, thanh lịch. Nhà máy Bohemia - Séc.',
     N'Pha lê Bohemia', N'Séc', N'Đựng đồ uống', 11, 'DANG_BAN', 1),
    (N'Ly Highball Thủy Tinh Cao Cấp',
     N'Ly Highball thủy tinh cao, thành mỏng, phù hợp pha cocktail, nước có ga, whisky on the rocks.',
     N'Thủy tinh cường lực', N'Việt Nam', N'Đựng đồ uống', 15, 'DANG_BAN', 0),
    (N'Bình Pha Lê Pasabahce Nhập Khẩu',
     N'Bình pha lê cao cấp nhập khẩu từ Thổ Nhĩ Kỳ, thương hiệu Pasabahce. Đặt trước 7-10 ngày làm việc.',
     N'Pha lê cao cấp', N'Thổ Nhĩ Kỳ', N'Trang trí', 9, 'DAT_TRUOC', 1);

UPDATE Products SET leadTimeDays = 10 WHERE tenSanPham LIKE N'%Pasabahce%';
GO

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (1, N'50ml - Nắp Gỗ',    50,  15000, NULL,  20, N'/images/products/chai-tron-50ml-nap-go.jpg',    0),
    (1, N'50ml - Nắp Nhựa',  50,  13000, NULL,  15, N'/images/products/chai-tron-50ml-nap-nhua.jpg',  0),
    (1, N'100ml - Nắp Gỗ',  100,  23000, NULL,  50, N'/images/products/chai-tron-100ml-nap-go.jpg',   1),
    (1, N'100ml - Nắp Nhựa',100,  20000, NULL,  40, N'/images/products/chai-tron-100ml-nap-nhua.jpg', 0),
    (1, N'250ml - Nắp Gỗ',  250,  38000, 34000, 30, N'/images/products/chai-tron-250ml-nap-go.jpg',   0),
    (1, N'250ml - Nắp Nhựa',250,  32000, NULL,  25, N'/images/products/chai-tron-250ml-nap-nhua.jpg', 0),
    (1, N'500ml - Nắp Gỗ',  500,  55000, 50000,  0, N'/images/products/chai-tron-500ml-nap-go.jpg',   0),
    (1, N'500ml - Nắp Nhựa',500,  48000, NULL,  15, N'/images/products/chai-tron-500ml-nap-nhua.jpg', 0),
    (1, N'750ml - Nắp Gỗ',  750,  72000, NULL,  10, N'/images/products/chai-tron-750ml-nap-go.jpg',   0),
    (1, N'750ml - Nắp Nhựa',750,  65000, NULL,   8, N'/images/products/chai-tron-750ml-nap-nhua.jpg', 0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (2, N'250ml - Nắp Bạc', 250, 25000, NULL,  35, N'/images/products/chai-vuong-250ml-nap-bac.jpg', 1),
    (2, N'500ml - Nắp Bạc', 500, 42000, NULL,  20, N'/images/products/chai-vuong-500ml-nap-bac.jpg', 0),
    (2, N'750ml - Nắp Bạc', 750, 68000, 60000, 12, N'/images/products/chai-vuong-750ml-nap-bac.jpg', 0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (3, N'Trong suốt - Cao 25cm',  NULL, 450000, NULL,   15, N'/images/products/binh-hoa-trong-25cm.jpg',  1),
    (3, N'Xanh Cobalt - Cao 25cm', NULL, 520000, 480000,  8, N'/images/products/binh-hoa-cobalt-25cm.jpg', 0),
    (3, N'Nâu khói - Cao 25cm',    NULL, 490000, NULL,   10, N'/images/products/binh-hoa-nau-25cm.jpg',    0),
    (3, N'Trong suốt - Cao 35cm',  NULL, 620000, NULL,    6, N'/images/products/binh-hoa-trong-35cm.jpg',  0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (4, N'200ml - Đơn chiếc', NULL,  84000, NULL,   56, N'/images/products/ly-vang-200ml-don.jpg',  1),
    (4, N'350ml - Đơn chiếc', NULL, 105000,  95000, 22, N'/images/products/ly-vang-350ml-don.jpg',  0),
    (4, N'Bộ 6 cái - 200ml',  NULL, 480000, 430000, 15, N'/images/products/ly-vang-200ml-bo6.jpg',  0),
    (4, N'Bộ 6 cái - 350ml',  NULL, 600000, 550000,  8, N'/images/products/ly-vang-350ml-bo6.jpg',  0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (5, N'300ml - Đơn chiếc', 300,  35000, NULL,   80, N'/images/products/ly-highball-300ml.jpg', 1),
    (5, N'450ml - Đơn chiếc', 450,  45000,  40000, 60, N'/images/products/ly-highball-450ml.jpg', 0),
    (5, N'Bộ 6 cái - 300ml',  300, 195000, 175000, 20, N'/images/products/ly-highball-bo6.jpg',   0);

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault) VALUES
    (6, N'500ml - Pha lê trắng', 500, 850000, NULL,   0, N'/images/products/pasabahce-500ml.jpg', 1),
    (6, N'750ml - Pha lê trắng', 750, 980000, 890000, 0, N'/images/products/pasabahce-750ml.jpg', 0);
GO

-- ============================================================
-- SEED: 18 san pham moi (anh thuc te trong uploads/, ten/mo ta
-- duoc phan tich truc tiep tu ten file anh)
-- ============================================================
INSERT INTO Products (tenSanPham, moTa, chatLieu, xuatXu, mucDichSuDung, danhMucId, trangThaiSanPham, isFeatured) VALUES
    (N'Bình Chiết Rượu Vang',
     N'Bình thủy tinh chiết rót rượu vang, giúp rượu "thở" nhanh hơn trước khi thưởng thức, thiết kế dày nhẹ sang trọng.',
     N'Thủy tinh cao cấp', N'Việt Nam', N'Đựng đồ uống', 10, 'DANG_BAN', 1),
    (N'Bình Cắm Hoa Trang Trí Loại Nhỏ',
     N'Bình cắm hoa thủy tinh kích thước nhỏ gọn, phù hợp bàn làm việc, bàn ăn, trang trí không gian nhỏ.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Trang trí', 9, 'DANG_BAN', 0),
    (N'Bình Hoa Pha Lê',
     N'Bình hoa pha lê cao cấp, bề mặt sáng bóng, là điểm nhấn sang trọng cho phòng khách.',
     N'Pha lê', N'Việt Nam', N'Trang trí', 9, 'DANG_BAN', 1),
    (N'Bình Khuếch Tán Tinh Dầu',
     N'Bình thủy tinh dùng khuếch tán tinh dầu, thiết kế thanh mảnh, phù hợp trang trí phòng ngủ, phòng khách.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Trang trí', 10, 'DANG_BAN', 0),
    (N'Bình Thủy Tinh Ngâm Rượu',
     N'Bình thủy tinh dung tích lớn chuyên ngâm rượu thuốc, rượu trái cây, miệng bình rộng dễ sơ chế nguyên liệu.',
     N'Thủy tinh dày', N'Việt Nam', N'Đựng đồ uống', 2, 'DANG_BAN', 0),
    (N'Bình Đựng Nước Thủy Tinh Dập Nổi Sang Trọng',
     N'Bình đựng nước thủy tinh họa tiết dập nổi tinh tế, sang trọng, phù hợp dùng bàn ăn gia đình hoặc nhà hàng.',
     N'Thủy tinh dập nổi', N'Việt Nam', N'Đựng đồ uống', 13, 'DANG_BAN', 1),
    (N'Bình Thủy Tinh Decanter Hario',
     N'Bình decanter thủy tinh thương hiệu Hario nhập khẩu Nhật Bản, thiết kế tinh giản, phù hợp pha và rót cà phê/trà.',
     N'Thủy tinh chịu nhiệt', N'Nhật Bản', N'Đựng đồ uống', 10, 'DANG_BAN', 1),
    (N'Bộ Bình Hoa Màu',
     N'Bộ bình hoa thủy tinh màu sắc tươi sáng, phối hợp linh hoạt, tạo điểm nhấn cho không gian sống.',
     N'Thủy tinh màu', N'Việt Nam', N'Trang trí', 9, 'DANG_BAN', 0),
    (N'Bộ Ly Thủy Tinh Màu 1',
     N'Bộ ly thủy tinh phối màu thời trang, phù hợp đựng nước, nước ép, là món quà tặng ý nghĩa.',
     N'Thủy tinh màu', N'Việt Nam', N'Đựng đồ uống', 13, 'DANG_BAN', 0),
    (N'Bộ Ly Thủy Tinh Màu 2',
     N'Bộ ly thủy tinh phối màu phong cách khác biệt, thiết kế hiện đại, dễ dàng phối với nội thất.',
     N'Thủy tinh màu', N'Việt Nam', N'Đựng đồ uống', 13, 'DANG_BAN', 0),
    (N'Bộ Tách Trà Thủy Tinh',
     N'Bộ tách trà thủy tinh chịu nhiệt, quan sát được màu nước trà, phù hợp làm quà tặng cho người thân, đối tác.',
     N'Thủy tinh chịu nhiệt', N'Việt Nam', N'Quà tặng', 5, 'DANG_BAN', 1),
    (N'Chai Nước Hoa Vuông Thủy Tinh',
     N'Chai thủy tinh dạng vuông dùng chiết nước hoa, tinh dầu, thiết kế nhỏ gọn tiện mang theo.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng mỹ phẩm', 7, 'DANG_BAN', 0),
    (N'Chai Rượu Vodka Thủy Tinh Dạng Tròn',
     N'Chai thủy tinh dạng tròn dùng đóng chai rượu Vodka, rượu trắng, miệng chai vừa nắp xoay tiêu chuẩn.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng đồ uống', 6, 'DANG_BAN', 0),
    (N'Cốc Thủy Tinh Có Quai',
     N'Cốc thủy tinh có quai cầm chắc tay, phù hợp đựng nước nóng/lạnh, cà phê, trà sữa.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng đồ uống', 13, 'DANG_BAN', 0),
    (N'Hũ Thủy Tinh Hình Trái Bí',
     N'Hũ thủy tinh tạo hình trái bí ngộ nghĩnh, phù hợp đựng bánh kẹo, đồ khô, trang trí bàn ăn dịp lễ.',
     N'Thủy tinh trong suốt', N'Việt Nam', N'Đựng đồ khô', 2, 'DANG_BAN', 1),
    (N'Hũ Thủy Tinh Nắp Cài Kín Hơi',
     N'Hũ thủy tinh nắp cài kín hơi, giữ đồ khô, gia vị, mứt tươi lâu, chống ẩm hiệu quả.',
     N'Thủy tinh dày', N'Việt Nam', N'Đựng đồ khô', 2, 'DANG_BAN', 0),
    (N'Hũ Thủy Tinh Sọc',
     N'Hũ thủy tinh họa tiết sọc dọc tinh tế, kích thước nhỏ gọn, phù hợp đựng gia vị, đồ khô số lượng ít.',
     N'Thủy tinh họa tiết', N'Việt Nam', N'Đựng đồ khô', 2, 'DANG_BAN', 0),
    (N'Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc',
     N'Ly thủy tinh dạng trụ tròn, họa tiết sọc nổi tinh tế, cầm chắc tay, phù hợp đựng nước, bia, nước ép.',
     N'Thủy tinh họa tiết', N'Việt Nam', N'Đựng đồ uống', 13, 'DANG_BAN', 0);
GO

INSERT INTO ProductVariants (productId, tenBienThe, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh, isDefault)
SELECT p.id, N'Mặc định', v.dungTich, v.giaGoc, v.giaKhuyenMai, v.soLuongTon, v.hinhAnh, 1
FROM Products p
CROSS APPLY (VALUES
    (N'Bình Chiết Rượu Vang',                       1000, 320000, 290000, 25, N'/images/products/binh-chiet-ruou-vang-1000ml.png'),
    (N'Bình Cắm Hoa Trang Trí Loại Nhỏ',              300,  95000, NULL,   40, N'/images/products/binh-cam-hoa-trang-tri-nho-300ml.jpg'),
    (N'Bình Hoa Pha Lê',                             1000, 680000, 620000, 12, N'/images/products/binh-hoa-pha-le-1000ml.jpg'),
    (N'Bình Khuếch Tán Tinh Dầu',                     350, 150000, NULL,   30, N'/images/products/binh-khuech-tan-tinh-dau-350ml.jpg'),
    (N'Bình Thủy Tinh Ngâm Rượu',                    5000, 280000, NULL,   15, N'/images/products/binh-ngam-ruou-5000ml.jpg'),
    (N'Bình Đựng Nước Thủy Tinh Dập Nổi Sang Trọng',  1000, 220000, 195000, 20, N'/images/products/binh-dung-nuoc-dap-noi-1000ml.jpg'),
    (N'Bình Thủy Tinh Decanter Hario',                 400, 385000, NULL,   18, N'/images/products/binh-decanter-hario-400ml.jpg'),
    (N'Bộ Bình Hoa Màu',                               800, 175000, NULL,   22, N'/images/products/bo-binh-hoa-mau-800ml.jpg'),
    (N'Bộ Ly Thủy Tinh Màu 1',                         350, 145000, NULL,   35, N'/images/products/bo-ly-mau-1-350ml.jpg'),
    (N'Bộ Ly Thủy Tinh Màu 2',                         350, 145000, NULL,   35, N'/images/products/bo-ly-mau-2-350ml.jpg'),
    (N'Bộ Tách Trà Thủy Tinh',                         200, 210000, 189000, 28, N'/images/products/bo-tach-tra-200ml.jpg'),
    (N'Chai Nước Hoa Vuông Thủy Tinh',                 100,  45000, NULL,   50, N'/images/products/chai-nuoc-hoa-vuong-100ml.jpg'),
    (N'Chai Rượu Vodka Thủy Tinh Dạng Tròn',           500,  38000, NULL,   60, N'/images/products/chai-vodka-tron-500ml.jpg'),
    (N'Cốc Thủy Tinh Có Quai',                         250,  42000, NULL,   45, N'/images/products/coc-co-quai-250ml.jpg'),
    (N'Hũ Thủy Tinh Hình Trái Bí',                     500,  68000, NULL,   32, N'/images/products/hu-hinh-trai-bi-500ml.jpg'),
    (N'Hũ Thủy Tinh Nắp Cài Kín Hơi',                  750,  55000, NULL,   38, N'/images/products/hu-nap-cai-kin-hoi-750ml.jpg'),
    (N'Hũ Thủy Tinh Sọc',                              100,  32000, NULL,   55, N'/images/products/hu-soc-100ml.jpg'),
    (N'Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc',       400,  40000, NULL,   42, N'/images/products/ly-tru-tron-hoa-tiet-soc-400ml.jpg')
) AS v(tenSanPham, dungTich, giaGoc, giaKhuyenMai, soLuongTon, hinhAnh)
WHERE p.tenSanPham = v.tenSanPham;
GO

INSERT INTO Addresses (userId, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, isDefault) VALUES
    (2, N'Nguyễn Văn An', '0912345678', N'Hải Phòng', N'Ngô Quyền', N'Máy Tơ', N'123 Trần Hưng Đạo', 1);

-- Cac ma khuyen mai "toan bo" (targetType='ALL' HOAC NULL — ca 2 deu roi vao nhanh
-- "default -> true" trong resolveEligibleAmount/isPromotionApplicableToProduct, tuc ap
-- dung cho MOI san pham) bi VO HIEU HOA (isActive=0): kho kiem soat, tung gay le pricing
-- kho hieu (vd COMBO30 tu dong ap vao ca hang Flash Sale luc checkout; KHAIHANG tu dong
-- hien -15% tren MOI trang san pham, ke ca hang da het/hang test). Rieng loaiGiam=PHAN_TRAM
-- voi targetType rong la nguy hiem nhat vi con bi chon lam "bestPercentagePromo" hien
-- thang len gia san pham. Van giu lai dong INSERT (chi doi isActive=0) thay vi xoa han,
-- vi UserVouchers/orders phia duoi con FK tham chieu toi.
INSERT INTO Promotions (maCode, tenChuongTrinh, loaiGiam, giaTriGiam, donHangToiThieu, giamToiDa, soLanDung, tuNgay, denNgay, targetType, targetIds, isActive) VALUES
    ('KHAIHANG', N'Khai trương DuaStore Hải Phòng', 'PHAN_TRAM', 15, 200000, 100000, 200, '2026-01-01', '2026-12-31', NULL, NULL, 0),
    ('FREESHIP',  N'Miễn phí vận chuyển đơn từ 500k', 'SO_TIEN',  30000, 500000, NULL, NULL, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('DECO50K',   N'Giảm 50k đơn từ 300k',           'SO_TIEN',  50000, 300000, NULL,  100, '2026-01-01', '2026-12-31', NULL, NULL, 1),
    ('SUMMER50',  N'Summer Sale - Giảm 50%',           'PHAN_TRAM', 50, 500000, 200000, 500, '2026-06-01', '2026-08-31', 'PRODUCT', '1,2,3,4,5', 1),
    ('NEWUSER',   N'Ưu đãi người mới - Giảm 20%',      'PHAN_TRAM', 20, 100000, 50000,  1000, '2026-01-01', '2026-12-31', 'ALL', NULL, 0),
    ('THANG9',    N'Ưu đãi tháng 9 - Giảm 10%',         'PHAN_TRAM', 10, 150000, 80000,  300, '2026-09-01', '2026-09-30', 'ALL', NULL, 0),
    ('SINHNHAT',  N'Quà sinh nhật thành viên',          'PHAN_TRAM', 15, 0,      100000, 500, '2026-01-01', '2026-12-31', 'ALL', NULL, 0),
    ('VIP100K',   N'Giảm 100k cho đơn VIP từ 1 triệu',  'SO_TIEN',   100000, 1000000, NULL, 100, '2026-01-01', '2026-12-31', 'ALL', NULL, 0),
    ('COMBO30',   N'Combo giảm 30% danh mục ly & cốc',  'PHAN_TRAM', 30, 100000, 150000, 200, '2026-01-01', '2026-12-31', 'ALL', NULL, 0),
    ('CUOINAM',   N'Ưu đãi cuối năm - Giảm 25%',        'PHAN_TRAM', 25, 200000, 150000, 400, '2026-10-01', '2026-12-31', 'ALL', NULL, 0);

INSERT INTO orders (maDon, userId, addressId, snapTenNguoiNhan, snapSoDienThoai, snapDiaChi,
    tienHang, phiVanChuyen, tienGiam, tongThanhToan,
    phuongThucTT, phuongThucGiaoHang, trangThaiTT, trangThaiDon, promotionId, ngayDat) VALUES
    ('DUA-20260001', 2, 1, N'Nguyễn Văn An', '0912345678',
     N'123 Trần Hưng Đạo, Máy Tơ, Ngô Quyền, Hải Phòng',
     354000, 30000, 30000, 354000, 'CHUYEN_KHOAN', 'SHIP', 'CHUA_THANH_TOAN', 'CHO_XAC_NHAN', 2, GETDATE());

INSERT INTO order_items (orderId, productId, variantId, tenSanPham, tenBienThe, hinhAnhSP, donGia, soLuong, thanhTien) VALUES
    (1, 1,  3, N'Chai Thủy Tinh Đựng Rượu Tròn', N'100ml - Nắp Gỗ',    N'/images/products/chai-tron-100ml-nap-go.jpg',  23000, 2,  46000),
    (1, 4, 14, N'Ly Rượu Vang Pha Lê Bohemia',   N'200ml - Đơn chiếc', N'/images/products/ly-vang-200ml-don.jpg',       84000, 3, 252000),
    (1, 5, 18, N'Ly Highball Thủy Tinh Cao Cấp',  N'300ml - Đơn chiếc', N'/images/products/ly-highball-300ml.jpg',       35000, 1,  35000);

INSERT INTO PostCategories (tenDanhMuc, moTa, thuTu, ngayTao) VALUES
    (N'Hướng Dẫn', N'Các bài hướng dẫn chọn và bảo quản đồ thủy tinh', 1, GETDATE()),
    (N'Xu Hướng',  N'Xu hướng trang trí và quà tặng', 2, GETDATE()),
    (N'Chăm Sóc & Bảo Quản', N'Mẹo vệ sinh, bảo quản và kéo dài tuổi thọ đồ thủy tinh - pha lê', 3, GETDATE());

INSERT INTO Posts (tieuDe, slug, metaDescription, tomTat, noiDung, hinhAnh, tacGiaId, danhMucId, trangThai, luotXem, isFeatured, ngayXuatBan, ngayTao, ngayCapNhat) VALUES
    (N'Hướng dẫn chọn chai thủy tinh theo mục đích sử dụng',
     N'huong-dan-chon-chai-thuy-tinh-theo-muc-dich-su-dung',
     N'Cách chọn chai thủy tinh phù hợp cho rượu, nước hoa hay thực phẩm.',
     N'Phân biệt chai đựng rượu, nước hoa, thực phẩm - điểm khác biệt về miệng chai, kiểu nắp và chất liệu.',
     N'<p>Mỗi loại chai thủy tinh được thiết kế riêng cho một mục đích sử dụng cụ thể. Chai đựng rượu thường có miệng hẹp, nắp gỗ hoặc nút bần để giữ hương vị lâu dài, trong khi chai nước hoa cần cổ chai nhỏ và nắp xịt kín khí.</p><p>Đối với thực phẩm, ưu tiên chai miệng rộng để dễ vệ sinh và nắp vặn kín hơi, tránh ẩm mốc. Việc chọn đúng loại chai không chỉ giúp bảo quản tốt hơn mà còn tăng tính thẩm mỹ khi trưng bày.</p>',
     N'https://picsum.photos/seed/duastore-chai-thuytinh/900/600', 1, 1, 'XUAT_BAN', 342, 1, DATEADD(DAY,-40,GETDATE()), DATEADD(DAY,-40,GETDATE()), DATEADD(DAY,-40,GETDATE())),
    (N'Ưu điểm của thủy tinh Borosilicate so với thủy tinh thường',
     N'uu-diem-thuy-tinh-borosilicate-so-voi-thuy-tinh-thuong',
     N'Vì sao thủy tinh Borosilicate được ưa chuộng trong ngành thực phẩm, dược phẩm.',
     N'Tại sao thủy tinh Borosilicate lại được ưa chuộng trong ngành thực phẩm và dược phẩm?',
     N'<p>Thủy tinh Borosilicate chịu được sốc nhiệt tốt hơn nhiều so với thủy tinh soda-lime thông thường, nhờ hệ số giãn nở nhiệt thấp. Điều này giúp sản phẩm ít bị nứt vỡ khi chuyển đổi nhiệt độ đột ngột, ví dụ từ tủ lạnh sang lò vi sóng.</p><p>Ngoài ra, Borosilicate trơ về mặt hóa học, không phản ứng với axit hay kiềm, nên rất an toàn khi dùng đựng thực phẩm và dược phẩm lâu dài.</p>',
     N'https://picsum.photos/seed/duastore-borosilicate/900/600', 1, 1, 'XUAT_BAN', 218, 0, DATEADD(DAY,-38,GETDATE()), DATEADD(DAY,-38,GETDATE()), DATEADD(DAY,-38,GETDATE())),
    (N'Top 5 mẫu bình trang trí được ưa chuộng nhất năm nay',
     N'top-5-mau-binh-trang-tri-duoc-ua-chuong-nhat-nam-nay',
     N'Điểm qua 5 mẫu bình trang trí thủy tinh, pha lê hot nhất hiện nay.',
     N'Khảo sát xu hướng thị trường bình thủy tinh và pha lê trang trí.',
     N'<p>Từ bình hoa pha lê cắt cạnh cổ điển đến bình thủy tinh màu tối giản kiểu Bắc Âu, thị trường trang trí nội thất năm nay chứng kiến sự lên ngôi của các thiết kế vừa sang trọng vừa gần gũi thiên nhiên.</p><p>Danh sách top 5 bao gồm các mẫu bán chạy nhất tại DuaStore, được tổng hợp dựa trên số liệu đơn hàng thực tế.</p>',
     N'https://picsum.photos/seed/duastore-binh-trang-tri/900/600', 1, 2, 'NHAP', 12, 0, NULL, DATEADD(DAY,-2,GETDATE()), DATEADD(DAY,-2,GETDATE())),
    (N'5 cách vệ sinh ly pha lê không để lại vết ố',
     N'5-cach-ve-sinh-ly-pha-le-khong-de-lai-vet-o',
     N'Mẹo rửa ly pha lê sáng bóng, không bị mờ, không để lại vết nước.',
     N'Hướng dẫn vệ sinh ly pha lê đúng cách để giữ độ trong suốt và tránh trầy xước.',
     N'<p>Ly pha lê rất dễ bị mờ hoặc trầy xước nếu vệ sinh sai cách. Nên rửa bằng tay với nước ấm pha giấm trắng thay vì máy rửa chén, dùng khăn microfiber lau khô ngay để tránh vết ố do khoáng chất trong nước.</p><p>Tránh xếp chồng ly khi cất giữ, nên dùng giá treo miệng ly xuống dưới để hạn chế bụi bám vào lòng ly.</p>',
     N'https://picsum.photos/seed/duastore-ve-sinh-ly-phale/900/600', 1, 3, 'XUAT_BAN', 501, 1, DATEADD(DAY,-33,GETDATE()), DATEADD(DAY,-33,GETDATE()), DATEADD(DAY,-33,GETDATE())),
    (N'Cách bảo quản bình hoa thủy tinh bền đẹp theo thời gian',
     N'cach-bao-quan-binh-hoa-thuy-tinh-ben-dep-theo-thoi-gian',
     N'Mẹo giữ bình hoa thủy tinh luôn sáng bóng, không bị ố vàng.',
     N'Những lưu ý khi vệ sinh và bảo quản bình hoa thủy tinh trong nhà.',
     N'<p>Bình hoa thủy tinh nếu cắm hoa tươi lâu ngày dễ bị đóng cặn ở đáy. Nên thay nước 2-3 ngày/lần và vệ sinh đáy bình bằng bàn chải mềm kết hợp baking soda.</p><p>Khi không sử dụng, nên lau khô hoàn toàn trước khi cất để tránh nấm mốc phát triển bên trong.</p>',
     N'https://picsum.photos/seed/duastore-binh-hoa-baoquan/900/600', 1, 3, 'XUAT_BAN', 276, 0, DATEADD(DAY,-30,GETDATE()), DATEADD(DAY,-30,GETDATE()), DATEADD(DAY,-30,GETDATE())),
    (N'Xu hướng trang trí bàn tiệc bằng ly và bình thủy tinh màu 2026',
     N'xu-huong-trang-tri-ban-tiec-bang-ly-va-binh-thuy-tinh-mau-2026',
     N'Cập nhật xu hướng trang trí bàn tiệc với ly, bình thủy tinh màu.',
     N'Gam màu pastel và thủy tinh cắt cạnh đang là lựa chọn hàng đầu cho tiệc cưới, tiệc sinh nhật.',
     N'<p>Năm nay, xu hướng trang trí bàn tiệc nghiêng về các gam màu pastel như xanh cobalt, hồng phấn kết hợp cùng ly pha lê cắt cạnh tạo hiệu ứng ánh sáng lung linh.</p><p>Nhiều gia đình cũng ưa chuộng bộ ly đồng bộ với bình hoa cùng tông màu để tạo điểm nhấn cho không gian tiệc.</p>',
     N'https://picsum.photos/seed/duastore-trang-tri-ban-tiec/900/600', 1, 2, 'XUAT_BAN', 189, 0, DATEADD(DAY,-28,GETDATE()), DATEADD(DAY,-28,GETDATE()), DATEADD(DAY,-28,GETDATE())),
    (N'Vì sao nên chọn ly thủy tinh cường lực cho quán cafe, nhà hàng',
     N'vi-sao-nen-chon-ly-thuy-tinh-cuong-luc-cho-quan-cafe-nha-hang',
     N'Ly thủy tinh cường lực bền, chịu va đập tốt, phù hợp kinh doanh F&B.',
     N'So sánh độ bền và tính kinh tế của ly cường lực so với ly thường trong môi trường kinh doanh.',
     N'<p>Ly thủy tinh cường lực được xử lý nhiệt đặc biệt giúp tăng khả năng chịu va đập gấp 3-4 lần so với ly thường, rất phù hợp cho quán cafe, nhà hàng có tần suất sử dụng cao.</p><p>Dù giá thành ban đầu nhỉnh hơn, nhưng về lâu dài chi phí thay thế do vỡ hỏng lại thấp hơn đáng kể.</p>',
     N'https://picsum.photos/seed/duastore-ly-cuong-luc/900/600', 1, 1, 'XUAT_BAN', 164, 0, DATEADD(DAY,-25,GETDATE()), DATEADD(DAY,-25,GETDATE()), DATEADD(DAY,-25,GETDATE())),
    (N'Cách chọn quà tặng bằng thủy tinh, pha lê ý nghĩa cho người thân',
     N'cach-chon-qua-tang-bang-thuy-tinh-pha-le-y-nghia-cho-nguoi-than',
     N'Gợi ý quà tặng thủy tinh pha lê phù hợp cho từng dịp lễ.',
     N'Từ sinh nhật, tân gia đến kỷ niệm ngày cưới - món quà thủy tinh nào phù hợp?',
     N'<p>Quà tặng bằng thủy tinh, pha lê luôn mang ý nghĩa về sự trong sáng, bền vững trong các mối quan hệ. Với dịp tân gia, bộ ly hoặc bình hoa pha lê là lựa chọn phổ biến.</p><p>Với kỷ niệm ngày cưới, có thể chọn cặp ly khắc tên hoặc bình rượu pha lê cao cấp để tăng phần trang trọng.</p>',
     N'https://picsum.photos/seed/duastore-qua-tang-phale/900/600', 1, 2, 'XUAT_BAN', 145, 0, DATEADD(DAY,-22,GETDATE()), DATEADD(DAY,-22,GETDATE()), DATEADD(DAY,-22,GETDATE())),
    (N'Phân biệt pha lê thật và thủy tinh giả pha lê',
     N'phan-biet-pha-le-that-va-thuy-tinh-gia-pha-le',
     N'Mẹo nhận biết pha lê thật qua độ trong, âm thanh và trọng lượng.',
     N'3 cách đơn giản giúp bạn phân biệt pha lê thật với hàng thủy tinh giả pha lê.',
     N'<p>Pha lê thật chứa hàm lượng oxit chì nhất định giúp tăng độ khúc xạ ánh sáng, khi gõ nhẹ sẽ phát ra âm thanh trong và vang dài hơn thủy tinh thường.</p><p>Ngoài ra, pha lê thật thường nặng tay hơn và có độ trong suốt, lấp lánh đặc trưng khi đưa ra ánh sáng.</p>',
     N'https://picsum.photos/seed/duastore-phale-that-gia/900/600', 1, 1, 'XUAT_BAN', 298, 1, DATEADD(DAY,-20,GETDATE()), DATEADD(DAY,-20,GETDATE()), DATEADD(DAY,-20,GETDATE())),
    (N'Bí quyết cắm hoa đẹp với bình thủy tinh dáng cao',
     N'bi-quyet-cam-hoa-dep-voi-binh-thuy-tinh-dang-cao',
     N'Hướng dẫn cắm hoa với bình thủy tinh dáng cao đơn giản mà đẹp mắt.',
     N'Mẹo phối hoa và bình thủy tinh dáng cao để tạo điểm nhấn cho phòng khách.',
     N'<p>Bình thủy tinh dáng cao phù hợp với các loại hoa cành dài như hoa ly, hoa lay ơn, cành lá trang trí. Nên cắm hoa theo tỷ lệ chiều cao hoa gấp 1.5 lần chiều cao bình để cân đối.</p><p>Có thể kết hợp thêm sỏi trang trí hoặc đèn led mini dưới đáy bình để tăng hiệu ứng ánh sáng vào buổi tối.</p>',
     N'https://picsum.photos/seed/duastore-cam-hoa-binh-cao/900/600', 1, 2, 'XUAT_BAN', 176, 0, DATEADD(DAY,-18,GETDATE()), DATEADD(DAY,-18,GETDATE()), DATEADD(DAY,-18,GETDATE())),
    (N'Có nên rửa đồ thủy tinh bằng máy rửa chén không?',
     N'co-nen-rua-do-thuy-tinh-bang-may-rua-chen-khong',
     N'Giải đáp việc dùng máy rửa chén cho đồ thủy tinh, pha lê.',
     N'Không phải loại thủy tinh nào cũng an toàn khi rửa bằng máy - đây là lý do vì sao.',
     N'<p>Thủy tinh Borosilicate và thủy tinh cường lực thường an toàn khi rửa máy, tuy nhiên pha lê cắt cạnh và các sản phẩm có họa tiết vẽ tay nên rửa tay để tránh phai màu hoặc trầy xước do va chạm trong máy.</p><p>Nếu bắt buộc dùng máy, nên chọn chế độ rửa nhẹ và tránh xếp chồng các món lên nhau.</p>',
     N'https://picsum.photos/seed/duastore-may-rua-chen/900/600', 1, 3, 'XUAT_BAN', 233, 0, DATEADD(DAY,-15,GETDATE()), DATEADD(DAY,-15,GETDATE()), DATEADD(DAY,-15,GETDATE())),
    (N'Xu hướng nội thất tối giản với đồ thủy tinh trong suốt',
     N'xu-huong-noi-that-toi-gian-voi-do-thuy-tinh-trong-suot',
     N'Đồ thủy tinh trong suốt lên ngôi trong phong cách nội thất tối giản.',
     N'Vì sao phong cách minimalism lại ưa chuộng vật dụng thủy tinh trong suốt?',
     N'<p>Phong cách nội thất tối giản (minimalism) đề cao sự gọn gàng, ít chi tiết thừa. Đồ thủy tinh trong suốt vừa có tính ứng dụng cao, vừa không gây rối mắt như các vật liệu có màu sắc, hoa văn phức tạp.</p><p>Xu hướng này đang được nhiều gia đình trẻ tại các thành phố lớn lựa chọn cho không gian bếp và phòng khách.</p>',
     N'https://picsum.photos/seed/duastore-noi-that-toi-gian/900/600', 1, 2, 'XUAT_BAN', 201, 0, DATEADD(DAY,-13,GETDATE()), DATEADD(DAY,-13,GETDATE()), DATEADD(DAY,-13,GETDATE())),
    (N'Hướng dẫn chọn hũ thủy tinh đựng thực phẩm khô an toàn',
     N'huong-dan-chon-hu-thuy-tinh-dung-thuc-pham-kho-an-toan',
     N'Tiêu chí chọn hũ thủy tinh bảo quản thực phẩm khô lâu dài.',
     N'Nắp cài kín hơi, chất liệu an toàn thực phẩm - những yếu tố cần lưu ý khi chọn hũ.',
     N'<p>Khi chọn hũ thủy tinh đựng thực phẩm khô như ngũ cốc, gia vị, nên ưu tiên loại có nắp cài kín hơi (airtight) để hạn chế ẩm mốc và côn trùng xâm nhập.</p><p>Chất liệu thủy tinh cần đạt chuẩn an toàn thực phẩm, không chứa BPA, đặc biệt với các hũ dùng đựng đồ ăn cho trẻ nhỏ.</p>',
     N'https://picsum.photos/seed/duastore-hu-thuy-tinh-thucpham/900/600', 1, 1, 'XUAT_BAN', 312, 0, DATEADD(DAY,-11,GETDATE()), DATEADD(DAY,-11,GETDATE()), DATEADD(DAY,-11,GETDATE())),
    (N'3 mẹo khắc phục ly thủy tinh bị mờ, ố vàng lâu ngày',
     N'3-meo-khac-phuc-ly-thuy-tinh-bi-mo-o-vang-lau-ngay',
     N'Mẹo dân gian giúp ly thủy tinh sáng bóng trở lại như mới.',
     N'Dùng giấm, baking soda hay vỏ chanh - đâu là cách hiệu quả nhất?',
     N'<p>Ly thủy tinh bị mờ, ố vàng thường do cặn khoáng trong nước hoặc dầu mỡ tích tụ lâu ngày. Ngâm ly trong dung dịch giấm trắng pha nước ấm khoảng 15 phút rồi chà nhẹ bằng bàn chải mềm là cách phổ biến và hiệu quả.</p><p>Với vết ố cứng đầu, có thể kết hợp thêm baking soda tạo hỗn hợp sệt để chà trực tiếp lên vùng bị ố.</p>',
     N'https://picsum.photos/seed/duastore-ly-bi-mo/900/600', 1, 3, 'XUAT_BAN', 421, 0, DATEADD(DAY,-9,GETDATE()), DATEADD(DAY,-9,GETDATE()), DATEADD(DAY,-9,GETDATE())),
    (N'Tại sao thủy tinh dập nổi đang là xu hướng decor 2026',
     N'tai-sao-thuy-tinh-dap-noi-dang-la-xu-huong-decor-2026',
     N'Họa tiết dập nổi trên thủy tinh mang lại vẻ đẹp hoài cổ, sang trọng.',
     N'Từ bình đựng nước đến ly uống nước, họa tiết dập nổi đang được ưa chuộng trở lại.',
     N'<p>Thủy tinh dập nổi (embossed glass) tạo hiệu ứng thị giác độc đáo nhờ các họa tiết chìm nổi trên bề mặt, mang phong cách hoài cổ pha lẫn hiện đại.</p><p>Xu hướng này phù hợp với không gian decor kiểu vintage hoặc industrial đang thịnh hành hiện nay.</p>',
     N'https://picsum.photos/seed/duastore-thuytinh-dapnoi/900/600', 1, 2, 'XUAT_BAN', 158, 0, DATEADD(DAY,-7,GETDATE()), DATEADD(DAY,-7,GETDATE()), DATEADD(DAY,-7,GETDATE())),
    (N'Hướng dẫn bảo quản rượu trong bình thủy tinh ngâm đúng cách',
     N'huong-dan-bao-quan-ruou-trong-binh-thuy-tinh-ngam-dung-cach',
     N'Cách chọn và bảo quản bình thủy tinh ngâm rượu để giữ hương vị tốt nhất.',
     N'Vị trí đặt bình, nhiệt độ và ánh sáng ảnh hưởng thế nào đến rượu ngâm?',
     N'<p>Bình thủy tinh ngâm rượu nên được đặt ở nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp vì tia UV có thể làm biến đổi hương vị và màu sắc của rượu theo thời gian.</p><p>Nên chọn bình có nắp đậy kín để hạn chế oxy hóa, đồng thời vệ sinh bình sạch sẽ trước khi ngâm để tránh nhiễm khuẩn.</p>',
     N'https://picsum.photos/seed/duastore-binh-ngam-ruou/900/600', 1, 1, 'XUAT_BAN', 267, 0, DATEADD(DAY,-5,GETDATE()), DATEADD(DAY,-5,GETDATE()), DATEADD(DAY,-5,GETDATE())),
    (N'Gợi ý set quà tặng thủy tinh dưới 300K cho dịp lễ',
     N'goi-y-set-qua-tang-thuy-tinh-duoi-300k-cho-dip-le',
     N'Tổng hợp các set quà thủy tinh giá tốt, phù hợp làm quà tặng dịp lễ.',
     N'Không cần chi quá nhiều vẫn có thể chọn được món quà thủy tinh tinh tế.',
     N'<p>Với ngân sách dưới 300.000₫, bạn vẫn có thể lựa chọn được nhiều set quà thủy tinh đẹp mắt như bộ ly thủy tinh màu, hũ thủy tinh trang trí hoặc cốc thủy tinh có quai kèm hộp quà.</p><p>Nên ưu tiên các sản phẩm có thiết kế tối giản, dễ phối hợp với nhiều phong cách nội thất khác nhau.</p>',
     N'https://picsum.photos/seed/duastore-set-qua-tang/900/600', 1, 2, 'XUAT_BAN', 134, 0, DATEADD(DAY,-3,GETDATE()), DATEADD(DAY,-3,GETDATE()), DATEADD(DAY,-3,GETDATE())),
    (N'Cách sắp xếp tủ trưng bày ly, bình thủy tinh gọn gàng',
     N'cach-sap-xep-tu-trung-bay-ly-binh-thuy-tinh-gon-gang',
     N'Mẹo sắp xếp tủ trưng bày đồ thủy tinh vừa gọn vừa đẹp mắt.',
     N'Nguyên tắc sắp xếp theo chiều cao, màu sắc giúp tủ trưng bày trông chuyên nghiệp hơn.',
     N'<p>Khi sắp xếp tủ trưng bày, nên nhóm các món đồ thủy tinh theo chiều cao từ thấp đến cao hoặc theo tông màu để tạo cảm giác hài hòa, có điểm nhấn.</p><p>Tránh xếp quá sát nhau để hạn chế va chạm gây nứt vỡ, đồng thời nên lót một lớp vải mềm ở đáy kệ để giảm rung lắc.</p>',
     N'https://picsum.photos/seed/duastore-tu-trung-bay/900/600', 1, 3, 'XUAT_BAN', 97, 0, DATEADD(DAY,-1,GETDATE()), DATEADD(DAY,-1,GETDATE()), DATEADD(DAY,-1,GETDATE()));

INSERT INTO Wishlists (userId, productId, ngayThem) VALUES
    (2, 3, GETDATE()),
    (2, 4, GETDATE());

INSERT INTO banners (title, image_url, link_url, active, display_order, description, created_at, updated_at)
VALUES (N'Banner DuaStore', N'/images/Banner 1 DuaStore.jpg', N'/san-pham', 1, 0,
        N'Banner chính trên trang chủ DuaStore', GETDATE(), GETDATE());

INSERT INTO store_info (tenCuaHang, soNha, duong, phuongXa, quanHuyen, tinhThanh, soDienThoai, email, isActive, isDefault, createdAt, updatedAt)
VALUES (N'DuaStore Hải Phòng', N'123', N'Trần Hưng Đạo', N'Máy Tơ', N'Ngô Quyền', N'Hải Phòng',
        '0936764369', 'contact@duastore.vn', 1, 1, GETDATE(), GETDATE());
GO

-- ============================================================
-- SEED: Du lieu phong phu cho 5 tai khoan USER moi (dia chi,
-- yeu thich, gio hang, danh gia, don hang, voucher) de kiem thu
-- ============================================================
INSERT INTO Addresses (userId, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, isDefault)
SELECT u.id, a.tenNguoiNhan, a.soDienThoai, a.tinhThanh, a.quanHuyen, a.phuongXa, a.diaChiCuThe, 1
FROM users u
CROSS APPLY (VALUES
    ('tranthib', N'Trần Thị Bình',  '0913456789', N'Hà Nội',     N'Cầu Giấy',  N'Dịch Vọng',   N'45 Xuân Thủy'),
    ('lehoangc', N'Lê Hoàng Cường', '0914567890', N'Hồ Chí Minh',N'Quận 1',    N'Bến Nghé',    N'12 Lê Lợi'),
    ('phamthid', N'Phạm Thị Dung',  '0915678901', N'Đà Nẵng',    N'Hải Châu',  N'Thạch Thang', N'88 Bạch Đằng'),
    ('vominhe',  N'Võ Minh Đức',    '0916789012', N'Hải Phòng',  N'Lê Chân',   N'An Biên',     N'201 Tô Hiệu'),
    ('dangthif', N'Đặng Thị Phương','0917890123', N'Cần Thơ',    N'Ninh Kiều', N'Tân An',      N'67 Hòa Bình')
) AS a(username, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe)
WHERE u.username = a.username;
GO

-- Wishlist: moi user them 2 san pham (tron ca hang cu va hang moi)
INSERT INTO Wishlists (userId, productId, ngayThem)
SELECT u.id, p.id, DATEADD(DAY, -w.d, GETDATE())
FROM (VALUES
    ('tranthib', N'Bình Hoa Pha Lê', 2),
    ('tranthib', N'Chai Thủy Tinh Đựng Rượu Tròn', 5),
    ('lehoangc', N'Bộ Tách Trà Thủy Tinh', 1),
    ('lehoangc', N'Ly Highball Thủy Tinh Cao Cấp', 3),
    ('phamthid', N'Hũ Thủy Tinh Hình Trái Bí', 4),
    ('phamthid', N'Bình Thủy Tinh Decanter Hario', 2),
    ('vominhe',  N'Ly Rượu Vang Pha Lê Bohemia', 6),
    ('vominhe',  N'Cốc Thủy Tinh Có Quai', 1),
    ('dangthif', N'Bình Pha Lê Pasabahce Nhập Khẩu', 3),
    ('dangthif', N'Chai Rượu Vodka Thủy Tinh Dạng Tròn', 7),
    ('nguyenvan', N'Bộ Ly Thủy Tinh Màu 1', 2),
    ('nguyenvan', N'Bộ Bình Hoa Màu', 6)
) AS w(username, tenSanPham, d)
JOIN users u ON u.username = w.username
JOIN Products p ON p.tenSanPham = w.tenSanPham;
GO

-- Gio hang: moi user co 1-2 mon dang de trong gio
INSERT INTO CartItems (userId, productId, variantId, soLuong, giaLucThem, ngayThem)
SELECT u.id, pv.productId, pv.id, c.soLuong, pv.giaGoc, GETDATE()
FROM (VALUES
    ('tranthib', N'/images/products/binh-cam-hoa-trang-tri-nho-300ml.jpg', 2),
    ('lehoangc', N'/images/products/ly-vang-350ml-don.jpg', 1),
    ('phamthid', N'/images/products/hu-soc-100ml.jpg', 3),
    ('vominhe',  N'/images/products/bo-ly-mau-2-350ml.jpg', 1),
    ('dangthif', N'/images/products/chai-tron-250ml-nap-go.jpg', 2),
    ('nguyenvan',N'/images/products/binh-khuech-tan-tinh-dau-350ml.jpg', 1)
) AS c(username, hinhAnh, soLuong)
JOIN users u ON u.username = c.username
JOIN ProductVariants pv ON pv.hinhAnh = c.hinhAnh;
GO

-- Danh gia san pham (mix da duyet / cho duyet, de kiem thu trang quan ly danh gia)
INSERT INTO Reviews (userId, productId, danhGia, binhLuan, isApproved, ngayTao)
SELECT u.id, p.id, r.danhGia, r.binhLuan, r.isApproved, DATEADD(DAY, -r.d, GETDATE())
FROM (VALUES
    ('tranthib', N'Chai Thủy Tinh Đựng Rượu Tròn', 5, N'Chai đẹp, thủy tinh dày dặn, đóng gói cẩn thận.', 1, 10),
    ('lehoangc', N'Ly Highball Thủy Tinh Cao Cấp', 4, N'Ly dùng tốt nhưng giao hàng hơi chậm.', 1, 9),
    ('phamthid', N'Bình Hoa Pha Lê', 5, N'Bình sang trọng, dùng trang trí phòng khách rất hợp.', 0, 2),
    ('vominhe',  N'Ly Rượu Vang Pha Lê Bohemia', 5, N'Chất lượng pha lê tốt, trong suốt, sáng bóng.', 1, 15),
    ('dangthif', N'Hũ Thủy Tinh Hình Trái Bí', 4, N'Hũ xinh, phù hợp đựng bánh kẹo ngày Tết.', 1, 6),
    ('nguyenvan',N'Chai Thủy Tinh Vuông Cổ Lãng', 3, N'Sản phẩm ổn, giá hơi cao so với mong đợi.', 1, 20),
    ('tranthib', N'Bộ Tách Trà Thủy Tinh', 5, N'Mua làm quà tặng, đối tác rất thích.', 0, 1),
    ('lehoangc', N'Chai Nước Hoa Vuông Thủy Tinh', 4, N'Chai nhỏ gọn, tiện mang theo khi đi công tác.', 1, 4),
    ('phamthid', N'Bình Pha Lê Pasabahce Nhập Khẩu', 5, N'Hàng nhập khẩu chính hãng, đúng như mô tả.', 1, 12),
    ('vominhe',  N'Cốc Thủy Tinh Có Quai', 4, N'Cốc cầm vừa tay, dùng uống cà phê mỗi sáng.', 0, 3)
) AS r(username, tenSanPham, danhGia, binhLuan, isApproved, d)
JOIN users u ON u.username = r.username
JOIN Products p ON p.tenSanPham = r.tenSanPham;
GO

-- Don hang: 1 don moi cho tung user moi + 1 don them cho nguyenvan,
-- da dang trang thai de kiem thu trang quan ly don hang
INSERT INTO orders (maDon, userId, addressId, snapTenNguoiNhan, snapSoDienThoai, snapDiaChi,
    tienHang, phiVanChuyen, tienGiam, tongThanhToan,
    phuongThucTT, phuongThucGiaoHang, trangThaiTT, trangThaiDon, ngayDat)
SELECT o.maDon, u.id, ad.id, ad.tenNguoiNhan, ad.soDienThoai,
       ad.diaChiCuThe + N', ' + ad.phuongXa + N', ' + ad.quanHuyen + N', ' + ad.tinhThanh,
       o.tienHang, o.phiVanChuyen, o.tienGiam, o.tongThanhToan,
       o.phuongThucTT, 'SHIP', o.trangThaiTT, o.trangThaiDon, DATEADD(DAY, -o.d, GETDATE())
FROM (VALUES
    ('DUA-20260002', 'tranthib', 113000, 20000, 0, 133000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_GIAO',      7),
    ('DUA-20260003', 'lehoangc', 280000, 20000, 0, 300000, 'COD',          'CHUA_THANH_TOAN', 'DANG_GIAO',    2),
    ('DUA-20260004', 'phamthid', 204000, 20000, 0, 224000, 'COD',          'CHUA_THANH_TOAN', 'CHO_XAC_NHAN', 0),
    ('DUA-20260005', 'vominhe',  764000,     0, 0, 764000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_HOAN_THANH',14),
    ('DUA-20260006', 'dangthif', 152000, 20000, 0, 172000, 'COD',          'CHUA_THANH_TOAN', 'DA_HUY',        5),
    ('DUA-20260007', 'nguyenvan',934000,     0, 0, 934000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_XAC_NHAN',   1),
    -- Them nhieu don moi cho tung trang thai (3 don/trang thai) de kiem thu day du, khong chi 1 don/trang thai
    ('DUA-20260013', 'vominhe',   30000, 20000, 0,  50000, 'COD',          'CHUA_THANH_TOAN', 'CHO_XAC_NHAN',         0),
    ('DUA-20260014', 'dangthif',  45000, 20000, 0,  65000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_XAC_NHAN',          1),
    ('DUA-20260015', 'nguyenvan', 95000, 20000, 0, 115000, 'COD',          'CHUA_THANH_TOAN', 'DA_XAC_NHAN',          2),
    ('DUA-20260016', 'tranthib', 110000, 20000, 0, 130000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DANG_GIAO',            1),
    ('DUA-20260017', 'lehoangc', 145000, 20000, 0, 165000, 'COD',          'CHUA_THANH_TOAN', 'DANG_GIAO',            1),
    ('DUA-20260018', 'phamthid', 126000, 20000, 0, 146000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_GIAO',              0),
    ('DUA-20260019', 'vominhe',  385000, 20000, 0, 405000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_GIAO',             12),
    ('DUA-20260020', 'dangthif', 210000, 20000, 0, 230000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_HOAN_THANH',       20),
    ('DUA-20260021', 'nguyenvan',175000, 20000, 0, 195000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN',   'DA_HOAN_THANH',        1),
    ('DUA-20260022', 'tranthib',  64000, 20000, 0,  84000, 'COD',          'CHUA_THANH_TOAN', 'DA_HUY',               3),
    ('DUA-20260023', 'lehoangc', 490000, 20000, 0, 510000, 'CHUYEN_KHOAN', 'CHUA_THANH_TOAN', 'DA_HUY',               8)
) AS o(maDon, username, tienHang, phiVanChuyen, tienGiam, tongThanhToan, phuongThucTT, trangThaiTT, trangThaiDon, d)
JOIN users u ON u.username = o.username
JOIN Addresses ad ON ad.userId = u.id;
GO

INSERT INTO order_items (orderId, productId, variantId, tenSanPham, tenBienThe, hinhAnhSP, donGia, soLuong, thanhTien)
SELECT ord.id, pv.productId, pv.id, p.tenSanPham, pv.tenBienThe, pv.hinhAnh, pv.giaGoc, oi.soLuong, pv.giaGoc * oi.soLuong
FROM (VALUES
    ('DUA-20260002', N'/images/products/chai-tron-100ml-nap-go.jpg', 1),
    ('DUA-20260002', N'/images/products/chai-nuoc-hoa-vuong-100ml.jpg', 2),
    ('DUA-20260003', N'/images/products/bo-tach-tra-200ml.jpg', 1),
    ('DUA-20260003', N'/images/products/ly-highball-300ml.jpg', 2),
    ('DUA-20260004', N'/images/products/hu-hinh-trai-bi-500ml.jpg', 3),
    ('DUA-20260005', N'/images/products/ly-vang-200ml-don.jpg', 1),
    ('DUA-20260005', N'/images/products/binh-hoa-pha-le-1000ml.jpg', 1),
    ('DUA-20260006', N'/images/products/chai-vodka-tron-500ml.jpg', 4),
    ('DUA-20260007', N'/images/products/coc-co-quai-250ml.jpg', 2),
    ('DUA-20260007', N'/images/products/pasabahce-500ml.jpg', 1),
    ('DUA-20260013', N'/images/products/chai-tron-50ml-nap-go.jpg', 2),
    ('DUA-20260014', N'/images/products/ly-highball-450ml.jpg', 1),
    ('DUA-20260015', N'/images/products/binh-cam-hoa-trang-tri-nho-300ml.jpg', 1),
    ('DUA-20260016', N'/images/products/hu-nap-cai-kin-hoi-750ml.jpg', 2),
    ('DUA-20260017', N'/images/products/bo-ly-mau-1-350ml.jpg', 1),
    ('DUA-20260018', N'/images/products/chai-vuong-500ml-nap-bac.jpg', 3),
    ('DUA-20260019', N'/images/products/binh-decanter-hario-400ml.jpg', 1),
    ('DUA-20260020', N'/images/products/ly-vang-350ml-don.jpg', 2),
    ('DUA-20260021', N'/images/products/bo-binh-hoa-mau-800ml.jpg', 1),
    ('DUA-20260022', N'/images/products/chai-tron-250ml-nap-nhua.jpg', 2),
    ('DUA-20260023', N'/images/products/binh-hoa-nau-25cm.jpg', 1)
) AS oi(maDon, hinhAnh, soLuong)
JOIN orders ord ON ord.maDon = oi.maDon
JOIN ProductVariants pv ON pv.hinhAnh = oi.hinhAnh
JOIN Products p ON p.id = pv.productId;
GO

-- Voucher da luu vao vi cua mot so user (kiem thu trang "Vi voucher")
INSERT INTO UserVouchers (userId, promotionId, remainingUses, totalSaved, savedAt, status, voucherCode)
SELECT u.id, pr.id, 1, 0, GETDATE(), 'AVAILABLE', pr.maCode
FROM (VALUES
    ('tranthib', 'NEWUSER'),
    ('lehoangc', 'FREESHIP'),
    ('phamthid', 'DECO50K'),
    ('vominhe',  'NEWUSER'),
    ('dangthif', 'FREESHIP')
) AS uv(username, maCode)
JOIN users u ON u.username = uv.username
JOIN Promotions pr ON pr.maCode = uv.maCode;
GO

-- ============================================================
-- SEED BO SUNG: nang cac bang phu len toi thieu 50 dong/bang
-- (Addresses, Wishlists, CartItems, Reviews, UserVouchers) de
-- co du lieu phong phu hon khi kiem thu.
-- ============================================================
-- Addresses: moi khach hang co them ~8 dia chi giao hang khac nhau
INSERT INTO Addresses (userId, tenNguoiNhan, soDienThoai, tinhThanh, quanHuyen, phuongXa, diaChiCuThe, isDefault)
SELECT u.id, u.hoTen, u.soDienThoai, a.tinhThanh, a.quanHuyen, a.phuongXa, a.diaChiCuThe, 0
FROM users u
CROSS JOIN (VALUES
    (N'Hà Nội', N'Đống Đa', N'Láng Hạ', N'15 Láng Hạ'),
    (N'Hồ Chí Minh', N'Bình Thạnh', N'Phường 25', N'88 Điện Biên Phủ'),
    (N'Đà Nẵng', N'Thanh Khê', N'Chính Gián', N'22 Nguyễn Tất Thành'),
    (N'Hải Phòng', N'Hồng Bàng', N'Minh Khai', N'5 Điện Biên Phủ'),
    (N'Cần Thơ', N'Ninh Kiều', N'Cái Khế', N'44 Mậu Thân'),
    (N'Huế', N'Phú Nhuận', N'Vĩnh Ninh', N'11 Lê Lợi'),
    (N'Nha Trang', N'Lộc Thọ', N'Lộc Thọ', N'77 Trần Phú'),
    (N'Vũng Tàu', N'Thắng Nhất', N'Thắng Nhất', N'33 Lê Hồng Phong'),
    (N'Biên Hòa', N'Trảng Dài', N'Trảng Dài', N'9 Võ Thị Sáu')
) AS a(tinhThanh, quanHuyen, phuongXa, diaChiCuThe)
WHERE u.username IN ('nguyenvan', 'tranthib', 'lehoangc', 'phamthid', 'vominhe', 'dangthif');
GO

-- Wishlists: moi khach hang them yeu thich them ~9 san pham (bo qua trung voi du lieu da co)
INSERT INTO Wishlists (userId, productId, ngayThem)
SELECT u.id, p.id, DATEADD(DAY, -((u.id * 7 + p.id * 3) % 90), GETDATE())
FROM users u
CROSS JOIN Products p
WHERE u.username IN ('nguyenvan', 'tranthib', 'lehoangc', 'phamthid', 'vominhe', 'dangthif')
  AND p.id <= 9
  AND NOT EXISTS (SELECT 1 FROM Wishlists w WHERE w.userId = u.id AND w.productId = p.id);
GO

-- CartItems: moi khach hang co them vai bien the dang de trong gio (bo qua trung)
INSERT INTO CartItems (userId, productId, variantId, soLuong, giaLucThem, ngayThem)
SELECT u.id, pv.productId, pv.id, 1 + (pv.id % 3), pv.giaGoc, DATEADD(HOUR, -(pv.id % 48), GETDATE())
FROM users u
CROSS JOIN ProductVariants pv
WHERE u.username IN ('nguyenvan', 'tranthib', 'lehoangc', 'phamthid', 'vominhe', 'dangthif')
  AND pv.id <= 9
  AND NOT EXISTS (SELECT 1 FROM CartItems c WHERE c.userId = u.id AND c.variantId = pv.id);
GO

-- Reviews: moi khach hang danh gia them ~9 san pham, noi dung xoay vong cho da dang
INSERT INTO Reviews (userId, productId, danhGia, binhLuan, isApproved, ngayTao)
SELECT u.id, p.id,
       2 + ((u.id + p.id) % 4),
       r.binhLuan,
       CASE WHEN (u.id + p.id) % 5 = 0 THEN 0 ELSE 1 END,
       DATEADD(DAY, -((u.id * 5 + p.id * 2) % 120), GETDATE())
FROM users u
CROSS JOIN Products p
CROSS APPLY (VALUES (
    CASE (u.id + p.id) % 6
        WHEN 0 THEN N'Sản phẩm đúng như mô tả, đóng gói cẩn thận, giao hàng nhanh.'
        WHEN 1 THEN N'Chất lượng tốt trong tầm giá, sẽ ủng hộ shop lần sau.'
        WHEN 2 THEN N'Màu sắc đẹp, thủy tinh dày dặn chắc chắn.'
        WHEN 3 THEN N'Giao hàng hơi chậm nhưng sản phẩm ổn.'
        WHEN 4 THEN N'Rất ưng ý, dùng làm quà tặng cũng phù hợp.'
        ELSE N'Bình thường, dùng được, không có gì nổi bật.'
    END
)) AS r(binhLuan)
WHERE u.username IN ('nguyenvan', 'tranthib', 'lehoangc', 'phamthid', 'vominhe', 'dangthif')
  AND p.id <= 9
  AND NOT EXISTS (SELECT 1 FROM Reviews rv WHERE rv.userId = u.id AND rv.productId = p.id);
GO

-- UserVouchers: luu them voucher vao vi cho tung khach hang (toi da moi nguoi x moi promotion 1 lan)
INSERT INTO UserVouchers (userId, promotionId, remainingUses, totalSaved, savedAt, status, voucherCode)
SELECT u.id, pr.id, 1, 0, DATEADD(DAY, -((u.id + pr.id) % 30), GETDATE()), 'AVAILABLE', pr.maCode
FROM users u
CROSS JOIN Promotions pr
WHERE u.username IN ('nguyenvan', 'tranthib', 'lehoangc', 'phamthid', 'vominhe', 'dangthif')
  AND NOT EXISTS (SELECT 1 FROM UserVouchers uv WHERE uv.userId = u.id AND uv.promotionId = pr.id);
GO

PRINT 'Seed du lieu co ban hoan tat!';
GO

-- ============================================================
-- SEED: SiteSettings (cau hinh mac dinh - TOAN BO BAT)
-- ============================================================
INSERT INTO SiteSettings (settingGroup, settingKey, settingValue, createdAt) VALUES
-- Store
('store', 'store_address', N'Phố Tôn Thất Thuyết, Phan Bội Châu, Phường Hồng Bàng, Thành phố Hải Phòng, 18000, Việt Nam', GETDATE()),
('store', 'store_phone', '0936764369', GETDATE()),
('store', 'store_email', 'contact@duastore.vn', GETDATE()),
('store', 'store_latitude', '20.8565', GETDATE()),
('store', 'store_longitude', '106.6756', GETDATE()),
('store', 'store_business_hours', N'{"mon":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"tue":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"wed":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"thu":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"fri":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"sat":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"sun":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]}}', GETDATE()),
-- Payment (toggle names = payment_cod, payment_bank, payment_sepay)
('payment', 'payment_cod', '1', GETDATE()),
('payment', 'payment_bank', '1', GETDATE()),
('payment', 'payment_sepay', '1', GETDATE()),
('payment', 'payment_bank_code', 'MBB', GETDATE()),
('payment', 'payment_bank_account', '118830072008', GETDATE()),
('payment', 'payment_bank_holder', 'PHÙNG LÊ ANH', GETDATE()),
('payment', 'payment_bank_name', 'MBBank', GETDATE()),
('payment', 'payment_bank_branch', 'Hai Phong', GETDATE()),
('payment', 'payment_qr_url', '/images/payment-qr.jpg', GETDATE()),
('payment', 'sepay_merchant_id', '', GETDATE()),
('payment', 'sepay_secret_key', '', GETDATE()),
-- Shipping (toggle names = shipping_free, carrier_ghn_enabled, carrier_ghtk_enabled)
('shipping', 'shipping_free', '1', GETDATE()),
('shipping', 'shipping_free_min', '0', GETDATE()),
('shipping', 'carrier_ghn_enabled', '0', GETDATE()),
('shipping', 'carrier_ghtk_enabled', '1', GETDATE()),
('shipping', 'carrier_ghn_base_fee', '15000', GETDATE()),
('shipping', 'carrier_ghn_rate_km', '2000', GETDATE()),
('shipping', 'carrier_ghn_min_fee', '15000', GETDATE()),
('shipping', 'carrier_ghtk_base_fee', '15000', GETDATE()),
('shipping', 'carrier_ghtk_rate_km', '2000', GETDATE()),
('shipping', 'carrier_ghtk_min_fee', '15000', GETDATE()),
('shipping', 'carrier_default_base_fee', '15000', GETDATE()),
('shipping', 'carrier_default_rate_km', '2000', GETDATE()),
('shipping', 'carrier_default_min_fee', '15000', GETDATE()),
('shipping', 'carrier_default_max_fee', '100000', GETDATE()),
('shipping', 'ghn_test_mode', 'true', GETDATE()),
('shipping', 'ghn_default_district_id', '1444', GETDATE()),
('shipping', 'ghn_default_ward_code', '21012', GETDATE()),
-- Appearance: Header (null = default ON)
('appearance', 'header_logo', '1', GETDATE()),
('appearance', 'header_hotline', '1', GETDATE()),
('appearance', 'header_search', '1', GETDATE()),
('appearance', 'header_cart', '1', GETDATE()),
('appearance', 'header_account', '1', GETDATE()),
-- Appearance: Widgets
('appearance', 'widget_messenger', '1', GETDATE()),
('appearance', 'widget_zalo', '1', GETDATE()),
('appearance', 'widget_call', '1', GETDATE()),
('appearance', 'widget_chatbot', '1', GETDATE()),
('appearance', 'widget_backtotop', '1', GETDATE()),
('appearance', 'widget_popup', '1', GETDATE()),
('appearance', 'popup_promo_active', '0', GETDATE()),
('appearance', 'popup_promo_image', '', GETDATE()),
('appearance', 'popup_promo_link', '', GETDATE()),
('appearance', 'popup_promo_mode', 'once', GETDATE()),
('appearance', 'popup_promo_interval', '60', GETDATE()),
('appearance', 'custom_css', '', GETDATE()),
('appearance', 'hp_3_limit', '6', GETDATE()),
('appearance', 'hp_4_limit', '7', GETDATE()),
('appearance', 'hp_5_limit', '8', GETDATE()),
('appearance', 'hp_3_layout', '3', GETDATE()),
('appearance', 'hp_4_layout', '4', GETDATE()),
('appearance', 'hp_5_layout', '4', GETDATE()),
-- Loyalty
('loyalty', 'loyalty_earn_rate', '10000', GETDATE()),
('loyalty', 'loyalty_redeem_rate', '100', GETDATE()),
('loyalty', 'loyalty_expiry_months', '12', GETDATE()),
('loyalty', 'loyalty_expiry_enabled', 'true', GETDATE()),
-- Order
('order', 'order_auto_cancel_hours', '24', GETDATE()),
-- Email
('email', 'email_host', 'smtp.gmail.com', GETDATE()),
('email', 'email_port', '587', GETDATE()),
('email', 'email_encryption', 'tls', GETDATE()),
('email', 'email_username', '', GETDATE()),
('email', 'email_password', '', GETDATE()),
('email', 'email_from', '', GETDATE()),
('email', 'email_from_name', 'DuaStore', GETDATE());
GO

PRINT 'SiteSettings seed hoan tat!';
GO

-- ============================================================
-- BUOC 5: VIEW HO TRO
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
PRINT ' Tong so bang  : 44 (gom 2 bang join: role_permissions, user_roles)';
PRINT ' Views         : vw_DoanhThu, vw_ProductPrice, vw_PostsPublished';
PRINT ' 4 vai tro     : PRODUCT_OWNER, ADMIN, STAFF, USER';
PRINT ' Tai khoan admin: admin / admin@123 (vai tro PRODUCT_OWNER)';
PRINT '====================================================';

-- ============================================================
-- NANG CAP DB CU (chi chay khi DB da tao TU BAN SCRIPT CU):
-- Bang user_auth_providers truoc day dat cot snake_case
-- (user_id, created_at) trong khi @Entity UserAuthProvider dung
-- camelCase (userId, linkedAt) -> Hibernate validate bao loi
-- -> dang nhap Google (OAuth2) bi loi 500. Doan nay chuyen
-- sang camelCase. VO HAI khi chay tren DB moi (guard kiem tra
-- tung cot truoc khi doi).
-- ============================================================
IF OBJECT_ID('user_auth_providers') IS NOT NULL
BEGIN
    -- 1) Cot userId (camelCase), backfill tu user_id
    IF COL_LENGTH('user_auth_providers', 'userId') IS NULL
    BEGIN
        ALTER TABLE user_auth_providers ADD userId int NULL;
        IF COL_LENGTH('user_auth_providers', 'user_id') IS NOT NULL
        BEGIN
            EXEC('UPDATE user_auth_providers SET userId = user_id');
        END
    END
    IF COL_LENGTH('user_auth_providers', 'user_id') IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE user_auth_providers DROP COLUMN user_id');
    END

    -- 2) Cot linkedAt (camelCase), backfill tu created_at
    IF COL_LENGTH('user_auth_providers', 'linkedAt') IS NULL
    BEGIN
        ALTER TABLE user_auth_providers ADD linkedAt datetime2(7) NULL;
        IF COL_LENGTH('user_auth_providers', 'created_at') IS NOT NULL
        BEGIN
            EXEC('UPDATE user_auth_providers SET linkedAt = created_at');
        END
    END
    IF COL_LENGTH('user_auth_providers', 'linkedAt') IS NOT NULL
    BEGIN
        EXEC('UPDATE user_auth_providers SET linkedAt = SYSUTCDATETIME() WHERE linkedAt IS NULL');
    END
    IF COL_LENGTH('user_auth_providers', 'created_at') IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE user_auth_providers DROP COLUMN created_at');
    END

    -- 3) Rang buoc NOT NULL + FK
    ALTER TABLE user_auth_providers ALTER COLUMN userId int NOT NULL;
    ALTER TABLE user_auth_providers ALTER COLUMN linkedAt datetime2(7) NOT NULL;
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_user_auth_providers_userId')
    BEGIN
        ALTER TABLE user_auth_providers ADD CONSTRAINT FK_user_auth_providers_userId
            FOREIGN KEY (userId) REFERENCES users;
    END

    PRINT 'user_auth_providers: da nang cap sang cot camelCase (userId, linkedAt)';
END
ELSE
BEGIN
    PRINT 'user_auth_providers: khong ton tai (bo qua buoc nang cap)';
END
GO

-- ============================================================
-- CAP NHAT: Them cot severity vao CustomerNotes
-- ============================================================
IF EXISTS (SELECT 1 FROM sys.tables WHERE name = 'CustomerNotes')
    AND NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('CustomerNotes') AND name = 'severity')
BEGIN
    ALTER TABLE CustomerNotes ADD severity nvarchar(20) NOT NULL DEFAULT 'INFO';
    PRINT 'CustomerNotes: da them cot severity';
END
GO

-- ============================================================
-- BANG MOI: StockMovements (Lich su xuat nhap kho)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'StockMovements')
BEGIN
    CREATE TABLE StockMovements (
        id int identity not null,
        variantId int not null,
        quantity int not null,
        type nvarchar(20) not null,
        orderId int null,
        userId int not null,
        note nvarchar(500) null,
        stockBefore int not null,
        stockAfter int not null,
        createdAt datetime2(7) not null,
        primary key (id)
    );
    ALTER TABLE StockMovements ADD CONSTRAINT FK_StockMovements_variantId
        FOREIGN KEY (variantId) REFERENCES ProductVariants;
    ALTER TABLE StockMovements ADD CONSTRAINT FK_StockMovements_userId
        FOREIGN KEY (userId) REFERENCES users;
    CREATE INDEX IX_StockMovements_variantId ON StockMovements(variantId);
    CREATE INDEX IX_StockMovements_createdAt ON StockMovements(createdAt);
    PRINT 'StockMovements: da tao bang';
END
GO

-- ============================================================
-- BANG MOI: ReviewReplies (Phan hoi danh gia)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'ReviewReplies')
BEGIN
    CREATE TABLE ReviewReplies (
        id int identity not null,
        reviewId int not null,
        content nvarchar(max) not null,
        createdBy int not null,
        createdAt datetime2(7) not null,
        primary key (id)
    );
    ALTER TABLE ReviewReplies ADD CONSTRAINT FK_ReviewReplies_reviewId
        FOREIGN KEY (reviewId) REFERENCES Reviews;
    ALTER TABLE ReviewReplies ADD CONSTRAINT FK_ReviewReplies_createdBy
        FOREIGN KEY (createdBy) REFERENCES users;
    CREATE INDEX IX_ReviewReplies_reviewId ON ReviewReplies(reviewId);
    PRINT 'ReviewReplies: da tao bang';
END
GO

-- ============================================================
-- BANG MOI: ContactReplies (Phan hoi tin nhan lien he)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'ContactReplies')
BEGIN
    CREATE TABLE ContactReplies (
        id int identity not null,
        contactId int not null,
        content nvarchar(max) not null,
        createdBy int not null,
        createdAt datetime2(7) not null,
        primary key (id)
    );
    ALTER TABLE ContactReplies ADD CONSTRAINT FK_ContactReplies_contactId
        FOREIGN KEY (contactId) REFERENCES contact_messages;
    ALTER TABLE ContactReplies ADD CONSTRAINT FK_ContactReplies_createdBy
        FOREIGN KEY (createdBy) REFERENCES users;
    CREATE INDEX IX_ContactReplies_contactId ON ContactReplies(contactId);
    PRINT 'ContactReplies: da tao bang';
END
GO

-- ============================================================
-- BANG MOI: PageViews (Theo doi luot truy cap + conversion funnel)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'PageViews')
BEGIN
    CREATE TABLE PageViews (
        id int identity not null,
        sessionId nvarchar(100) not null,
        eventType nvarchar(50) not null,
        pagePath nvarchar(500),
        productId int,
        userId int,
        metadata nvarchar(max),
        createdAt datetime2(7) not null,
        primary key (id)
    );
    CREATE INDEX IX_PageViews_sessionId ON PageViews(sessionId);
    CREATE INDEX IX_PageViews_eventType ON PageViews(eventType);
    CREATE INDEX IX_PageViews_createdAt ON PageViews(createdAt);
    CREATE INDEX IX_PageViews_productId ON PageViews(productId);
    PRINT 'PageViews: da tao bang';
END
GO

-- ============================================================
-- BUOC 6: SEED DU LIEU BO SUNG (cac bang con thieu du lieu)
-- ============================================================

-- ---------- FlashSales ----------
INSERT INTO FlashSales (isActive, priority, ngayBatDau, ngayKetThuc, tenChuongTrinh, moTa) VALUES
    (1, 10, DATEADD(DAY,-2,GETDATE()), DATEADD(DAY,3,GETDATE()),  N'Flash Sale Cuối Tuần', N'Giảm giá sốc cuối tuần cho các sản phẩm thủy tinh cao cấp'),
    (1, 8,  DATEADD(DAY,-5,GETDATE()), DATEADD(DAY,5,GETDATE()),  N'Sale Giữa Tháng', N'Ưu đãi giữa tháng dành cho khách hàng thân thiết'),
    (0, 5,  DATEADD(DAY,-30,GETDATE()),DATEADD(DAY,-20,GETDATE()),N'Khai Trương Tháng 9', N'Chương trình khai trương chi nhánh mới'),
    (1, 7,  GETDATE(),                 DATEADD(DAY,7,GETDATE()),  N'Sale Đón Trung Thu', N'Ưu đãi mùa Trung Thu cho bộ sưu tập bình hoa và ly'),
    (0, 6,  DATEADD(DAY,20,GETDATE()), DATEADD(DAY,25,GETDATE()), N'Flash Sale Black Friday', N'Sự kiện giảm giá lớn nhất năm'),
    (0, 4,  DATEADD(DAY,60,GETDATE()), DATEADD(DAY,65,GETDATE()), N'Sale Tất Niên', N'Chương trình khuyến mãi cuối năm dành cho mọi khách hàng');
GO

-- ---------- FlashSaleItems (phan bo bien the vao 3 flash sale con hieu luc) ----------
INSERT INTO FlashSaleItems (flashSaleId, variantId, giaGoc, giaSale, soLuongToiDa, soLuongDaBan, isActive)
SELECT fs.id, pv.id, pv.giaGoc, CAST(pv.giaGoc * 0.75 AS numeric(12,0)), 20, (pv.id % 10), 1
FROM FlashSales fs
CROSS JOIN ProductVariants pv
WHERE fs.tenChuongTrinh = N'Flash Sale Cuối Tuần' AND pv.id % 3 = 0
UNION ALL
SELECT fs.id, pv.id, pv.giaGoc, CAST(pv.giaGoc * 0.8 AS numeric(12,0)), 15, (pv.id % 8), 1
FROM FlashSales fs
CROSS JOIN ProductVariants pv
WHERE fs.tenChuongTrinh = N'Sale Giữa Tháng' AND pv.id % 3 = 1
UNION ALL
SELECT fs.id, pv.id, pv.giaGoc, CAST(pv.giaGoc * 0.7 AS numeric(12,0)), 25, (pv.id % 12), 1
FROM FlashSales fs
CROSS JOIN ProductVariants pv
WHERE fs.tenChuongTrinh = N'Sale Đón Trung Thu' AND pv.id % 3 = 2;
GO

-- ---------- linked_accounts (lien ket tai khoan phu) ----------
INSERT INTO linked_accounts (linkedUserId, userId, createdAt)
SELECT (SELECT id FROM users WHERE username='tranthib'), (SELECT id FROM users WHERE username='nguyenvan'), DATEADD(DAY,-40,GETDATE())
UNION ALL
SELECT (SELECT id FROM users WHERE username='nguyenvan'), (SELECT id FROM users WHERE username='tranthib'), DATEADD(DAY,-40,GETDATE())
UNION ALL
SELECT (SELECT id FROM users WHERE username='admin'), (SELECT id FROM users WHERE username='admin2'), DATEADD(DAY,-90,GETDATE())
UNION ALL
SELECT (SELECT id FROM users WHERE username='staff2'), (SELECT id FROM users WHERE username='staff1'), DATEADD(DAY,-15,GETDATE());
GO

-- ---------- Notifications ----------
-- (a) Thong bao ca nhan cho khach hang theo don hang cua ho
INSERT INTO Notifications (isActive, linkId, userId, createdAt, linkType, targetRole, linkUrl, content, linkLabel)
SELECT 1, o.id, o.userId, DATEADD(HOUR, -(o.id*3), GETDATE()), 'ORDER', NULL,
       '/tai-khoan/don-hang/' + CAST(o.id AS nvarchar(10)),
       N'Đơn hàng ' + o.maDon + N' đã chuyển sang trạng thái: ' +
       CASE o.trangThaiDon
           WHEN 'CHO_XAC_NHAN' THEN N'Chờ xác nhận'
           WHEN 'DA_XAC_NHAN' THEN N'Đã xác nhận'
           WHEN 'DANG_GIAO' THEN N'Đang giao'
           WHEN 'DA_GIAO' THEN N'Đã giao'
           WHEN 'DA_HOAN_THANH' THEN N'Hoàn thành'
           WHEN 'DA_HUY' THEN N'Đã hủy'
       END,
       N'Xem đơn hàng'
FROM orders o;
GO

-- (b) Thong bao broadcast cho STAFF (don moi, danh gia moi, san pham sap het hang)
INSERT INTO Notifications (isActive, linkId, userId, createdAt, linkType, targetRole, linkUrl, content, linkLabel) VALUES
    (1, 1, NULL, DATEADD(DAY,-1,GETDATE()), 'ORDER', 'STAFF', '/admin/don-hang', N'Khách hàng đã đặt đơn hàng mới: DUA-20260001', N'Xem đơn hàng'),
    (1, 8, NULL, DATEADD(DAY,-2,GETDATE()), 'ORDER', 'STAFF', '/admin/don-hang', N'Khách hàng vừa đặt đơn hàng mới: DUA-20260013', N'Xem đơn hàng'),
    (1, NULL, NULL, DATEADD(DAY,-3,GETDATE()), NULL, 'STAFF', '/admin/khach-hang', N'Khach hang moi: Tran Thi Binh (tranthib@duastore.vn)', N'Xem khách hàng'),
    (1, 1, NULL, DATEADD(DAY,-4,GETDATE()), 'PRODUCT', 'STAFF', '/admin/san-pham/sua/1', N'⚠️ Sản phẩm "Chai Thủy Tinh Đựng Rượu Tròn" sắp hết hàng (còn 3)', N'Xem sản phẩm'),
    (1, NULL, NULL, DATEADD(DAY,-5,GETDATE()), NULL, 'STAFF', '/admin/don-hang?trangThai=CHO_XAC_NHAN', N'⚠️ Có 2 đơn hàng chờ xác nhận quá 24 giờ!', N'Xem đơn hàng'),
    (1, 20, NULL, DATEADD(DAY,-6,GETDATE()), 'PRODUCT', 'STAFF', '/admin/danh-gia', N'Co danh gia moi cho san pham Ly Highball Thuy Tinh Cao Cap can duyet', N'Xem đánh giá'),
    (1, 3, NULL, DATEADD(DAY,-7,GETDATE()), 'PROMOTION', 'STAFF', '/admin/khuyen-mai', N'Admin admin đã tạo khuyến mãi: Sinh Nhật DuaStore', N'Xem khuyến mãi'),
    (1, 7, NULL, DATEADD(DAY,-8,GETDATE()), 'PRODUCT', 'STAFF', '/admin/san-pham/sua/7', N'Sản phẩm Bình Chiết Rượu Vang vừa hết hàng!', N'Xem sản phẩm');
GO

-- (c) Thong bao ca nhan he thong khac (khuyen mai, gio hang, san pham moi)
INSERT INTO Notifications (isActive, linkId, userId, createdAt, linkType, targetRole, linkUrl, content, linkLabel)
SELECT 1, NULL, u.id, DATEADD(DAY,-(u.id), GETDATE()), 'CART', NULL, '/gio-hang', N'🛒 Bạn còn sản phẩm trong giỏ hàng! Quay lại để hoàn tất đặt hàng.', N'Đến giỏ hàng'
FROM users u WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
UNION ALL
SELECT 1, 7, u.id, DATEADD(DAY,-(u.id+2), GETDATE()), 'PRODUCT', NULL, '/san-pham/7', N'Sản phẩm mới: Bình Chiết Rượu Vang', NULL
FROM users u WHERE u.username IN ('nguyenvan','tranthib','lehoangc');
GO

-- ---------- order_notes (ghi chu noi bo cua admin/staff cho don hang) ----------
-- Luu y: users.id co dinh theo thu tu insert trong script nay (staff1=11, staff2=12, staff3=13, admin=1)
INSERT INTO order_notes (admin_id, order_id, ngayTao, tag, noiDung)
SELECT a.adminId, o.id, DATEADD(HOUR, -(o.id*2), GETDATE()), a.tag, a.noiDung
FROM orders o
CROSS APPLY (VALUES
    (11, N'Kho', N'Đã kiểm tra tồn kho, đủ hàng để xuất đơn.'),
    (12, N'CSKH', N'Đã gọi điện xác nhận địa chỉ giao hàng với khách.'),
    (13, N'Hệ thống', N'Đơn hàng được đồng bộ tự động từ website.'),
    (1,  N'Kế toán', N'Đã đối soát thanh toán, khớp với hóa đơn.')
) AS a(adminId, tag, noiDung)
WHERE o.id % 4 = (CASE a.tag WHEN N'Kho' THEN 1 WHEN N'CSKH' THEN 2 WHEN N'Hệ thống' THEN 3 ELSE 0 END);
GO

-- ---------- ProductImages (thu vien anh cho tung san pham, tu anh cac bien the) ----------
INSERT INTO ProductImages (isActive, productId, sortOrder, createdAt, imageUrl)
SELECT 1, pv.productId, ROW_NUMBER() OVER (PARTITION BY pv.productId ORDER BY pv.id) - 1, GETDATE(), pv.hinhAnh
FROM ProductVariants pv
WHERE pv.hinhAnh IS NOT NULL;
GO

-- ---------- SavedCartItems (luu de mua sau) ----------
INSERT INTO SavedCartItems (giaLuu, productId, soLuong, userId, variantId, ngayLuu)
SELECT ISNULL(pv.giaKhuyenMai, pv.giaGoc), pv.productId, 1, u.id, pv.id, DATEADD(DAY,-(pv.id%10), GETDATE())
FROM users u
CROSS APPLY (SELECT TOP 2 id, productId, giaGoc, giaKhuyenMai FROM ProductVariants WHERE id % 6 = (u.id % 6) ORDER BY id) pv
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif');
GO

-- ---------- ProductViews (lich su xem san pham) ----------
INSERT INTO ProductViews (productId, userId, viewedAt)
SELECT p.id, u.id, DATEADD(HOUR, -((p.id * u.id) % 200), GETDATE())
FROM users u
CROSS JOIN Products p
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
  AND p.id % 4 <> (u.id % 4);
GO

-- ---------- UserActivityLogs (nhat ky hoat dong) ----------
INSERT INTO UserActivityLogs (userId, activityAt, ipAddress, activityType, description)
SELECT u.id, DATEADD(HOUR, -(ROW_NUMBER() OVER (PARTITION BY u.id ORDER BY a.activityType)) * 5, GETDATE()),
       '192.168.1.' + CAST((u.id * 7) % 254 + 1 AS nvarchar(10)), a.activityType, a.description
FROM users u
CROSS JOIN (VALUES
    ('LOGIN', N'Đăng nhập thành công'),
    ('LOGOUT', N'Đăng xuất khỏi hệ thống'),
    ('UPDATE_PROFILE', N'Cập nhật thông tin cá nhân'),
    ('CHANGE_PASSWORD', N'Đổi mật khẩu tài khoản'),
    ('VIEW_ORDER', N'Xem chi tiết đơn hàng')
) AS a(activityType, description);
GO

-- ---------- contact_messages ----------
INSERT INTO contact_messages (is_read, is_spam, created_at, phan_loai, hoTen, email, noiDung) VALUES
    (1, 0, DATEADD(DAY,-20,GETDATE()), 'DON_HANG',  N'Nguyễn Văn An', 'nguyenvan@duastore.vn', N'Đơn hàng của tôi bao giờ giao tới ạ? Đặt đã 3 ngày rồi.'),
    (1, 0, DATEADD(DAY,-19,GETDATE()), 'SAN_PHAM',  N'Trần Thị Bình', 'tranthib@duastore.vn', N'Sản phẩm bình hoa pha lê có màu khác ngoài trong suốt không shop?'),
    (1, 0, DATEADD(DAY,-18,GETDATE()), 'GIAO_HANG', N'Lê Hoàng Cường', 'lehoangc@duastore.vn', N'Shop có giao hàng ngoài giờ hành chính không ạ?'),
    (0, 0, DATEADD(DAY,-17,GETDATE()), 'THANH_TOAN',N'Phạm Thị Dung', 'phamthid@duastore.vn', N'Tôi chuyển khoản rồi nhưng đơn vẫn báo chưa thanh toán.'),
    (1, 0, DATEADD(DAY,-16,GETDATE()), 'KHIEU_NAI', N'Võ Minh Đức', 'vominhe@duastore.vn', N'Ly thủy tinh nhận được bị nứt ở đáy, tôi muốn đổi trả.'),
    (0, 0, DATEADD(DAY,-15,GETDATE()), 'HOP_TAC',   N'Đặng Thị Phương', 'dangthif@duastore.vn', N'Tôi muốn hợp tác làm đại lý phân phối sản phẩm của shop.'),
    (1, 0, DATEADD(DAY,-14,GETDATE()), 'KHAC',      N'Hoàng Văn Nam', 'hoangvannam@gmail.com', N'Cho tôi hỏi cửa hàng có chi nhánh ở Đà Nẵng không?'),
    (0, 1, DATEADD(DAY,-13,GETDATE()), 'RAC',       N'Spam Bot', 'spam123@fakemail.com', N'Click here to win a free prize now!!! www.spam-link.fake'),
    (1, 0, DATEADD(DAY,-12,GETDATE()), 'DON_HANG',  N'Nguyễn Thị Hoa', 'hoanguyen@gmail.com', N'Đơn DUA-20260002 giao thiếu 1 sản phẩm so với đặt hàng.'),
    (1, 0, DATEADD(DAY,-11,GETDATE()), 'SAN_PHAM',  N'Bùi Văn Sơn', 'buivanson@gmail.com', N'Chai thủy tinh 750ml nắp bạc có phải hàng nhập khẩu không ạ?'),
    (0, 0, DATEADD(DAY,-10,GETDATE()), 'GIAO_HANG', N'Đỗ Thị Lan', 'dothilan@gmail.com', N'Phí ship về Cần Thơ là bao nhiêu vậy shop?'),
    (1, 0, DATEADD(DAY,-9,GETDATE()),  'THANH_TOAN',N'Ngô Văn Tài', 'ngovantai@gmail.com', N'Shop có hỗ trợ thanh toán trả góp qua thẻ tín dụng không?'),
    (1, 0, DATEADD(DAY,-8,GETDATE()),  'KHIEU_NAI', N'Vũ Thị Mai', 'vuthimai@gmail.com', N'Nhân viên giao hàng thái độ không tốt, mong shop nhắc nhở.'),
    (0, 0, DATEADD(DAY,-7,GETDATE()),  'HOP_TAC',   N'Công ty TNHH Thủy Tinh Việt', 'contact@thuytinhviet.vn', N'Chúng tôi muốn đặt hàng sỉ số lượng lớn, xin báo giá.'),
    (1, 0, DATEADD(DAY,-6,GETDATE()),  'KHAC',      N'Trịnh Văn Hùng', 'trinhvanhung@gmail.com', N'Cửa hàng có chương trình tích điểm thành viên không ạ?'),
    (0, 1, DATEADD(DAY,-5,GETDATE()),  'RAC',       N'Casino Win', 'winbig@fakecasino.net', N'Bạn đã trúng thưởng 100 triệu đồng, bấm vào đây để nhận!'),
    (1, 0, DATEADD(DAY,-4,GETDATE()),  'DON_HANG',  N'Lý Thị Kim', 'lythikim@gmail.com', N'Tôi muốn hủy đơn DUA-20260014 vì đặt nhầm số lượng.'),
    (1, 0, DATEADD(DAY,-3,GETDATE()),  'SAN_PHAM',  N'Phan Văn Đạt', 'phanvandat@gmail.com', N'Bộ tách trà thủy tinh có kèm khay đựng không shop?'),
    (0, 0, DATEADD(DAY,-2,GETDATE()),  'GIAO_HANG', N'Mai Thị Thu', 'maithithu@gmail.com', N'Có thể đổi địa chỉ giao hàng sau khi đã đặt không ạ?'),
    (1, 0, DATEADD(DAY,-1,GETDATE()),  'KHAC',      N'Đinh Văn Long', 'dinhvanlong@gmail.com', N'Cảm ơn shop, sản phẩm rất đẹp và đóng gói cẩn thận!');
GO

-- ---------- popup_banners ----------
INSERT INTO popup_banners (active, interval_minutes, created_at, updated_at, display_mode, title, image_url, link_url) VALUES
    (1, NULL, DATEADD(DAY,-10,GETDATE()), DATEADD(DAY,-1,GETDATE()), 'EVERY_VISIT', N'Chào mừng đến với DuaStore!', '/images/products/binh-hoa-pha-le-1000ml.jpg', '/khuyen-mai'),
    (1, 60,   DATEADD(DAY,-5,GETDATE()),  DATEADD(DAY,-1,GETDATE()), 'ONCE_PER_SESSION', N'Flash Sale Cuối Tuần - Giảm đến 25%', '/images/products/ly-vang-350ml-bo6.jpg', '/san-pham'),
    (0, NULL, DATEADD(DAY,-30,GETDATE()), DATEADD(DAY,-20,GETDATE()),'EVERY_VISIT', N'Khai Trương Chi Nhánh Mới', '/images/products/bo-binh-hoa-mau-800ml.jpg', '/lien-he');
GO

-- ---------- user_settings ----------
INSERT INTO user_settings (userId, settingKey, settingValue)
SELECT u.id, s.settingKey, s.settingValue
FROM users u
CROSS JOIN (VALUES
    ('theme', 'light'),
    ('email_notifications', 'true'),
    ('sms_notifications', 'false')
) AS s(settingKey, settingValue);
GO

-- ---------- CustomerNotes (ghi chu cham soc khach hang cua admin/staff) ----------
INSERT INTO CustomerNotes (userId, content, createdBy, createdAt)
SELECT u.id, n.content, n.createdBy, DATEADD(DAY,-(u.id), GETDATE())
FROM users u
CROSS APPLY (VALUES
    (N'Khách hàng thân thiết, thường mua vào cuối tuần.', N'admin'),
    (N'Ưu tiên gọi điện xác nhận trước khi giao vì hay đổi địa chỉ.', N'staff1'),
    (N'Đã từng khiếu nại về vận chuyển, cần chăm sóc kỹ.', N'staff2')
) AS n(content, createdBy)
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
  AND (u.id + LEN(n.content)) % 3 = 0;
GO

-- ---------- CustomerTags ----------
INSERT INTO CustomerTags (userId, tag, createdAt)
SELECT u.id, t.tag, DATEADD(DAY,-(u.id*2), GETDATE())
FROM users u
CROSS APPLY (VALUES (N'VIP'), (N'Thân thiết'), (N'Mua nhiều'), (N'Khách mới')) AS t(tag)
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
  AND (u.id + LEN(t.tag)) % 4 < 2;
GO

-- ---------- PriceHistory (lich su thay doi gia) ----------
INSERT INTO PriceHistory (variantId, variantName, productId, productName, giaCu, giaMoi, nguoiThayDoiId, ngayThayDoi, nguon)
SELECT pv.id, pv.tenBienThe, pv.productId, p.tenSanPham,
       CAST(pv.giaGoc * 1.1 AS numeric(18,2)), CAST(pv.giaGoc AS numeric(18,2)),
       (SELECT id FROM users WHERE username='admin'), DATEADD(DAY,-(pv.id), GETDATE()), N'Điều chỉnh giá thủ công'
FROM ProductVariants pv
JOIN Products p ON p.id = pv.productId
WHERE pv.id % 2 = 0;
GO

-- ---------- footer_links ----------
INSERT INTO footer_links (title, url, display_order, is_active, columnIndex, created_at) VALUES
    (N'Giới thiệu',            '/gioi-thieu',      1, 1, 1, GETDATE()),
    (N'Liên hệ',                '/lien-he',         2, 1, 1, GETDATE()),
    (N'Tuyển dụng',             '/tuyen-dung',      3, 1, 1, GETDATE()),
    (N'Chính sách đổi trả',     '/chinh-sach-doi-tra', 1, 1, 2, GETDATE()),
    (N'Chính sách bảo mật',     '/chinh-sach-bao-mat', 2, 1, 2, GETDATE()),
    (N'Chính sách vận chuyển',  '/chinh-sach-van-chuyen', 3, 1, 2, GETDATE()),
    (N'Câu hỏi thường gặp',     '/faq',             1, 1, 3, GETDATE()),
    (N'Hướng dẫn mua hàng',     '/huong-dan-mua-hang', 2, 1, 3, GETDATE()),
    (N'Kênh hỗ trợ khách hàng', '/ho-tro',          3, 1, 3, GETDATE()),
    (N'Theo dõi đơn hàng',      '/tai-khoan/don-hang', 4, 1, 3, GETDATE());
GO

-- ---------- LoyaltyTransactions (diem thuong khach hang) ----------
INSERT INTO LoyaltyTransactions (userId, points, balance, type, referenceId, note, createdAt)
SELECT o.userId, CAST(o.tongThanhToan / 10000 AS int), CAST(o.tongThanhToan / 10000 AS int), 'EARNED', o.id,
       N'Tích điểm từ đơn hàng ' + o.maDon, DATEADD(DAY,-1, o.ngayDat)
FROM orders o
WHERE o.trangThaiDon = 'DA_HOAN_THANH'
UNION ALL
SELECT u.id, -50, 100, 'REDEEMED', NULL, N'Đổi điểm lấy voucher giảm giá', DATEADD(DAY,-3,GETDATE())
FROM users u WHERE u.username IN ('nguyenvan','tranthib')
UNION ALL
SELECT u.id, 20, 120, 'ADJUSTED', NULL, N'Điều chỉnh điểm thưởng do sai lệch hệ thống', DATEADD(DAY,-2,GETDATE())
FROM users u WHERE u.username = 'lehoangc'
UNION ALL
SELECT u.id, -30, 0, 'EXPIRED', NULL, N'Điểm thưởng hết hạn sử dụng', DATEADD(DAY,-60,GETDATE())
FROM users u WHERE u.username = 'phamthid';
GO

-- ---------- LoyaltyBalances (backfill so du hien tai tu dong cuoi cua LoyaltyTransactions,
-- giu dung "so du hien tai" ma he thong da hien thi truoc gio — xem findCurrentBalanceByUserId) ----------
INSERT INTO LoyaltyBalances (userId, balance)
SELECT lt.userId, lt.balance
FROM LoyaltyTransactions lt
WHERE lt.id = (SELECT MAX(lt2.id) FROM LoyaltyTransactions lt2 WHERE lt2.userId = lt.userId);
GO

-- ---------- ReviewImages (thu vien anh danh gia) ----------
INSERT INTO ReviewImages (reviewId, imageUrl, sortOrder)
SELECT r.id, pv.hinhAnh, 0
FROM Reviews r
JOIN Products p ON p.id = r.productId
CROSS APPLY (SELECT TOP 1 hinhAnh FROM ProductVariants WHERE productId = p.id AND hinhAnh IS NOT NULL ORDER BY id) pv
WHERE r.id % 4 = 0;
GO

-- ---------- StockMovements (nhap/xuat kho) ----------
-- Nhap kho ban dau cho tat ca bien the
INSERT INTO StockMovements (variantId, quantity, type, orderId, userId, note, stockBefore, stockAfter, createdAt)
SELECT pv.id, pv.soLuongTon, 'IN', NULL, (SELECT id FROM users WHERE username='staff1'),
       N'Nhập kho ban đầu', 0, pv.soLuongTon, DATEADD(DAY,-90,GETDATE())
FROM ProductVariants pv;
GO
-- Xuat kho theo don hang thuc te (order_items)
INSERT INTO StockMovements (variantId, quantity, type, orderId, userId, note, stockBefore, stockAfter, createdAt)
SELECT oi.variantId, -oi.soLuong, 'OUT', oi.orderId, (SELECT id FROM users WHERE username='staff2'),
       N'Xuất kho cho đơn hàng ' + o.maDon, pv.soLuongTon + oi.soLuong, pv.soLuongTon, o.ngayDat
FROM order_items oi
JOIN orders o ON o.id = oi.orderId
JOIN ProductVariants pv ON pv.id = oi.variantId
WHERE oi.variantId IS NOT NULL;
GO
-- Dieu chinh kho (kiem ke)
INSERT INTO StockMovements (variantId, quantity, type, orderId, userId, note, stockBefore, stockAfter, createdAt)
SELECT pv.id, -1, 'ADJUST', NULL, (SELECT id FROM users WHERE username='admin'),
       N'Điều chỉnh sau kiểm kê kho', pv.soLuongTon + 1, pv.soLuongTon, DATEADD(DAY,-15,GETDATE())
FROM ProductVariants pv
WHERE pv.id % 7 = 0;
GO

-- ---------- ReviewReplies (phan hoi danh gia tu admin/staff) ----------
INSERT INTO ReviewReplies (reviewId, content, createdBy, createdAt)
SELECT r.id, N'Cảm ơn bạn đã tin tưởng và ủng hộ DuaStore! Chúng tôi rất vui khi sản phẩm làm bạn hài lòng.',
       (SELECT id FROM users WHERE username='staff3'), DATEADD(DAY,-(r.id % 20), GETDATE())
FROM Reviews r
WHERE r.isApproved = 1 AND r.danhGia >= 4 AND r.id % 5 = 0
UNION ALL
SELECT r.id, N'Cảm ơn phản hồi của bạn. Shop rất tiếc vì trải nghiệm chưa tốt, vui lòng liên hệ hotline để được hỗ trợ đổi trả nhé!',
       (SELECT id FROM users WHERE username='staff4'), DATEADD(DAY,-(r.id % 15), GETDATE())
FROM Reviews r
WHERE r.isApproved = 1 AND r.danhGia <= 3 AND r.id % 6 = 0;
GO

-- ---------- ContactReplies (phan hoi tin nhan lien he) ----------
INSERT INTO ContactReplies (contactId, content, createdBy, createdAt)
SELECT cm.id, N'Chào ' + cm.hoTen + N', cảm ơn bạn đã liên hệ DuaStore. Chúng tôi đã tiếp nhận và sẽ phản hồi trong thời gian sớm nhất.',
       (SELECT id FROM users WHERE username='staff5'), DATEADD(HOUR,4,cm.created_at)
FROM contact_messages cm
WHERE cm.is_spam = 0 AND cm.is_read = 1;
GO

-- ---------- PageViews (theo doi truy cap / conversion funnel) ----------
IF NOT EXISTS (SELECT 1 FROM PageViews)
BEGIN
    INSERT INTO PageViews (sessionId, eventType, pagePath, productId, userId, metadata, createdAt)
    SELECT 'sess-' + CAST(u.id AS nvarchar(10)) + '-' + CAST(p.id AS nvarchar(10)), 'PAGE_VIEW',
           '/san-pham/' + CAST(p.id AS nvarchar(10)), p.id, u.id, NULL,
           DATEADD(HOUR, -((u.id * p.id) % 300), GETDATE())
    FROM users u
    CROSS JOIN Products p
    WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
      AND p.id % 3 = (u.id % 3)
    UNION ALL
    SELECT 'sess-anon-' + CAST(p.id AS nvarchar(10)) + '-' + s.suffix, 'PAGE_VIEW',
           '/san-pham/' + CAST(p.id AS nvarchar(10)), p.id, NULL, NULL,
           DATEADD(HOUR, -(p.id * 5), GETDATE())
    FROM Products p
    CROSS APPLY (VALUES ('a'), ('b')) AS s(suffix)
    UNION ALL
    SELECT 'sess-checkout-' + CAST(o.id AS nvarchar(10)), 'CHECKOUT_COMPLETE', '/thanh-toan/thanh-cong',
           NULL, o.userId, N'{"orderId":' + CAST(o.id AS nvarchar(10)) + '}', o.ngayDat
    FROM orders o;
END
GO

PRINT '====================================================';
PRINT ' Seed du lieu bo sung (22 bang) hoan tat!';
PRINT '====================================================';
GO

-- ============================================================
-- BUOC 7: SEED BO SUNG - nhat ky he thong (admin_action_logs, order_assignments, order_status_logs)
-- Luu y: users.id co dinh (admin=1, staff1=11, staff2=12, staff3=13, staff4=14, staff5=15)
-- ============================================================

-- ---------- order_assignments (phan cong don hang cho admin/staff) ----------
INSERT INTO order_assignments (adminId, orderId, ngayPhan, trangThai)
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id,
    DATEADD(HOUR, -(o.id*2+1), GETDATE()),
    CASE WHEN o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH','DA_HUY') THEN 'HOAN_THANH' ELSE 'DANG_XU_LY' END
FROM orders o;
GO

-- ---------- order_status_logs (lich su chuyen trang thai don hang) ----------
INSERT INTO order_status_logs (nguoi_thuc_hien_id, order_id, thoi_gian, loai_su_kien, trang_thai_cu, trang_thai_moi, ghiChu)
SELECT o.userId, o.id, DATEADD(HOUR,-(o.id*3), GETDATE()), 'CREATE_ORDER', NULL, 'CHO_XAC_NHAN', N'Khách hàng đặt đơn hàng ' + o.maDon
FROM orders o
UNION ALL
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id, DATEADD(HOUR,-(o.id*2), GETDATE()), 'ASSIGN_ADMIN', NULL, NULL,
    N'Phân đơn cho nhân viên xử lý'
FROM orders o
UNION ALL
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id, DATEADD(HOUR,-(o.id), GETDATE()), 'STATUS_CHANGE', 'CHO_XAC_NHAN', o.trangThaiDon,
    N'Cập nhật trạng thái đơn hàng ' + o.maDon
FROM orders o
WHERE o.trangThaiDon <> 'CHO_XAC_NHAN' AND o.trangThaiDon <> 'DA_HUY'
UNION ALL
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id, DATEADD(HOUR,-(o.id), GETDATE()), 'CANCEL_ORDER', 'CHO_XAC_NHAN', 'DA_HUY',
    N'Đơn hàng bị hủy theo yêu cầu'
FROM orders o
WHERE o.trangThaiDon = 'DA_HUY'
UNION ALL
SELECT o.userId, o.id, DATEADD(HOUR,-(o.id), GETDATE()), 'PAYMENT_CONFIRMED', 'CHUA_THANH_TOAN', 'DA_THANH_TOAN',
       N'Xác nhận thanh toán thành công'
FROM orders o
WHERE o.trangThaiTT = 'DA_THANH_TOAN';
GO

-- ---------- admin_action_logs (nhat ky thao tac cua admin/staff) ----------
INSERT INTO admin_action_logs (adminId, entityId, ngayTao, hanhDong, ipAddress, loaiEntity, giaTriCu, giaTriMoi, moTa)
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id, DATEADD(HOUR,-(o.id), GETDATE()), 'PHAN_DON', '192.168.1.' + CAST((o.id%254)+1 AS nvarchar(10)),
    'ORDER', NULL, N'Đã phân đơn', N'Tự động phân đơn ' + o.maDon + N' cho nhân viên xử lý'
FROM orders o
UNION ALL
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id, DATEADD(HOUR,-(o.id+1), GETDATE()), 'CAP_NHAT_TRANG_THAI', '192.168.1.' + CAST((o.id%254)+1 AS nvarchar(10)),
    'ORDER', 'CHO_XAC_NHAN', o.trangThaiDon, N'Cập nhật trạng thái đơn hàng ' + o.maDon
FROM orders o
WHERE o.trangThaiDon <> 'CHO_XAC_NHAN'
UNION ALL
SELECT 1, u.id, DATEADD(DAY,-(u.id), GETDATE()), 'TAO_USER', '127.0.0.1', 'USER', NULL, u.username,
       N'Tạo tài khoản mới ' + u.hoTen
FROM users u
WHERE u.username IN ('staff1','staff2','staff3','staff4','staff5','admin2','admin3')
UNION ALL
SELECT 1, r.id, DATEADD(DAY,-100,GETDATE()), 'TAO_ROLE', '127.0.0.1', 'ROLE', NULL, r.name,
       N'Khởi tạo vai trò hệ thống ' + r.name
FROM roles r
UNION ALL
SELECT 1, p.id, DATEADD(DAY,-(p.id % 30), GETDATE()), 'SUA_SAN_PHAM', '127.0.0.1', 'PRODUCT', NULL, p.tenSanPham,
       N'Cập nhật thông tin sản phẩm ' + p.tenSanPham
FROM Products p
WHERE p.id % 4 = 0
UNION ALL
SELECT
    CASE WHEN r.userId % 2 = 0 THEN 12 ELSE 13 END,
    r.id, DATEADD(DAY,-(r.id % 20), GETDATE()), 'DUYET_DANH_GIA', '127.0.0.1', 'REVIEW', N'CHO_DUYET', N'DA_DUYET',
    N'Duyệt đánh giá sản phẩm'
FROM Reviews r
WHERE r.isApproved = 1 AND r.id % 6 = 0;
GO

PRINT '====================================================';
PRINT ' Seed nhat ky he thong (3 bang) hoan tat!';
PRINT '====================================================';
GO

-- ============================================================
-- BUOC 8: Phan lai san pham vao cac danh muc con moi (them phong phu)
-- ============================================================
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Bình Hoa Pha Lê')
    WHERE tenSanPham IN (N'Bình Hoa Pha Lê Cắt Cạnh', N'Bình Hoa Pha Lê', N'Bình Pha Lê Pasabahce Nhập Khẩu');
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Bình Decanter Rượu')
    WHERE tenSanPham IN (N'Bình Chiết Rượu Vang', N'Bình Thủy Tinh Decanter Hario');
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Hũ Đựng Thực Phẩm')
    WHERE tenSanPham IN (N'Hũ Thủy Tinh Hình Trái Bí', N'Hũ Thủy Tinh Nắp Cài Kín Hơi', N'Hũ Thủy Tinh Sọc');
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Hũ Ngâm Rượu')
    WHERE tenSanPham = N'Bình Thủy Tinh Ngâm Rượu';
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Ly Màu Nghệ Thuật')
    WHERE tenSanPham IN (N'Bộ Ly Thủy Tinh Màu 1', N'Bộ Ly Thủy Tinh Màu 2');
UPDATE Products SET danhMucId = (SELECT id FROM Categories WHERE tenDanhMuc = N'Ly Highball')
    WHERE tenSanPham = N'Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc';
GO

PRINT '====================================================';
PRINT ' Bo sung 5 danh muc con + phan lai san pham hoan tat!';
PRINT '====================================================';
GO

-- ============================================================
-- BUOC 9: Gan voucher cho mot so don hang DA_GIAO/DA_HOAN_THANH
-- (de bao cao "Hieu qua chuong trinh khuyen mai" trong Phan tich co du lieu)
-- ============================================================
UPDATE orders SET promotionId = (SELECT id FROM Promotions WHERE maCode = 'SINHNHAT'), tienGiam = 16950, tongThanhToan = 116050 WHERE maDon = 'DUA-20260002';
UPDATE orders SET promotionId = (SELECT id FROM Promotions WHERE maCode = 'THANG9'),   tienGiam = 76400, tongThanhToan = 687600 WHERE maDon = 'DUA-20260005';
UPDATE orders SET promotionId = (SELECT id FROM Promotions WHERE maCode = 'NEWUSER'),  tienGiam = 50000, tongThanhToan = 355000 WHERE maDon = 'DUA-20260019';
UPDATE orders SET promotionId = (SELECT id FROM Promotions WHERE maCode = 'SINHNHAT'), tienGiam = 26250, tongThanhToan = 168750 WHERE maDon = 'DUA-20260021';
GO

PRINT '====================================================';
PRINT ' Gan voucher cho don hang hoan tat!';
PRINT '====================================================';
GO

-- ============================================================
-- Backfill Products.hinhAnhChinh tu anh cua bien the mac dinh
-- (INSERT Products o tren khong set cot nay, chi ProductVariants
-- moi co anh — cac trang admin/client hien anh dai dien san pham
-- (VD: /admin/san-pham) doc truc tiep hinhAnhChinh nen can co du lieu).
-- ============================================================
UPDATE p
SET p.hinhAnhChinh = pv.hinhAnh
FROM Products p
CROSS APPLY (
    SELECT TOP 1 hinhAnh FROM ProductVariants
    WHERE productId = p.id AND hinhAnh IS NOT NULL
    ORDER BY isDefault DESC, id ASC
) pv
WHERE p.hinhAnhChinh IS NULL;
GO

PRINT '====================================================';
PRINT ' Da backfill hinhAnhChinh cho tat ca san pham!';
PRINT '====================================================';
GO

