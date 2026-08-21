package com.duastore.service.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.dto.TreeNodeDto;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý danh mục.
 */
public class AdminCategoryService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("[^a-z0-9\\-]+");
    private static final Pattern MULTIPLE_HYPHENS_PATTERN = Pattern.compile("-+");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;

    public AdminCategoryService(CategoryRepository categoryRepository, ProductRepository productRepository,
            FileUploadService fileUploadService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
    }

    // ===== Slug Generation Helper Methods =====

    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = ascii.toLowerCase();
        slug = SLUG_PATTERN.matcher(slug).replaceAll("-");
        slug = MULTIPLE_HYPHENS_PATTERN.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    private String generateUniqueSlug(String baseSlug, Integer excludeId) {
        if (baseSlug.isBlank()) {
            return "danh-muc";
        }
        String slug = baseSlug;
        int counter = 1;
        while (true) {
            boolean conflict = categoryRepository.existsBySlug(slug);
            if (conflict && excludeId != null) {
                Category existing = categoryRepository.findBySlug(slug).orElse(null);
                conflict = existing != null && !existing.getId().equals(excludeId);
            }
            if (!conflict) {
                return slug;
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
    }

    public List<Category> findAll() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getThuTuHienThi, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Category::getId))
                .toList();
    }

    public long countRootCategories() {
        return categoryRepository.countByParentIsNull();
    }

    public long countChildCategories() {
        return categoryRepository.countByParentIsNotNull();
    }

    public Page<Category> findAllPaged(int page, int size) {
        List<Category> sorted = findAll();
        int start = page * size;
        int end = Math.min(start + size, sorted.size());
        List<Category> pageContent = start < sorted.size() ? sorted.subList(start, end) : List.of();
        return new PageImpl<>(pageContent, PageRequest.of(page, size), sorted.size());
    }

    public List<Category> findAvailableParents(Integer currentId) {
        List<Category> excludeIds = new ArrayList<>();
        if (currentId != null) {
            excludeIds.addAll(getAllDescendantIds(currentId));
            excludeIds.add(findById(currentId));
        }
        List<Integer> exclude = excludeIds.stream()
                .filter(Objects::nonNull)
                .map(Category::getId)
                .toList();
        return findAll().stream()
                .filter(Category::isActive)
                .filter(c -> !exclude.contains(c.getId()))
                .toList();
    }

    public List<TreeNodeDto> findAvailableParentTree(Integer currentId) {
        List<Category> excludeIds = new ArrayList<>();
        if (currentId != null) {
            excludeIds.addAll(getAllDescendantIds(currentId));
            excludeIds.add(findById(currentId));
        }
        Set<Integer> exclude = excludeIds.stream()
                .filter(Objects::nonNull)
                .map(Category::getId)
                .collect(java.util.stream.Collectors.toSet());
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
        List<TreeNodeDto> result = new ArrayList<>();
        buildAvailableTree(roots, 0, null, exclude, result);
        return result;
    }

    private void buildAvailableTree(List<Category> nodes, int level, String parentPath, Set<Integer> exclude, List<TreeNodeDto> result) {
        for (Category cat : nodes) {
            if (exclude.contains(cat.getId())) {
                continue;
            }
            TreeNodeDto dto = new TreeNodeDto();
            dto.setId(cat.getId());
            String path = parentPath != null ? parentPath + " › " + cat.getTenDanhMuc() : cat.getTenDanhMuc();
            dto.setFullPath(path);
            dto.setTenDanhMuc(cat.getTenDanhMuc());
            dto.setImageUrl(cat.getImageUrl());
            dto.setActive(cat.isActive());
            dto.setThuTuHienThi(cat.getThuTuHienThi());
            dto.setHasChildren(!cat.getChildren().isEmpty());
            dto.setLevel(level);
            result.add(dto);
            if (!cat.getChildren().isEmpty()) {
                buildAvailableTree(cat.getChildren(), level + 1, path, exclude, result);
            }
        }
    }

    private List<Category> getAllDescendantIds(Integer parentId) {
        List<Category> descendants = new ArrayList<>();
        List<Category> children = categoryRepository
                .findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parentId);
        for (Category child : children) {
            descendants.add(child);
            descendants.addAll(getAllDescendantIds(child.getId()));
        }
        return descendants;
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public CategoryDTO findByIdAsDto(Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return null;
        }
        return toDto(category);
    }

    public CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setTenDanhMuc(category.getTenDanhMuc());
        dto.setMoTa(category.getMoTa());
        dto.setThuTuHienThi(category.getThuTuHienThi());
        dto.setActive(category.isActive());
        dto.setImageUrl(category.getImageUrl());
        dto.setSlug(category.getSlug());
        dto.setNgayTao(category.getNgayTao());
        dto.setNgayCapNhat(category.getNgayCapNhat());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setTenDanhMucCha(category.getParent().getTenDanhMuc());
        }
        return dto;
    }

    @Transactional
    public Category save(CategoryDTO dto) {
        Category category = dto.getId() == null
                ? new Category()
                : categoryRepository.findById(dto.getId()).orElse(new Category());

        String oldImageUrl = category.getImageUrl();
        String newImageUrl = dto.getImageUrl();
        category.setTenDanhMuc(dto.getTenDanhMuc());
        String baseSlug = dto.getSlug() != null && !dto.getSlug().isBlank()
                ? generateSlug(dto.getSlug())
                : generateSlug(dto.getTenDanhMuc());
        category.setSlug(generateUniqueSlug(baseSlug, dto.getId()));
        category.setMoTa(com.duastore.util.HtmlSanitizer.sanitize(dto.getMoTa()));
        category.setThuTuHienThi(dto.getThuTuHienThi() == null ? 0 : dto.getThuTuHienThi());
        category.setActive(dto.isActive());
        category.setImageUrl(newImageUrl);

        // Set parent — validate to prevent circular reference
        if (dto.getParentId() != null) {
            // Don't allow setting self as parent
            if (dto.getId() != null && dto.getParentId().equals(dto.getId())) {
                category.setParent(null);
            } else {
                Category newParent = categoryRepository.findById(dto.getParentId()).orElse(null);
                // Check that newParent is not a descendant of this category
                if (dto.getId() != null && newParent != null
                        && getAllDescendantIds(dto.getId()).contains(newParent)) {
                    category.setParent(null);
                } else {
                    category.setParent(newParent);
                }
            }
        } else {
            category.setParent(null);
        }

        Category saved = categoryRepository.saveAndFlush(category);
        if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
            fileUploadService.deleteAfterCommit(oldImageUrl);
        }
        return saved;
    }

    @Transactional
    public void clearImageUrl(Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            String oldImage = category.getImageUrl();
            category.setImageUrl(null);
            categoryRepository.save(category);
            fileUploadService.deleteAfterCommit(oldImage);
        }
    }

    @Transactional
    public boolean softDelete(Integer id) {
        Category category = findById(id);
        if (category == null) {
            return false;
        }
        category.setActive(false);
        if (category.getChildren() != null) {
            category.getChildren().forEach(c -> {
                c.setActive(false);
                categoryRepository.save(c);
            });
        }
        List<Product> products = productRepository.findByDanhMucIdAndIsActiveTrue(id);
        products.forEach(p -> {
            p.setActive(false);
            productRepository.save(p);
        });
        categoryRepository.save(category);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Category> findChildrenByParentId(Integer parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parentId);
    }

    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(Integer categoryId) {
        return productRepository.findByDanhMucIdAndIsActiveTrue(categoryId);
    }

    @Transactional(readOnly = true)
    public long countProductByCategoryId(Integer categoryId) {
        return productRepository.countByDanhMucIdAndIsActiveTrue(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Category> getTree() {
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
        for (Category root : roots) {
            root.setChildren(getChildrenRecursive(root.getId()));
        }
        return roots;
    }

    private List<Category> getChildrenRecursive(Integer parentId) {
        List<Category> children = categoryRepository
                .findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parentId);
        for (Category child : children) {
            child.setChildren(getChildrenRecursive(child.getId()));
        }
        return children;
    }

    public List<TreeNodeDto> getFlatTree(Map<Integer, Long> productCountMap) {
        List<Category> roots = getTree();
        List<TreeNodeDto> result = new ArrayList<>();
        buildFlatTree(roots, 0, null, productCountMap, result);
        return result;
    }

    private void buildFlatTree(List<Category> nodes, int level, String parentPath, Map<Integer, Long> productCountMap, List<TreeNodeDto> result) {
        for (Category cat : nodes) {
            TreeNodeDto dto = new TreeNodeDto();
            dto.setId(cat.getId());
            String path = parentPath != null ? parentPath + " › " + cat.getTenDanhMuc() : cat.getTenDanhMuc();
            dto.setFullPath(path);
            dto.setTenDanhMuc(cat.getTenDanhMuc());
            dto.setImageUrl(cat.getImageUrl());
            dto.setActive(cat.isActive());
            dto.setThuTuHienThi(cat.getThuTuHienThi());
            dto.setHasChildren(!cat.getChildren().isEmpty());
            dto.setLevel(level);
            dto.setProductCount(productCountMap.getOrDefault(cat.getId(), 0L));
            dto.setChildCount(countRecursiveChildren(cat));
            result.add(dto);
            if (!cat.getChildren().isEmpty()) {
                buildFlatTree(cat.getChildren(), level + 1, path, productCountMap, result);
            }
        }
    }

    private int countRecursiveChildren(Category cat) {
        int count = 0;
        for (Category child : cat.getChildren()) {
            count += 1 + countRecursiveChildren(child);
        }
        return count;
    }

    public List<Category> search(String keyword, String status) {
        Stream<Category> stream = findAll().stream();

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            stream = stream.filter(c
                    -> (c.getTenDanhMuc() != null && c.getTenDanhMuc().toLowerCase().contains(kw))
                    || (c.getMoTa() != null && c.getMoTa().toLowerCase().contains(kw))
            );
        }

        if ("active".equals(status)) {
            stream = stream.filter(Category::isActive);
        } else if ("inactive".equals(status)) {
            stream = stream.filter(c -> !c.isActive());
        }

        return stream.toList();
    }

    public boolean hasChildren(Integer id) {
        return !categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(id).isEmpty();
    }

    public boolean hasProducts(Integer id) {
        return productRepository.countByDanhMucIdAndIsActiveTrue(id) > 0;
    }

    public Map<Integer, Long> getProductCountMap() {
        List<Object[]> rows = productRepository.countProductsByDanhMuc();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            Integer danhMucId = (Integer) row[0];
            Long count = (Long) row[1];
            map.put(danhMucId, count);
        }
        return map;
    }
}
