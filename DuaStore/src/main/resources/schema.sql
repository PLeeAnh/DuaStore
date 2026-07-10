
    create table Addresses (
        ghnDistrictId int,
        id int identity not null,
        isDefault bit not null,
        latitude float(53),
        longitude float(53),
        userId int not null,
        soDienThoai varchar(15) not null,
        ghnWardCode varchar(20),
        phuongXa varchar(100) not null,
        quanHuyen varchar(100) not null,
        tenNguoiNhan varchar(100) not null,
        tinhThanh varchar(100) not null,
        diaChiCuThe varchar(200) not null,
        primary key (id)
    );

    create table admin_action_logs (
        adminId int not null,
        entityId int not null,
        id int identity not null,
        ngayTao datetime2(7) not null,
        hanhDong varchar(50) not null,
        ipAddress varchar(50),
        loaiEntity varchar(50) not null,
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
        title varchar(200) not null,
        description varchar(500),
        image_url varchar(500) not null,
        link_url varchar(1000),
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
        imageUrl varchar(500),
        moTa varchar(255),
        tenDanhMuc varchar(255) not null,
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
        linkType varchar(20),
        targetRole varchar(20),
        linkUrl varchar(500),
        content NVARCHAR(MAX) not null,
        linkLabel varchar(255),
        primary key (id)
    );

    create table order_assignments (
        adminId int not null,
        id int identity not null,
        orderId int not null,
        ngayPhan datetime2(7) not null,
        trangThai varchar(20),
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
        loaiGia varchar(20),
        tenBienThe varchar(150),
        tenSanPham varchar(200) not null,
        hinhAnhSP varchar(255),
        primary key (id)
    );

    create table order_notes (
        admin_id int not null,
        id int identity not null,
        order_id int not null,
        ngayTao datetime2(7) not null,
        tag varchar(50),
        noiDung varchar(1000) not null,
        primary key (id)
    );

    create table order_status_logs (
        id int identity not null,
        nguoi_thuc_hien_id int,
        order_id int not null,
        thoi_gian datetime2(7) not null,
        loai_su_kien varchar(50) not null check ((loai_su_kien in ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED'))),
        trang_thai_cu varchar(50),
        trang_thai_moi varchar(50),
        ghiChu varchar(500),
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
        snapSoDienThoai varchar(15) not null,
        maDon varchar(20) not null,
        phuongThucGiaoHang varchar(20) not null,
        phuongThucTT varchar(20) not null,
        trangThaiDon varchar(20) not null,
        trangThaiTT varchar(25) not null,
        maVanDon varchar(50),
        snapTenNguoiNhan varchar(100) not null,
        ghiChu varchar(500),
        snapDiaChi varchar(500) not null,
        primary key (id)
    );

    create table permissions (
        id int identity not null,
        ngayTao datetime2(7) not null,
        action varchar(50) not null,
        module varchar(50) not null,
        moTa varchar(200),
        primary key (id)
    );

    create table PostCategories (
        id int identity not null,
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc varchar(200) not null,
        slug varchar(300),
        moTa varchar(500),
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
        trangThai varchar(15) not null,
        tieuDe varchar(300) not null,
        metaDescription varchar(500),
        slug varchar(500),
        tomTat varchar(500),
        hinhAnh varchar(255),
        noiDung NVARCHAR(MAX),
        primary key (id)
    );

    create table ProductImages (
        id int identity not null,
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl varchar(255) not null,
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
        chatLieu varchar(255),
        hinhAnhChinh varchar(255),
        hinhDang varchar(255),
        kinhLoai varchar(255),
        moTa NVARCHAR(MAX),
        mucDichSuDung varchar(255),
        tenSanPham varchar(255) not null,
        thuongHieu varchar(255),
        trangThaiSanPham varchar(255) not null,
        xuatXu varchar(255),
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
        hinhAnh varchar(255),
        tenBienThe varchar(255) not null,
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
        loaiGiam varchar(15) not null,
        targetType varchar(20),
        voucherType varchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode varchar(50) not null,
        tenChuongTrinh varchar(200) not null,
        targetIds varchar(500),
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
        lydo varchar(2000) not null,
        anhMinhChung varchar(255),
        ghiChuXuLy varchar(255),
        phuongThucHoan varchar(255),
        trangThai varchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh varchar(500),
        binhLuan varchar(1000),
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
        name varchar(50) not null,
        moTa varchar(200),
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
        settingGroup varchar(50),
        settingKey varchar(100) not null,
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
        soDienThoai varchar(20),
        email varchar(100),
        phuongXa varchar(100),
        quanHuyen varchar(100),
        soNha varchar(100),
        tinhThanh varchar(100),
        duong varchar(200),
        tenCuaHang varchar(200) not null,
        primary key (id)
    );

    create table user_auth_providers (
        id int identity not null,
        userId int not null,
        linkedAt datetime2(7) not null,
        provider varchar(20) not null,
        provider_sub varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id int not null,
        user_id int not null,
        primary key (role_id, user_id)
    );

    create table user_settings (
        userId int not null,
        settingKey varchar(50) not null,
        settingValue varchar(500),
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
        soDienThoai varchar(15),
        status varchar(20),
        username varchar(50) not null,
        email varchar(100) not null,
        hoTen varchar(100) not null,
        nickname varchar(100),
        avatar varchar(255),
        password varchar(255) not null,
        resetToken varchar(255),
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
        status varchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
        voucherCode varchar(50),
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
        soDienThoai varchar(15) not null,
        ghnWardCode varchar(20),
        phuongXa varchar(100) not null,
        quanHuyen varchar(100) not null,
        tenNguoiNhan varchar(100) not null,
        tinhThanh varchar(100) not null,
        diaChiCuThe varchar(200) not null,
        primary key (id)
    );

    create table admin_action_logs (
        adminId int not null,
        entityId int not null,
        id int identity not null,
        ngayTao datetime2(7) not null,
        hanhDong varchar(50) not null,
        ipAddress varchar(50),
        loaiEntity varchar(50) not null,
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
        title varchar(200) not null,
        description varchar(500),
        image_url varchar(500) not null,
        link_url varchar(1000),
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
        imageUrl varchar(500),
        moTa varchar(255),
        tenDanhMuc varchar(255) not null,
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
        linkType varchar(20),
        targetRole varchar(20),
        linkUrl varchar(500),
        content NVARCHAR(MAX) not null,
        linkLabel varchar(255),
        primary key (id)
    );

    create table order_assignments (
        adminId int not null,
        id int identity not null,
        orderId int not null,
        ngayPhan datetime2(7) not null,
        trangThai varchar(20),
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
        loaiGia varchar(20),
        tenBienThe varchar(150),
        tenSanPham varchar(200) not null,
        hinhAnhSP varchar(255),
        primary key (id)
    );

    create table order_notes (
        admin_id int not null,
        id int identity not null,
        order_id int not null,
        ngayTao datetime2(7) not null,
        tag varchar(50),
        noiDung varchar(1000) not null,
        primary key (id)
    );

    create table order_status_logs (
        id int identity not null,
        nguoi_thuc_hien_id int,
        order_id int not null,
        thoi_gian datetime2(7) not null,
        loai_su_kien varchar(50) not null check ((loai_su_kien in ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED'))),
        trang_thai_cu varchar(50),
        trang_thai_moi varchar(50),
        ghiChu varchar(500),
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
        snapSoDienThoai varchar(15) not null,
        maDon varchar(20) not null,
        phuongThucGiaoHang varchar(20) not null,
        phuongThucTT varchar(20) not null,
        trangThaiDon varchar(20) not null,
        trangThaiTT varchar(25) not null,
        maVanDon varchar(50),
        snapTenNguoiNhan varchar(100) not null,
        ghiChu varchar(500),
        snapDiaChi varchar(500) not null,
        primary key (id)
    );

    create table permissions (
        id int identity not null,
        ngayTao datetime2(7) not null,
        action varchar(50) not null,
        module varchar(50) not null,
        moTa varchar(200),
        primary key (id)
    );

    create table PostCategories (
        id int identity not null,
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc varchar(200) not null,
        slug varchar(300),
        moTa varchar(500),
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
        trangThai varchar(15) not null,
        tieuDe varchar(300) not null,
        metaDescription varchar(500),
        slug varchar(500),
        tomTat varchar(500),
        hinhAnh varchar(255),
        noiDung NVARCHAR(MAX),
        primary key (id)
    );

    create table ProductImages (
        id int identity not null,
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl varchar(255) not null,
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
        chatLieu varchar(255),
        hinhAnhChinh varchar(255),
        hinhDang varchar(255),
        kinhLoai varchar(255),
        moTa NVARCHAR(MAX),
        mucDichSuDung varchar(255),
        tenSanPham varchar(255) not null,
        thuongHieu varchar(255),
        trangThaiSanPham varchar(255) not null,
        xuatXu varchar(255),
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
        hinhAnh varchar(255),
        tenBienThe varchar(255) not null,
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
        loaiGiam varchar(15) not null,
        targetType varchar(20),
        voucherType varchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode varchar(50) not null,
        tenChuongTrinh varchar(200) not null,
        targetIds varchar(500),
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
        lydo varchar(2000) not null,
        anhMinhChung varchar(255),
        ghiChuXuLy varchar(255),
        phuongThucHoan varchar(255),
        trangThai varchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh varchar(500),
        binhLuan varchar(1000),
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
        name varchar(50) not null,
        moTa varchar(200),
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
        settingGroup varchar(50),
        settingKey varchar(100) not null,
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
        soDienThoai varchar(20),
        email varchar(100),
        phuongXa varchar(100),
        quanHuyen varchar(100),
        soNha varchar(100),
        tinhThanh varchar(100),
        duong varchar(200),
        tenCuaHang varchar(200) not null,
        primary key (id)
    );

    create table user_auth_providers (
        id int identity not null,
        userId int not null,
        linkedAt datetime2(7) not null,
        provider varchar(20) not null,
        provider_sub varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id int not null,
        user_id int not null,
        primary key (role_id, user_id)
    );

    create table user_settings (
        userId int not null,
        settingKey varchar(50) not null,
        settingValue varchar(500),
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
        soDienThoai varchar(15),
        status varchar(20),
        username varchar(50) not null,
        email varchar(100) not null,
        hoTen varchar(100) not null,
        nickname varchar(100),
        avatar varchar(255),
        password varchar(255) not null,
        resetToken varchar(255),
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
        status varchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
        voucherCode varchar(50),
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
        soDienThoai varchar(15) not null,
        ghnWardCode varchar(20),
        phuongXa varchar(100) not null,
        quanHuyen varchar(100) not null,
        tenNguoiNhan varchar(100) not null,
        tinhThanh varchar(100) not null,
        diaChiCuThe varchar(200) not null,
        primary key (id)
    );

    create table admin_action_logs (
        adminId int not null,
        entityId int not null,
        id int identity not null,
        ngayTao datetime2(7) not null,
        hanhDong varchar(50) not null,
        ipAddress varchar(50),
        loaiEntity varchar(50) not null,
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
        title varchar(200) not null,
        description varchar(500),
        image_url varchar(500) not null,
        link_url varchar(1000),
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
        imageUrl varchar(500),
        moTa varchar(255),
        tenDanhMuc varchar(255) not null,
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
        linkType varchar(20),
        targetRole varchar(20),
        linkUrl varchar(500),
        content NVARCHAR(MAX) not null,
        linkLabel varchar(255),
        primary key (id)
    );

    create table order_assignments (
        adminId int not null,
        id int identity not null,
        orderId int not null,
        ngayPhan datetime2(7) not null,
        trangThai varchar(20),
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
        loaiGia varchar(20),
        tenBienThe varchar(150),
        tenSanPham varchar(200) not null,
        hinhAnhSP varchar(255),
        primary key (id)
    );

    create table order_notes (
        admin_id int not null,
        id int identity not null,
        order_id int not null,
        ngayTao datetime2(7) not null,
        tag varchar(50),
        noiDung varchar(1000) not null,
        primary key (id)
    );

    create table order_status_logs (
        id int identity not null,
        nguoi_thuc_hien_id int,
        order_id int not null,
        thoi_gian datetime2(7) not null,
        loai_su_kien varchar(50) not null check ((loai_su_kien in ('CREATE_ORDER','ASSIGN_ADMIN','STATUS_CHANGE','CANCEL_ORDER','PAYMENT_CONFIRMED'))),
        trang_thai_cu varchar(50),
        trang_thai_moi varchar(50),
        ghiChu varchar(500),
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
        snapSoDienThoai varchar(15) not null,
        maDon varchar(20) not null,
        phuongThucGiaoHang varchar(20) not null,
        phuongThucTT varchar(20) not null,
        trangThaiDon varchar(20) not null,
        trangThaiTT varchar(25) not null,
        maVanDon varchar(50),
        snapTenNguoiNhan varchar(100) not null,
        ghiChu varchar(500),
        snapDiaChi varchar(500) not null,
        primary key (id)
    );

    create table permissions (
        id int identity not null,
        ngayTao datetime2(7) not null,
        action varchar(50) not null,
        module varchar(50) not null,
        moTa varchar(200),
        primary key (id)
    );

    create table PostCategories (
        id int identity not null,
        thuTu int,
        ngayTao datetime2(7) not null,
        tenDanhMuc varchar(200) not null,
        slug varchar(300),
        moTa varchar(500),
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
        trangThai varchar(15) not null,
        tieuDe varchar(300) not null,
        metaDescription varchar(500),
        slug varchar(500),
        tomTat varchar(500),
        hinhAnh varchar(255),
        noiDung NVARCHAR(MAX),
        primary key (id)
    );

    create table ProductImages (
        id int identity not null,
        isActive bit not null,
        productId int not null,
        sortOrder int,
        createdAt datetime2(7),
        imageUrl varchar(255) not null,
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
        chatLieu varchar(255),
        hinhAnhChinh varchar(255),
        hinhDang varchar(255),
        kinhLoai varchar(255),
        moTa NVARCHAR(MAX),
        mucDichSuDung varchar(255),
        tenSanPham varchar(255) not null,
        thuongHieu varchar(255),
        trangThaiSanPham varchar(255) not null,
        xuatXu varchar(255),
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
        hinhAnh varchar(255),
        tenBienThe varchar(255) not null,
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
        loaiGiam varchar(15) not null,
        targetType varchar(20),
        voucherType varchar(20) check ((voucherType in ('VOUCHER','FREESHIP','MEMBER','BIRTHDAY'))),
        maCode varchar(50) not null,
        tenChuongTrinh varchar(200) not null,
        targetIds varchar(500),
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
        lydo varchar(2000) not null,
        anhMinhChung varchar(255),
        ghiChuXuLy varchar(255),
        phuongThucHoan varchar(255),
        trangThai varchar(255) not null,
        primary key (id)
    );

    create table Reviews (
        danhGia int not null,
        id int identity not null,
        isApproved bit not null,
        productId int not null,
        userId int not null,
        ngayTao datetime2(7) not null,
        hinhAnh varchar(500),
        binhLuan varchar(1000),
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
        name varchar(50) not null,
        moTa varchar(200),
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
        settingGroup varchar(50),
        settingKey varchar(100) not null,
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
        soDienThoai varchar(20),
        email varchar(100),
        phuongXa varchar(100),
        quanHuyen varchar(100),
        soNha varchar(100),
        tinhThanh varchar(100),
        duong varchar(200),
        tenCuaHang varchar(200) not null,
        primary key (id)
    );

    create table user_auth_providers (
        id int identity not null,
        userId int not null,
        linkedAt datetime2(7) not null,
        provider varchar(20) not null,
        provider_sub varchar(255),
        primary key (id)
    );

    create table user_roles (
        role_id int not null,
        user_id int not null,
        primary key (role_id, user_id)
    );

    create table user_settings (
        userId int not null,
        settingKey varchar(50) not null,
        settingValue varchar(500),
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
        soDienThoai varchar(15),
        status varchar(20),
        username varchar(50) not null,
        email varchar(100) not null,
        hoTen varchar(100) not null,
        nickname varchar(100),
        avatar varchar(255),
        password varchar(255) not null,
        resetToken varchar(255),
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
        status varchar(15) not null check ((status in ('AVAILABLE','USED','EXPIRED'))),
        voucherCode varchar(50),
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
