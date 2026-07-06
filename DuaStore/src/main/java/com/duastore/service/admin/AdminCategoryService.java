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

import java.util.*;
import java.util.stream.Stream;

@Service
// Tầng nghiệp vụ của Danh Mục quản trị. Lớp này sắp xếp dữ liệu, chuyển Entity/DTO,
// xây cây cha-con và kiểm tra ràng buộc trước khi Controller quyết định hiển thị
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        // nullsLast giữ các danh mục chưa có thứ tự ở cuối; id là tiêu chí phụ để kết quả ổn định.
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
        // Phân trang trên danh sách đã sắp xếp; hiện tại chưa truy vấn Page trực tiếp từ CSDL.
        List<Category> sorted = findAll();
        int start = page * size;
        int end = Math.min(start + size, sorted.size());
        List<Category> pageContent = start < sorted.size() ? sorted.subList(start, end) : List.of();
        return new PageImpl<>(pageContent, PageRequest.of(page, size), sorted.size());
    }

    public List<Category> findAvailableParents(Integer currentId) {
        // Loại chính danh mục đang sửa để ngăn chọn nó làm cha của chính nó.
        return findAll().stream()
                .filter(Category::isActive)
                .filter(c -> !c.getId().equals(currentId))
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
        // Chỉ đọc parent khi tồn tại; DTO giữ cả id và tên cha cho form/chi tiết.
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

    @Transactional
    public Category save(CategoryDTO dto) {
        // id rỗng nghĩa là thêm mới; có id thì nạp bản ghi cũ để cập nhật.
        Category category = dto.getId() == null
                ? new Category()
                : categoryRepository.findById(dto.getId()).orElse(new Category());

        category.setTenDanhMuc(dto.getTenDanhMuc());
        category.setMoTa(dto.getMoTa());
        category.setThuTuHienThi(dto.getThuTuHienThi() == null ? 0 : dto.getThuTuHienThi());
        category.setActive(dto.isActive());
        category.setImageUrl(dto.getImageUrl());

        // parentId quyết định đây là danh mục gốc hay danh mục con.
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

    @Transactional
    public boolean softDelete(Integer id) {
        // Không xóa vật lý nhằm bảo toàn các liên kết và lịch sử dữ liệu.
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
        // Bắt đầu từ các nút gốc rồi nạp con đệ quy cho từng nhánh.
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
        for (Category root : roots) {
            root.setChildren(getChildrenRecursive(root.getId()));
        }
        return roots;
    }

    private List<Category> getChildrenRecursive(Integer parentId) {
        // Lấy con trực tiếp của một nút, sau đó tiếp tục tới khi không còn con.
        List<Category> children = categoryRepository
                .findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parentId);
        for (Category child : children) {
            child.setChildren(getChildrenRecursive(child.getId()));
        }
        return children;
    }

    public List<TreeNodeDto> getFlatTree(Map<Integer, Long> productCountMap) {
        // View dùng danh sách phẳng và level để render bảng cây đơn giản hơn.
        List<Category> roots = getTree();
        List<TreeNodeDto> result = new ArrayList<>();
        buildFlatTree(roots, 0, productCountMap, result);
        return result;
    }

    private void buildFlatTree(List<Category> nodes, int level, Map<Integer, Long> productCountMap, List<TreeNodeDto> result) {
        // Duyệt tiền thứ tự: thêm cha trước, rồi mới thêm toàn bộ các con.
        for (Category cat : nodes) {
            TreeNodeDto dto = new TreeNodeDto();
            dto.setId(cat.getId());
            dto.setParentId(cat.getParent() != null ? cat.getParent().getId() : null);
            dto.setTenDanhMuc(cat.getTenDanhMuc());
            dto.setImageUrl(cat.getImageUrl());
            dto.setActive(cat.isActive());
            dto.setThuTuHienThi(cat.getThuTuHienThi());
            dto.setHasChildren(!cat.getChildren().isEmpty());
            dto.setLevel(level);
            dto.setProductCount(productCountMap.getOrDefault(cat.getId(), 0L));
            // Giao diện ghi "danh mục con" nên chỉ đếm các con trực tiếp của nút hiện tại.
            dto.setChildCount(cat.getChildren().size());
            result.add(dto);
            if (!cat.getChildren().isEmpty()) {
                buildFlatTree(cat.getChildren(), level + 1, productCountMap, result);
            }
        }
    }

    public List<Category> search(String keyword, String status) {
        // Bộ lọc được thực hiện trong bộ nhớ trên danh sách findAll hiện tại.
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
        // Mỗi row là Object[]{categoryId, count}; chuyển thành Map để Controller/View tra nhanh.
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
