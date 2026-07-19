package com.duastore.service.admin;

import com.duastore.model.CartItem;
import com.duastore.model.Order;
import com.duastore.model.Product;
import com.duastore.model.User;
import com.duastore.repository.*;
import com.duastore.service.EmailService;
import com.duastore.service.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final NotificationHelper notificationHelper;
    private final EmailService emailService;

    @Value("${app.url}")
    private String appUrl;

    public AlertService(ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            NotificationHelper notificationHelper,
            EmailService emailService) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.notificationHelper = notificationHelper;
        this.emailService = emailService;
    }

    @Transactional
    public void checkLowStock(int threshold) {
        List<Object[]> lowStockProducts = variantRepository.findLowStockProductIds(threshold);
        if (lowStockProducts.isEmpty()) return;
        List<Integer> productIds = lowStockProducts.stream()
                .map(row -> (Integer) row[0])
                .collect(Collectors.toList());
        List<Product> products = productRepository.findAllById(productIds);
        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (Object[] row : lowStockProducts) {
            Integer pid = (Integer) row[0];
            Integer totalStock = ((Number) row[1]).intValue();
            Product p = productMap.get(pid);
            if (p == null) continue;
            notificationHelper.notifyStaff(
                    "⚠️ Sản phẩm \"" + p.getTenSanPham() + "\" sắp hết hàng (còn " + totalStock + ")",
                    "PRODUCT", p.getId(), "/admin/san-pham/sua/" + p.getId(), "Xem sản phẩm");
        }
    }

    @Transactional
    public void checkUrgentOrders(int hours) {
        LocalDateTime before = LocalDateTime.now().minusHours(hours);
        long count = orderRepository.countByTrangThaiDonAndNgayDatBefore("CHO_XAC_NHAN", before);
        if (count > 0) {
            notificationHelper.notifyStaff(
                    "⚠️ Có " + count + " đơn hàng chờ xác nhận quá " + hours + " giờ!",
                    "ORDER", null, "/admin/don-hang?trangThai=CHO_XAC_NHAN", "Xem đơn hàng");
        }
    }

    public int countAbandonedCarts(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<CartItem> oldItems = cartItemRepository.findOldItems(cutoff);
        Set<Integer> userIds = oldItems.stream()
                .map(CartItem::getUserId)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return 0;
        List<User> users = userRepository.findAllById(userIds);
        int count = 0;
        for (User u : users) {
            List<Order> userOrders = orderRepository.findAllByUserId(u.getId());
            boolean hasRecentOrder = userOrders.stream()
                    .anyMatch(o -> o.getNgayDat().isAfter(cutoff));
            if (!hasRecentOrder) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public void sendAbandonedCartReminders(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<CartItem> oldItems = cartItemRepository.findOldItems(cutoff);
        Set<Integer> userIds = oldItems.stream()
                .map(CartItem::getUserId)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        List<User> users = userRepository.findAllById(userIds);
        for (User u : users) {
            if (u.getEmail() == null || u.getEmail().isBlank()) continue;
            List<Order> userOrders = orderRepository.findAllByUserId(u.getId());
            boolean hasRecentOrder = userOrders.stream()
                    .anyMatch(o -> o.getNgayDat().isAfter(cutoff));
            if (hasRecentOrder) continue;
            List<CartItem> items = cartItemRepository.findByUserIdOrderByNgayThemDesc(u.getId());
            if (items.isEmpty()) continue;
            String itemSummary = items.stream()
                    .map(i -> i.getProduct() != null ? i.getProduct().getTenSanPham() : "Sản phẩm #" + i.getProductId())
                    .collect(Collectors.joining(", "));
            emailService.send(u.getEmail(),
                    "🛒 Bạn còn sản phẩm trong giỏ hàng - DuaStore",
                    "<html><body style='font-family:Arial;padding:20px;'>"
                    + "<h3 style='color:#e53935;'>Bạn còn sản phẩm trong giỏ hàng!</h3>"
                    + "<p>Xin chào <strong>" + u.getHoTen() + "</strong>,</p>"
                    + "<p>Chúng tôi thấy bạn vẫn còn những sản phẩm sau trong giỏ hàng:</p>"
                    + "<p style='background:#f5f5f5;padding:12px;border-radius:8px;'>" + itemSummary + "</p>"
                    + "<p><a href='" + appUrl + "/gio-hang' "
                    + "style='background:#e53935;color:#fff;padding:10px 24px;border-radius:8px;text-decoration:none;'>"
                    + "Đến giỏ hàng</a></p>"
                    + "<p style='color:#9e9e9e;font-size:12px;'>DuaStore - Đồ Thủy Tinh Cao Cấp</p>"
                    + "</body></html>");
            notificationHelper.notifyAll(
                    "🛒 Bạn còn sản phẩm trong giỏ hàng! Quay lại để hoàn tất đặt hàng.",
                    "CART", null, "/gio-hang", "Đến giỏ hàng",
                    u.getId());
        }
    }
}
