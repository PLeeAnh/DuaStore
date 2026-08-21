package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import com.duastore.service.SiteSettingService;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "com.duastore.controller.client")
/**
 * phía khách hàng (client) — Lớp @ControllerAdvice bổ sung dữ liệu/logic dùng chung cho các Controller liên quan tới client navbar advice.
 */
public class ClientNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(ClientNavbarAdvice.class);

    // Mặc định thanh menu + footer luôn bật hết khi admin chưa cấu hình.
    // Giá trị lưu trong DB (group "appearance") sẽ ghi đè các giá trị mặc định này.
    private static final Map<String, String> THEME_DEFAULTS = new LinkedHashMap<>();

    static {
        THEME_DEFAULTS.put("menu_1_label", "Trang chủ");
        THEME_DEFAULTS.put("menu_1_url", "/");
        THEME_DEFAULTS.put("menu_1_active", "1");
        THEME_DEFAULTS.put("menu_2_label", "Sản phẩm");
        THEME_DEFAULTS.put("menu_2_url", "/san-pham");
        THEME_DEFAULTS.put("menu_2_active", "1");
        THEME_DEFAULTS.put("menu_3_label", "Khuyến mãi");
        THEME_DEFAULTS.put("menu_3_url", "/khuyen-mai");
        THEME_DEFAULTS.put("menu_3_active", "1");
        THEME_DEFAULTS.put("menu_4_label", "Bài viết");
        THEME_DEFAULTS.put("menu_4_url", "/bai-viet");
        THEME_DEFAULTS.put("menu_4_active", "1");
        THEME_DEFAULTS.put("menu_5_label", "Liên hệ");
        THEME_DEFAULTS.put("menu_5_url", "/lien-he");
        THEME_DEFAULTS.put("menu_5_active", "1");
        THEME_DEFAULTS.put("footer_col_1_title", "Về chúng tôi");
        THEME_DEFAULTS.put("footer_col_1_content", "Giới thiệu về cửa hàng\nLiên hệ | /lien-he");
        THEME_DEFAULTS.put("footer_col_2_title", "Chính sách");
        THEME_DEFAULTS.put("footer_col_2_content", "Chính sách đổi trả\nChính sách bảo mật\nChính sách vận chuyển");
        THEME_DEFAULTS.put("footer_col_3_title", "Liên hệ");
        THEME_DEFAULTS.put("footer_col_3_content", "Khuyến mãi | /khuyen-mai\nSản phẩm | /san-pham\nLiên hệ | /lien-he");
        THEME_DEFAULTS.put("footer_copyright", "© 2026 DuaStore. Tất cả quyền được bảo lưu.");
        THEME_DEFAULTS.put("footer_facebook", "https://www.facebook.com/duastore.vn");
        THEME_DEFAULTS.put("footer_instagram", "https://www.instagram.com/duastore.vn");
        THEME_DEFAULTS.put("footer_tiktok", "https://www.tiktok.com/@duastore.vn");
        THEME_DEFAULTS.put("footer_youtube", "https://www.youtube.com/@duastore");
        THEME_DEFAULTS.put("widget_zalo", "1");
        THEME_DEFAULTS.put("widget_zalo_phone", "0983595240");
        THEME_DEFAULTS.put("widget_messenger", "1");
        THEME_DEFAULTS.put("widget_messenger_page_id", "duastore.vn");
        THEME_DEFAULTS.put("widget_call", "1");
        THEME_DEFAULTS.put("widget_call_phone", "0983595240");
    }

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;
    private final NotificationRepository notificationRepository;
    private final SiteSettingService siteSettingService;

    public ClientNavbarAdvice(CartService cartService, WishlistService wishlistService,
            SecurityUtil securityUtil,
            NotificationRepository notificationRepository,
            SiteSettingService siteSettingService) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
        this.notificationRepository = notificationRepository;
        this.siteSettingService = siteSettingService;
    }

    @SuppressWarnings("unchecked")
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        Integer userId = securityUtil.getCurrentUserId();

        if (userId != null) {
            model.addAttribute("myCart", cartService.getItems(userId));
            model.addAttribute("cartCount", cartService.count(userId));
            model.addAttribute("myWishlist", wishlistService.getWishlistByUser(userId));
            model.addAttribute("likedIds", wishlistService.getLikedProductIds(userId));

            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("notifReadIds");
            final Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : java.util.Collections.emptySet();
            List<Notification> allNotifs = notificationRepository.findCustomerNotifications(userId);

            List<Notification> unread = allNotifs.stream()
                    .filter(n -> !readIds.contains(n.getId()))
                    .toList();
            model.addAttribute("recentNotifs", unread);
            model.addAttribute("notifCount", (long) unread.size());
        } else {
            // Guest user: load cart from session
            String GUEST_CART_SESSION_KEY = "guestCart";
            Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute(GUEST_CART_SESSION_KEY);
            if (guestCart == null) {
                guestCart = new HashMap<>();
            }
            List<com.duastore.dto.CartItemDTO> guestItems = guestCart.entrySet().stream()
                    .map(entry -> {
                        com.duastore.dto.CartItemDTO dto = new com.duastore.dto.CartItemDTO();
                        dto.setVariantId(entry.getKey());
                        dto.setSoLuong(entry.getValue());
                        return dto;
                    })
                    .collect(Collectors.toList());
            model.addAttribute("myCart", guestItems);
            model.addAttribute("cartCount", guestCart.size());
            model.addAttribute("myWishlist", java.util.List.of());
            model.addAttribute("likedIds", java.util.List.of());
            model.addAttribute("recentNotifs", java.util.List.of());
            model.addAttribute("notifCount", 0L);
        }

        // Inject appearance settings
        model.addAttribute("themeSettings", new java.util.HashMap<>());
        model.addAttribute("customCss", "");
        model.addAttribute("storeSettings", new java.util.HashMap<>());
        try {
            Map<String, String> themeSettings = new HashMap<>(THEME_DEFAULTS);
            themeSettings.putAll(siteSettingService.getGroup("appearance"));
            model.addAttribute("themeSettings", themeSettings);
            model.addAttribute("customCss", siteSettingService.getValue("custom_css", ""));
            Map<String, String> storeSettings = new HashMap<>(SiteSettingService.STORE_DEFAULTS);
            storeSettings.putAll(siteSettingService.getGroup("store"));
            model.addAttribute("storeSettings", storeSettings);
        } catch (Exception e) {
            log.warn("Loi load appearance settings: {}", e.getMessage());
        }

        // Tách sẵn nội dung footer thành danh sách dòng ở Java (không dùng regex trong Thymeleaf,
        // tránh lỗi split làm hỏng chữ n/r). Hỗ trợ cả newline thật lẫn chuỗi literal "\n".
        Map<Integer, List<String>> footerCols = new HashMap<>();
        try {
            Map<String, String> themeSettings = (Map<String, String>) model.getAttribute("themeSettings");
            if (themeSettings != null) {
                for (int i = 1; i <= 6; i++) {
                    String content = themeSettings.get("footer_col_" + i + "_content");
                    if (content == null || content.isEmpty()) {
                        continue;
                    }
                    List<String> lines = new ArrayList<>();
                    for (String raw : content.split("\\\\n|\\r\\n|\\r|\\n")) {
                        String line = raw.trim();
                        if (!line.isEmpty()) {
                            lines.add(line);
                        }
                    }
                    if (!lines.isEmpty()) {
                        footerCols.put(i, lines);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Loi tach footer content: {}", e.getMessage());
        }
        model.addAttribute("footerCols", footerCols);
    }

}
