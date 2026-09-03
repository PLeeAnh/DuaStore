package com.duastore.service.admin;

import com.duastore.repository.OrderRepository;
import com.duastore.repository.UserRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ContactMessageRepository;
import com.duastore.util.PriceUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý trợ lý AI.
 */
public class AdminCopilotService {

    private final AdminAnalyticsService analyticsService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final AdminReviewService reviewService;
    private final ContactMessageRepository contactMessageRepository;

    public AdminCopilotService(AdminAnalyticsService analyticsService,
                                OrderRepository orderRepository,
                                UserRepository userRepository,
                                ProductVariantRepository variantRepository,
                                ProductRepository productRepository,
                                AdminReviewService reviewService,
                                ContactMessageRepository contactMessageRepository) {
        this.analyticsService = analyticsService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.reviewService = reviewService;
        this.contactMessageRepository = contactMessageRepository;
    }

    public Map<String, Object> answer(String query) {
        String q = query.toLowerCase().trim();

        if (q.contains("so sanh") || q.contains("so sánh") || q.contains("so với")) {
            return handleComparison(q);
        }
        if (q.contains("top") || q.contains("bán chạy") || q.contains("ban chay")) {
            return handleTopProducts(q);
        }
        if (q.contains("tồn kho") || q.contains("tồn kho") || q.contains("hết hàng") || q.contains("het hang") || q.contains("sap het")) {
            return handleLowStock();
        }
        if (q.contains("khách hàng mới") || q.contains("khach hang moi") || q.contains("khách mới")) {
            return handleNewCustomers(q);
        }
        if (q.contains("tổng sản phẩm") || q.contains("tong san pham")) {
            return singleAnswer("Tổng sản phẩm trong hệ thống: **" + productRepository.count() + "** sản phẩm.");
        }
        if (q.contains("tổng khách hàng") || q.contains("tong khach hang")) {
            return singleAnswer("Tổng khách hàng trong hệ thống: **" + userRepository.count() + "** khách hàng.");
        }
        if (q.contains("tỷ lệ hoàn thành") || q.contains("ti le hoan thanh") || q.contains("completion")) {
            return handleCompletionRate(q);
        }
        if (q.contains("giảm giá") || q.contains("giam gia") || q.contains("discount")) {
            return handleDiscount(q);
        }
        if (q.contains("đơn cod") || q.contains("don cod") || q.contains("đơn online") || q.contains("don online")) {
            return handlePaymentMethod(q);
        }
        if (q.contains("doanh thu") || q.contains("revenue")) {
            return handleRevenue(q);
        }
        if (q.contains("chờ xác nhận") || q.contains("cho xac nhan") || q.contains("đơn chờ") || q.contains("don cho")) {
            return handlePendingOrders();
        }
        if (q.contains("đánh giá") || q.contains("danh gia") || q.contains("review")) {
            return handleReviews();
        }
        if (q.contains("tin nhắn") || q.contains("tin nhan") || q.contains("liên hệ") || q.contains("lien he")) {
            return handleContactMessages();
        }
        if (q.contains("đơn hàng") || q.contains("don hang") || q.contains("order")) {
            return handleOrders(q);
        }

        return singleAnswer("Xin lỗi, tôi chưa hiểu câu hỏi. Bạn có thể thử:\n" +
                "• **Doanh thu** hôm nay / tuần này / tháng này\n" +
                "• **So sánh** doanh thu tuần này với tuần trước\n" +
                "• **Đơn hàng** tháng này\n" +
                "• **Đơn chờ xác nhận**\n" +
                "• **Top** sản phẩm bán chạy\n" +
                "• **Tồn kho** thấp\n" +
                "• **Khách hàng mới** tháng này\n" +
                "• **Tỷ lệ hoàn thành**\n" +
                "• **Đánh giá** chưa trả lời\n" +
                "• **Tin nhắn** chưa đọc");
    }

    // ===== Query handlers =====

