# --- BANNERS ---
$bannerList = @(@{title='Banner DuaStore'; imageUrl='/images/Banner 1 DuaStore.jpg'; linkUrl='/san-pham'; active=$true; displayOrder=0; description='Banner chính trên trang chủ DuaStore'})

# --- STORE INFO ---
$storeInfo = @(@{name='DuaStore Hải Phòng'; soNha='123'; duong='Trần Hưng Đạo'; phuongXa='Máy Tơ'; quanHuyen='Ngô Quyền'; tinhThanh='Hải Phòng'; soDienThoai='0936764369'; email='contact@duastore.vn'; isActive=$true; isDefault=$true})

# --- CART ITEMS (6 initial + dynamic) ---
$cartList = [System.Collections.ArrayList]@()
$cartList.Add(@{user_ref='tranthib'; variant_ref='Mặc định'; product_ref='Bình Cắm Hoa Trang Trí Loại Nhỏ'; soLuong=2}) | Out-Null
$cartList.Add(@{user_ref='lehoangc'; variant_ref='350ml - Đơn chiếc'; product_ref='Ly Rượu Vang Pha Lê Bohemia'; soLuong=1}) | Out-Null
$cartList.Add(@{user_ref='phamthid'; variant_ref='Mặc định'; product_ref='Hũ Thủy Tinh Sọc'; soLuong=3}) | Out-Null
$cartList.Add(@{user_ref='vominhe'; variant_ref='Mặc định'; product_ref='Bộ Ly Thủy Tinh Màu 2'; soLuong=1}) | Out-Null
$cartList.Add(@{user_ref='dangthif'; variant_ref='250ml - Nắp Gỗ'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; soLuong=2}) | Out-Null
$cartList.Add(@{user_ref='nguyenvan'; variant_ref='Mặc định'; product_ref='Bình Khuếch Tán Tinh Dầu'; soLuong=1}) | Out-Null

