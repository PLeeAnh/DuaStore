$BCRYPT = '$2a$10$nVW/exPWTVtztHe0.kxk7exU6sktiHM86HuRE60PtuEZM/tORbImO'

$allRoles = @(
  @{name='PRODUCT_OWNER'; moTa='Chủ sở hữu sản phẩm - toàn quyền hệ thống'},
  @{name='ADMIN'; moTa='Quản trị viên được gán quyền cụ thể'},
  @{name='STAFF'; moTa='Nhân viên xử lý đơn hàng, sản phẩm, đánh giá'},
  @{name='USER'; moTa='Khách hàng'}
)

$pMA = @(
  @('DASHBOARD','READ'),@('PRODUCT','CREATE'),@('PRODUCT','READ'),@('PRODUCT','UPDATE'),@('PRODUCT','DELETE'),
  @('ORDER','READ'),@('ORDER','UPDATE'),@('USER','READ'),@('USER','UPDATE'),@('USER','CREATE'),
  @('CATEGORY','CREATE'),@('CATEGORY','READ'),@('CATEGORY','UPDATE'),@('CATEGORY','DELETE'),
  @('PROMOTION','CREATE'),@('PROMOTION','READ'),@('PROMOTION','UPDATE'),@('PROMOTION','DELETE'),
  @('REVIEW','READ'),@('REVIEW','APPROVE'),@('REVIEW','HIDE'),@('REVIEW','DELETE'),
  @('POST','CREATE'),@('POST','READ'),@('POST','UPDATE'),@('POST','DELETE'),
  @('VARIANT','CREATE'),@('VARIANT','READ'),@('VARIANT','UPDATE'),@('VARIANT','DELETE'),
  @('ROLE','CREATE'),@('ROLE','READ'),@('ROLE','UPDATE'),@('ROLE','DELETE'),
  @('NOTIFICATION','CREATE'),@('NOTIFICATION','READ'),@('NOTIFICATION','UPDATE'),@('NOTIFICATION','DELETE'),
  @('AUDIT_LOG','READ'),
  @('STORE','CREATE'),@('STORE','READ'),@('STORE','UPDATE'),@('STORE','DELETE'),
  @('BANNER','CREATE'),@('BANNER','READ'),@('BANNER','UPDATE'),@('BANNER','DELETE'),
  @('CUSTOMER','READ'),@('CUSTOMER','UPDATE'),
  @('HOMEPAGE','READ'),@('HOMEPAGE','UPDATE'),
  @('APPEARANCE','READ'),@('APPEARANCE','UPDATE'),
  @('ANALYTICS','READ'),
  @('EMAIL_SETTING','READ'),@('EMAIL_SETTING','UPDATE'),
  @('PAYMENT_SETTING','READ'),@('PAYMENT_SETTING','UPDATE'),
  @('SHIPPING_SETTING','READ'),@('SHIPPING_SETTING','UPDATE'),
  @('POST_CATEGORY','CREATE'),@('POST_CATEGORY','READ'),@('POST_CATEGORY','UPDATE'),@('POST_CATEGORY','DELETE'),
  @('FLASH_SALE','CREATE'),@('FLASH_SALE','READ'),@('FLASH_SALE','UPDATE'),@('FLASH_SALE','DELETE'),
  @('FOOTER_LINK','CREATE'),@('FOOTER_LINK','READ'),@('FOOTER_LINK','UPDATE'),@('FOOTER_LINK','DELETE'),
  @('PRICE_HISTORY','READ'),
  @('LOYALTY','READ'),@('LOYALTY','UPDATE'),
  @('ALERT','READ'),@('ALERT','UPDATE'),
  @('CONTACT_MESSAGE','READ'),@('CONTACT_MESSAGE','UPDATE'),@('CONTACT_MESSAGE','DELETE')
)
$pDesc = @(
  'Xem trang tổng quan','Thêm sản phẩm','Xem sản phẩm','Sửa sản phẩm','Xóa sản phẩm',
  'Xem đơn hàng','Cập nhật đơn hàng','Xem người dùng','Sửa người dùng','Thêm người dùng',
  'Thêm danh mục','Xem danh mục','Sửa danh mục','Xóa danh mục',
  'Thêm khuyến mãi','Xem khuyến mãi','Sửa khuyến mãi','Xóa khuyến mãi',
  'Xem đánh giá','Duyệt đánh giá','Ẩn đánh giá','Xóa đánh giá',
  'Thêm bài viết','Xem bài viết','Sửa bài viết','Xóa bài viết',
  'Thêm biến thể','Xem biến thể','Sửa biến thể','Xóa biến thể',
  'Thêm vai trò','Xem vai trò','Sửa vai trò','Xóa vai trò',
  'Tạo thông báo','Xem thông báo','Sửa thông báo','Xóa thông báo',
  'Xem nhật ký hệ thống',
  'Thêm địa chỉ cửa hàng','Xem địa chỉ cửa hàng','Sửa địa chỉ cửa hàng','Xóa địa chỉ cửa hàng',
  'Thêm banner','Xem banner','Sửa banner','Xóa banner',
  'Xem khách hàng','Sửa khách hàng',
  'Xem cấu hình trang chủ','Sửa cấu hình trang chủ',
  'Xem giao diện','Sửa giao diện',
  'Xem phân tích',
  'Xem cấu hình email','Sửa cấu hình email',
  'Xem cấu hình thanh toán','Sửa cấu hình thanh toán',
  'Xem cấu hình vận chuyển','Sửa cấu hình vận chuyển',
  'Thêm danh mục bài viết','Xem danh mục bài viết','Sửa danh mục bài viết','Xóa danh mục bài viết',
  'Thêm chương trình flash sale','Xem chương trình flash sale','Sửa chương trình flash sale','Xóa chương trình flash sale',
  'Thêm link chân trang','Xem link chân trang','Sửa link chân trang','Xóa link chân trang',
  'Xem lịch sử giá',
  'Xem tích điểm khách hàng','Sửa tích điểm khách hàng',
  'Xem cảnh báo','Sửa cảnh báo',
  'Xem tin nhắn liên hệ','Sửa tin nhắn liên hệ','Xóa tin nhắn liên hệ'
)
$allPerms = for($ix=0;$ix -lt $pMA.Count;$ix++){
  @{module=$pMA[$ix][0]; action=$pMA[$ix][1]; moTa=$pDesc[$ix]}
}

