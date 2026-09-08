# This script assumes $allRoles, $allPerms, $rpList, $allUsers, $uid, $urList, $uapList,
# $cats, $allProds, $vars, $promos are already defined from gen_full.ps1

# --- ADDRESSES ---
$addrs = [System.Collections.ArrayList]@()
# Address 1: nguyenvan default
$addrs.Add(@{user_ref='nguyenvan'; tenNguoiNhan='Nguyễn Văn An'; soDienThoai='0912345678'; tinhThanh='Hải Phòng'; quanHuyen='Ngô Quyền'; phuongXa='Máy Tơ'; diaChiCuThe='123 Trần Hưng Đạo'; isDefault=$true}) | Out-Null
# Addresses 2-6: customer defaults
$addrs.Add(@{user_ref='tranthib'; tenNguoiNhan='Trần Thị Bình'; soDienThoai='0913456789'; tinhThanh='Hà Nội'; quanHuyen='Cầu Giấy'; phuongXa='Dịch Vọng'; diaChiCuThe='45 Xuân Thủy'; isDefault=$true}) | Out-Null
$addrs.Add(@{user_ref='lehoangc'; tenNguoiNhan='Lê Hoàng Cường'; soDienThoai='0914567890'; tinhThanh='Hồ Chí Minh'; quanHuyen='Quận 1'; phuongXa='Bến Nghé'; diaChiCuThe='12 Lê Lợi'; isDefault=$true}) | Out-Null
$addrs.Add(@{user_ref='phamthid'; tenNguoiNhan='Phạm Thị Dung'; soDienThoai='0915678901'; tinhThanh='Đà Nẵng'; quanHuyen='Hải Châu'; phuongXa='Thạch Thang'; diaChiCuThe='88 Bạch Đằng'; isDefault=$true}) | Out-Null
$addrs.Add(@{user_ref='vominhe'; tenNguoiNhan='Võ Minh Đức'; soDienThoai='0916789012'; tinhThanh='Hải Phòng'; quanHuyen='Lê Chân'; phuongXa='An Biên'; diaChiCuThe='201 Tô Hiệu'; isDefault=$true}) | Out-Null
$addrs.Add(@{user_ref='dangthif'; tenNguoiNhan='Đặng Thị Phương'; soDienThoai='0917890123'; tinhThanh='Cần Thơ'; quanHuyen='Ninh Kiều'; phuongXa='Tân An'; diaChiCuThe='67 Hòa Bình'; isDefault=$true}) | Out-Null
# Additional addresses from CROSS JOIN (9 templates × 6 users = 54)
$addrTemplates = @(
  @('Hà Nội','Đống Đa','Láng Hạ','15 Láng Hạ'),
  @('Hồ Chí Minh','Bình Thạnh','Phường 25','88 Điện Biên Phủ'),
  @('Đà Nẵng','Thanh Khê','Chính Gián','22 Nguyễn Tất Thành'),
  @('Hải Phòng','Hồng Bàng','Minh Khai','5 Điện Biên Phủ'),
  @('Cần Thơ','Ninh Kiều','Cái Khế','44 Mậu Thân'),
  @('Huế','Phú Nhuận','Vĩnh Ninh','11 Lê Lợi'),
  @('Nha Trang','Lộc Thọ','Lộc Thọ','77 Trần Phú'),
  @('Vũng Tàu','Thắng Nhất','Thắng Nhất','33 Lê Hồng Phong'),
  @('Biên Hòa','Trảng Dài','Trảng Dài','9 Võ Thị Sáu')
)
$userNames6 = @('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')
$userFullNames = @{'nguyenvan'='Nguyễn Văn An';'tranthib'='Trần Thị Bình';'lehoangc'='Lê Hoàng Cường';'phamthid'='Phạm Thị Dung';'vominhe'='Võ Minh Đức';'dangthif'='Đặng Thị Phương'}
$userPhones = @{'nguyenvan'='0912345678';'tranthib'='0913456789';'lehoangc'='0914567890';'phamthid'='0915678901';'vominhe'='0916789012';'dangthif'='0917890123'}
foreach($un in $userNames6){
  foreach($at in $addrTemplates){
    $addrs.Add(@{user_ref=$un; tenNguoiNhan=$userFullNames[$un]; soDienThoai=$userPhones[$un]; tinhThanh=$at[0]; quanHuyen=$at[1]; phuongXa=$at[2]; diaChiCuThe=$at[3]; isDefault=$false}) | Out-Null
  }
}

