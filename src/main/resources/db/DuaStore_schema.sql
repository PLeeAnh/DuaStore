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
DROP TABLE IF EXISTS account_lock_requests;
DROP TABLE IF EXISTS LoyaltyBalances;
DROP TABLE IF EXISTS PageViews;
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
        severity nvarchar(20) default 'INFO' not null,
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

    -- Bo sung 23 FK con thieu (phat hien khi ra soat lai toan bo schema) --
    alter table Addresses add constraint FK_Addresses_userId foreign key (userId) references users;
    alter table CartItems add constraint FK_CartItems_userId foreign key (userId) references users;
    alter table Wishlists add constraint FK_Wishlists_userId foreign key (userId) references users;
    alter table Reviews add constraint FK_Reviews_userId foreign key (userId) references users;
    alter table Reviews add constraint FK_Reviews_productId foreign key (productId) references Products;
    alter table SavedCartItems add constraint FK_SavedCartItems_userId foreign key (userId) references users;
    alter table UserVouchers add constraint FK_UserVouchers_userId foreign key (userId) references users;
    alter table Notifications add constraint FK_Notifications_userId foreign key (userId) references users;
    alter table ProductViews add constraint FK_ProductViews_userId foreign key (userId) references users;
    alter table ProductViews add constraint FK_ProductViews_productId foreign key (productId) references Products;
    alter table UserActivityLogs add constraint FK_UserActivityLogs_userId foreign key (userId) references users;
    alter table user_settings add constraint FK_user_settings_userId foreign key (userId) references users;
    alter table linked_accounts add constraint FK_linked_accounts_userId foreign key (userId) references users;
    alter table linked_accounts add constraint FK_linked_accounts_linkedUserId foreign key (linkedUserId) references users;
    alter table Posts add constraint FK_Posts_tacGiaId foreign key (tacGiaId) references users;
    alter table order_items add constraint FK_order_items_productId foreign key (productId) references Products;
    alter table order_items add constraint FK_order_items_variantId foreign key (variantId) references ProductVariants;
    alter table FlashSaleItems add constraint FK_FlashSaleItems_variantId foreign key (variantId) references ProductVariants;
    alter table orders add constraint FK_orders_addressId foreign key (addressId) references Addresses;
    alter table orders add constraint FK_orders_createdBy foreign key (createdBy) references users;
    alter table orders add constraint FK_orders_lastModifiedBy foreign key (lastModifiedBy) references users;
GO
-- PageViews.userId va StockMovements.orderId duoc them FK ngay sau khi 2 bang nay
-- duoc tao ben duoi (BUOC 6), vi chung chua ton tai o thoi diem nay.

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
-- V2: Index for custom variants
CREATE INDEX IX_Variants_Custom ON ProductVariants (isCustom, isActive);
-- V4: Indexes for admin_action_logs
CREATE INDEX IX_admin_action_logs_entity_lookup ON admin_action_logs (loaiEntity, entityId, ngayTao DESC);
CREATE INDEX IX_admin_action_logs_admin_lookup ON admin_action_logs (adminId, ngayTao DESC);
CREATE INDEX IX_admin_action_logs_ngay_tao ON admin_action_logs (ngayTao DESC);
-- Flash sale items
CREATE INDEX IX_FlashSaleItems_FlashSale ON FlashSaleItems (flashSaleId, isActive);
CREATE INDEX IX_FlashSaleItems_Variant ON FlashSaleItems (variantId);
-- User activity logs
CREATE INDEX IX_UserActivityLogs_UserTime ON UserActivityLogs(userId, activityAt DESC);
CREATE INDEX IX_UserActivityLogs_Time ON UserActivityLogs(activityAt DESC);
-- Product views
CREATE INDEX IX_ProductViews_ProductTime ON ProductViews(productId, viewedAt DESC);
GO

-- ============================================================
-- BUOC 4: SEED DU LIEU MAU
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

END
ELSE
BEGIN
END
GO

-- ============================================================
-- CAP NHAT: Them cot severity vao CustomerNotes
-- ============================================================
IF EXISTS (SELECT 1 FROM sys.tables WHERE name = 'CustomerNotes')
    AND NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('CustomerNotes') AND name = 'severity')
