# Categories with final imageUrl
$cats = [System.Collections.ArrayList]@()
$cats.Add(@{name='Chai Thủy Tinh'; description='Các loại chai thủy tinh đựng rượu, nước hoa, thực phẩm'; displayOrder=1; imageUrl='/images/products/chai-tron-250ml-nap-go.jpg'}) | Out-Null
$cats.Add(@{name='Hũ Thủy Tinh'; description='Hũ đựng đồ khô, thực phẩm, gia vị'; displayOrder=2; imageUrl='/images/products/hu-hinh-trai-bi-500ml.jpg'}) | Out-Null
$cats.Add(@{name='Bình Trang Trí'; description='Bình hoa, bình decor, trưng bày nhà cửa'; displayOrder=3; imageUrl='/images/products/binh-hoa-trong-25cm.jpg'}) | Out-Null
$cats.Add(@{name='Ly & Cốc'; description='Các loại ly cốc thủy tinh cao cấp'; displayOrder=4; imageUrl='/images/products/ly-highball-300ml.jpg'}) | Out-Null
$cats.Add(@{name='Quà Tặng'; description='Bộ set quà tặng thủy tinh sang trọng'; displayOrder=5; imageUrl='/images/products/bo-tach-tra-200ml.jpg'}) | Out-Null
# Subcategories under Chai Thủy Tinh (parentId=1)
$cats.Add(@{name='Chai Rượu'; parentId_ref='Chai Thủy Tinh'; displayOrder=1; imageUrl='/images/products/chai-vodka-tron-500ml.jpg'}) | Out-Null
$cats.Add(@{name='Chai Nước Hoa'; parentId_ref='Chai Thủy Tinh'; displayOrder=2; imageUrl='/images/products/chai-nuoc-hoa-vuong-100ml.jpg'}) | Out-Null
$cats.Add(@{name='Chai Thực Phẩm'; parentId_ref='Chai Thủy Tinh'; displayOrder=3; imageUrl='/images/products/chai-tron-500ml-nap-nhua.jpg'}) | Out-Null
# Subcategories under Bình Trang Trí (parentId=3)
$cats.Add(@{name='Bình Hoa'; parentId_ref='Bình Trang Trí'; displayOrder=1; imageUrl='/images/products/bo-binh-hoa-mau-800ml.jpg'}) | Out-Null
$cats.Add(@{name='Bình Decor'; parentId_ref='Bình Trang Trí'; displayOrder=2; imageUrl='/images/products/binh-hoa-cobalt-25cm.jpg'}) | Out-Null
# Subcategories under Ly & Cốc (parentId=4)
$cats.Add(@{name='Ly Rượu Vang'; parentId_ref='Ly & Cốc'; displayOrder=1; imageUrl='/images/products/ly-vang-350ml-don.jpg'}) | Out-Null
$cats.Add(@{name='Ly Whisky'; parentId_ref='Ly & Cốc'; displayOrder=2; imageUrl='/images/products/ly-highball-450ml.jpg'}) | Out-Null
$cats.Add(@{name='Ly Nước'; parentId_ref='Ly & Cốc'; displayOrder=3; imageUrl='/images/products/coc-co-quai-250ml.jpg'}) | Out-Null
$cats.Add(@{name='Ly Champagne'; parentId_ref='Ly & Cốc'; displayOrder=4; imageUrl='/images/products/ly-vang-200ml-don.jpg'}) | Out-Null
$cats.Add(@{name='Ly Highball'; parentId_ref='Ly & Cốc'; displayOrder=5; imageUrl='/images/products/ly-highball-bo6.jpg'}) | Out-Null
# Additional subcats under Bình Trang Trí
$cats.Add(@{name='Bình Hoa Pha Lê'; parentId_ref='Bình Trang Trí'; displayOrder=3; imageUrl='/images/products/binh-hoa-pha-le-1000ml.jpg'}) | Out-Null
$cats.Add(@{name='Bình Decanter Rượu'; parentId_ref='Bình Trang Trí'; displayOrder=4; imageUrl='/images/products/binh-decanter-hario-400ml.jpg'}) | Out-Null
# Subcats under Hũ Thủy Tinh
$cats.Add(@{name='Hũ Đựng Thực Phẩm'; parentId_ref='Hũ Thủy Tinh'; displayOrder=1; imageUrl='/images/products/hu-hinh-trai-bi-500ml.jpg'}) | Out-Null
$cats.Add(@{name='Hũ Ngâm Rượu'; parentId_ref='Hũ Thủy Tinh'; displayOrder=2; imageUrl='/images/products/binh-ngam-ruou-5000ml.jpg'}) | Out-Null
# Additional subcat under Ly & Cốc
$cats.Add(@{name='Ly Màu Nghệ Thuật'; parentId_ref='Ly & Cốc'; displayOrder=6; imageUrl='/images/products/bo-ly-mau-1-350ml.jpg'}) | Out-Null

