package com.duastore.service.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.dto.TreeNodeDto;
import com.duastore.model.Category;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
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
        return findAll().stream()
                .filter(Category::isActive)
                .filter(c -> currentId == null || !c.getId().equals(currentId))
                .toList();
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public CategoryDTO findByIdAsDto(Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) return null;
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
        dto.setNgayTao(category.getNgayTao());
        dto.setNgayCapNhat(category.getNgayCapNhat());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setTenDanhMucCha(category.getParent().getTenDanhMuc());
        }
        return dto;
    }

    public Category save(CategoryDTO dto) {
        Category category = dto.getId() == null
                ? new Category()
                : categoryRepository.findById(dto.getId()).orElse(new Category());

        category.setTenDanhMuc(dto.getTenDanhMuc());
        category.setMoTa(dto.getMoTa());
        category.setThuTuHienThi(dto.getThuTuHienThi() == null ? 0 : dto.getThuTuHienThi());
        category.setActive(dto.isActive());
        category.setImageUrl(dto.getImageUrl());

        if (dto.getParentId() != null) {
            category.setParent(categoryRepository.findById(dto.getParentId()).orElse(null));
        } else {
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void clearImageUrl(Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            category.setImageUrl(null);
            categoryRepository.save(category);
        }
    }

    public boolean softDelete(Integer id) {
        Category category = findById(id);
        if (category == null) {
            return false;
        }
        category.setActive(false);
        categoryRepository.save(category);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Category> findChildrenByParentId(Integer parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parentId);
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
        buildFlatTree(roots, 0, productCountMap, result);
        return result;
    }

    private void buildFlatTree(List<Category> nodes, int level, Map<Integer, Long> productCountMap, List<TreeNodeDto> result) {
        for (Category cat : nodes) {
            TreeNodeDto dto = new TreeNodeDto();
            dto.setId(cat.getId());
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
                buildFlatTree(cat.getChildren(), level + 1, productCountMap, result);
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
            stream = stream.filter(c ->
                    (c.getTenDanhMuc() != null && c.getTenDanhMuc().toLowerCase().contains(kw)) ||
                    (c.getMoTa() != null && c.getMoTa().toLowerCase().contains(kw))
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