BEGIN
    ALTER TABLE CustomerNotes ADD severity nvarchar(20) NOT NULL DEFAULT 'INFO';
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
    ALTER TABLE StockMovements ADD CONSTRAINT FK_StockMovements_orderId
        FOREIGN KEY (orderId) REFERENCES orders;
    CREATE INDEX IX_StockMovements_variantId ON StockMovements(variantId);
    CREATE INDEX IX_StockMovements_createdAt ON StockMovements(createdAt);
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
    CREATE INDEX IX_PageViews_UserTime ON PageViews(userId, createdAt DESC);
    ALTER TABLE PageViews ADD CONSTRAINT FK_PageViews_userId
        FOREIGN KEY (userId) REFERENCES users;
END
GO

-- ============================================================
-- BUOC 6: SEED DU LIEU BO SUNG (cac bang con thieu du lieu)
-- ============================================================

-- ---------- FlashSales ----------
    (1, 10, DATEADD(DAY,-2,GETDATE()), DATEADD(DAY,3,GETDATE()),  N'Flash Sale Cuối Tuần', N'Giảm giá sốc cuối tuần cho các sản phẩm thủy tinh cao cấp'),
    (1, 8,  DATEADD(DAY,-5,GETDATE()), DATEADD(DAY,5,GETDATE()),  N'Sale Giữa Tháng', N'Ưu đãi giữa tháng dành cho khách hàng thân thiết'),
    (0, 5,  DATEADD(DAY,-30,GETDATE()),DATEADD(DAY,-20,GETDATE()),N'Khai Trương Tháng 9', N'Chương trình khai trương chi nhánh mới'),
    (1, 7,  GETDATE(),                 DATEADD(DAY,7,GETDATE()),  N'Sale Đón Trung Thu', N'Ưu đãi mùa Trung Thu cho bộ sưu tập bình hoa và ly'),
    (0, 6,  DATEADD(DAY,20,GETDATE()), DATEADD(DAY,25,GETDATE()), N'Flash Sale Black Friday', N'Sự kiện giảm giá lớn nhất năm'),
    (0, 4,  DATEADD(DAY,60,GETDATE()), DATEADD(DAY,65,GETDATE()), N'Sale Tất Niên', N'Chương trình khuyến mãi cuối năm dành cho mọi khách hàng');
GO

-- ---------- FlashSaleItems (phan bo bien the vao 3 flash sale con hieu luc) ----------
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
SELECT 1, NULL, u.id, DATEADD(DAY,-(u.id), GETDATE()), 'CART', NULL, '/gio-hang', N'🛒 Bạn còn sản phẩm trong giỏ hàng! Quay lại để hoàn tất đặt hàng.', N'Đến giỏ hàng'
FROM users u WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
UNION ALL
SELECT 1, 7, u.id, DATEADD(DAY,-(u.id+2), GETDATE()), 'PRODUCT', NULL, '/san-pham/7', N'Sản phẩm mới: Bình Chiết Rượu Vang', NULL
FROM users u WHERE u.username IN ('nguyenvan','tranthib','lehoangc');
GO

-- ---------- order_notes (ghi chu noi bo cua admin/staff cho don hang) ----------
-- Luu y: users.id co dinh theo thu tu insert trong script nay (staff1=11, staff2=12, staff3=13, admin=1)
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
SELECT 1, pv.productId, ROW_NUMBER() OVER (PARTITION BY pv.productId ORDER BY pv.id) - 1, GETDATE(), pv.hinhAnh
FROM ProductVariants pv
WHERE pv.hinhAnh IS NOT NULL;
GO

-- ---------- SavedCartItems (luu de mua sau) ----------
SELECT ISNULL(pv.giaKhuyenMai, pv.giaGoc), pv.productId, 1, u.id, pv.id, DATEADD(DAY,-(pv.id%10), GETDATE())
FROM users u
CROSS APPLY (SELECT TOP 2 id, productId, giaGoc, giaKhuyenMai FROM ProductVariants WHERE id % 6 = (u.id % 6) ORDER BY id) pv
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif');
GO

