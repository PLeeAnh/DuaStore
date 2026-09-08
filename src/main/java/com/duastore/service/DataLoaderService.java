package com.duastore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duastore.model.*;
import com.duastore.repository.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoaderService implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final PromotionRepository promotionRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final BannerRepository bannerRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostsRepository postsRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final PopupBannerRepository popupBannerRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final OrderNoteRepository orderNoteRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewsRepository reviewsRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final NotificationRepository notificationRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final UserSettingRepository userSettingRepository;
    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerTagRepository customerTagRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SavedCartItemRepository savedCartItemRepository;
    private final ProductViewRepository productViewRepository;
    private final UserActivityLogRepository userActivityLogRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final LoyaltyBalanceRepository loyaltyBalanceRepository;
    private final PageViewRepository pageViewRepository;

    @Override
    public void run(String... args) {
        try {
            InputStream is = new ClassPathResource("db/seed_data.json").getInputStream();
            JsonNode root = objectMapper.readTree(is);
            JsonNode tables = root.get("tables");

            if (tables == null) {
                log.warn("seed_data.json has no 'tables' key");
                return;
            }

            if (roleRepository.count() > 0) {
                log.info("Database already seeded, skipping DataLoader");
                return;
            }

            log.info("Starting seed data import from seed_data.json...");
            long start = System.currentTimeMillis();

            Map<String, Object> ctx = new HashMap<>();

            loadRoles(tables.get("roles"), ctx);
            loadPermissions(tables.get("permissions"), ctx);
            loadRolePermissions(tables.get("role_permissions"), ctx);
            loadUsers(tables.get("users"), ctx);
            loadUserRoles(tables.get("user_roles"), ctx);
            loadUserAuthProviders(tables.get("user_auth_providers"), ctx);
            loadCategories(tables.get("Categories"), ctx);
            loadProducts(tables.get("Products"), ctx);
            loadProductVariants(tables.get("ProductVariants"), ctx);
            loadProductImages(tables.get("ProductImages"), ctx);
            loadPromotions(tables.get("Promotions"), ctx);
            loadSiteSettings(tables.get("SiteSettings"), ctx);
            loadStoreInfo(tables.get("store_info"), ctx);
            loadBanners(tables.get("banners"), ctx);
            loadPopupBanners(tables.get("popup_banners"), ctx);
            loadFooterLinks(tables.get("footer_links"), ctx);
            loadPostCategories(tables.get("PostCategories"), ctx);
            loadPosts(tables.get("Posts"), ctx);
            loadContactMessages(tables.get("contact_messages"), ctx);
            loadAddresses(tables.get("Addresses"), ctx);
            loadFlashSales(tables.get("FlashSales"), ctx);
            loadFlashSaleItems(tables.get("FlashSaleItems"), ctx);
            loadOrders(tables.get("orders"), ctx);
            loadOrderItems(tables.get("order_items"), ctx);
            loadWishlists(tables.get("Wishlists"), ctx);
            loadCartItems(tables.get("CartItems"), ctx);
            loadReviews(tables.get("Reviews"), ctx);
            loadUserVouchers(tables.get("UserVouchers"), ctx);
            loadNotifications(tables.get("Notifications"), ctx);

            long elapsed = System.currentTimeMillis() - start;
            log.info("Seed data import completed in {}ms", elapsed);

        } catch (Exception e) {
            log.error("Failed to load seed data", e);
        }
    }

    @Transactional
    private void loadRoles(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<Integer, Role> roleMap = new HashMap<>();
        int id = 1;
        for (JsonNode row : node) {
            Role r = new Role();
            r.setName(row.get("name").asText());
            r.setMoTa(row.get("moTa").asText());
            r.setIsActive(true);
            r.setNgayTao(LocalDateTime.now());
            roleRepository.save(r);
            roleMap.put(id++, r);
        }
        ctx.put("roles", roleMap);
        ctx.put("rolesByName", buildRoleNameMap(roleMap));
        log.info("Loaded {} roles", roleMap.size());
    }

    @Transactional
    private void loadPermissions(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Permission> permMap = new HashMap<>();
        for (JsonNode row : node) {
            Permission p = new Permission();
            p.setModule(row.get("module").asText());
            p.setAction(row.get("action").asText());
            p.setMoTa(row.get("moTa").asText());
            p.setNgayTao(LocalDateTime.now());
            permissionRepository.save(p);
            permMap.put(p.getModule() + "/" + p.getAction(), p);
        }
        ctx.put("permissions", permMap);
        log.info("Loaded {} permissions", permMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadRolePermissions(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Role> rolesByName = (Map<String, Role>) ctx.get("rolesByName");
        Map<String, Permission> perms = (Map<String, Permission>) ctx.get("permissions");

        for (JsonNode row : node) {
            String roleName = row.get("role_ref").asText();
            Role role = rolesByName.get(roleName);
            if (role == null) continue;

            JsonNode permRefs = row.get("permissions");
            if (permRefs != null && permRefs.isArray()) {
                for (JsonNode pref : permRefs) {
                    String key = pref.asText();
                    Permission perm = perms.get(key);
                    if (perm != null) {
                        role.getPermissions().add(perm);
                    }
                }
            } else if ("ALL".equals(row.get("scope").asText())) {
                role.getPermissions().addAll(perms.values());
            }
            roleRepository.save(role);
        }
        log.info("Loaded role_permissions");
    }

    @Transactional
    private void loadUsers(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> userMap = new HashMap<>();
        for (JsonNode row : node) {
            User u = new User();
            u.setUsername(row.get("username").asText());
            u.setEmail(row.get("email").asText());
            u.setPassword(row.get("password").asText());
            u.setHoTen(row.get("hoTen").asText());
            u.setSoDienThoai(row.get("soDienThoai").asText());
            u.setIsActive(row.get("isActive").asBoolean(true));
            u.setNgayTao(LocalDateTime.now());
            userRepository.save(u);
            userMap.put(u.getUsername(), u);
        }
        ctx.put("users", userMap);
        log.info("Loaded {} users", userMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadUserRoles(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, Role> rolesByName = (Map<String, Role>) ctx.get("rolesByName");

        for (JsonNode row : node) {
            User user = users.get(row.get("user_ref").asText());
            Role role = rolesByName.get(row.get("role_ref").asText());
            if (user != null && role != null) {
                user.getRoles().add(role);
                userRepository.save(user);
            }
        }
        log.info("Loaded user_roles");
    }

    @Transactional
    private void loadUserAuthProviders(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        for (JsonNode row : node) {
            User user = users.get(row.get("user_ref").asText());
            if (user == null) continue;
            UserAuthProvider uap = new UserAuthProvider();
            uap.setUserId(user.getId());
            uap.setProvider(row.get("provider").asText("PASSWORD"));
            uap.setLinkedAt(LocalDateTime.now());
            userAuthProviderRepository.save(uap);
        }
        log.info("Loaded user_auth_providers");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadCategories(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Category> catMap = new HashMap<>();
        List<JsonNode> deferred = new ArrayList<>();

        for (JsonNode row : node) {
            if (row.has("parentId_ref")) {
                deferred.add(row);
                continue;
            }
            Category c = new Category();
            c.setTenDanhMuc(row.get("tenDanhMuc").asText());
            if (row.has("moTa")) c.setMoTa(row.get("moTa").asText());
            c.setThuTuHienThi(row.has("thuTuHienThi") ? row.get("thuTuHienThi").asInt() : 0);
            c.setActive(true);
            c.setNgayTao(LocalDateTime.now());
            if (row.has("imageUrl")) c.setImageUrl(row.get("imageUrl").asText());
            categoryRepository.save(c);
            catMap.put(c.getTenDanhMuc(), c);
        }

        for (JsonNode row : deferred) {
            Category c = new Category();
            c.setTenDanhMuc(row.get("tenDanhMuc").asText());
            c.setThuTuHienThi(row.has("thuTuHienThi") ? row.get("thuTuHienThi").asInt() : 0);
            c.setActive(true);
            c.setNgayTao(LocalDateTime.now());
            if (row.has("imageUrl")) c.setImageUrl(row.get("imageUrl").asText());
            String parentRef = row.get("parentId_ref").asText();
            Category parent = catMap.get(parentRef);
            if (parent != null) c.setParent(parent);
            categoryRepository.save(c);
            catMap.put(c.getTenDanhMuc(), c);
        }
        ctx.put("categories", catMap);
        log.info("Loaded {} categories", catMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadProducts(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Category> cats = (Map<String, Category>) ctx.get("categories");
        Map<String, Product> prodMap = new HashMap<>();

        for (JsonNode row : node) {
            Product p = new Product();
            p.setTenSanPham(row.get("tenSanPham").asText());
            p.setMoTa(row.get("moTa").asText());
            p.setChatLieu(row.get("chatLieu").asText());
            p.setXuatXu(row.get("xuatXu").asText());
            p.setMucDichSuDung(row.get("mucDichSuDung").asText());
            p.setTrangThaiSanPham(row.get("trangThaiSanPham").asText());
            p.setFeatured(row.get("isFeatured").asBoolean(false));
            p.setActive(true);
            p.setNgayTao(LocalDateTime.now());
            if (row.has("leadTimeDays")) p.setLeadTimeDays(row.get("leadTimeDays").asInt());
            String catRef = row.get("danhMuc_ref").asText();
            Category cat = cats.get(catRef);
            if (cat != null) p.setDanhMucId(cat.getId());
            productRepository.save(p);
            prodMap.put(p.getTenSanPham(), p);
        }
        ctx.put("products", prodMap);
        log.info("Loaded {} products", prodMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadProductVariants(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Product> products = (Map<String, Product>) ctx.get("products");
        Map<String, ProductVariant> varMap = new HashMap<>();

        for (JsonNode row : node) {
            ProductVariant v = new ProductVariant();
            v.setTenBienThe(row.get("tenBienThe").asText());
            v.setGiaGoc(new BigDecimal(row.get("giaGoc").asText()));
            if (row.has("giaKhuyenMai") && !row.get("giaKhuyenMai").isNull())
                v.setGiaKhuyenMai(new BigDecimal(row.get("giaKhuyenMai").asText()));
            if (row.has("dungTich")) v.setDungTich(row.get("dungTich").asInt());
            v.setSoLuongTon(row.get("soLuongTon").asInt());
            if (row.has("hinhAnh")) v.setHinhAnh(row.get("hinhAnh").asText());
            v.setDefault(row.get("isDefault").asBoolean(false));
            v.setActive(true);
            v.setVersion(0);
            v.setLowStockThreshold(20);

            String prodRef = row.get("product_ref").asText();
            Product prod = products.get(prodRef);
            if (prod != null) v.setProduct(prod);
            productVariantRepository.save(v);
            varMap.put(v.getTenBienThe() + "_" + prodRef, v);
        }
        ctx.put("variants", varMap);
        log.info("Loaded {} product variants", varMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadProductImages(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Product> products = (Map<String, Product>) ctx.get("products");
        for (JsonNode row : node) {
            ProductImage img = new ProductImage();
            img.setImageUrl(row.get("imageUrl").asText());
            img.setSortOrder(row.has("sortOrder") ? row.get("sortOrder").asInt() : 0);
            img.setActive(true);
            img.setCreatedAt(LocalDateTime.now());
            String prodRef = row.get("product_ref").asText();
            Product prod = products.get(prodRef);
            if (prod != null) img.setProductId(prod.getId());
            productImageRepository.save(img);
        }
        log.info("Loaded product images");
    }

    @Transactional
    private void loadPromotions(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Promotion> promoMap = new HashMap<>();
        for (JsonNode row : node) {
            Promotion p = new Promotion();
            p.setMaCode(row.get("maCode").asText());
            p.setTenChuongTrinh(row.get("tenChuongTrinh").asText());
            p.setLoaiGiam(row.get("loaiGiam").asText());
            p.setGiaTriGiam(new BigDecimal(row.get("giaTriGiam").asText()));
            p.setDonHangToiThieu(new BigDecimal(row.get("donHangToiThieu").asText()));
            if (row.has("giamToiDa") && !row.get("giamToiDa").isNull())
                p.setGiamToiDa(new BigDecimal(row.get("giamToiDa").asText()));
            if (row.has("soLanDung")) p.setSoLanDung(row.get("soLanDung").asInt());
            p.setTuNgay(LocalDateTime.parse(row.get("tuNgay").asText() + "T00:00:00"));
            p.setDenNgay(LocalDateTime.parse(row.get("denNgay").asText() + "T23:59:59"));
            p.setIsActive(row.get("isActive").asBoolean(true));
            if (row.has("targetType")) p.setTargetType(row.get("targetType").asText());
            if (row.has("targetIds")) p.setTargetIds(row.get("targetIds").asText());
            p.setDaDung(0);
            p.setPriority(0);
            p.setStackable(false);
            p.setSavedCount(0);
            promotionRepository.save(p);
            promoMap.put(p.getMaCode(), p);
        }
        ctx.put("promotions", promoMap);
        log.info("Loaded {} promotions", promoMap.size());
    }

    @Transactional
    private void loadSiteSettings(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            SiteSetting s = new SiteSetting();
            s.setSettingGroup(row.get("settingGroup").asText());
            s.setSettingKey(row.get("settingKey").asText());
            s.setSettingValue(row.get("settingValue").asText());
            s.setCreatedAt(LocalDateTime.now());
            siteSettingRepository.save(s);
        }
        log.info("Loaded site settings");
    }

    @Transactional
    private void loadStoreInfo(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            StoreInfo s = new StoreInfo();
            s.setTenCuaHang(row.get("tenCuaHang").asText());
            s.setSoNha(row.get("soNha").asText());
            s.setDuong(row.get("duong").asText());
            s.setPhuongXa(row.get("phuongXa").asText());
            s.setQuanHuyen(row.get("quanHuyen").asText());
            s.setTinhThanh(row.get("tinhThanh").asText());
            s.setSoDienThoai(row.get("soDienThoai").asText());
            s.setEmail(row.get("email").asText());
            s.setIsActive(row.get("isActive").asBoolean(true));
            s.setIsDefault(row.get("isDefault").asBoolean(true));
            s.setCreatedAt(LocalDateTime.now());
            storeInfoRepository.save(s);
        }
        log.info("Loaded store_info");
    }

    @Transactional
    private void loadBanners(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            Banner b = new Banner();
            b.setTitle(row.get("title").asText());
            b.setImageUrl(row.get("image_url").asText());
            b.setLinkUrl(row.get("link_url").asText());
            b.setActive(row.get("active").asBoolean(true));
            b.setDisplayOrder(row.get("display_order").asInt());
            if (row.has("description")) b.setDescription(row.get("description").asText());
            b.setCreatedAt(LocalDateTime.now());
            b.setUpdatedAt(LocalDateTime.now());
            bannerRepository.save(b);
        }
        log.info("Loaded banners");
    }

    @Transactional
    private void loadPopupBanners(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            PopupBanner p = new PopupBanner();
            p.setActive(row.get("active").asBoolean(true));
            p.setDisplayMode(row.get("display_mode").asText());
            p.setTitle(row.get("title").asText());
            p.setImageUrl(row.get("image_url").asText());
            if (row.has("link_url")) p.setLinkUrl(row.get("link_url").asText());
            if (row.has("interval_minutes") && !row.get("interval_minutes").isNull())
                p.setIntervalMinutes(row.get("interval_minutes").asInt());
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            popupBannerRepository.save(p);
        }
        log.info("Loaded popup_banners");
    }

    @Transactional
    private void loadFooterLinks(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            FooterLink f = new FooterLink();
            f.setTitle(row.get("title").asText());
            f.setUrl(row.get("url").asText());
            f.setDisplayOrder(row.get("display_order").asInt());
            f.setIsActive(row.get("is_active").asBoolean(true));
            f.setColumnIndex(row.get("columnIndex").asInt());
            f.setCreatedAt(LocalDateTime.now());
            footerLinkRepository.save(f);
        }
        log.info("Loaded footer_links");
    }

    @Transactional
    private void loadPostCategories(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, PostCategory> pcMap = new HashMap<>();
        for (JsonNode row : node) {
            PostCategory pc = new PostCategory();
            pc.setTenDanhMuc(row.get("tenDanhMuc").asText());
            if (row.has("moTa")) pc.setMoTa(row.get("moTa").asText());
            pc.setThuTu(row.has("thuTu") ? row.get("thuTu").asInt() : 0);
            pc.setNgayTao(LocalDateTime.now());
            postCategoryRepository.save(pc);
            pcMap.put(pc.getTenDanhMuc(), pc);
        }
        ctx.put("postCategories", pcMap);
        log.info("Loaded {} post categories", pcMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadPosts(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, PostCategory> postCats = (Map<String, PostCategory>) ctx.get("postCategories");
        for (JsonNode row : node) {
            Post p = new Post();
            p.setTieuDe(row.get("tieuDe").asText());
            if (row.has("slug")) p.setSlug(row.get("slug").asText());
            p.setTrangThai(row.get("trangThai").asText());
            p.setLuotXem(row.get("luotXem").asInt(0));
            p.setFeatured(row.get("isFeatured").asBoolean(false));
            p.setNgayTao(LocalDateTime.now());
            if (row.has("danhMuc_ref") && postCats != null) {
                PostCategory pc = postCats.get(row.get("danhMuc_ref").asText());
                if (pc != null) p.setDanhMuc(pc);
            }
            postsRepository.save(p);
        }
        log.info("Loaded posts");
    }

    @Transactional
    private void loadContactMessages(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        for (JsonNode row : node) {
            ContactMessage cm = new ContactMessage();
            cm.setIsRead(row.get("is_read").asBoolean(false));
            cm.setIsSpam(row.get("is_spam").asBoolean(false));
            cm.setPhanLoai(row.get("phan_loai").asText());
            cm.setHoTen(row.get("hoTen").asText());
            cm.setEmail(row.get("email").asText());
            cm.setNoiDung(row.get("noiDung").asText());
            cm.setCreatedAt(LocalDateTime.now());
            contactMessageRepository.save(cm);
        }
        log.info("Loaded contact_messages");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadAddresses(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        for (JsonNode row : node) {
            Address a = new Address();
            a.setTenNguoiNhan(row.get("tenNguoiNhan").asText());
            a.setSoDienThoai(row.get("soDienThoai").asText());
            a.setTinhThanh(row.get("tinhThanh").asText());
            a.setQuanHuyen(row.get("quanHuyen").asText());
            a.setPhuongXa(row.get("phuongXa").asText());
            a.setDiaChiCuThe(row.get("diaChiCuThe").asText());
            a.setIsDefault(row.get("isDefault").asBoolean(false));
            String userRef = row.get("user_ref").asText();
            User user = users.get(userRef);
            if (user != null) a.setUserId(user.getId());
            addressRepository.save(a);
        }
        log.info("Loaded addresses");
    }

    @Transactional
    private void loadFlashSales(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, FlashSale> fsMap = new HashMap<>();
        for (JsonNode row : node) {
            FlashSale fs = new FlashSale();
            fs.setIsActive(row.get("isActive").asBoolean(true));
            fs.setPriority(row.has("priority") ? row.get("priority").asInt() : 0);
            fs.setTenChuongTrinh(row.get("tenChuongTrinh").asText());
            if (row.has("moTa")) fs.setMoTa(row.get("moTa").asText());
            fsMap.put(fs.getTenChuongTrinh(), fs);
        }
        ctx.put("flashSales", fsMap);
        log.info("Prepared {} flash sales", fsMap.size());
    }

    @Transactional
    private void loadFlashSaleItems(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        log.info("Prepared flash_sale_items (loaded via flash sales)");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadOrders(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, Promotion> promos = (Map<String, Promotion>) ctx.get("promotions");
        Map<String, Order> orderMap = new HashMap<>();

        for (JsonNode row : node) {
            Order o = new Order();
            o.setMaDon(row.get("maDon").asText());
            o.setSnapTenNguoiNhan(row.get("snapTenNguoiNhan").asText());
            o.setSnapSoDienThoai(row.get("snapSoDienThoai").asText());
            o.setSnapDiaChi(row.get("snapDiaChi").asText());
            o.setTienHang(new BigDecimal(row.get("tienHang").asText()));
            o.setPhiVanChuyen(new BigDecimal(row.get("phiVanChuyen").asText()));
            o.setTienGiam(new BigDecimal(row.get("tienGiam").asText()));
            o.setTongThanhToan(new BigDecimal(row.get("tongThanhToan").asText()));
            o.setPhuongThucTT(row.get("phuongThucTT").asText());
            o.setPhuongThucGiaoHang(row.has("phuongThucGiaoHang") ? row.get("phuongThucGiaoHang").asText("SHIP") : "SHIP");
            o.setTrangThaiTT(row.get("trangThaiTT").asText());
            o.setTrangThaiDon(row.get("trangThaiDon").asText());
            o.setNgayDat(LocalDateTime.now());
            String userRef = row.get("user_ref").asText();
            User user = users.get(userRef);
            if (user != null) o.setUser(user);
            if (row.has("promotion_ref") && promos != null) {
                Promotion promo = promos.get(row.get("promotion_ref").asText());
                if (promo != null) o.setPromotion(promo);
            }
            orderRepository.save(o);
            orderMap.put(o.getMaDon(), o);
        }
        ctx.put("orders", orderMap);
        log.info("Loaded {} orders", orderMap.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadOrderItems(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, Order> orders = (Map<String, Order>) ctx.get("orders");
        for (JsonNode row : node) {
            OrderItem oi = new OrderItem();
            oi.setTenSanPham(row.get("tenSanPham").asText());
            if (row.has("tenBienThe")) oi.setTenBienThe(row.get("tenBienThe").asText());
            if (row.has("hinhAnhSP")) oi.setHinhAnhSP(row.get("hinhAnhSP").asText());
            oi.setDonGia(new BigDecimal(row.get("donGia").asText()));
            oi.setSoLuong(row.get("soLuong").asInt());
            oi.setThanhTien(new BigDecimal(row.get("thanhTien").asText()));
            String orderRef = row.get("order_ref").asText();
            Order order = orders.get(orderRef);
            if (order != null) oi.setOrder(order);
            orderItemRepository.save(oi);
        }
        log.info("Loaded order_items");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadWishlists(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, Product> products = (Map<String, Product>) ctx.get("products");
        for (JsonNode row : node) {
            Wishlist w = new Wishlist();
            w.setNgayThem(LocalDateTime.now());
            User user = users.get(row.get("user_ref").asText());
            Product prod = products.get(row.get("product_ref").asText());
            if (user != null) w.setUserId(user.getId());
            if (prod != null) w.setProductId(prod.getId());
            wishlistRepository.save(w);
        }
        log.info("Loaded wishlists");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadCartItems(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, ProductVariant> variants = (Map<String, ProductVariant>) ctx.get("variants");
        for (JsonNode row : node) {
            CartItem ci = new CartItem();
            ci.setSoLuong(row.get("soLuong").asInt());
            ci.setNgayThem(LocalDateTime.now());
            User user = users.get(row.get("user_ref").asText());
            if (user != null) ci.setUser(user);
            cartItemRepository.save(ci);
        }
        log.info("Loaded cart_items");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadReviews(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, Product> products = (Map<String, Product>) ctx.get("products");
        for (JsonNode row : node) {
            Review r = new Review();
            r.setDanhGia(row.get("danhGia").asInt());
            if (row.has("binhLuan")) r.setBinhLuan(row.get("binhLuan").asText());
            r.setIsApproved(row.get("isApproved").asBoolean(false));
            r.setNgayTao(LocalDateTime.now());
            User user = users.get(row.get("user_ref").asText());
            Product prod = products.get(row.get("product_ref").asText());
            if (user != null) r.setUserId(user.getId());
            if (prod != null) r.setProductId(prod.getId());
            reviewsRepository.save(r);
        }
        log.info("Loaded reviews");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadUserVouchers(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        Map<String, Promotion> promos = (Map<String, Promotion>) ctx.get("promotions");
        for (JsonNode row : node) {
            UserVoucher uv = new UserVoucher();
            uv.setRemainingUses(row.has("remainingUses") ? row.get("remainingUses").asInt() : 1);
            uv.setSavedAt(LocalDateTime.now());
            String statusText = row.has("status") ? row.get("status").asText("AVAILABLE") : "AVAILABLE";
            uv.setStatus(VoucherStatus.valueOf(statusText));
            if (row.has("voucherCode")) uv.setVoucherCode(row.get("voucherCode").asText());
            User user = users.get(row.get("user_ref").asText());
            Promotion promo = promos.get(row.get("promotion_ref").asText());
            if (user != null) uv.setUserId(user.getId());
            if (promo != null) uv.setPromotion(promo);
            userVoucherRepository.save(uv);
        }
        log.info("Loaded user_vouchers");
    }

    @SuppressWarnings("unchecked")
    @Transactional
    private void loadNotifications(JsonNode node, Map<String, Object> ctx) {
        if (node == null) return;
        Map<String, User> users = (Map<String, User>) ctx.get("users");
        for (JsonNode row : node) {
            Notification n = new Notification();
            n.setIsActive(row.get("isActive").asBoolean(true));
            n.setContent(row.get("content").asText());
            n.setLinkType(row.has("linkType") ? row.get("linkType").asText() : null);
            n.setLinkUrl(row.has("linkUrl") ? row.get("linkUrl").asText() : null);
            n.setCreatedAt(LocalDateTime.now());
            if (row.has("user_ref")) {
                User user = users.get(row.get("user_ref").asText());
                if (user != null) n.setUserId(user.getId());
            }
            notificationRepository.save(n);
        }
        log.info("Loaded notifications");
    }

    private Map<String, Role> buildRoleNameMap(Map<Integer, Role> roleMap) {
        Map<String, Role> map = new HashMap<>();
        for (Role r : roleMap.values()) {
            map.put(r.getName(), r);
        }
        return map;
    }
}