$rpList = [System.Collections.ArrayList]@()
for($ix=0;$ix -lt 80;$ix++){
  $rpList.Add(@{role_ref='PRODUCT_OWNER'; permission_ref="$($pMA[$ix][0])_$($pMA[$ix][1])"}) | Out-Null
}
for($ix=0;$ix -lt 80;$ix++){
  $rpList.Add(@{role_ref='ADMIN'; permission_ref="$($pMA[$ix][0])_$($pMA[$ix][1])"}) | Out-Null
}
$staffPids = @(1,2,3,4,6,7,12,13,19,20,21,27,28,29,48,49,78,79)
foreach($spid in $staffPids){
  $rpList.Add(@{role_ref='STAFF'; permission_ref="$($pMA[$spid-1][0])_$($pMA[$spid-1][1])"}) | Out-Null
}

$ud = @(
  @('admin','admin@duastore.vn','Quản Trị Viên','0901234567'),
  @('nguyenvan','nguyen@gmail.com','Nguyễn Văn An','0912345678'),
  @('owner2','owner2@duastore.vn','Phạm Văn Chu','0930000001'),
  @('owner3','owner3@duastore.vn','Lê Thị Hằng','0930000002'),
  @('owner4','owner4@duastore.vn','Trần Văn Kiệt','0930000003'),
  @('admin2','admin2@duastore.vn','Nguyễn Thị Quyên','0931000001'),
  @('admin3','admin3@duastore.vn','Trần Văn Quang','0931000002'),
  @('admin4','admin4@duastore.vn','Lê Thị Huệ','0931000003'),
  @('admin5','admin5@duastore.vn','Phạm Văn Tài','0931000004'),
  @('admin6','admin6@duastore.vn','Võ Thị Ngân','0931000005'),
  @('staff1','staff1@duastore.vn','Bùi Văn Nhân','0932000001'),
  @('staff2','staff2@duastore.vn','Đỗ Thị Thảo','0932000002'),
  @('staff3','staff3@duastore.vn','Ngô Văn Lộc','0932000003'),
  @('staff4','staff4@duastore.vn','Đặng Thị Yến','0932000004'),
  @('staff5','staff5@duastore.vn','Hoàng Văn Phong','0932000005'),
  @('tranthib','tranthib@gmail.com','Trần Thị Bình','0913456789'),
  @('lehoangc','lehoangc@gmail.com','Lê Hoàng Cường','0914567890'),
  @('phamthid','phamthid@gmail.com','Phạm Thị Dung','0915678901'),
  @('vominhe','vominhe@gmail.com','Võ Minh Đức','0916789012'),
  @('dangthif','dangthif@gmail.com','Đặng Thị Phương','0917890123')
)
$allUsers = foreach($u in $ud){
  @{username=$u[0]; email=$u[1]; password=$BCRYPT; hoTen=$u[2]; soDienThoai=$u[3]; isActive=$true}
}
$uid = @{}; for($ix=0;$ix -lt $ud.Count;$ix++){$uid[$ud[$ix][0]]=$ix+1}