    private Map<String, Object> handleRevenue(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        String rev = analyticsService.getTotalRevenue(p.from, p.to);
        long orders = getCompletedOrders(p.from, p.to);
        String avg = orders > 0 ? analyticsService.getAvgOrderValue(p.from, p.to) : "0₫";
        StringBuilder sb = new StringBuilder("Doanh thu " + p.label + ": **" + rev + "**");
        sb.append("\n• Đơn thành công: " + orders + " đơn");
        sb.append("\n• Giá trị đơn TB: " + avg);
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleOrders(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        long total = analyticsService.getTotalOrders(p.from, p.to);
        long completed = analyticsService.getCompletedOrders(p.from, p.to);
        long cancelled = analyticsService.getCancelledOrders(p.from, p.to);
        String rate = analyticsService.getCompletionRate(p.from, p.to);
        StringBuilder sb = new StringBuilder("Đơn hàng " + p.label + ": **" + total + " đơn**");
        sb.append("\n• Thành công: " + completed + " đơn");
        sb.append("\n• Đã huỷ: " + cancelled + " đơn");
        sb.append("\n• Tỷ lệ hoàn thành: " + rate);
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleComparison(String q) {
        String[] periods = extractTwoPeriods(q);
        if (periods == null) {
            return singleAnswer("Vui lòng nói rõ hai mốc thời gian, ví dụ: **so sánh doanh thu tuần này với tuần trước**");
        }
        PeriodResult p1 = parsePeriodRaw(periods[0]);
        PeriodResult p2 = parsePeriodRaw(periods[1]);
        if (p1 == null || p2 == null) {
            return singleAnswer("Không xác định được thời gian. Hãy thử: so sánh doanh thu tuần này với tuần trước");
        }

        boolean isRevenue = q.contains("doanh thu") || q.contains("revenue");
        boolean isOrder = q.contains("đơn") || q.contains("don") || q.contains("order");

        StringBuilder sb = new StringBuilder();
        if (isRevenue) {
            BigDecimal v1 = getRevenueRaw(p1.from, p1.to);
            BigDecimal v2 = getRevenueRaw(p2.from, p2.to);
            sb.append("**So sánh doanh thu:**\n");
            sb.append("• ").append(p1.label).append(": **").append(PriceUtils.format(v1)).append("**\n");
            sb.append("• ").append(p2.label).append(": **").append(PriceUtils.format(v2)).append("**\n");
            sb.append("➡️ ").append(formatChangePct(v1, v2, true));
        } else if (isOrder) {
            long v1 = analyticsService.getTotalOrders(p1.from, p1.to);
            long v2 = analyticsService.getTotalOrders(p2.from, p2.to);
            sb.append("**So sánh đơn hàng:**\n");
            sb.append("• ").append(p1.label).append(": **").append(v1).append(" đơn**\n");
            sb.append("• ").append(p2.label).append(": **").append(v2).append(" đơn**\n");
            sb.append("➡️ ").append(formatChangePct((double) v1, (double) v2));
        } else {
            return handleRevenue(q);
        }
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleTopProducts(String q) {
        int limit = 5;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("top\\s*(\\d+)").matcher(q);
        if (m.find()) limit = Math.min(Integer.parseInt(m.group(1)), 10);
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().minusDays(30), LocalDate.now(), "30 ngày qua");

        List<Map<String, Object>> products = analyticsService.getTopSellingProducts(p.from, p.to);
        if (products.isEmpty()) {
            return singleAnswer("Chưa có dữ liệu sản phẩm bán chạy trong " + p.label + ".");
        }
        StringBuilder sb = new StringBuilder("**Top " + Math.min(limit, products.size()) + " sản phẩm bán chạy " + p.label + ":**\n");
        for (int i = 0; i < Math.min(limit, products.size()); i++) {
            Map<String, Object> pr = products.get(i);
            sb.append((i + 1) + ". **" + pr.get("tenSanPham") + "** — "
                    + pr.get("totalSold") + " cái, "
                    + PriceUtils.format((BigDecimal) pr.get("revenue")) + "\n");
        }
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleLowStock() {
        long count = analyticsService.getLowStockProducts();
        String answer = "**Tồn kho thấp:** " + count + " biến thể sắp hết hàng (tồn < 5).";
        if (count > 0) {
            answer += "\nKiểm tra tại tab **Sản phẩm** để xem chi tiết.";
        }
        return singleAnswer(answer);
    }

    private Map<String, Object> handleNewCustomers(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        long newCust = analyticsService.getNewCustomers(p.from, p.to);
        long total = analyticsService.getTotalCustomers();
        String avg = analyticsService.getAvgRevenuePerCustomer(p.from, p.to);
        StringBuilder sb = new StringBuilder("Khách hàng mới " + p.label + ": **" + newCust + "**");
        sb.append("\nTổng khách hàng: **" + total + "**");
        if (total > 0) {
            sb.append("\nChiếm " + String.format("%.1f", newCust * 100.0 / total) + "% tổng số.");
        }
        sb.append("\nDoanh thu TB/khách mới: " + avg);
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleCompletionRate(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        long total = analyticsService.getTotalOrders(p.from, p.to);
        if (total == 0) return singleAnswer("Chưa có đơn hàng trong " + p.label + ".");
        String rate = analyticsService.getCompletionRate(p.from, p.to);
        long completed = analyticsService.getCompletedOrders(p.from, p.to);
        long cancelled = analyticsService.getCancelledOrders(p.from, p.to);
        StringBuilder sb = new StringBuilder("Tỷ lệ hoàn thành " + p.label + ": **" + rate + "**");
        sb.append("\n• Tổng đơn: " + total);
        sb.append("\n• Thành công: " + completed);
        sb.append("\n• Đã huỷ: " + cancelled);
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleDiscount(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        String discount = analyticsService.getTotalDiscountGiven(p.from, p.to);
        return singleAnswer("Tổng giảm giá đã cấp " + p.label + ": **" + discount + "**");
    }

    private Map<String, Object> handlePendingOrders() {
        long pending = orderRepository.countByTrangThaiDon("CHO_XAC_NHAN");
        StringBuilder sb = new StringBuilder("**Đơn chờ xác nhận:** " + pending + " đơn");
        if (pending > 0) {
            sb.append("\nVào tab **Đơn hàng** để xử lý ngay, tránh khách chờ lâu.");
        } else {
            sb.append("\nKhông có đơn nào đang chờ xác nhận.");
        }
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleReviews() {
        long unanswered = reviewService.countUnanswered();
        long lowRating = reviewService.countLowRating();
        double avg = reviewService.getOverallAverageRating();
        StringBuilder sb = new StringBuilder("**Đánh giá sản phẩm:**\n");
        sb.append("• Điểm trung bình: " + String.format("%.1f", avg) + "★\n");
        sb.append("• Chưa trả lời: " + unanswered + " đánh giá\n");
        sb.append("• 1–2★ cần xử lý: " + lowRating + " đánh giá");
        if (unanswered > 0) {
            sb.append("\nVào tab **Đánh giá** để trả lời khách sớm.");
        }
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handleContactMessages() {
        long open = contactMessageRepository.countByIsResolvedFalseAndIsSpamFalse();
        long unread = contactMessageRepository.countByIsRead(false);
        StringBuilder sb = new StringBuilder("**Tin nhắn liên hệ:**\n");
        sb.append("• Đang mở: " + open + " tin nhắn\n");
        sb.append("• Chưa đọc: " + unread + " tin nhắn");
        if (unread > 0) {
            sb.append("\nVào tab **Tin nhắn liên hệ** để phản hồi khách.");
        }
        return singleAnswer(sb.toString());
    }

    private Map<String, Object> handlePaymentMethod(String q) {
        PeriodResult p = parsePeriod(q);
        if (p == null) p = new PeriodResult(LocalDate.now().withDayOfMonth(1), LocalDate.now(), "tháng này");
        long cod = analyticsService.getPaymentCount("COD", p.from, p.to);
        long online = analyticsService.getOnlineOrderCount(p.from, p.to);
        StringBuilder sb = new StringBuilder("**Phương thức thanh toán " + p.label + ":**\n");
        sb.append("• COD: " + cod + " đơn\n");
        sb.append("• Online: " + online + " đơn");
        return singleAnswer(sb.toString());
    }

    // ===== Period parsing =====

    private PeriodResult parsePeriod(String q) {
        if (q.contains("hôm nay") || q.contains("hom nay")) {
            LocalDate d = LocalDate.now();
            return new PeriodResult(d, d, "hôm nay");
        }
        if (q.contains("hôm qua") || q.contains("hom qua")) {
            LocalDate d = LocalDate.now().minusDays(1);
            return new PeriodResult(d, d, "hôm qua");
        }
        if (q.contains("7 ngày") || q.contains("7 ngay") || q.contains("7 qua")) {
            LocalDate to = LocalDate.now();
            return new PeriodResult(to.minusDays(6), to, "7 ngày qua");
        }
        if (q.contains("tuần trước") || q.contains("tuan truoc")) {
            LocalDate today = LocalDate.now();
            LocalDate sun = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDate end = sun.minusWeeks(1);
            LocalDate start = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new PeriodResult(start, end, "tuần trước");
        }
        if (q.contains("tuần này") || q.contains("tuan nay")) {
            LocalDate today = LocalDate.now();
            LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new PeriodResult(start, today, "tuần này");
        }
        if (q.contains("tháng trước") || q.contains("thang truoc")) {
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusMonths(1).withDayOfMonth(1);
            LocalDate end = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
            return new PeriodResult(start, end, "tháng trước");
        }
        if (q.contains("tháng này") || q.contains("thang nay")) {
            LocalDate today = LocalDate.now();
            return new PeriodResult(today.withDayOfMonth(1), today, "tháng này");
        }
        if (q.contains("quý này") || q.contains("quy nay")) {
            LocalDate today = LocalDate.now();
            int quarter = (today.getMonthValue() - 1) / 3;
            return new PeriodResult(LocalDate.of(today.getYear(), quarter * 3 + 1, 1), today, "quý này");
        }
        if (q.contains("năm nay") || q.contains("nam nay")) {
            LocalDate today = LocalDate.now();
            return new PeriodResult(LocalDate.of(today.getYear(), 1, 1), today, "năm nay");
        }
        return null;
    }

    private PeriodResult parsePeriodRaw(String raw) { return parsePeriod(raw); }

    private String[] extractTwoPeriods(String q) {
        q = q.toLowerCase().replaceAll("so sánh|so sanh|so với|so voi", "").trim();
        String[][] pairs = {{" với ", " voi "}, {" vs "}, {" và ", " va "}};
        for (String[] seps : pairs) {
            for (String sep : seps) {
                int idx = q.indexOf(sep);
                if (idx > 0) {
                    String first = q.substring(0, idx).trim();
                    String second = q.substring(idx + sep.length()).trim();
                    return extractPeriodPhrase(first, second);
                }
            }
        }
        return null;
    }

    private String[] extractPeriodPhrase(String first, String second) {
        String[] keywords = {"hôm nay", "hôm qua", "hom nay", "hom qua",
                "tuần này", "tuần trước", "tuan nay", "tuan truoc",
                "tháng này", "tháng trước", "thang nay", "thang truoc",
                "năm nay", "nam nay", "7 ngày", "7 ngay", "quý này", "quy nay"};
        String p1 = null, p2 = null;
        for (String kw : keywords) {
            if (first.contains(kw)) p1 = kw;
            if (second.contains(kw)) p2 = kw;
        }
        return (p1 != null && p2 != null) ? new String[]{p1, p2} : null;
    }

    // ===== Data helpers =====

    private BigDecimal getRevenueRaw(LocalDate from, LocalDate to) {
        BigDecimal total = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(
                from.atStartOfDay(), to.atTime(LocalTime.MAX));
        return total != null ? total : BigDecimal.ZERO;
    }

    private long getCompletedOrders(LocalDate from, LocalDate to) {
        return analyticsService.getCompletedOrders(from, to);
    }

    // ===== Format helpers =====

    private String formatChangePct(BigDecimal newVal, BigDecimal oldVal, boolean isCurrency) {
        if (oldVal.compareTo(BigDecimal.ZERO) == 0) {
            return newVal.compareTo(BigDecimal.ZERO) > 0 ? "Tăng từ 0 (kỳ trước không có)" : "Không thay đổi";
        }
        BigDecimal diff = newVal.subtract(oldVal);
        BigDecimal pct = diff.multiply(new BigDecimal("100")).divide(oldVal, 1, RoundingMode.HALF_UP);
        String prefix = diff.compareTo(BigDecimal.ZERO) >= 0 ? "Tăng " : "Giảm ";
        String absDiff = isCurrency ? PriceUtils.format(diff.abs()) : String.valueOf(diff.abs().longValue());
        return prefix + "**" + absDiff + "** (" + (diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + pct + "%)";
    }

    private String formatChangePct(double newVal, double oldVal) {
        if (oldVal == 0) return newVal > 0 ? "Tăng từ 0 (kỳ trước không có)" : "Không thay đổi";
        double diff = newVal - oldVal;
        double pct = diff * 100.0 / oldVal;
        String prefix = diff >= 0 ? "Tăng " : "Giảm ";
        return prefix + "**" + Math.abs(Math.round(diff)) + "** (" + (diff >= 0 ? "+" : "") + String.format("%.1f", pct) + "%)";
    }

    private Map<String, Object> singleAnswer(String answer) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("answer", answer);
        return r;
    }

    private static class PeriodResult {
        LocalDate from, to;
        String label;
        PeriodResult(LocalDate from, LocalDate to, String label) {
            this.from = from; this.to = to; this.label = label;
        }
    }
}
