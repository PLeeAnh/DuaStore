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
        loai_su_kien nvarchar(50) not null check ((loai_su_kien in ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED','REFUND_ORDER'))),
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
        vnpTransactionNo nvarchar(100),
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
        primary key (id)
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
        primary key (id)
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
    (N'PRODUCT_OWNER', N'Chu so huu san pham - toan quyen he thong', GETDATE()),
    (N'ADMIN',       N'Quan tri vien duoc gan quyen cu the', GETDATE()),
    (N'STAFF',       N'Nhan vien xu ly don hang, san pham, danh gia', GETDATE()),
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
    (N'USER', N'CREATE', N'Them nguoi dung', GETDATE()),
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
    (N'SHIPPING_SETTING', N'UPDATE', N'Sua cau hinh van chuyen', GETDATE()),
    (N'POST_CATEGORY', N'CREATE', N'Them danh muc bai viet', GETDATE()),
    (N'POST_CATEGORY', N'READ',   N'Xem danh muc bai viet', GETDATE()),
    (N'POST_CATEGORY', N'UPDATE', N'Sua danh muc bai viet', GETDATE()),
    (N'POST_CATEGORY', N'DELETE', N'Xoa danh muc bai viet', GETDATE()),
    (N'FLASH_SALE', N'CREATE', N'Them chuong trinh flash sale', GETDATE()),
    (N'FLASH_SALE', N'READ',   N'Xem chuong trinh flash sale', GETDATE()),
    (N'FLASH_SALE', N'UPDATE', N'Sua chuong trinh flash sale', GETDATE()),
    (N'FLASH_SALE', N'DELETE', N'Xoa chuong trinh flash sale', GETDATE()),
    (N'FOOTER_LINK', N'CREATE', N'Them link chan trang', GETDATE()),
    (N'FOOTER_LINK', N'READ',   N'Xem link chan trang', GETDATE()),
    (N'FOOTER_LINK', N'UPDATE', N'Sua link chan trang', GETDATE()),
    (N'FOOTER_LINK', N'DELETE', N'Xoa link chan trang', GETDATE()),
    (N'PRICE_HISTORY', N'READ', N'Xem lich su gia', GETDATE()),
    (N'LOYALTY', N'READ',   N'Xem tich diem khach hang', GETDATE()),
    (N'LOYALTY', N'UPDATE', N'Sua tich diem khach hang', GETDATE()),
    (N'ALERT', N'READ',   N'Xem canh bao', GETDATE()),
    (N'ALERT', N'UPDATE', N'Sua canh bao', GETDATE()),
    (N'CONTACT_MESSAGE', N'READ',   N'Xem tin nhan lien he', GETDATE()),
    (N'CONTACT_MESSAGE', N'UPDATE', N'Sua tin nhan lien he', GETDATE()),
    (N'CONTACT_MESSAGE', N'DELETE', N'Xoa tin nhan lien he', GETDATE());
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
     N'Quan Tri Vien', '0901234567', 1, GETDATE());
INSERT INTO users (username, email, password, hoTen, soDienThoai, isActive, ngayTao) VALUES
    ('nguyenvan', 'nguyen@gmail.com',
     '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO',
     N'Nguyen Van An', '0912345678', 1, GETDATE());
GO

-- Gan vai tro: admin -> PRODUCT_OWNER (toan quyen), nguyenvan -> USER
INSERT INTO user_roles (user_id, role_id) VALUES
    ((SELECT id FROM users WHERE username = 'admin'),     (SELECT id FROM roles WHERE name = N'PRODUCT_OWNER')),
    ((SELECT id FROM users WHERE username = 'nguyenvan'), (SELECT id FROM roles WHERE name = N'USER'));
GO

-- Seed: mac dinh moi user co phuong thuc dang nhap PASSWORD
INSERT INTO user_auth_providers (userId, provider, linkedAt)
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

INSERT INTO Promotions (maCode, tenChuongTrinh, loaiGiam, giaTriGiam, donHangToiThieu, giamToiDa, soLanDung, tuNgay, denNgay, targetType, targetIds) VALUES
    ('KHAIHANG', N'Khai truong DuaStore Hai Phong', 'PHAN_TRAM', 15, 200000, 100000, 200, '2026-01-01', '2026-12-31', NULL, NULL),
    ('FREESHIP',  N'Mien phi van chuyen don tu 500k', 'SO_TIEN',  30000, 500000, NULL, NULL, '2026-01-01', '2026-12-31', NULL, NULL),
    ('DECO50K',   N'Giam 50k don tu 300k',           'SO_TIEN',  50000, 300000, NULL,  100, '2026-01-01', '2026-12-31', NULL, NULL),
    ('SUMMER50',  N'Summer Sale - Giam 50%',           'PHAN_TRAM', 50, 500000, 200000, 500, '2026-06-01', '2026-08-31', 'PRODUCT', '1,2,3,4,5'),
    ('NEWUSER',   N'Uu dai nguoi moi - Giam 20%',      'PHAN_TRAM', 20, 100000, 50000,  1000, '2026-01-01', '2026-12-31', 'ALL', NULL);

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