package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý phát hiện gian lận.
 */
public class FraudDetectionService {

    private static final int UNUSUAL_QTY_THRESHOLD = 50;
    private static final int MAX_CANCELLATIONS = 3;
    private static final int CANCEL_WINDOW_HOURS = 24;

    private final OrderRepository orderRepository;

    public FraudDetectionService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String analyzeOrder(Order order) {
        List<String> warnings = new ArrayList<>();

        String fakeAddr = checkFakeAddress(order);
        if (fakeAddr != null) warnings.add(fakeAddr);

        String unusualQty = checkUnusualQuantity(order.getOrderItems());
        if (unusualQty != null) warnings.add(unusualQty);

        String cancelFlood = checkFrequentCancellations(order);
        if (cancelFlood != null) warnings.add(cancelFlood);

        return warnings.isEmpty() ? null : String.join(";;", warnings);
    }

    @Transactional
    public void analyzeAndPersist(Order order) {
        String warnings = analyzeOrder(order);
        orderRepository.setFraudWarning(order.getId(), warnings);
    }

    private String checkFakeAddress(Order order) {
        String address = order.getSnapDiaChi();
        String name = order.getSnapTenNguoiNhan();
        String phone = order.getSnapSoDienThoai();

        if (address == null || address.isBlank()) return "Địa chỉ giao hàng trống";
        if (name == null || name.isBlank()) return "Tên người nhận trống";
        if (phone == null || phone.isBlank() || phone.replaceAll("\\D", "").length() < 10)
            return "Số điện thoại không hợp lệ";
        if (address.length() < 15) return "Địa chỉ quá ngắn, có thể là địa chỉ ảo";

        boolean hasLocationKeyword = address.matches(".*(?i)(phường|xã|quận|huyện|tỉnh|thành phố|thị xã|thị trấn|đường|phố|thôn|ấp|khu phố|tổ dân phố|số |ngách|ngõ|xa|thi tran|khu pho).*");
        boolean hasStructuredParts = address.split("[,，]").length >= 3;
        if (!hasLocationKeyword && !hasStructuredParts) return "Địa chỉ không chứa thông tin hành chính (phường/xã/quận/huyện)";

        String clean = address.replaceAll("[\\s,.\"'\\-]", "").toLowerCase();
        if (clean.matches("(.)\\1{8,}")) return "Địa chỉ chứa ký tự lặp bất thường";
        if (clean.matches("\\d{10,}")) return "Địa chỉ chỉ gồm số, có thể là địa chỉ ảo";
        if (clean.matches(".*(test|fake|ao|gia|haha|abc|xxx).*")) return "Địa chỉ chứa từ khóa đáng ngờ";

        return null;
    }

    private String checkUnusualQuantity(List<OrderItem> items) {
        if (items == null) return null;
        for (OrderItem item : items) {
            if (item.getSoLuong() != null && item.getSoLuong() > UNUSUAL_QTY_THRESHOLD) {
                String name = item.getTenSanPham() != null ? item.getTenSanPham() : "sản phẩm";
                return "Số lượng '" + name + "' bất thường (x" + item.getSoLuong() + ")";
            }
        }
        return null;
    }

    private String checkFrequentCancellations(Order order) {
        String phone = order.getSnapSoDienThoai();
        if (phone == null || phone.isBlank()) return null;

        LocalDateTime since = LocalDateTime.now().minusHours(CANCEL_WINDOW_HOURS);
        long cancelledCount = orderRepository.countCancelledByPhoneSince(phone, since);

        if (cancelledCount >= MAX_CANCELLATIONS) {
            return "SĐT " + phone + " đã hủy " + cancelledCount + " đơn trong " + CANCEL_WINDOW_HOURS + "h qua";
        }
        return null;
    }
}