# --- ORDERS (23) ---
$ordList = [System.Collections.ArrayList]@()
$ordList.Add(@{orderCode='DUA-20260001'; user_ref='nguyenvan'; addressUser_ref='nguyenvan'; snapTenNguoiNhan='Nguyễn Văn An'; snapSoDienThoai='0912345678'; snapDiaChi='123 Trần Hưng Đạo, Máy Tơ, Ngô Quyền, Hải Phòng'; tienHang=354000; phiVanChuyen=30000; tienGiam=30000; tongThanhToan=354000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='CHO_XAC_NHAN'; promotion_ref='FREESHIP'}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260002'; user_ref='tranthib'; addressUser_ref='tranthib'; snapTenNguoiNhan='Trần Thị Bình'; snapSoDienThoai='0913456789'; snapDiaChi='45 Xuân Thủy, Dịch Vọng, Cầu Giấy, Hà Nội'; tienHang=113000; phiVanChuyen=20000; tienGiam=16950; tongThanhToan=116050; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_GIAO'; promotion_ref='SINHNHAT'}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260003'; user_ref='lehoangc'; addressUser_ref='lehoangc'; snapTenNguoiNhan='Lê Hoàng Cường'; snapSoDienThoai='0914567890'; snapDiaChi='12 Lê Lợi, Bến Nghé, Quận 1, Hồ Chí Minh'; tienHang=280000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=300000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DANG_GIAO'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260004'; user_ref='phamthid'; addressUser_ref='phamthid'; snapTenNguoiNhan='Phạm Thị Dung'; snapSoDienThoai='0915678901'; snapDiaChi='88 Bạch Đằng, Thạch Thang, Hải Châu, Đà Nẵng'; tienHang=204000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=224000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='CHO_XAC_NHAN'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260005'; user_ref='vominhe'; addressUser_ref='vominhe'; snapTenNguoiNhan='Võ Minh Đức'; snapSoDienThoai='0916789012'; snapDiaChi='201 Tô Hiệu, An Biên, Lê Chân, Hải Phòng'; tienHang=764000; phiVanChuyen=0; tienGiam=76400; tongThanhToan=687600; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_HOAN_THANH'; promotion_ref='THANG9'}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260006'; user_ref='dangthif'; addressUser_ref='dangthif'; snapTenNguoiNhan='Đặng Thị Phương'; snapSoDienThoai='0917890123'; snapDiaChi='67 Hòa Bình, Tân An, Ninh Kiều, Cần Thơ'; tienHang=152000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=172000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DA_HUY'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260007'; user_ref='nguyenvan'; addressUser_ref='nguyenvan'; snapTenNguoiNhan='Nguyễn Văn An'; snapSoDienThoai='0912345678'; snapDiaChi='123 Trần Hưng Đạo, Máy Tơ, Ngô Quyền, Hải Phòng'; tienHang=934000; phiVanChuyen=0; tienGiam=0; tongThanhToan=934000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_XAC_NHAN'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260013'; user_ref='vominhe'; addressUser_ref='vominhe'; snapTenNguoiNhan='Võ Minh Đức'; snapSoDienThoai='0916789012'; snapDiaChi='201 Tô Hiệu, An Biên, Lê Chân, Hải Phòng'; tienHang=30000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=50000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='CHO_XAC_NHAN'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260014'; user_ref='dangthif'; addressUser_ref='dangthif'; snapTenNguoiNhan='Đặng Thị Phương'; snapSoDienThoai='0917890123'; snapDiaChi='67 Hòa Bình, Tân An, Ninh Kiều, Cần Thơ'; tienHang=45000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=65000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_XAC_NHAN'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260015'; user_ref='nguyenvan'; addressUser_ref='nguyenvan'; snapTenNguoiNhan='Nguyễn Văn An'; snapSoDienThoai='0912345678'; snapDiaChi='123 Trần Hưng Đạo, Máy Tơ, Ngô Quyền, Hải Phòng'; tienHang=95000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=115000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DA_XAC_NHAN'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260016'; user_ref='tranthib'; addressUser_ref='tranthib'; snapTenNguoiNhan='Trần Thị Bình'; snapSoDienThoai='0913456789'; snapDiaChi='45 Xuân Thủy, Dịch Vọng, Cầu Giấy, Hà Nội'; tienHang=110000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=130000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DANG_GIAO'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260017'; user_ref='lehoangc'; addressUser_ref='lehoangc'; snapTenNguoiNhan='Lê Hoàng Cường'; snapSoDienThoai='0914567890'; snapDiaChi='12 Lê Lợi, Bến Nghé, Quận 1, Hồ Chí Minh'; tienHang=145000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=165000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DANG_GIAO'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260018'; user_ref='phamthid'; addressUser_ref='phamthid'; snapTenNguoiNhan='Phạm Thị Dung'; snapSoDienThoai='0915678901'; snapDiaChi='88 Bạch Đằng, Thạch Thang, Hải Châu, Đà Nẵng'; tienHang=126000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=146000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_GIAO'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260019'; user_ref='vominhe'; addressUser_ref='vominhe'; snapTenNguoiNhan='Võ Minh Đức'; snapSoDienThoai='0916789012'; snapDiaChi='201 Tô Hiệu, An Biên, Lê Chân, Hải Phòng'; tienHang=385000; phiVanChuyen=20000; tienGiam=50000; tongThanhToan=355000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_GIAO'; promotion_ref='NEWUSER'}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260020'; user_ref='dangthif'; addressUser_ref='dangthif'; snapTenNguoiNhan='Đặng Thị Phương'; snapSoDienThoai='0917890123'; snapDiaChi='67 Hòa Bình, Tân An, Ninh Kiều, Cần Thơ'; tienHang=210000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=230000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_HOAN_THANH'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260021'; user_ref='nguyenvan'; addressUser_ref='nguyenvan'; snapTenNguoiNhan='Nguyễn Văn An'; snapSoDienThoai='0912345678'; snapDiaChi='123 Trần Hưng Đạo, Máy Tơ, Ngô Quyền, Hải Phòng'; tienHang=175000; phiVanChuyen=20000; tienGiam=26250; tongThanhToan=168750; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='DA_THANH_TOAN'; trangThaiDon='DA_HOAN_THANH'; promotion_ref='SINHNHAT'}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260022'; user_ref='tranthib'; addressUser_ref='tranthib'; snapTenNguoiNhan='Trần Thị Bình'; snapSoDienThoai='0913456789'; snapDiaChi='45 Xuân Thủy, Dịch Vọng, Cầu Giấy, Hà Nội'; tienHang=64000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=84000; phuongThucTT='COD'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DA_HUY'; promotion_ref=$null}) | Out-Null
$ordList.Add(@{orderCode='DUA-20260023'; user_ref='lehoangc'; addressUser_ref='lehoangc'; snapTenNguoiNhan='Lê Hoàng Cường'; snapSoDienThoai='0914567890'; snapDiaChi='12 Lê Lợi, Bến Nghé, Quận 1, Hồ Chí Minh'; tienHang=490000; phiVanChuyen=20000; tienGiam=0; tongThanhToan=510000; phuongThucTT='CHUYEN_KHOAN'; phuongThucGiaoHang='SHIP'; trangThaiTT='CHUA_THANH_TOAN'; trangThaiDon='DA_HUY'; promotion_ref=$null}) | Out-Null

