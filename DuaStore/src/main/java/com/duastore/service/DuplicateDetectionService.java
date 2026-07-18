package com.duastore.service;

import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public DuplicateDetectionService(CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<DuplicateMatch> findCategoryDuplicates(String name, Integer excludeId) {
        List<Category> all = categoryRepository.findByIsActiveTrue();
        return all.stream()
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .map(c -> {
                    double score = similarity(name, c.getTenDanhMuc());
                    return new DuplicateMatch(c.getId(), c.getTenDanhMuc(), "danh-muc", score,
                            buildCategoryPath(c));
                })
                .filter(m -> m.score() > 0.3)
                .sorted(Comparator.<DuplicateMatch, Double>comparing(DuplicateMatch::score).reversed())
                .limit(5)
                .toList();
    }

    public List<DuplicateMatch> findVariantDuplicates(String name, Integer productId, Integer excludeId) {
        if (productId == null) return List.of();
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(productId);
        String productName = productRepository.findById(productId)
                .map(Product::getTenSanPham)
                .orElse("Sản phẩm #" + productId);
        return variants.stream()
                .filter(v -> excludeId == null || !v.getId().equals(excludeId))
                .map(v -> {
                    double score = similarity(name, v.getTenBienThe());
                    return new DuplicateMatch(v.getId(), v.getTenBienThe(), "bien-the", score,
                            productName + " → " + v.getTenBienThe());
                })
                .filter(m -> m.score() > 0.3)
                .sorted(Comparator.<DuplicateMatch, Double>comparing(DuplicateMatch::score).reversed())
                .limit(5)
                .toList();
    }

    public List<DuplicateMatch> findProductDuplicates(String name, Integer excludeId) {
        List<Product> all = productRepository.findByIsActiveTrueOrderByNgayTaoDesc();
        return all.stream()
                .filter(p -> excludeId == null || !p.getId().equals(excludeId))
                .map(p -> {
                    double score = similarity(name, p.getTenSanPham());
                    return new DuplicateMatch(p.getId(), p.getTenSanPham(), "san-pham", score,
                            getCategoryName(p.getDanhMucId()));
                })
                .filter(m -> m.score() > 0.3)
                .sorted(Comparator.<DuplicateMatch, Double>comparing(DuplicateMatch::score).reversed())
                .limit(5)
                .toList();
    }

    private double similarity(String a, String b) {
        if (a == null || b == null) return 0;
        Set<String> tokensA = tokenize(a);
        Set<String> tokensB = tokenize(b);
        if (tokensA.isEmpty() && tokensB.isEmpty()) return 1;
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0;

        double jaccard = jaccardSimilarity(tokensA, tokensB);
        double overlap = overlapCoefficient(tokensA, tokensB);
        double containment = containmentScore(a, b);

        return 0.5 * jaccard + 0.3 * overlap + 0.2 * containment;
    }

    private Set<String> tokenize(String text) {
        String normalized = Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
        if (normalized.isEmpty()) return Set.of();
        return new HashSet<>(Arrays.asList(normalized.split("\\s+")));
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    private double overlapCoefficient(Set<String> a, Set<String> b) {
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        int min = Math.min(a.size(), b.size());
        return min == 0 ? 0 : (double) intersection.size() / min;
    }

    private double containmentScore(String a, String b) {
        String na = normalizeRaw(a);
        String nb = normalizeRaw(b);
        if (na.length() < 3 || nb.length() < 3) return 0;
        if (na.contains(nb) || nb.contains(na)) return 0.9;
        return 0;
    }

    private String normalizeRaw(String text) {
        return Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", "");
    }

    private String buildCategoryPath(Category category) {
        List<String> names = new ArrayList<>();
        Category current = category;
        while (current != null) {
            names.add(current.getTenDanhMuc());
            current = current.getParent();
        }
        Collections.reverse(names);
        return String.join(" → ", names);
    }

    private String getCategoryName(Integer danhMucId) {
        if (danhMucId == null) return "";
        return categoryRepository.findById(danhMucId)
                .map(Category::getTenDanhMuc)
                .orElse("");
    }

    public record DuplicateMatch(Integer id, String name, String type, double score, String path) {
        public String formattedScore() {
            return String.format("%.0f%%", score * 100);
        }
    }
}