$urList = [System.Collections.ArrayList]@()
foreach($u in @('admin','owner2','owner3','owner4')){$urList.Add(@{user_ref=$u;role_ref='PRODUCT_OWNER'}) | Out-Null}
foreach($u in @('admin2','admin3','admin4','admin5','admin6')){$urList.Add(@{user_ref=$u;role_ref='ADMIN'}) | Out-Null}
foreach($u in @('staff1','staff2','staff3','staff4','staff5')){$urList.Add(@{user_ref=$u;role_ref='STAFF'}) | Out-Null}
foreach($u in @('nguyenvan','tranthib','lehoangc','phamthid','vominhe','dangthif')){$urList.Add(@{user_ref=$u;role_ref='USER'}) | Out-Null}

$uapList = foreach($u in $ud){@{user_ref=$u[0]; provider='PASSWORD'}}

$seed = [ordered]@{
  version = 'V10'
  importOrder = @(
    'roles','permissions','role_permissions','users','user_roles',
    'user_auth_providers','Categories','Products','ProductVariants',
    'ProductImages','Promotions','Addresses','orders','order_items',
    'order_notes','order_assignments','order_status_logs','admin_action_logs',
    'PostCategories','Posts','Wishlists','banners','store_info',
    'CartItems','Reviews','ReviewImages','ReviewReplies','UserVouchers',
    'SiteSettings','FlashSales','FlashSaleItems','linked_accounts',
    'Notifications','StockMovements','ContactReplies','ProductViews',
    'UserActivityLogs','contact_messages','popup_banners','user_settings',
    'CustomerNotes','CustomerTags','PriceHistory','footer_links',
    'LoyaltyTransactions','LoyaltyBalances','SavedCartItems','PageViews'
  )
  tables = [ordered]@{
    roles = $allRoles
    permissions = $allPerms
    role_permissions = $rpList.ToArray()
    users = $allUsers
    user_roles = $urList.ToArray()
    user_auth_providers = $uapList
  }
}

$json = $seed | ConvertTo-Json -Depth 10
[System.IO.File]::WriteAllText("E:\Study\Class\JavaStudy\Website\DuaStore\DuaStore\src\main\resources\db\seed_data_part1.json", $json, [System.Text.Encoding]::UTF8)
Write-Host "role_permissions: $($rpList.Count), user_roles: $($urList.Count)"
