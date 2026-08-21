package com.duastore.service;

import com.duastore.model.Product;
import com.duastore.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý gợi ý sản phẩm liên quan.
 */
public class RecommendationService {

    private static final int VIEW_LOOKBACK_DAYS = 30;
    private static final int PURCHASE_LOOKBACK_DAYS = 90;
    private static final int MAX_CANDIDATES = 100;

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductViewRepository productViewRepository;
    private final WishlistRepository wishlistRepository;
    private final CartItemRepository cartItemRepository;

    public RecommendationService(ProductRepository productRepository,
                                 OrderItemRepository orderItemRepository,
                                 ProductViewRepository productViewRepository,
                                 WishlistRepository wishlistRepository,
                                 CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.productViewRepository = productViewRepository;
        this.wishlistRepository = wishlistRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<Product> getPersonalizedSuggestions(Integer userId, int limit) {
        if (userId == null) {
            return getFallbackSuggestions(limit);
        }

        LocalDateTime sinceViews = LocalDateTime.now().minusDays(VIEW_LOOKBACK_DAYS);
        LocalDateTime sincePurchases = LocalDateTime.now().minusDays(PURCHASE_LOOKBACK_DAYS);

        Set<Integer> ownedProductIds = getOwnedProductIds(userId);
        Map<Integer, Double> signalScores = new HashMap<>();

        gatherPurchaseSignals(userId, sincePurchases, ownedProductIds, signalScores);
        gatherViewSignals(userId, sinceViews, ownedProductIds, signalScores);
        gatherWishlistSignals(userId, ownedProductIds, signalScores);
        gatherCartSignals(userId, ownedProductIds, signalScores);

        if (signalScores.isEmpty()) {
            return getFallbackSuggestions(limit);
        }

        Set<Integer> signalProductIds = signalScores.keySet();
        Set<Integer> candidateIds = new HashSet<>();
        Map<Integer, Double> candidateScores = new HashMap<>();

        List<Product> signalProducts = productRepository.findAllById(signalProductIds);
        for (Product signalProduct : signalProducts) {
            double signalWeight = signalScores.get(signalProduct.getId());
            List<Product> sameCategory = productRepository
                    .findByDanhMucIdAndIsActiveTrue(signalProduct.getDanhMucId());

            for (Product candidate : sameCategory) {
                if (ownedProductIds.contains(candidate.getId())) continue;
                if (signalProductIds.contains(candidate.getId())) continue;
                if (!"DANG_BAN".equals(candidate.getTrangThaiSanPham())) continue;
                candidateIds.add(candidate.getId());
                candidateScores.merge(candidate.getId(), signalWeight * 1.0, Double::sum);

                if (candidate.getThuongHieu() != null && candidate.getThuongHieu().equals(signalProduct.getThuongHieu())) {
                    if (!candidate.getThuongHieu().isBlank()) {
                        candidateScores.merge(candidate.getId(), signalWeight * 0.5, Double::sum);
                    }
                }
            }

            addCoPurchasedProducts(signalProduct.getId(), userId, ownedProductIds, signalProductIds, candidateIds, candidateScores);
        }

        List<Integer> topCandidateIds = candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Product> result = new ArrayList<>();
        for (Integer id : topCandidateIds) {
            productRepository.findById(id).ifPresent(p -> {
                if ("DANG_BAN".equals(p.getTrangThaiSanPham())) {
                    result.add(p);
                }
            });
        }

        if (result.size() < limit) {
            List<Product> fallback = getFallbackSuggestions(limit - result.size());
            Set<Integer> existing = result.stream().map(Product::getId).collect(Collectors.toSet());
            for (Product p : fallback) {
                if (!existing.contains(p.getId())) {
                    result.add(p);
                }
            }
        }

        return result;
    }

    private void gatherPurchaseSignals(Integer userId, LocalDateTime since,
                                        Set<Integer> owned, Map<Integer, Double> scores) {
        List<Integer> purchasedIds = orderItemRepository.findPurchasedProductIdsByUserSince(userId, since);
        for (Integer pid : purchasedIds) {
            scores.merge(pid, 5.0, Double::sum);
        }
    }

    private void gatherViewSignals(Integer userId, LocalDateTime since,
                                    Set<Integer> owned, Map<Integer, Double> scores) {
        List<Integer> viewedIds = productViewRepository.findProductIdsViewedByUserSince(userId, since);
        for (Integer pid : viewedIds) {
            scores.merge(pid, 3.0, Double::sum);
        }
    }

    private void gatherWishlistSignals(Integer userId, Set<Integer> owned,
                                        Map<Integer, Double> scores) {
        List<Integer> wishlistIds = wishlistRepository.findProductIdsByUserId(userId);
        for (Integer pid : wishlistIds) {
            scores.merge(pid, 4.0, Double::sum);
        }
    }

    private void gatherCartSignals(Integer userId, Set<Integer> owned,
                                    Map<Integer, Double> scores) {
        List<Integer> cartIds = cartItemRepository.findProductIdsByUserId(userId);
        for (Integer pid : cartIds) {
            scores.merge(pid, 2.0, Double::sum);
        }
    }

    private void addCoPurchasedProducts(Integer productId, Integer userId,
                                         Set<Integer> owned, Set<Integer> signals,
                                         Set<Integer> candidateIds, Map<Integer, Double> scores) {
        List<Integer> coPurchased = orderItemRepository.findCoPurchasedProductIds(productId);
        for (Integer cpId : coPurchased) {
            if (owned.contains(cpId)) continue;
            if (signals.contains(cpId)) continue;
            candidateIds.add(cpId);
            scores.merge(cpId, 4.0, Double::sum);
        }
    }

    private Set<Integer> getOwnedProductIds(Integer userId) {
        Set<Integer> owned = new HashSet<>();
        owned.addAll(orderItemRepository.findPurchasedProductIdsByUserSince(userId, LocalDateTime.now().minusYears(5)));
        owned.addAll(cartItemRepository.findProductIdsByUserId(userId));
        return owned;
    }

    private List<Product> getFallbackSuggestions(int limit) {
        return productRepository.findFeaturedWithVariants().stream()
                .filter(p -> "DANG_BAN".equals(p.getTrangThaiSanPham()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
