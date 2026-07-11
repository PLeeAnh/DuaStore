
    create table Addresses (
        ghnDistrictId int,
        id int identity not null,
        isDefault bit not null,
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
        active bit not null,
        display_order int not null,
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
        soLuong int not null,
        userId int not null,
        variantId int not null,
        ngayThem datetime2(7),
        primary key (id)
    );

    create table Categories (
        id int identity not null,
        isActive bit not null,
        parentId int,
        thuTuHienThi int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7),
        imageUrl nvarchar(500),
        moTa nvarchar(255),
        tenDanhMuc nvarchar(255) not null,
        primary key (id)
    );

    create table FlashSales (
        giaTriGiam numeric(5,2) not null,
        id int identity not null,
        isActive bit not null,
        productId int not null,
        soLuongDaBan int not null,
        soLuongToiDa int not null,
        ngayBatDau datetime2(7) not null,
        ngayKetThuc datetime2(7) not null,
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
        isActive bit not null,
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
        phiVanChuyen numeric(10,0) not null,
        promotionId int,
        tienGiam numeric(10,0) not null,
        tienHang numeric(12,0) not null,
        tongThanhToan numeric(12,0) not null,
        userId int not null,
        ngayCapNhat datetime2(7),
        ngayDat datetime2(7) not null,
        snapSoDienThoai nvarchar(15) not null,
        maDon nvarchar(20) not null,
        phuongThucGiaoHang nvarchar(20) not null,
        phuongThucTT nvarchar(20) not null,
        trangThaiDon nvarchar(20) not null,
        trangThaiTT nvarchar(25) not null,
        maVanDon nvarchar(50),
        snapTenNguoiNhan nvarchar(100) not null,
        ghiChu nvarchar(500),
        snapDiaChi nvarchar(500) not null,
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
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc nvarchar(200) not null,
        slug nvarchar(300),
        moTa nvarchar(500),
        primary key (id)
    );

    create table Posts (
        danhMucId int,
        id int identity not null,
        isFeatured bit,
        luotXem int not null,
        tacGiaId int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        ngayXuatBan datetime2(7),
        trangThai nvarchar(15) not null,
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
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl nvarchar(255) not null,
        primary key (id)
    );

    create table Products (
        danhMucId int not null,
        id int identity not null,
        isActive bit not null,
        isFeatured bit not null,
        leadTimeDays int,
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
        trangThaiSanPham nvarchar(255) not null,
        xuatXu nvarchar(255),
        primary key (id)
    );

    create table ProductVariants (
        dungTich int,
        giaGoc numeric(12,0) not null,
        giaKhuyenMai numeric(12,0),
        id int identity not null,
        isActive bit not null,
        isDefault bit not null,
        productId int not null,
        soLuongTon int not null,
        hinhAnh nvarchar(255),
        tenBienThe nvarchar(255) not null,
        primary key (id)
    );

    create table Promotions (
        budget numeric(12,0),
        daDung int not null,
        donHangToiThieu numeric(12,0) not null,
        giaTriGiam numeric(10,2) not null,
        giamToiDa numeric(12,0),
        id int identity not null,
        isActive bit not null,
        maxClaims int,
        maxClaimsPerUser int,
        priority int,
        savedCount int,
        soLanDung int,
        stackable bit,
        usedBudget numeric(12,0),
        denNgay datetime2(7) not null,
        tuNgay datetime2(7) not null,
        loaiGiam nvarchar(15) not null,
        targetType nvarchar(20),
        voucherType nvarchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode nvarchar(50) not null,
        tenChuongTrinh nvarchar(200) not null,
        targetIds nvarchar(500),
        primary key (id)
    );

    create table RefundRequests (
        id int identity not null,
        nguoiXuLyId int,
        orderId int not null,
        soTienHoan numeric(18,2) not null,
        userId int not null,
        ngayXuLy datetime2(7),
        ngayYeuCau datetime2(7) not null,
        lydo nvarchar(2000) not null,
        anhMinhChung nvarchar(255),
        ghiChuXuLy nvarchar(255),
        phuongThucHoan nvarchar(255),
        tenNganHang nvarchar(255),
        soTaiKhoan nvarchar(255),
        chuTaiKhoan nvarchar(255),
        trangThai nvarchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh nvarchar(500),
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
        soLuong int not null,
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
        isActive bit not null,
        isDefault bit not null,
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
        userId int not null,
        linkedAt datetime2(7) not null,
        provider nvarchar(20) not null,
        provider_sub nvarchar(255),
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
        isActive bit not null,
        phoneVisible bit,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        resetTokenExpiry datetime2(7),
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
        status nvarchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
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

    create table Addresses (
        ghnDistrictId int,
        id int identity not null,
        isDefault bit not null,
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
        active bit not null,
        display_order int not null,
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
        soLuong int not null,
        userId int not null,
        variantId int not null,
        ngayThem datetime2(7),
        primary key (id)
    );

    create table Categories (
        id int identity not null,
        isActive bit not null,
        parentId int,
        thuTuHienThi int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7),
        imageUrl nvarchar(500),
        moTa nvarchar(255),
        tenDanhMuc nvarchar(255) not null,
        primary key (id)
    );

    create table FlashSales (
        giaTriGiam numeric(5,2) not null,
        id int identity not null,
        isActive bit not null,
        productId int not null,
        soLuongDaBan int not null,
        soLuongToiDa int not null,
        ngayBatDau datetime2(7) not null,
        ngayKetThuc datetime2(7) not null,
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
        isActive bit not null,
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
        phiVanChuyen numeric(10,0) not null,
        promotionId int,
        tienGiam numeric(10,0) not null,
        tienHang numeric(12,0) not null,
        tongThanhToan numeric(12,0) not null,
        userId int not null,
        ngayCapNhat datetime2(7),
        ngayDat datetime2(7) not null,
        snapSoDienThoai nvarchar(15) not null,
        maDon nvarchar(20) not null,
        phuongThucGiaoHang nvarchar(20) not null,
        phuongThucTT nvarchar(20) not null,
        trangThaiDon nvarchar(20) not null,
        trangThaiTT nvarchar(25) not null,
        maVanDon nvarchar(50),
        snapTenNguoiNhan nvarchar(100) not null,
        ghiChu nvarchar(500),
        snapDiaChi nvarchar(500) not null,
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
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc nvarchar(200) not null,
        slug nvarchar(300),
        moTa nvarchar(500),
        primary key (id)
    );

    create table Posts (
        danhMucId int,
        id int identity not null,
        isFeatured bit,
        luotXem int not null,
        tacGiaId int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        ngayXuatBan datetime2(7),
        trangThai nvarchar(15) not null,
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
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl nvarchar(255) not null,
        primary key (id)
    );

    create table Products (
        danhMucId int not null,
        id int identity not null,
        isActive bit not null,
        isFeatured bit not null,
        leadTimeDays int,
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
        trangThaiSanPham nvarchar(255) not null,
        xuatXu nvarchar(255),
        primary key (id)
    );

    create table ProductVariants (
        dungTich int,
        giaGoc numeric(12,0) not null,
        giaKhuyenMai numeric(12,0),
        id int identity not null,
        isActive bit not null,
        isDefault bit not null,
        productId int not null,
        soLuongTon int not null,
        hinhAnh nvarchar(255),
        tenBienThe nvarchar(255) not null,
        primary key (id)
    );

    create table Promotions (
        budget numeric(12,0),
        daDung int not null,
        donHangToiThieu numeric(12,0) not null,
        giaTriGiam numeric(10,2) not null,
        giamToiDa numeric(12,0),
        id int identity not null,
        isActive bit not null,
        maxClaims int,
        maxClaimsPerUser int,
        priority int,
        savedCount int,
        soLanDung int,
        stackable bit,
        usedBudget numeric(12,0),
        denNgay datetime2(7) not null,
        tuNgay datetime2(7) not null,
        loaiGiam nvarchar(15) not null,
        targetType nvarchar(20),
        voucherType nvarchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode nvarchar(50) not null,
        tenChuongTrinh nvarchar(200) not null,
        targetIds nvarchar(500),
        primary key (id)
    );

    create table RefundRequests (
        id int identity not null,
        nguoiXuLyId int,
        orderId int not null,
        soTienHoan numeric(18,2) not null,
        userId int not null,
        ngayXuLy datetime2(7),
        ngayYeuCau datetime2(7) not null,
        lydo nvarchar(2000) not null,
        anhMinhChung nvarchar(255),
        ghiChuXuLy nvarchar(255),
        phuongThucHoan nvarchar(255),
        tenNganHang nvarchar(255),
        soTaiKhoan nvarchar(255),
        chuTaiKhoan nvarchar(255),
        trangThai nvarchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh nvarchar(500),
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
        soLuong int not null,
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
        isActive bit not null,
        isDefault bit not null,
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
        userId int not null,
        linkedAt datetime2(7) not null,
        provider nvarchar(20) not null,
        provider_sub nvarchar(255),
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
        isActive bit not null,
        phoneVisible bit,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        resetTokenExpiry datetime2(7),
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
        status nvarchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
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

    create table Addresses (
        ghnDistrictId int,
        id int identity not null,
        isDefault bit not null,
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
        active bit not null,
        display_order int not null,
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
        soLuong int not null,
        userId int not null,
        variantId int not null,
        ngayThem datetime2(7),
        primary key (id)
    );

    create table Categories (
        id int identity not null,
        isActive bit not null,
        parentId int,
        thuTuHienThi int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7),
        imageUrl nvarchar(500),
        moTa nvarchar(255),
        tenDanhMuc nvarchar(255) not null,
        primary key (id)
    );

    create table FlashSales (
        giaTriGiam numeric(5,2) not null,
        id int identity not null,
        isActive bit not null,
        productId int not null,
        soLuongDaBan int not null,
        soLuongToiDa int not null,
        ngayBatDau datetime2(7) not null,
        ngayKetThuc datetime2(7) not null,
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
        isActive bit not null,
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
        phiVanChuyen numeric(10,0) not null,
        promotionId int,
        tienGiam numeric(10,0) not null,
        tienHang numeric(12,0) not null,
        tongThanhToan numeric(12,0) not null,
        userId int not null,
        ngayCapNhat datetime2(7),
        ngayDat datetime2(7) not null,
        snapSoDienThoai nvarchar(15) not null,
        maDon nvarchar(20) not null,
        phuongThucGiaoHang nvarchar(20) not null,
        phuongThucTT nvarchar(20) not null,
        trangThaiDon nvarchar(20) not null,
        trangThaiTT nvarchar(25) not null,
        maVanDon nvarchar(50),
        snapTenNguoiNhan nvarchar(100) not null,
        ghiChu nvarchar(500),
        snapDiaChi nvarchar(500) not null,
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
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc nvarchar(200) not null,
        slug nvarchar(300),
        moTa nvarchar(500),
        primary key (id)
    );

    create table Posts (
        danhMucId int,
        id int identity not null,
        isFeatured bit,
        luotXem int not null,
        tacGiaId int,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        ngayXuatBan datetime2(7),
        trangThai nvarchar(15) not null,
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
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl nvarchar(255) not null,
        primary key (id)
    );

    create table Products (
        danhMucId int not null,
        id int identity not null,
        isActive bit not null,
        isFeatured bit not null,
        leadTimeDays int,
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
        trangThaiSanPham nvarchar(255) not null,
        xuatXu nvarchar(255),
        primary key (id)
    );

    create table ProductVariants (
        dungTich int,
        giaGoc numeric(12,0) not null,
        giaKhuyenMai numeric(12,0),
        id int identity not null,
        isActive bit not null,
        isDefault bit not null,
        productId int not null,
        soLuongTon int not null,
        hinhAnh nvarchar(255),
        tenBienThe nvarchar(255) not null,
        primary key (id)
    );

    create table Promotions (
        budget numeric(12,0),
        daDung int not null,
        donHangToiThieu numeric(12,0) not null,
        giaTriGiam numeric(10,2) not null,
        giamToiDa numeric(12,0),
        id int identity not null,
        isActive bit not null,
        maxClaims int,
        maxClaimsPerUser int,
        priority int,
        savedCount int,
        soLanDung int,
        stackable bit,
        usedBudget numeric(12,0),
        denNgay datetime2(7) not null,
        tuNgay datetime2(7) not null,
        loaiGiam nvarchar(15) not null,
        targetType nvarchar(20),
        voucherType nvarchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode nvarchar(50) not null,
        tenChuongTrinh nvarchar(200) not null,
        targetIds nvarchar(500),
        primary key (id)
    );

    create table RefundRequests (
        id int identity not null,
        nguoiXuLyId int,
        orderId int not null,
        soTienHoan numeric(18,2) not null,
        userId int not null,
        ngayXuLy datetime2(7),
        ngayYeuCau datetime2(7) not null,
        lydo nvarchar(2000) not null,
        anhMinhChung nvarchar(255),
        ghiChuXuLy nvarchar(255),
        phuongThucHoan nvarchar(255),
        tenNganHang nvarchar(255),
        soTaiKhoan nvarchar(255),
        chuTaiKhoan nvarchar(255),
        trangThai nvarchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh nvarchar(500),
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
        soLuong int not null,
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
        isActive bit not null,
        isDefault bit not null,
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
        userId int not null,
        linkedAt datetime2(7) not null,
        provider nvarchar(20) not null,
        provider_sub nvarchar(255),
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
        isActive bit not null,
        phoneVisible bit,
        ngayCapNhat datetime2(7),
        ngayTao datetime2(7) not null,
        resetTokenExpiry datetime2(7),
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
        status nvarchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
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

    create table PriceHistory (
        id int identity not null,
        variantId int not null,
        variantName nvarchar(255),
        productId int not null,
        productName nvarchar(255),
        giaCu numeric(18,2),
        giaMoi numeric(18,2),
        nguoiThayDoiId int,
        ngayThayDoi datetime2(7) not null,
        nguon nvarchar(50),
        primary key (id)
    );

    create table footer_links (
        id int identity not null,
        display_order int not null,
        column_index int not null,
        is_active bit not null,
        created_at datetime2(7) not null,
        title nvarchar(200) not null,
        url nvarchar(500) not null,
        primary key (id)
    );