-- ---------- ProductViews (lich su xem san pham) ----------
SELECT p.id, u.id, DATEADD(HOUR, -((p.id * u.id) % 200), GETDATE())
FROM users u
CROSS JOIN Products p
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
  AND p.id % 4 <> (u.id % 4);
GO

-- ---------- UserActivityLogs (nhat ky hoat dong) ----------
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
    (1, NULL, DATEADD(DAY,-10,GETDATE()), DATEADD(DAY,-1,GETDATE()), 'EVERY_VISIT', N'Chào mừng đến với DuaStore!', '/images/products/binh-hoa-pha-le-1000ml.jpg', '/khuyen-mai'),
    (1, 60,   DATEADD(DAY,-5,GETDATE()),  DATEADD(DAY,-1,GETDATE()), 'ONCE_PER_SESSION', N'Flash Sale Cuối Tuần - Giảm đến 25%', '/images/products/ly-vang-350ml-bo6.jpg', '/san-pham'),
    (0, NULL, DATEADD(DAY,-30,GETDATE()), DATEADD(DAY,-20,GETDATE()),'EVERY_VISIT', N'Khai Trương Chi Nhánh Mới', '/images/products/bo-binh-hoa-mau-800ml.jpg', '/lien-he');
GO

-- ---------- user_settings ----------
SELECT u.id, s.settingKey, s.settingValue
FROM users u
CROSS JOIN (VALUES
    ('theme', 'light'),
    ('email_notifications', 'true'),
    ('sms_notifications', 'false')
) AS s(settingKey, settingValue);
GO