# --- REVIEWS (10 initial) ---
$reviewList = [System.Collections.ArrayList]@()
$reviewList.Add(@{user_ref='tranthib'; product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; danhGia=5; binhLuan='Chai đẹp, thủy tinh dày dặn, đóng gói cẩn thận.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='lehoangc'; product_ref='Ly Highball Thủy Tinh Cao Cấp'; danhGia=4; binhLuan='Ly dùng tốt nhưng giao hàng hơi chậm.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='phamthid'; product_ref='Bình Hoa Pha Lê'; danhGia=5; binhLuan='Bình sang trọng, dùng trang trí phòng khách rất hợp.'; isApproved=$false}) | Out-Null
$reviewList.Add(@{user_ref='vominhe'; product_ref='Ly Rượu Vang Pha Lê Bohemia'; danhGia=5; binhLuan='Chất lượng pha lê tốt, trong suốt, sáng bóng.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='dangthif'; product_ref='Hũ Thủy Tinh Hình Trái Bí'; danhGia=4; binhLuan='Hũ xinh, phù hợp đựng bánh kẹo ngày Tết.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='nguyenvan'; product_ref='Chai Thủy Tinh Vuông Cổ Lãng'; danhGia=3; binhLuan='Sản phẩm ổn, giá hơi cao so với mong đợi.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='tranthib'; product_ref='Bộ Tách Trà Thủy Tinh'; danhGia=5; binhLuan='Mua làm quà tặng, đối tác rất thích.'; isApproved=$false}) | Out-Null
$reviewList.Add(@{user_ref='lehoangc'; product_ref='Chai Nước Hoa Vuông Thủy Tinh'; danhGia=4; binhLuan='Chai nhỏ gọn, tiện mang theo khi đi công tác.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='phamthid'; product_ref='Bình Pha Lê Pasabahce Nhập Khẩu'; danhGia=5; binhLuan='Hàng nhập khẩu chính hãng, đúng như mô tả.'; isApproved=$true}) | Out-Null
$reviewList.Add(@{user_ref='vominhe'; product_ref='Cốc Thủy Tinh Có Quai'; danhGia=4; binhLuan='Cốc cầm vừa tay, dùng uống cà phê mỗi sáng.'; isApproved=$false}) | Out-Null

# --- USER VOUCHERS (5 initial) ---
$uvList = [System.Collections.ArrayList]@()
$uvList.Add(@{user_ref='tranthib'; promotion_ref='NEWUSER'; status='AVAILABLE'}) | Out-Null
$uvList.Add(@{user_ref='lehoangc'; promotion_ref='FREESHIP'; status='AVAILABLE'}) | Out-Null
$uvList.Add(@{user_ref='phamthid'; promotion_ref='DECO50K'; status='AVAILABLE'}) | Out-Null
$uvList.Add(@{user_ref='vominhe'; promotion_ref='NEWUSER'; status='AVAILABLE'}) | Out-Null
$uvList.Add(@{user_ref='dangthif'; promotion_ref='FREESHIP'; status='AVAILABLE'}) | Out-Null

# --- SITE SETTINGS (70+) ---
$siteSettings = @(
  @{group='store'; key='store_address'; value='Phố Tôn Thất Thuyết, Phan Bội Châu, Phường Hồng Bàng, Thành phố Hải Phòng, 18000, Việt Nam'},
  @{group='store'; key='store_phone'; value='0936764369'},
  @{group='store'; key='store_email'; value='contact@duastore.vn'},
  @{group='store'; key='store_latitude'; value='20.8565'},
  @{group='store'; key='store_longitude'; value='106.6756'},
  @{group='store'; key='store_business_hours'; value='{"mon":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"tue":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"wed":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"thu":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"fri":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"sat":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]},"sun":{"open":true,"allDay":false,"slots":[{"open":"08:00","close":"19:00"}]}}'},
  @{group='payment'; key='payment_cod'; value='1'},
  @{group='payment'; key='payment_bank'; value='1'},
  @{group='payment'; key='payment_sepay'; value='1'},
  @{group='payment'; key='payment_bank_code'; value='MB'},
  @{group='payment'; key='payment_bank_account'; value='118830072008'},
  @{group='payment'; key='payment_bank_holder'; value='PHÙNG LÊ ANH'},
  @{group='payment'; key='payment_bank_name'; value='MBBank'},
  @{group='payment'; key='payment_bank_branch'; value='Hai Phong'},
  @{group='payment'; key='payment_qr_url'; value='/images/payment-qr.jpg'},
  @{group='payment'; key='sepay_merchant_id'; value=''},
  @{group='payment'; key='sepay_secret_key'; value=''},
  @{group='shipping'; key='shipping_free'; value='1'},
  @{group='shipping'; key='shipping_free_min'; value='0'},
  @{group='shipping'; key='carrier_ghn_enabled'; value='0'},
  @{group='shipping'; key='carrier_ghtk_enabled'; value='1'},
  @{group='shipping'; key='carrier_ghn_base_fee'; value='15000'},
  @{group='shipping'; key='carrier_ghn_rate_km'; value='2000'},
  @{group='shipping'; key='carrier_ghn_min_fee'; value='15000'},
  @{group='shipping'; key='carrier_ghtk_base_fee'; value='15000'},
  @{group='shipping'; key='carrier_ghtk_rate_km'; value='2000'},
  @{group='shipping'; key='carrier_ghtk_min_fee'; value='15000'},
  @{group='shipping'; key='carrier_default_base_fee'; value='15000'},
  @{group='shipping'; key='carrier_default_rate_km'; value='2000'},
  @{group='shipping'; key='carrier_default_min_fee'; value='15000'},
  @{group='shipping'; key='carrier_default_max_fee'; value='100000'},
  @{group='shipping'; key='ghn_test_mode'; value='true'},
  @{group='shipping'; key='ghn_default_district_id'; value='1444'},
  @{group='shipping'; key='ghn_default_ward_code'; value='21012'},
  @{group='appearance'; key='header_logo'; value='1'},
  @{group='appearance'; key='header_hotline'; value='1'},
  @{group='appearance'; key='header_search'; value='1'},
  @{group='appearance'; key='header_cart'; value='1'},
  @{group='appearance'; key='header_account'; value='1'},
  @{group='appearance'; key='widget_messenger'; value='1'},
  @{group='appearance'; key='widget_zalo'; value='1'},
  @{group='appearance'; key='widget_call'; value='1'},
  @{group='appearance'; key='widget_chatbot'; value='1'},
  @{group='appearance'; key='widget_backtotop'; value='1'},
  @{group='appearance'; key='widget_popup'; value='1'},
  @{group='appearance'; key='popup_promo_active'; value='0'},
  @{group='appearance'; key='popup_promo_image'; value=''},
  @{group='appearance'; key='popup_promo_link'; value=''},
  @{group='appearance'; key='popup_promo_mode'; value='once'},
  @{group='appearance'; key='popup_promo_interval'; value='60'},
  @{group='appearance'; key='custom_css'; value=''},
  @{group='appearance'; key='hp_3_limit'; value='6'},
  @{group='appearance'; key='hp_4_limit'; value='7'},
  @{group='appearance'; key='hp_5_limit'; value='8'},
  @{group='appearance'; key='hp_3_layout'; value='3'},
  @{group='appearance'; key='hp_4_layout'; value='4'},
  @{group='appearance'; key='hp_5_layout'; value='4'},
  @{group='loyalty'; key='loyalty_earn_rate'; value='10000'},
  @{group='loyalty'; key='loyalty_redeem_rate'; value='100'},
  @{group='loyalty'; key='loyalty_expiry_months'; value='12'},
  @{group='loyalty'; key='loyalty_expiry_enabled'; value='true'},
  @{group='order'; key='order_auto_cancel_hours'; value='24'},
  @{group='email'; key='email_host'; value='smtp.gmail.com'},
  @{group='email'; key='email_port'; value='587'},
  @{group='email'; key='email_encryption'; value='tls'},
  @{group='email'; key='email_username'; value=''},
  @{group='email'; key='email_password'; value=''},
  @{group='email'; key='email_from'; value=''},
  @{group='email'; key='email_from_name'; value='DuaStore'}
)

# --- FLASH SALES ---
$flashSales = @(
  @{name='Flash Sale Cuối Tuần'; priority=10; isActive=$true; moTa='Giảm giá sốc cuối tuần cho các sản phẩm thủy tinh cao cấp'},
  @{name='Sale Giữa Tháng'; priority=8; isActive=$true; moTa='Ưu đãi giữa tháng dành cho khách hàng thân thiết'},
  @{name='Khai Trương Tháng 9'; priority=5; isActive=$false; moTa='Chương trình khai trương chi nhánh mới'},
  @{name='Sale Đón Trung Thu'; priority=7; isActive=$true; moTa='Ưu đãi mùa Trung Thu cho bộ sưu tập bình hoa và ly'},
  @{name='Flash Sale Black Friday'; priority=6; isActive=$false; moTa='Sự kiện giảm giá lớn nhất năm'},
  @{name='Sale Tất Niên'; priority=4; isActive=$false; moTa='Chương trình khuyến mãi cuối năm dành cho mọi khách hàng'}
)

# --- LINKED ACCOUNTS ---
$linkedAccts = @(
  @{linkedUser_ref='tranthib'; user_ref='nguyenvan'},
  @{linkedUser_ref='nguyenvan'; user_ref='tranthib'},
  @{linkedUser_ref='admin'; user_ref='admin2'},
  @{linkedUser_ref='staff2'; user_ref='staff1'}
)

# --- NOTIFICATIONS (8 broadcast staff) ---
$notifList = [System.Collections.ArrayList]@()
$notifList.Add(@{linkType='ORDER'; targetRole='STAFF'; linkUrl='/admin/don-hang'; content='Khách hàng đã đặt đơn hàng mới: DUA-20260001'; linkLabel='Xem đơn hàng'}) | Out-Null
$notifList.Add(@{linkType='ORDER'; targetRole='STAFF'; linkUrl='/admin/don-hang'; content='Khách hàng vừa đặt đơn hàng mới: DUA-20260013'; linkLabel='Xem đơn hàng'}) | Out-Null
$notifList.Add(@{linkType=$null; targetRole='STAFF'; linkUrl='/admin/khach-hang'; content='Khach hang moi: Tran Thi Binh (tranthib@duastore.vn)'; linkLabel='Xem khách hàng'}) | Out-Null
$notifList.Add(@{linkType='PRODUCT'; targetRole='STAFF'; linkUrl='/admin/san-pham/sua/1'; content='Sản phẩm "Chai Thủy Tinh Đựng Rượu Tròn" sắp hết hàng (còn 3)'; linkLabel='Xem sản phẩm'}) | Out-Null
$notifList.Add(@{linkType=$null; targetRole='STAFF'; linkUrl='/admin/don-hang?trangThai=CHO_XAC_NHAN'; content='Có 2 đơn hàng chờ xác nhận quá 24 giờ!'; linkLabel='Xem đơn hàng'}) | Out-Null
$notifList.Add(@{linkType='PRODUCT'; targetRole='STAFF'; linkUrl='/admin/danh-gia'; content='Co danh gia moi cho san pham Ly Highball Thuy Tinh Cao Cap can duyet'; linkLabel='Xem đánh giá'}) | Out-Null
$notifList.Add(@{linkType='PROMOTION'; targetRole='STAFF'; linkUrl='/admin/khuyen-mai'; content='Admin admin đã tạo khuyến mãi: Sinh Nhật DuaStore'; linkLabel='Xem khuyến mãi'}) | Out-Null
$notifList.Add(@{linkType='PRODUCT'; targetRole='STAFF'; linkUrl='/admin/san-pham/sua/7'; content='Sản phẩm Bình Chiết Rượu Vang vừa hết hàng!'; linkLabel='Xem sản phẩm'}) | Out-Null

# --- CONTACT MESSAGES (20) ---
$contactMsgs = @(
  @{hoTen='Nguyễn Văn An'; email='nguyenvan@duastore.vn'; phanLoai='DON_HANG'; noiDung='Đơn hàng của tôi bao giờ giao tới ạ? Đặt đã 3 ngày rồi.'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Trần Thị Bình'; email='tranthib@duastore.vn'; phanLoai='SAN_PHAM'; noiDung='Sản phẩm bình hoa pha lê có màu khác ngoài trong suốt không shop?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Lê Hoàng Cường'; email='lehoangc@duastore.vn'; phanLoai='GIAO_HANG'; noiDung='Shop có giao hàng ngoài giờ hành chính không ạ?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Phạm Thị Dung'; email='phamthid@duastore.vn'; phanLoai='THANH_TOAN'; noiDung='Tôi chuyển khoản rồi nhưng đơn vẫn báo chưa thanh toán.'; isRead=$false; isSpam=$false; isResolved=$false},
  @{hoTen='Võ Minh Đức'; email='vominhe@duastore.vn'; phanLoai='KHIEU_NAI'; noiDung='Ly thủy tinh nhận được bị nứt ở đáy, tôi muốn đổi trả.'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Đặng Thị Phương'; email='dangthif@duastore.vn'; phanLoai='HOP_TAC'; noiDung='Tôi muốn hợp tác làm đại lý phân phối sản phẩm của shop.'; isRead=$false; isSpam=$false; isResolved=$false},
  @{hoTen='Hoàng Văn Nam'; email='hoangvannam@gmail.com'; phanLoai='KHAC'; noiDung='Cho tôi hỏi cửa hàng có chi nhánh ở Đà Nẵng không?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Spam Bot'; email='spam123@fakemail.com'; phanLoai='RAC'; noiDung='Click here to win a free prize now!!! www.spam-link.fake'; isRead=$false; isSpam=$true; isResolved=$false},
  @{hoTen='Nguyễn Thị Hoa'; email='hoanguyen@gmail.com'; phanLoai='DON_HANG'; noiDung='Đơn DUA-20260002 giao thiếu 1 sản phẩm so với đặt hàng.'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Bùi Văn Sơn'; email='buivanson@gmail.com'; phanLoai='SAN_PHAM'; noiDung='Chai thủy tinh 750ml nắp bạc có phải hàng nhập khẩu không ạ?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Đỗ Thị Lan'; email='dothilan@gmail.com'; phanLoai='GIAO_HANG'; noiDung='Phí ship về Cần Thơ là bao nhiêu vậy shop?'; isRead=$false; isSpam=$false; isResolved=$false},
  @{hoTen='Ngô Văn Tài'; email='ngovantai@gmail.com'; phanLoai='THANH_TOAN'; noiDung='Shop có hỗ trợ thanh toán trả góp qua thẻ tín dụng không?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Vũ Thị Mai'; email='vuthimai@gmail.com'; phanLoai='KHIEU_NAI'; noiDung='Nhân viên giao hàng thái độ không tốt, mong shop nhắc nhở.'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Công ty TNHH Thủy Tinh Việt'; email='contact@thuytinhviet.vn'; phanLoai='HOP_TAC'; noiDung='Chúng tôi muốn đặt hàng sỉ số lượng lớn, xin báo giá.'; isRead=$false; isSpam=$false; isResolved=$false},
  @{hoTen='Trịnh Văn Hùng'; email='trinhvanhung@gmail.com'; phanLoai='KHAC'; noiDung='Cửa hàng có chương trình tích điểm thành viên không ạ?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Casino Win'; email='winbig@fakecasino.net'; phanLoai='RAC'; noiDung='Bạn đã trúng thưởng 100 triệu đồng, bấm vào đây để nhận!'; isRead=$false; isSpam=$true; isResolved=$false},
  @{hoTen='Lý Thị Kim'; email='lythikim@gmail.com'; phanLoai='DON_HANG'; noiDung='Tôi muốn hủy đơn DUA-20260014 vì đặt nhầm số lượng.'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Phan Văn Đạt'; email='phanvandat@gmail.com'; phanLoai='SAN_PHAM'; noiDung='Bộ tách trà thủy tinh có kèm khay đựng không shop?'; isRead=$true; isSpam=$false; isResolved=$false},
  @{hoTen='Mai Thị Thu'; email='maithithu@gmail.com'; phanLoai='GIAO_HANG'; noiDung='Có thể đổi địa chỉ giao hàng sau khi đã đặt không ạ?'; isRead=$false; isSpam=$false; isResolved=$false},
  @{hoTen='Đinh Văn Long'; email='dinhvanlong@gmail.com'; phanLoai='KHAC'; noiDung='Cảm ơn shop, sản phẩm rất đẹp và đóng gói cẩn thận!'; isRead=$true; isSpam=$false; isResolved=$false}
)

# --- POPUP BANNERS ---
$popBanners = @(
  @{title='Chào mừng đến với DuaStore!'; imageUrl='/images/products/binh-hoa-pha-le-1000ml.jpg'; linkUrl='/khuyen-mai'; active=$true; displayMode='EVERY_VISIT'},
  @{title='Flash Sale Cuối Tuần - Giảm đến 25%'; imageUrl='/images/products/ly-vang-350ml-bo6.jpg'; linkUrl='/san-pham'; active=$true; displayMode='ONCE_PER_SESSION'; intervalMinutes=60},
  @{title='Khai Trương Chi Nhánh Mới'; imageUrl='/images/products/bo-binh-hoa-mau-800ml.jpg'; linkUrl='/lien-he'; active=$false; displayMode='EVERY_VISIT'}
)

# --- USER SETTINGS ---
$userSettings = [System.Collections.ArrayList]@()
foreach($un in @('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')){
  $userSettings.Add(@{user_ref=$un; settingKey='theme'; settingValue='light'}) | Out-Null
  $userSettings.Add(@{user_ref=$un; settingKey='email_notifications'; settingValue='true'}) | Out-Null
  $userSettings.Add(@{user_ref=$un; settingKey='sms_notifications'; settingValue='false'}) | Out-Null
}

# --- FOOTER LINKS ---
$footerLinks = @(
  @{title='Giới thiệu'; url='/gioi-thieu'; displayOrder=1; columnIndex=1},
  @{title='Liên hệ'; url='/lien-he'; displayOrder=2; columnIndex=1},
  @{title='Tuyển dụng'; url='/tuyen-dung'; displayOrder=3; columnIndex=1},
  @{title='Chính sách đổi trả'; url='/chinh-sach-doi-tra'; displayOrder=1; columnIndex=2},
  @{title='Chính sách bảo mật'; url='/chinh-sach-bao-mat'; displayOrder=2; columnIndex=2},
  @{title='Chính sách vận chuyển'; url='/chinh-sach-van-chuyen'; displayOrder=3; columnIndex=2},
  @{title='Câu hỏi thường gặp'; url='/faq'; displayOrder=1; columnIndex=3},
  @{title='Hướng dẫn mua hàng'; url='/huong-dan-mua-hang'; displayOrder=2; columnIndex=3},
  @{title='Kênh hỗ trợ khách hàng'; url='/ho-tro'; displayOrder=3; columnIndex=3},
  @{title='Theo dõi đơn hàng'; url='/tai-khoan/don-hang'; displayOrder=4; columnIndex=3}
)

# --- CUSTOMER NOTES (subset) ---
$custNotes = @(
  @{user_ref='nguyenvan'; content='Khách hàng thân thiết, thường mua vào cuối tuần.'; createdBy='admin'},
  @{user_ref='tranthib'; content='Ưu tiên gọi điện xác nhận trước khi giao vì hay đổi địa chỉ.'; createdBy='staff1'},
  @{user_ref='lehoangc'; content='Đã từng khiếu nại về vận chuyển, cần chăm sóc kỹ.'; createdBy='staff2'}
)

# --- CUSTOMER TAGS ---
$custTags = @(
  @{user_ref='nguyenvan'; tag='VIP'},
  @{user_ref='tranthib'; tag='Thân thiết'},
  @{user_ref='lehoangc'; tag='Mua nhiều'},
  @{user_ref='phamthid'; tag='Khách mới'},
  @{user_ref='vominhe'; tag='VIP'},
  @{user_ref='dangthif'; tag='Thân thiết'}
)

# --- LOYALTY ---
$loyaltyTx = @(
  @{user_ref='vominhe'; points=76; balance=76; type='EARNED'; note='Tích điểm từ đơn hàng DUA-20260005'},
  @{user_ref='dangthif'; points=23; balance=23; type='EARNED'; note='Tích điểm từ đơn hàng DUA-20260020'},
  @{user_ref='nguyenvan'; points=16; balance=16; type='EARNED'; note='Tích điểm từ đơn hàng DUA-20260021'},
  @{user_ref='tranthib'; points=-50; balance=100; type='REDEEMED'; note='Đổi điểm lấy voucher giảm giá'},
  @{user_ref='lehoangc'; points=20; balance=120; type='ADJUSTED'; note='Điều chỉnh điểm thưởng do sai lệch hệ thống'},
  @{user_ref='phamthid'; points=-30; balance=0; type='EXPIRED'; note='Điểm thưởng hết hạn sử dụng'}
)
$loyaltyBal = @(
  @{user_ref='nguyenvan'; balance=16},
  @{user_ref='tranthib'; balance=100},
  @{user_ref='lehoangc'; balance=120},
  @{user_ref='phamthid'; balance=0},
  @{user_ref='vominhe'; balance=76},
  @{user_ref='dangthif'; balance=23}
)

Write-Host "SiteSettings: $($siteSettings.Count) FlashSales: $($flashSales.Count) Notifications: $($notifList.Count) ContactMsgs: $($contactMsgs.Count)"
