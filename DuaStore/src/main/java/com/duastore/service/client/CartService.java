package com.duastore.service.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.model.CartItem;
import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CartItemRepository;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.PricingService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final PricingService pricingService;
    private final FlashSaleItemRepository flashSaleItemRepository;

    public CartService(CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            PricingService pricingService,
            FlashSaleItemRepository flashSaleItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
        this.flashSaleItemRepository = flashSaleItemRepository;
    }

    public List<CartItemDTO> getItems(Integer userId) {
        return cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public CartResult add(Integer userId, Integer variantId, Integer quantity) {
        if (variantId == null) {
            return CartResult.fail("Vui long chon bien the san pham");
        }
        int qty = normalizeQuantity(quantity);
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null || !variant.isActive()) {
            return CartResult.fail("Bien the san pham khong ton tai");
        }
        if (variant.getSoLuongTon() <= 0) {
            return CartResult.fail("San pham da het hang");
        }

        // Check flash sale quota
        FlashSaleItem flashItem = flashSaleItemRepository.findBestActiveByVariantId(variantId, LocalDateTime.now())
                .orElse(null);
        if (flashItem != null && !pricingService.hasRemainingQuota(flashItem)) {
            return CartResult.fail("San pham da het sua Flash Sale");
        }

        int finalQty = Math.min(qty, variant.getSoLuongTon());
        PricingService.PriceResult priced = pricingService.resolvePrice(variant);
        BigDecimal currentPrice = priced.finalPrice();
        CartItem item = cartItemRepository.findByUserIdAndVariantId(userId, variantId).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setUserId(userId);
            item.setProductId(variant.getProductId());
            item.setVariantId(variantId);
            item.setSoLuong(finalQty);
            item.setGiaLucThem(currentPrice);
        } else {
            item.setSoLuong(Math.min(item.getSoLuong() + finalQty, variant.getSoLuongTon()));
        }
        cartItemRepository.save(item);
        return CartResult.ok(count(userId));
    }

    @Transactional
    public CartResult updateQuantity(Integer userId, Integer itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getUserId().equals(userId)) {
            return CartResult.fail("Khong tim thay san pham trong gio");
        }
        ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
        if (variant == null || !variant.isActive() || variant.getSoLuongTon() <= 0) {
            cartItemRepository.delete(item);
            return CartResult.fail("San pham da het hang hoac ngung ban");
        }
        item.setSoLuong(Math.min(normalizeQuantity(quantity), variant.getSoLuongTon()));
        cartItemRepository.save(item);
        return CartResult.ok(count(userId));
    }

    @Transactional
    public void remove(Integer userId, Integer itemId) {
        cartItemRepository.deleteByIdAndUserId(itemId, userId);
    }

    public int count(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return cartItemRepository.countByUserId(userId);
    }

    public java.util.Optional<ProductVariant> findDefaultVariant(Integer productId) {
        if (productId == null) {
            return java.util.Optional.empty();
        }
        var opt = variantRepository.findByProductIdAndIsDefaultTrue(productId);
        if (opt.isPresent()) {
            return opt;
        }
        return variantRepository.findByProductIdAndIsActiveTrue(productId).stream().findFirst();
    }

    @Transactional
    public void removeByVariantId(Integer userId, Integer variantId) {
        cartItemRepository.findByUserIdAndVariantId(userId, variantId)
                .ifPresent(item -> cartItemRepository.delete(item));
    }

    @Transactional
    public CartResult updateQuantityByVariantId(Integer userId,
            Integer variantId,
            Integer quantity) {

        CartItem item = cartItemRepository
                .findByUserIdAndVariantId(userId, variantId)
                .orElse(null);

        if (item == null) {
            return CartResult.fail("Không tìm thấy sản phẩm trong giỏ");
        }

        ProductVariant variant
                = variantRepository.findById(variantId).orElse(null);

        if (variant == null
                || !variant.isActive()
                || variant.getSoLuongTon() <= 0) {

            cartItemRepository.delete(item);

            return CartResult.fail(
                    "Sản phẩm đã hết hàng hoặc ngừng bán"
            );
        }

        int stock = variant.getSoLuongTon();

        if (quantity > stock) {
            item.setSoLuong(stock);
            cartItemRepository.save(item);

            return new CartResult(
                    false,
                    "Chỉ còn " + stock + " sản phẩm trong kho",
                    count(userId)
            );
        }

        item.setSoLuong(quantity);

        cartItemRepository.save(item);

        return CartResult.ok(count(userId));
    }

    public BigDecimal total(List<CartItemDTO> items) {
        return items.stream()
                .map(CartItemDTO::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItemDTO toDto(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        if (product == null || variant == null) {
            return null;
        }
        PricingService.PriceResult priced = pricingService.resolvePrice(variant);
        BigDecimal price = priced.finalPrice();

        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariantId());
        dto.setTenSanPham(product.getTenSanPham());
        dto.setTenBienThe(variant.getTenBienThe());
        dto.setHinhAnh(variant.getHinhAnh() != null ? variant.getHinhAnh() : product.getHinhAnhChinh());
        dto.setGiaBan(price);
        dto.setSoLuong(item.getSoLuong());
        dto.setSoLuongTon(variant.getSoLuongTon());
        dto.setThanhTien(price.multiply(BigDecimal.valueOf(item.getSoLuong())));
        dto.setGiaLucThem(item.getGiaLucThem());
        dto.setGiaThayDoi(item.getGiaLucThem() != null && price.compareTo(item.getGiaLucThem()) != 0);
        dto.setNgayThem(item.getNgayThem());
        dto.setNguonGia(priced.source().name());
        return dto;
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            return 1;
        }
        return Math.min(quantity, 99);
    }

    public record CartResult(boolean success, String message, int cartCount) {

        static CartResult ok(int cartCount) {
            return new CartResult(true, "OK", cartCount);
        }

        static CartResult fail(String message) {
            return new CartResult(false, message, 0);
        }
    }

    public List<Product> getSuggestions(Integer userId, int limit) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByNgayThemDesc(userId);
        if (cartItems.isEmpty()) {
            return productRepository.findFeaturedWithVariants().stream()
                    .filter(p -> p.getTrangThaiSanPham().equals("DANG_BAN"))
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        Set<Integer> categoryIds = cartItems.stream()
                .map(ci -> {
                    Product p = ci.getProduct();
                    return p != null ? p.getDanhMucId() : null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Integer> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
        List<Product> related = productRepository.findByDanhMucIdInAndIsActiveTrue(new ArrayList<>(categoryIds));
        return related.stream()
                .filter(p -> !productIds.contains(p.getId()))
                .filter(p -> p.getTrangThaiSanPham().equals("DANG_BAN"))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> getStockWarnings(Integer userId) {
        List<String> warnings = new ArrayList<>();
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByNgayThemDesc(userId);
        for (CartItem ci : cartItems) {
            ProductVariant v = ci.getVariant();
            Product p = ci.getProduct();
            if (v == null || p == null) {
                continue;
            }
            if (!v.isActive() || v.getSoLuongTon() <= 0) {
                warnings.add(p.getTenSanPham() + " (" + v.getTenBienThe() + ") đã hết hàng");
            } else if (ci.getSoLuong() > v.getSoLuongTon()) {
                warnings.add(p.getTenSanPham() + " (" + v.getTenBienThe() + ") chỉ còn " + v.getSoLuongTon() + " trong kho");
            }
        }
        return warnings;
    }

    public boolean hasOutOfStockItems(Integer userId) {
        return cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .anyMatch(ci -> {
                    ProductVariant v = ci.getVariant();
                    return v == null || !v.isActive() || v.getSoLuongTon() <= 0;
                });
    }

    public boolean hasStockWarnings(Integer userId) {
        return cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .anyMatch(ci -> {
                    ProductVariant v = ci.getVariant();
                    return v != null && ci.getSoLuong() > v.getSoLuongTon() && v.getSoLuongTon() > 0;
                });
    }

    @Transactional
    public void mergeGuestCart(Integer userId, java.util.Map<Integer, Integer> guestCart) {
        if (guestCart == null || guestCart.isEmpty()) {
            return;
        }
        for (java.util.Map.Entry<Integer, Integer> entry : guestCart.entrySet()) {
            Integer variantId = entry.getKey();
            Integer quantity = entry.getValue();
            if (variantId == null || quantity == null || quantity <= 0) {
                continue;
            }
            ProductVariant variant = variantRepository.findById(variantId).orElse(null);
            if (variant == null || !variant.isActive() || variant.getSoLuongTon() <= 0) {
                continue;
            }
            CartItem existing = cartItemRepository.findByUserIdAndVariantId(userId, variantId).orElse(null);
            if (existing != null) {
                existing.setSoLuong(Math.min(existing.getSoLuong() + quantity, variant.getSoLuongTon()));
            } else {
                existing = new CartItem();
                existing.setUserId(userId);
                existing.setProductId(variant.getProductId());
                existing.setVariantId(variantId);
                existing.setSoLuong(Math.min(quantity, variant.getSoLuongTon()));
                existing.setGiaLucThem(pricingService.resolvePrice(variant).finalPrice());
            }
            cartItemRepository.save(existing);
        }
    }

    @Transactional
    public void reorder(Integer userId, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Đơn hàng không thuộc về bạn");
        }
        String status = order.getTrangThaiDon();
        if (!"DA_GIAO".equals(status) && !"DA_HOAN_THANH".equals(status) && !"DA_HUY".equals(status)) {
            throw new RuntimeException("Chỉ có thể mua lại đơn hàng đã giao, hoàn thành hoặc đã hủy");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getVariantId() == null) {
                continue;
            }
            ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
            if (variant == null || !variant.isActive() || variant.getSoLuongTon() <= 0) {
                continue;
            }
            int qty = Math.min(item.getSoLuong(), variant.getSoLuongTon());
            CartItem existing = cartItemRepository.findByUserIdAndVariantId(userId, item.getVariantId()).orElse(null);
            if (existing != null) {
                existing.setSoLuong(Math.min(existing.getSoLuong() + qty, variant.getSoLuongTon()));
                cartItemRepository.save(existing);
            } else {
                CartItem ci = new CartItem();
                ci.setUserId(userId);
                ci.setProductId(item.getProductId());
                ci.setVariantId(item.getVariantId());
                ci.setSoLuong(qty);
                ci.setGiaLucThem(pricingService.resolvePrice(variant).finalPrice());
                cartItemRepository.save(ci);
            }
        }
    }

    public boolean hasPriceChanges(Integer userId) {
        return cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .anyMatch(ci -> {
                    ProductVariant v = ci.getVariant();
                    if (v == null || ci.getGiaLucThem() == null) {
                        return false;
                    }
                    BigDecimal currentPrice = pricingService.resolvePrice(v).finalPrice();
                    return currentPrice.compareTo(ci.getGiaLucThem()) != 0;
                });
    }
}