-- ---------- CustomerNotes (ghi chu cham soc khach hang cua admin/staff) ----------
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
SELECT u.id, t.tag, DATEADD(DAY,-(u.id*2), GETDATE())
FROM users u
CROSS APPLY (VALUES (N'VIP'), (N'Thân thiết'), (N'Mua nhiều'), (N'Khách mới')) AS t(tag)
WHERE u.username IN ('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
  AND (u.id + LEN(t.tag)) % 4 < 2;
GO

-- ---------- PriceHistory (lich su thay doi gia) ----------
SELECT pv.id, pv.tenBienThe, pv.productId, p.tenSanPham,
       CAST(pv.giaGoc * 1.1 AS numeric(18,2)), CAST(pv.giaGoc AS numeric(18,2)),
       (SELECT id FROM users WHERE username='admin'), DATEADD(DAY,-(pv.id), GETDATE()), N'Điều chỉnh giá thủ công'
FROM ProductVariants pv
JOIN Products p ON p.id = pv.productId
WHERE pv.id % 2 = 0;
GO

-- ---------- footer_links ----------
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
SELECT lt.userId, lt.balance
FROM LoyaltyTransactions lt
WHERE lt.id = (SELECT MAX(lt2.id) FROM LoyaltyTransactions lt2 WHERE lt2.userId = lt.userId);
GO

-- ---------- ReviewImages (thu vien anh danh gia) ----------
SELECT r.id, pv.hinhAnh, 0
FROM Reviews r
JOIN Products p ON p.id = r.productId
CROSS APPLY (SELECT TOP 1 hinhAnh FROM ProductVariants WHERE productId = p.id AND hinhAnh IS NOT NULL ORDER BY id) pv
WHERE r.id % 4 = 0;
GO

-- ---------- StockMovements (nhap/xuat kho) ----------
-- Nhap kho ban dau cho tat ca bien the
SELECT pv.id, pv.soLuongTon, 'IN', NULL, (SELECT id FROM users WHERE username='staff1'),
       N'Nhập kho ban đầu', 0, pv.soLuongTon, DATEADD(DAY,-90,GETDATE())
FROM ProductVariants pv;
GO
-- Xuat kho theo don hang thuc te (order_items)
SELECT oi.variantId, -oi.soLuong, 'OUT', oi.orderId, (SELECT id FROM users WHERE username='staff2'),
       N'Xuất kho cho đơn hàng ' + o.maDon, pv.soLuongTon + oi.soLuong, pv.soLuongTon, o.ngayDat
FROM order_items oi
JOIN orders o ON o.id = oi.orderId
JOIN ProductVariants pv ON pv.id = oi.variantId
WHERE oi.variantId IS NOT NULL;
GO
-- Dieu chinh kho (kiem ke)
SELECT pv.id, -1, 'ADJUST', NULL, (SELECT id FROM users WHERE username='admin'),
       N'Điều chỉnh sau kiểm kê kho', pv.soLuongTon + 1, pv.soLuongTon, DATEADD(DAY,-15,GETDATE())
FROM ProductVariants pv
WHERE pv.id % 7 = 0;
GO

-- ---------- ReviewReplies (phan hoi danh gia tu admin/staff) ----------
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
SELECT cm.id, N'Chào ' + cm.hoTen + N', cảm ơn bạn đã liên hệ DuaStore. Chúng tôi đã tiếp nhận và sẽ phản hồi trong thời gian sớm nhất.',
       (SELECT id FROM users WHERE username='staff5'), DATEADD(HOUR,4,cm.created_at)
FROM contact_messages cm
WHERE cm.is_spam = 0 AND cm.is_read = 1;
GO

-- ---------- PageViews (theo doi truy cap / conversion funnel) ----------
IF NOT EXISTS (SELECT 1 FROM PageViews)
BEGIN
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

GO

-- ============================================================
-- BUOC 7: SEED BO SUNG - nhat ky he thong (admin_action_logs, order_assignments, order_status_logs)
-- Luu y: users.id co dinh (admin=1, staff1=11, staff2=12, staff3=13, staff4=14, staff5=15)
-- ============================================================

-- ---------- order_assignments (phan cong don hang cho admin/staff) ----------
SELECT
    CASE (o.id % 5) WHEN 0 THEN 11 WHEN 1 THEN 12 WHEN 2 THEN 13 WHEN 3 THEN 14 ELSE 15 END,
    o.id,
    DATEADD(HOUR, -(o.id*2+1), GETDATE()),
    CASE WHEN o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH','DA_HUY') THEN 'HOAN_THANH' ELSE 'DANG_XU_LY' END
FROM orders o;
GO

-- ---------- order_status_logs (lich su chuyen trang thai don hang) ----------
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

GO

-- ============================================================
-- BUOC 8: Phan lai san pham vao cac danh muc con moi (them phong phu)
-- ============================================================
    WHERE tenSanPham IN (N'Bình Hoa Pha Lê Cắt Cạnh', N'Bình Hoa Pha Lê', N'Bình Pha Lê Pasabahce Nhập Khẩu');
    WHERE tenSanPham IN (N'Bình Chiết Rượu Vang', N'Bình Thủy Tinh Decanter Hario');
    WHERE tenSanPham IN (N'Hũ Thủy Tinh Hình Trái Bí', N'Hũ Thủy Tinh Nắp Cài Kín Hơi', N'Hũ Thủy Tinh Sọc');
    WHERE tenSanPham = N'Bình Thủy Tinh Ngâm Rượu';
    WHERE tenSanPham IN (N'Bộ Ly Thủy Tinh Màu 1', N'Bộ Ly Thủy Tinh Màu 2');
    WHERE tenSanPham = N'Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc';
GO

GO

-- ============================================================
-- BUOC 9: Gan voucher cho mot so don hang DA_GIAO/DA_HOAN_THANH
-- (de bao cao "Hieu qua chuong trinh khuyen mai" trong Phan tich co du lieu)
-- ============================================================
GO

GO

-- ============================================================
-- Backfill Products.hinhAnhChinh tu anh cua bien the mac dinh
-- (INSERT Products o tren khong set cot nay, chi ProductVariants
-- moi co anh — cac trang admin/client hien anh dai dien san pham
-- (VD: /admin/san-pham) doc truc tiep hinhAnhChinh nen can co du lieu).
-- ============================================================
SET p.hinhAnhChinh = pv.hinhAnh
FROM Products p
CROSS APPLY (
    SELECT TOP 1 hinhAnh FROM ProductVariants
    WHERE productId = p.id AND hinhAnh IS NOT NULL
    ORDER BY isDefault DESC, id ASC
) pv
WHERE p.hinhAnhChinh IS NULL;
GO

GO