# Products (final danhMucRef after BUOC 8 updates + hinhAnhChinh backfill)
$prods = [System.Collections.ArrayList]@()
$prods.Add(@{name='Chai Thủy Tinh Đựng Rượu Tròn'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Chai Rượu'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/chai-tron-100ml-nap-go.jpg'}) | Out-Null
$prods.Add(@{name='Chai Thủy Tinh Vuông Cổ Lãng'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Chai Rượu'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/chai-vuong-250ml-nap-bac.jpg'}) | Out-Null
$prods.Add(@{name='Bình Hoa Pha Lê Cắt Cạnh'; material='Pha lê cắt cạnh'; origin='Châu Âu'; usage='Trang trí'; category_ref='Bình Hoa Pha Lê'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/binh-hoa-trong-25cm.jpg'}) | Out-Null
$prods.Add(@{name='Ly Rượu Vang Pha Lê Bohemia'; material='Pha lê Bohemia'; origin='Séc'; usage='Đựng đồ uống'; category_ref='Ly Rượu Vang'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/ly-vang-200ml-don.jpg'}) | Out-Null
$prods.Add(@{name='Ly Highball Thủy Tinh Cao Cấp'; material='Thủy tinh cường lực'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Highball'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/ly-highball-300ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Pha Lê Pasabahce Nhập Khẩu'; material='Pha lê cao cấp'; origin='Thổ Nhĩ Kỳ'; usage='Trang trí'; category_ref='Bình Hoa Pha Lê'; status='DAT_TRUOC'; featured=$true; leadTimeDays=10; hinhAnhChinh='/images/products/pasabahce-500ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Chiết Rượu Vang'; material='Thủy tinh cao cấp'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Bình Decanter Rượu'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/binh-chiet-ruou-vang-1000ml.png'}) | Out-Null
$prods.Add(@{name='Bình Cắm Hoa Trang Trí Loại Nhỏ'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Trang trí'; category_ref='Bình Hoa'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/binh-cam-hoa-trang-tri-nho-300ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Hoa Pha Lê'; material='Pha lê'; origin='Việt Nam'; usage='Trang trí'; category_ref='Bình Hoa Pha Lê'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/binh-hoa-pha-le-1000ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Khuếch Tán Tinh Dầu'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Trang trí'; category_ref='Bình Decor'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/binh-khuech-tan-tinh-dau-350ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Thủy Tinh Ngâm Rượu'; material='Thủy tinh dày'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Hũ Ngâm Rượu'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/binh-ngam-ruou-5000ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Đựng Nước Thủy Tinh Dập Nổi Sang Trọng'; material='Thủy tinh dập nổi'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Nước'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/binh-dung-nuoc-dap-noi-1000ml.jpg'}) | Out-Null
$prods.Add(@{name='Bình Thủy Tinh Decanter Hario'; material='Thủy tinh chịu nhiệt'; origin='Nhật Bản'; usage='Đựng đồ uống'; category_ref='Bình Decanter Rượu'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/binh-decanter-hario-400ml.jpg'}) | Out-Null
$prods.Add(@{name='Bộ Bình Hoa Màu'; material='Thủy tinh màu'; origin='Việt Nam'; usage='Trang trí'; category_ref='Bình Hoa'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/bo-binh-hoa-mau-800ml.jpg'}) | Out-Null
$prods.Add(@{name='Bộ Ly Thủy Tinh Màu 1'; material='Thủy tinh màu'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Màu Nghệ Thuật'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/bo-ly-mau-1-350ml.jpg'}) | Out-Null
$prods.Add(@{name='Bộ Ly Thủy Tinh Màu 2'; material='Thủy tinh màu'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Màu Nghệ Thuật'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/bo-ly-mau-2-350ml.jpg'}) | Out-Null
$prods.Add(@{name='Bộ Tách Trà Thủy Tinh'; material='Thủy tinh chịu nhiệt'; origin='Việt Nam'; usage='Quà tặng'; category_ref='Quà Tặng'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/bo-tach-tra-200ml.jpg'}) | Out-Null
$prods.Add(@{name='Chai Nước Hoa Vuông Thủy Tinh'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng mỹ phẩm'; category_ref='Chai Nước Hoa'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/chai-nuoc-hoa-vuong-100ml.jpg'}) | Out-Null
$prods.Add(@{name='Chai Rượu Vodka Thủy Tinh Dạng Tròn'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Chai Rượu'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/chai-vodka-tron-500ml.jpg'}) | Out-Null
$prods.Add(@{name='Cốc Thủy Tinh Có Quai'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Nước'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/coc-co-quai-250ml.jpg'}) | Out-Null
$prods.Add(@{name='Hũ Thủy Tinh Hình Trái Bí'; material='Thủy tinh trong suốt'; origin='Việt Nam'; usage='Đựng đồ khô'; category_ref='Hũ Đựng Thực Phẩm'; status='DANG_BAN'; featured=$true; hinhAnhChinh='/images/products/hu-hinh-trai-bi-500ml.jpg'}) | Out-Null
$prods.Add(@{name='Hũ Thủy Tinh Nắp Cài Kín Hơi'; material='Thủy tinh dày'; origin='Việt Nam'; usage='Đựng đồ khô'; category_ref='Hũ Đựng Thực Phẩm'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/hu-nap-cai-kin-hoi-750ml.jpg'}) | Out-Null
$prods.Add(@{name='Hũ Thủy Tinh Sọc'; material='Thủy tinh họa tiết'; origin='Việt Nam'; usage='Đựng đồ khô'; category_ref='Hũ Đựng Thực Phẩm'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/hu-soc-100ml.jpg'}) | Out-Null
$prods.Add(@{name='Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc'; material='Thủy tinh họa tiết'; origin='Việt Nam'; usage='Đựng đồ uống'; category_ref='Ly Highball'; status='DANG_BAN'; featured=$false; hinhAnhChinh='/images/products/ly-tru-tron-hoa-tiet-soc-400ml.jpg'}) | Out-Null

# ProductVariants (44 total)
$vars = [System.Collections.ArrayList]@()
# Product 1: 10 variants
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='50ml - Nắp Gỗ'; dungTich=50; giaGoc=15000; giaKhuyenMai=$null; soLuongTon=20; hinhAnh='/images/products/chai-tron-50ml-nap-go.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='50ml - Nắp Nhựa'; dungTich=50; giaGoc=13000; giaKhuyenMai=$null; soLuongTon=15; hinhAnh='/images/products/chai-tron-50ml-nap-nhua.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='100ml - Nắp Gỗ'; dungTich=100; giaGoc=23000; giaKhuyenMai=$null; soLuongTon=50; hinhAnh='/images/products/chai-tron-100ml-nap-go.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='100ml - Nắp Nhựa'; dungTich=100; giaGoc=20000; giaKhuyenMai=$null; soLuongTon=40; hinhAnh='/images/products/chai-tron-100ml-nap-nhua.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='250ml - Nắp Gỗ'; dungTich=250; giaGoc=38000; giaKhuyenMai=34000; soLuongTon=30; hinhAnh='/images/products/chai-tron-250ml-nap-go.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='250ml - Nắp Nhựa'; dungTich=250; giaGoc=32000; giaKhuyenMai=$null; soLuongTon=25; hinhAnh='/images/products/chai-tron-250ml-nap-nhua.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='500ml - Nắp Gỗ'; dungTich=500; giaGoc=55000; giaKhuyenMai=50000; soLuongTon=0; hinhAnh='/images/products/chai-tron-500ml-nap-go.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='500ml - Nắp Nhựa'; dungTich=500; giaGoc=48000; giaKhuyenMai=$null; soLuongTon=15; hinhAnh='/images/products/chai-tron-500ml-nap-nhua.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='750ml - Nắp Gỗ'; dungTich=750; giaGoc=72000; giaKhuyenMai=$null; soLuongTon=10; hinhAnh='/images/products/chai-tron-750ml-nap-go.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Đựng Rượu Tròn'; name='750ml - Nắp Nhựa'; dungTich=750; giaGoc=65000; giaKhuyenMai=$null; soLuongTon=8; hinhAnh='/images/products/chai-tron-750ml-nap-nhua.jpg'; isDefault=$false}) | Out-Null
# Product 2: 3 variants
$vars.Add(@{product_ref='Chai Thủy Tinh Vuông Cổ Lãng'; name='250ml - Nắp Bạc'; dungTich=250; giaGoc=25000; giaKhuyenMai=$null; soLuongTon=35; hinhAnh='/images/products/chai-vuong-250ml-nap-bac.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Vuông Cổ Lãng'; name='500ml - Nắp Bạc'; dungTich=500; giaGoc=42000; giaKhuyenMai=$null; soLuongTon=20; hinhAnh='/images/products/chai-vuong-500ml-nap-bac.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Chai Thủy Tinh Vuông Cổ Lãng'; name='750ml - Nắp Bạc'; dungTich=750; giaGoc=68000; giaKhuyenMai=60000; soLuongTon=12; hinhAnh='/images/products/chai-vuong-750ml-nap-bac.jpg'; isDefault=$false}) | Out-Null
# Product 3: 4 variants
$vars.Add(@{product_ref='Bình Hoa Pha Lê Cắt Cạnh'; name='Trong suốt - Cao 25cm'; dungTich=$null; giaGoc=450000; giaKhuyenMai=$null; soLuongTon=15; hinhAnh='/images/products/binh-hoa-trong-25cm.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Hoa Pha Lê Cắt Cạnh'; name='Xanh Cobalt - Cao 25cm'; dungTich=$null; giaGoc=520000; giaKhuyenMai=480000; soLuongTon=8; hinhAnh='/images/products/binh-hoa-cobalt-25cm.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Bình Hoa Pha Lê Cắt Cạnh'; name='Nâu khói - Cao 25cm'; dungTich=$null; giaGoc=490000; giaKhuyenMai=$null; soLuongTon=10; hinhAnh='/images/products/binh-hoa-nau-25cm.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Bình Hoa Pha Lê Cắt Cạnh'; name='Trong suốt - Cao 35cm'; dungTich=$null; giaGoc=620000; giaKhuyenMai=$null; soLuongTon=6; hinhAnh='/images/products/binh-hoa-trong-35cm.jpg'; isDefault=$false}) | Out-Null
# Product 4: 4 variants
$vars.Add(@{product_ref='Ly Rượu Vang Pha Lê Bohemia'; name='200ml - Đơn chiếc'; dungTich=$null; giaGoc=84000; giaKhuyenMai=$null; soLuongTon=56; hinhAnh='/images/products/ly-vang-200ml-don.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Ly Rượu Vang Pha Lê Bohemia'; name='350ml - Đơn chiếc'; dungTich=$null; giaGoc=105000; giaKhuyenMai=95000; soLuongTon=22; hinhAnh='/images/products/ly-vang-350ml-don.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Ly Rượu Vang Pha Lê Bohemia'; name='Bộ 6 cái - 200ml'; dungTich=$null; giaGoc=480000; giaKhuyenMai=430000; soLuongTon=15; hinhAnh='/images/products/ly-vang-200ml-bo6.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Ly Rượu Vang Pha Lê Bohemia'; name='Bộ 6 cái - 350ml'; dungTich=$null; giaGoc=600000; giaKhuyenMai=550000; soLuongTon=8; hinhAnh='/images/products/ly-vang-350ml-bo6.jpg'; isDefault=$false}) | Out-Null
# Product 5: 3 variants
$vars.Add(@{product_ref='Ly Highball Thủy Tinh Cao Cấp'; name='300ml - Đơn chiếc'; dungTich=300; giaGoc=35000; giaKhuyenMai=$null; soLuongTon=80; hinhAnh='/images/products/ly-highball-300ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Ly Highball Thủy Tinh Cao Cấp'; name='450ml - Đơn chiếc'; dungTich=450; giaGoc=45000; giaKhuyenMai=40000; soLuongTon=60; hinhAnh='/images/products/ly-highball-450ml.jpg'; isDefault=$false}) | Out-Null
$vars.Add(@{product_ref='Ly Highball Thủy Tinh Cao Cấp'; name='Bộ 6 cái - 300ml'; dungTich=300; giaGoc=195000; giaKhuyenMai=175000; soLuongTon=20; hinhAnh='/images/products/ly-highball-bo6.jpg'; isDefault=$false}) | Out-Null
# Product 6: 2 variants
$vars.Add(@{product_ref='Bình Pha Lê Pasabahce Nhập Khẩu'; name='500ml - Pha lê trắng'; dungTich=500; giaGoc=850000; giaKhuyenMai=$null; soLuongTon=0; hinhAnh='/images/products/pasabahce-500ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Pha Lê Pasabahce Nhập Khẩu'; name='750ml - Pha lê trắng'; dungTich=750; giaGoc=980000; giaKhuyenMai=890000; soLuongTon=0; hinhAnh='/images/products/pasabahce-750ml.jpg'; isDefault=$false}) | Out-Null
# Products 7-24: 1 variant each (Mặc định)
$vars.Add(@{product_ref='Bình Chiết Rượu Vang'; name='Mặc định'; dungTich=1000; giaGoc=320000; giaKhuyenMai=290000; soLuongTon=25; hinhAnh='/images/products/binh-chiet-ruou-vang-1000ml.png'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Cắm Hoa Trang Trí Loại Nhỏ'; name='Mặc định'; dungTich=300; giaGoc=95000; giaKhuyenMai=$null; soLuongTon=40; hinhAnh='/images/products/binh-cam-hoa-trang-tri-nho-300ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Hoa Pha Lê'; name='Mặc định'; dungTich=1000; giaGoc=680000; giaKhuyenMai=620000; soLuongTon=12; hinhAnh='/images/products/binh-hoa-pha-le-1000ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Khuếch Tán Tinh Dầu'; name='Mặc định'; dungTich=350; giaGoc=150000; giaKhuyenMai=$null; soLuongTon=30; hinhAnh='/images/products/binh-khuech-tan-tinh-dau-350ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Thủy Tinh Ngâm Rượu'; name='Mặc định'; dungTich=5000; giaGoc=280000; giaKhuyenMai=$null; soLuongTon=15; hinhAnh='/images/products/binh-ngam-ruou-5000ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Đựng Nước Thủy Tinh Dập Nổi Sang Trọng'; name='Mặc định'; dungTich=1000; giaGoc=220000; giaKhuyenMai=195000; soLuongTon=20; hinhAnh='/images/products/binh-dung-nuoc-dap-noi-1000ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bình Thủy Tinh Decanter Hario'; name='Mặc định'; dungTich=400; giaGoc=385000; giaKhuyenMai=$null; soLuongTon=18; hinhAnh='/images/products/binh-decanter-hario-400ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bộ Bình Hoa Màu'; name='Mặc định'; dungTich=800; giaGoc=175000; giaKhuyenMai=$null; soLuongTon=22; hinhAnh='/images/products/bo-binh-hoa-mau-800ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bộ Ly Thủy Tinh Màu 1'; name='Mặc định'; dungTich=350; giaGoc=145000; giaKhuyenMai=$null; soLuongTon=35; hinhAnh='/images/products/bo-ly-mau-1-350ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bộ Ly Thủy Tinh Màu 2'; name='Mặc định'; dungTich=350; giaGoc=145000; giaKhuyenMai=$null; soLuongTon=35; hinhAnh='/images/products/bo-ly-mau-2-350ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Bộ Tách Trà Thủy Tinh'; name='Mặc định'; dungTich=200; giaGoc=210000; giaKhuyenMai=189000; soLuongTon=28; hinhAnh='/images/products/bo-tach-tra-200ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Chai Nước Hoa Vuông Thủy Tinh'; name='Mặc định'; dungTich=100; giaGoc=45000; giaKhuyenMai=$null; soLuongTon=50; hinhAnh='/images/products/chai-nuoc-hoa-vuong-100ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Chai Rượu Vodka Thủy Tinh Dạng Tròn'; name='Mặc định'; dungTich=500; giaGoc=38000; giaKhuyenMai=$null; soLuongTon=60; hinhAnh='/images/products/chai-vodka-tron-500ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Cốc Thủy Tinh Có Quai'; name='Mặc định'; dungTich=250; giaGoc=42000; giaKhuyenMai=$null; soLuongTon=45; hinhAnh='/images/products/coc-co-quai-250ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Hũ Thủy Tinh Hình Trái Bí'; name='Mặc định'; dungTich=500; giaGoc=68000; giaKhuyenMai=$null; soLuongTon=32; hinhAnh='/images/products/hu-hinh-trai-bi-500ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Hũ Thủy Tinh Nắp Cài Kín Hơi'; name='Mặc định'; dungTich=750; giaGoc=55000; giaKhuyenMai=$null; soLuongTon=38; hinhAnh='/images/products/hu-nap-cai-kin-hoi-750ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Hũ Thủy Tinh Sọc'; name='Mặc định'; dungTich=100; giaGoc=32000; giaKhuyenMai=$null; soLuongTon=55; hinhAnh='/images/products/hu-soc-100ml.jpg'; isDefault=$true}) | Out-Null
$vars.Add(@{product_ref='Ly Thủy Tinh Dạng Trụ Tròn Họa Tiết Sọc'; name='Mặc định'; dungTich=400; giaGoc=40000; giaKhuyenMai=$null; soLuongTon=42; hinhAnh='/images/products/ly-tru-tron-hoa-tiet-soc-400ml.jpg'; isDefault=$true}) | Out-Null

# ProductImages (one per variant)
$prodImgs = [System.Collections.ArrayList]@()
$sortIdx = @{}
foreach($v in $vars){
  $pn = $v.product_ref
  if(-not $sortIdx.ContainsKey($pn)){$sortIdx[$pn]=0}
  $prodImgs.Add(@{product_ref=$pn; imageUrl=$v.hinhAnh; sortOrder=$sortIdx[$pn]; isActive=$true}) | Out-Null
  $sortIdx[$pn]++
}

# Promotions
$promos = @(
  @{code='KHAIHANG'; name='Khai trương DuaStore Hải Phòng'; loaiGiam='PHAN_TRAM'; giaTriGiam=15; donHangToiThieu=200000; giamToiDa=100000; soLanDung=200; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType=$null; targetIds=$null; isActive=$false},
  @{code='FREESHIP'; name='Miễn phí vận chuyển đơn từ 500k'; loaiGiam='SO_TIEN'; giaTriGiam=30000; donHangToiThieu=500000; giamToiDa=$null; soLanDung=$null; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType=$null; targetIds=$null; isActive=$true},
  @{code='DECO50K'; name='Giảm 50k đơn từ 300k'; loaiGiam='SO_TIEN'; giaTriGiam=50000; donHangToiThieu=300000; giamToiDa=$null; soLanDung=100; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType=$null; targetIds=$null; isActive=$true},
  @{code='SUMMER50'; name='Summer Sale - Giảm 50%'; loaiGiam='PHAN_TRAM'; giaTriGiam=50; donHangToiThieu=500000; giamToiDa=200000; soLanDung=500; tuNgay='2026-06-01'; denNgay='2026-08-31'; targetType='PRODUCT'; targetIds='1,2,3,4,5'; isActive=$true},
  @{code='NEWUSER'; name='Ưu đãi người mới - Giảm 20%'; loaiGiam='PHAN_TRAM'; giaTriGiam=20; donHangToiThieu=100000; giamToiDa=50000; soLanDung=1000; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='THANG9'; name='Ưu đãi tháng 9 - Giảm 10%'; loaiGiam='PHAN_TRAM'; giaTriGiam=10; donHangToiThieu=150000; giamToiDa=80000; soLanDung=300; tuNgay='2026-09-01'; denNgay='2026-09-30'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='SINHNHAT'; name='Quà sinh nhật thành viên'; loaiGiam='PHAN_TRAM'; giaTriGiam=15; donHangToiThieu=0; giamToiDa=100000; soLanDung=500; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='VIP100K'; name='Giảm 100k cho đơn VIP từ 1 triệu'; loaiGiam='SO_TIEN'; giaTriGiam=100000; donHangToiThieu=1000000; giamToiDa=$null; soLanDung=100; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='COMBO30'; name='Combo giảm 30% danh mục ly & cốc'; loaiGiam='PHAN_TRAM'; giaTriGiam=30; donHangToiThieu=100000; giamToiDa=150000; soLanDung=200; tuNgay='2026-01-01'; denNgay='2026-12-31'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='CUOINAM'; name='Ưu đãi cuối năm - Giảm 25%'; loaiGiam='PHAN_TRAM'; giaTriGiam=25; donHangToiThieu=200000; giamToiDa=150000; soLanDung=400; tuNgay='2026-10-01'; denNgay='2026-12-31'; targetType='ALL'; targetIds=$null; isActive=$false},
  @{code='CHAOMOI10'; name='Chào mừng thành viên mới - Giảm 10%'; loaiGiam='PHAN_TRAM'; giaTriGiam=10; donHangToiThieu=100000; giamToiDa=30000; soLanDung=$null; tuNgay='2026-01-01'; denNgay='2027-12-31'; targetType='PRODUCT'; targetIds='1,2,3,4,5,6'; isActive=$true}
)

Write-Host "Categories: $($cats.Count), Products: $($prods.Count), Variants: $($vars.Count), ProductImages: $($prodImgs.Count), Promotions: $($promos.Count)"