# --- ORDER_ITEMS (resolved from dynamic SQL) ---
$oiList = [System.Collections.ArrayList]@()
# Order 1 items (from INSERT VALUES)
$oiList.Add(@{order_ref='DUA-20260001'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; variant_ref='100ml - Nắp Gỗ'; tenSanPham='Chai Thủy Tinh Đựng Rượu Tròn'; tenBienThe='100ml - Nắp Gỗ'; donGia=23000; soLuong=2; thanhTien=46000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260001'; product_ref='Ly Rượu Vang Pha Lê Bohemia'; variant_ref='200ml - Đơn chiếc'; tenSanPham='Ly Rượu Vang Pha Lê Bohemia'; tenBienThe='200ml - Đơn chiếc'; donGia=84000; soLuong=3; thanhTien=252000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260001'; product_ref='Ly Highball Thủy Tinh Cao Cấp'; variant_ref='300ml - Đơn chiếc'; tenSanPham='Ly Highball Thủy Tinh Cao Cấp'; tenBienThe='300ml - Đơn chiếc'; donGia=35000; soLuong=1; thanhTien=35000}) | Out-Null
# Order 2 items
$oiList.Add(@{order_ref='DUA-20260002'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; variant_ref='100ml - Nắp Gỗ'; tenSanPham='Chai Thủy Tinh Đựng Rượu Tròn'; tenBienThe='100ml - Nắp Gỗ'; donGia=23000; soLuong=1; thanhTien=23000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260002'; product_ref='Chai Nước Hoa Vuông Thủy Tinh'; variant_ref='Mặc định'; tenSanPham='Chai Nước Hoa Vuông Thủy Tinh'; tenBienThe='Mặc định'; donGia=45000; soLuong=2; thanhTien=90000}) | Out-Null
# Order 3 items
$oiList.Add(@{order_ref='DUA-20260003'; product_ref='Bộ Tách Trà Thủy Tinh'; variant_ref='Mặc định'; tenSanPham='Bộ Tách Trà Thủy Tinh'; tenBienThe='Mặc định'; donGia=210000; soLuong=1; thanhTien=210000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260003'; product_ref='Ly Highball Thủy Tinh Cao Cấp'; variant_ref='300ml - Đơn chiếc'; tenSanPham='Ly Highball Thủy Tinh Cao Cấp'; tenBienThe='300ml - Đơn chiếc'; donGia=35000; soLuong=2; thanhTien=70000}) | Out-Null
# Order 4 items
$oiList.Add(@{order_ref='DUA-20260004'; product_ref='Hũ Thủy Tinh Hình Trái Bí'; variant_ref='Mặc định'; tenSanPham='Hũ Thủy Tinh Hình Trái Bí'; tenBienThe='Mặc định'; donGia=68000; soLuong=3; thanhTien=204000}) | Out-Null
# Order 5 items
$oiList.Add(@{order_ref='DUA-20260005'; product_ref='Ly Rượu Vang Pha Lê Bohemia'; variant_ref='200ml - Đơn chiếc'; tenSanPham='Ly Rượu Vang Pha Lê Bohemia'; tenBienThe='200ml - Đơn chiếc'; donGia=84000; soLuong=1; thanhTien=84000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260005'; product_ref='Bình Hoa Pha Lê'; variant_ref='Mặc định'; tenSanPham='Bình Hoa Pha Lê'; tenBienThe='Mặc định'; donGia=680000; soLuong=1; thanhTien=680000}) | Out-Null
# Order 6 items
$oiList.Add(@{order_ref='DUA-20260006'; product_ref='Chai Rượu Vodka Thủy Tinh Dạng Tròn'; variant_ref='Mặc định'; tenSanPham='Chai Rượu Vodka Thủy Tinh Dạng Tròn'; tenBienThe='Mặc định'; donGia=38000; soLuong=4; thanhTien=152000}) | Out-Null
# Order 7 items
$oiList.Add(@{order_ref='DUA-20260007'; product_ref='Cốc Thủy Tinh Có Quai'; variant_ref='Mặc định'; tenSanPham='Cốc Thủy Tinh Có Quai'; tenBienThe='Mặc định'; donGia=42000; soLuong=2; thanhTien=84000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260007'; product_ref='Bình Pha Lê Pasabahce Nhập Khẩu'; variant_ref='500ml - Pha lê trắng'; tenSanPham='Bình Pha Lê Pasabahce Nhập Khẩu'; tenBienThe='500ml - Pha lê trắng'; donGia=850000; soLuong=1; thanhTien=850000}) | Out-Null
# Order 13-23 items
$oiList.Add(@{order_ref='DUA-20260013'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; variant_ref='50ml - Nắp Gỗ'; tenSanPham='Chai Thủy Tinh Đựng Rượu Tròn'; tenBienThe='50ml - Nắp Gỗ'; donGia=15000; soLuong=2; thanhTien=30000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260014'; product_ref='Ly Highball Thủy Tinh Cao Cấp'; variant_ref='450ml - Đơn chiếc'; tenSanPham='Ly Highball Thủy Tinh Cao Cấp'; tenBienThe='450ml - Đơn chiếc'; donGia=45000; soLuong=1; thanhTien=45000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260015'; product_ref='Bình Cắm Hoa Trang Trí Loại Nhỏ'; variant_ref='Mặc định'; tenSanPham='Bình Cắm Hoa Trang Trí Loại Nhỏ'; tenBienThe='Mặc định'; donGia=95000; soLuong=1; thanhTien=95000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260016'; product_ref='Hũ Thủy Tinh Nắp Cài Kín Hơi'; variant_ref='Mặc định'; tenSanPham='Hũ Thủy Tinh Nắp Cài Kín Hơi'; tenBienThe='Mặc định'; donGia=55000; soLuong=2; thanhTien=110000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260017'; product_ref='Bộ Ly Thủy Tinh Màu 1'; variant_ref='Mặc định'; tenSanPham='Bộ Ly Thủy Tinh Màu 1'; tenBienThe='Mặc định'; donGia=145000; soLuong=1; thanhTien=145000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260018'; product_ref='Chai Thủy Tinh Vuông Cổ Lãng'; variant_ref='500ml - Nắp Bạc'; tenSanPham='Chai Thủy Tinh Vuông Cổ Lãng'; tenBienThe='500ml - Nắp Bạc'; donGia=42000; soLuong=3; thanhTien=126000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260019'; product_ref='Bình Thủy Tinh Decanter Hario'; variant_ref='Mặc định'; tenSanPham='Bình Thủy Tinh Decanter Hario'; tenBienThe='Mặc định'; donGia=385000; soLuong=1; thanhTien=385000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260020'; product_ref='Ly Rượu Vang Pha Lê Bohemia'; variant_ref='350ml - Đơn chiếc'; tenSanPham='Ly Rượu Vang Pha Lê Bohemia'; tenBienThe='350ml - Đơn chiếc'; donGia=105000; soLuong=2; thanhTien=210000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260021'; product_ref='Bộ Bình Hoa Màu'; variant_ref='Mặc định'; tenSanPham='Bộ Bình Hoa Màu'; tenBienThe='Mặc định'; donGia=175000; soLuong=1; thanhTien=175000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260022'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; variant_ref='250ml - Nắp Nhựa'; tenSanPham='Chai Thủy Tinh Đựng Rượu Tròn'; tenBienThe='250ml - Nắp Nhựa'; donGia=32000; soLuong=2; thanhTien=64000}) | Out-Null
$oiList.Add(@{order_ref='DUA-20260023'; product_ref='Bình Hoa Pha Lê Cắt Cạnh'; variant_ref='Nâu khói - Cao 25cm'; tenSanPham='Bình Hoa Pha Lê Cắt Cạnh'; tenBienThe='Nâu khói - Cao 25cm'; donGia=490000; soLuong=1; thanhTien=490000}) | Out-Null

# --- POST CATEGORIES ---
$postCats = @(
  @{name='Hướng Dẫn'; description='Các bài hướng dẫn chọn và bảo quản đồ thủy tinh'; displayOrder=1},
  @{name='Xu Hướng'; description='Xu hướng trang trí và quà tặng'; displayOrder=2},
  @{name='Chăm Sóc & Bảo Quản'; description='Mẹo vệ sinh, bảo quản và kéo dài tuổi thọ đồ thủy tinh - pha lê'; displayOrder=3}
)

# --- POSTS (18) ---
$posts = [System.Collections.ArrayList]@()
$posts.Add(@{title='Hướng dẫn chọn chai thủy tinh theo mục đích sử dụng'; slug='huong-dan-chon-chai-thuy-tinh-theo-muc-dich-su-dung'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$true; luotXem=342}) | Out-Null
$posts.Add(@{title='Ưu điểm của thủy tinh Borosilicate so với thủy tinh thường'; slug='uu-diem-thuy-tinh-borosilicate-so-voi-thuy-tinh-thuong'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=218}) | Out-Null
$posts.Add(@{title='Top 5 mẫu bình trang trí được ưa chuộng nhất năm nay'; slug='top-5-mau-binh-trang-tri-duoc-ua-chuong-nhat-nam-nay'; category_ref='Xu Hướng'; author_ref='admin'; status='NHAP'; isFeatured=$false; luotXem=12}) | Out-Null
$posts.Add(@{title='5 cách vệ sinh ly pha lê không để lại vết ố'; slug='5-cach-ve-sinh-ly-pha-le-khong-de-lai-vet-o'; category_ref='Chăm Sóc & Bảo Quản'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$true; luotXem=501}) | Out-Null
$posts.Add(@{title='Cách bảo quản bình hoa thủy tinh bền đẹp theo thời gian'; slug='cach-bao-quan-binh-hoa-thuy-tinh-ben-dep-theo-thoi-gian'; category_ref='Chăm Sóc & Bảo Quản'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=276}) | Out-Null
$posts.Add(@{title='Xu hướng trang trí bàn tiệc bằng ly và bình thủy tinh màu 2026'; slug='xu-huong-trang-tri-ban-tiec-bang-ly-va-binh-thuy-tinh-mau-2026'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=189}) | Out-Null
$posts.Add(@{title='Vì sao nên chọn ly thủy tinh cường lực cho quán cafe, nhà hàng'; slug='vi-sao-nen-chon-ly-thuy-tinh-cuong-luc-cho-quan-cafe-nha-hang'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=164}) | Out-Null
$posts.Add(@{title='Cách chọn quà tặng bằng thủy tinh, pha lê ý nghĩa cho người thân'; slug='cach-chon-qua-tang-bang-thuy-tinh-pha-le-y-nghia-cho-nguoi-than'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=145}) | Out-Null
$posts.Add(@{title='Phân biệt pha lê thật và thủy tinh giả pha lê'; slug='phan-biet-pha-le-that-va-thuy-tinh-gia-pha-le'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$true; luotXem=298}) | Out-Null
$posts.Add(@{title='Bí quyết cắm hoa đẹp với bình thủy tinh dáng cao'; slug='bi-quyet-cam-hoa-dep-voi-binh-thuy-tinh-dang-cao'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=176}) | Out-Null
$posts.Add(@{title='Có nên rửa đồ thủy tinh bằng máy rửa chén không?'; slug='co-nen-rua-do-thuy-tinh-bang-may-rua-chen-khong'; category_ref='Chăm Sóc & Bảo Quản'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=233}) | Out-Null
$posts.Add(@{title='Xu hướng nội thất tối giản với đồ thủy tinh trong suốt'; slug='xu-huong-noi-that-toi-gian-voi-do-thuy-tinh-trong-suot'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=201}) | Out-Null
$posts.Add(@{title='Hướng dẫn chọn hũ thủy tinh đựng thực phẩm khô an toàn'; slug='huong-dan-chon-hu-thuy-tinh-dung-thuc-pham-kho-an-toan'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=312}) | Out-Null
$posts.Add(@{title='3 mẹo khắc phục ly thủy tinh bị mờ, ố vàng lâu ngày'; slug='3-meo-khac-phuc-ly-thuy-tinh-bi-mo-o-vang-lau-ngay'; category_ref='Chăm Sóc & Bảo Quản'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=421}) | Out-Null
$posts.Add(@{title='Tại sao thủy tinh dập nổi đang là xu hướng decor 2026'; slug='tai-sao-thuy-tinh-dap-noi-dang-la-xu-huong-decor-2026'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=158}) | Out-Null
$posts.Add(@{title='Hướng dẫn bảo quản rượu trong bình thủy tinh ngâm đúng cách'; slug='huong-dan-bao-quan-ruou-trong-binh-thuy-tinh-ngam-dung-cach'; category_ref='Hướng Dẫn'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=267}) | Out-Null
$posts.Add(@{title='Gợi ý set quà tặng thủy tinh dưới 300K cho dịp lễ'; slug='goi-y-set-qua-tang-thuy-tinh-duoi-300k-cho-dip-le'; category_ref='Xu Hướng'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=134}) | Out-Null
$posts.Add(@{title='Cách sắp xếp tủ trưng bày ly, bình thủy tinh gọn gàng'; slug='cach-sap-xep-tu-trung-bay-ly-binh-thuy-tinh-gon-gang'; category_ref='Chăm Sóc & Bảo Quản'; author_ref='admin'; status='XUAT_BAN'; isFeatured=$false; luotXem=97}) | Out-Null

Write-Host "Addrs: $($addrs.Count) Orders: $($ordList.Count) OrderItems: $($oiList.Count) PostCats: $($postCats.Count) Posts: $($posts.Count)"
