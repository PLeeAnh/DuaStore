package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.User;
import com.duastore.model.ProductImage;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.LoyaltyPointsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminSuggestionService {

    private static final int ACCENT_SCAN_LIMIT = 400;

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final LoyaltyPointsService loyaltyPointsService;

    public AdminSuggestionService(ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository productImageRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            LoyaltyPointsService loyaltyPointsService) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.loyaltyPointsService = loyaltyPointsService;
    }

    public List<Map<String, Object>> search(String type, String q, int limit) {
        if (limit < 1) limit = 7;
        if (limit > 20) limit = 20;
        String query = q == null ? "" : q.trim();
        String queryNorm = normalize(query);

        switch (type == null ? "" : type) {
            case "product": return searchProducts(query, queryNorm, limit);
            case "variant": return searchVariants(query, queryNorm, limit);
            case "customer": return searchCustomers(query, queryNorm, limit);
            case "order": return searchOrders(query, queryNorm, limit);
            case "attribute": return searchAttributes(queryNorm, limit);
            default: return List.of();
        }
    }

    // ── Sản phẩm ──
    private List<Map<String, Object>> searchProducts(String q, String qNorm, int limit) {
        Set<Product> merged = new LinkedHashSet<>();
        if (!q.isEmpty()) {
            merged.addAll(productRepository.findTopByKeyword(q, PageRequest.of(0, 80)));
        }
        merged.addAll(accentFallbackProducts(qNorm));
        if (merged.isEmpty()) return List.of();

        List<Integer> ids = merged.stream().map(Product::getId).collect(Collectors.toList());
        List<ProductVariant> variants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
        Map<Integer, List<ProductVariant>> variantsByProduct = variants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));

        Map<Integer, Long> soldMap = new HashMap<>();
        for (Object[] row : orderItemRepository.sumSoldByProductIds(ids)) {
            soldMap.put((Integer) row[0], row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
        }

        List<Product> sorted = merged.stream()
                .sorted(productComparator(qNorm, soldMap))
                .limit(limit)
                .collect(Collectors.toList());

        List<Map<String, Object>> out = new ArrayList<>();
        for (Product p : sorted) {
            List<ProductVariant> pvs = variantsByProduct.getOrDefault(p.getId(), List.of());
            BigDecimal price = p.getMinPrice() != null ? p.getMinPrice() : minPrice(pvs);
            int stock = pvs.stream().mapToInt(v -> v.getSoLuongTon() == null ? 0 : v.getSoLuongTon()).sum();
            ProductVariant imgVariant = defaultVariant(pvs);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("map", "product");
            item.put("id", p.getId());
            item.put("name", p.getTenSanPham());
            item.put("sku", "#" + p.getId());
            item.put("image", p.getHinhAnhChinh() != null ? p.getHinhAnhChinh()
                    : (imgVariant != null ? imgVariant.getHinhAnh() : firstGalleryImage(p.getId())));
            item.put("price", price);
            item.put("stock", stock);
            item.put("disabled", stock <= 0);
            item.put("sold", soldMap.getOrDefault(p.getId(), 0L));
            out.add(item);
        }
        return out;
    }

    private List<Product> accentFallbackProducts(String qNorm) {
        if (qNorm.isEmpty()) return List.of();
        return productRepository.findByIsActiveTrueOrderByNgayTaoDesc(PageRequest.of(0, ACCENT_LIST_LIMIT))
                .stream()
                .filter(p -> normalize(p.getTenSanPham()).contains(qNorm))
                .limit(60)
                .collect(Collectors.toList());
    }

    private static final int ACCENT_LIST_LIMIT = 400;

    // ── Biến thể ──
    private List<Map<String, Object>> searchVariants(String q, String qNorm, int limit) {
        List<ProductVariant> candidates = variantRepository.searchAutocomplete(q, PageRequest.of(0, 200));
        if (qNorm.length() > 1) {
            List<ProductVariant> accent = variantRepository.searchAutocomplete(qNorm, PageRequest.of(0, 200))
                    .stream()
                    .filter(v -> v.getProduct() != null && normalize(v.getProduct().getTenSanPham() + " " + v.getTenBienThe()).contains(qNorm))
                    .collect(Collectors.toList());
            Set<Integer> seen = candidates.stream().map(ProductVariant::getId).collect(Collectors.toSet());
            for (ProductVariant v : accent) {
                if (seen.add(v.getId())) candidates.add(v);
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (ProductVariant v : candidates) {
            if (out.size() >= limit) break;
            Product p = v.getProduct();
            String productName = p != null ? p.getTenSanPham() : "SP #" + v.getProductId();
            BigDecimal price = v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc();
            int stock = v.getSoLuongTon() == null ? 0 : v.getSoLuongTon();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("map", "variant");
            item.put("id", v.getId());
            item.put("productId", v.getProductId());
            item.put("name", productName + " — " + v.getTenBienThe());
            item.put("label", v.getTenBienThe());
            item.put("image", v.getHinhAnh() != null ? v.getHinhAnh()
                    : (p != null ? p.getHinhAnhChinh() : null));
            item.put("price", price);
            item.put("stock", stock);
            item.put("disabled", stock <= 0);
            out.add(item);
        }
        return out;
    }

    // ── Khách hàng ──
    private List<Map<String, Object>> searchCustomers(String q, String qNorm, int limit) {
        List<User> merged = new ArrayList<>();
        if (!q.isEmpty()) {
            merged.addAll(userRepository.searchByKeywordStatusAndCity(q, "active", null, "USER",
                    PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "ngayTao"))).getContent());
        }
        if (!qNorm.isEmpty()) {
            List<User> recent = userRepository.findByRole("USER",
                    PageRequest.of(0, ACCENT_LIST_LIMIT, Sort.by(Sort.Direction.DESC, "ngayTao"))).getContent();
            Set<Integer> seen = merged.stream().map(User::getId).collect(Collectors.toSet());
            for (User u : recent) {
                if (!Boolean.TRUE.equals(u.getIsActive())) continue;
                if (seen.add(u.getId())
                        && normalize(u.getHoTen() + " " + u.getEmail() + " "
                                + (u.getSoDienThoai() != null ? u.getSoDienThoai() : "")).contains(qNorm)) {
                    merged.add(u);
                }
            }
        }
        if (merged.isEmpty()) return List.of();

        List<Integer> ids = merged.stream().map(User::getId).collect(Collectors.toList());
        Map<Integer, Long> spentMap = new HashMap<>();
        for (Object[] row : orderRepository.sumTotalSpentByUserIds(ids)) {
            spentMap.put((Integer) row[0], row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : merged) {
            if (out.size() >= limit) break;
            long spent = spentMap.getOrDefault(u.getId(), 0L);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("map", "customer");
            item.put("id", u.getId());
            item.put("name", u.getHoTen());
            item.put("phone", u.getSoDienThoai() != null ? u.getSoDienThoai() : "");
            item.put("email", u.getEmail() != null ? u.getEmail() : "");
            item.put("level", levelFor(spent));
            item.put("points", loyaltyPointsService.getBalance(u.getId()));
            out.add(item);
        }
        return out;
    }

    private String levelFor(long totalSpent) {
        if (totalSpent >= 10_000_000) return "VIP";
        if (totalSpent >= 2_000_000) return "MEMBER";
        return "NORMAL";
    }

    // ── Đơn hàng ──
    private List<Map<String, Object>> searchOrders(String q, String qNorm, int limit) {
        List<Order> candidates = orderRepository.searchOrdersAutocomplete(q, PageRequest.of(0, 50));
        if (qNorm.length() > 1) {
            List<Order> accent = orderRepository.searchOrdersAutocomplete(qNorm, PageRequest.of(0, 50));
            Set<Integer> seen = candidates.stream().map(Order::getId).collect(Collectors.toSet());
            for (Order o : accent) {
                if (seen.add(o.getId()) && normalize(o.getMaDon() + " " + o.getSnapTenNguoiNhan()
                        + " " + o.getSnapSoDienThoai()).contains(qNorm)) {
                    candidates.add(o);
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Order o : candidates) {
            if (out.size() >= limit) break;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("map", "order");
            item.put("id", o.getId());
            item.put("code", o.getMaDon());
            item.put("buyerName", o.getSnapTenNguoiNhan());
            item.put("phone", o.getSnapSoDienThoai());
            item.put("date", o.getNgayDat() != null ? o.getNgayDat().toString() : "");
            item.put("total", o.getTongThanhToan() != null ? o.getTongThanhToan() : BigDecimal.ZERO);
            item.put("status", o.getTrangThaiDon());
            item.put("payment", o.getPhuongThucTT());
            out.add(item);
        }
        return out;
    }

    // ── Thuộc tính (giá trị đã dùng, theo mức độ phổ biến) ──
    private List<Map<String, Object>> searchAttributes(String qNorm, int limit) {
        // Chỉ tìm kiếm thuộc tính khi có từ khóa (dropdown gợi ý nhanh)
        if (qNorm.isEmpty()) return List.of();
        Map<String, List<Object[]>> pools = new LinkedHashMap<>();
        pools.put("thuongHieu", productRepository.findThuongHieuCounts());
        pools.put("chatLieu", productRepository.findChatLieuCounts());
        pools.put("xuatXu", productRepository.findXuatXuCounts());
        pools.put("kinhLoai", productRepository.findKinhLoaiCounts());
        pools.put("mucDichSuDung", productRepository.findMucDichSuDungCounts());
        pools.put("hinhDang", productRepository.findHinhDangCounts());

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<Object[]>> e : pools.entrySet()) {
            for (Object[] row : e.getValue()) {
                if (out.size() >= limit) break;
                String value = row[0] == null ? null : String.valueOf(row[0]);
                long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (value != null && normalize(value).contains(qNorm)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("map", "attribute");
                    item.put("value", value);
                    item.put("count", count);
                    out.add(item);
                }
            }
        }
        return out;
    }

    public static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).trim();
    }

    private boolean startsWith(Product p, String qNorm) {
        return normalize(p.getTenSanPham()).startsWith(qNorm);
    }

    private Comparator<Product> productComparator(String qNorm, Map<Integer, Long> sold) {
        Comparator<Product> byPrefix = Comparator.comparing(p -> startsWith(p, qNorm) ? 0 : 1);
        Comparator<Product> bySold = Comparator.comparingLong(p -> sold.getOrDefault(p.getId(), 0L));
        Comparator<Product> byName = Comparator.comparing(p -> normalize(p.getTenSanPham()));
        Comparator<Product> byCreated = Comparator.comparing(p -> p.getNgayTao() != null ? p.getNgayTao() : LocalDateTime.MIN,
                Comparator.reverseOrder());
        return byPrefix.thenComparing(bySold.reversed()).thenComparing(byCreated).thenComparing(byName);
    }

    private BigDecimal minPrice(List<ProductVariant> pvs) {
        return pvs.stream()
                .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private ProductVariant defaultVariant(List<ProductVariant> pvs) {
        if (pvs == null || pvs.isEmpty()) return null;
        return pvs.stream().filter(ProductVariant::isDefault).findFirst().orElse(pvs.get(0));
    }

    private String firstGalleryImage(Integer productId) {
        List<ProductImage> imgs = productImageRepository
                .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(productId);
        return imgs.isEmpty() ? null : imgs.get(0).getImageUrl();
    }
}