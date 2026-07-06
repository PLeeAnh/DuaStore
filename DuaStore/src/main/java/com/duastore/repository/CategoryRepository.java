package com.duastore.repository;

import com.duastore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Lấy danh mục gốc, không xét trạng thái; dùng cho tác vụ quản trị/tổng hợp.
    List<Category> findByParentIsNull();

    // Homepage dùng danh sách active để cộng số sản phẩm từ danh mục con lên cha.
    List<Category> findByIsActiveTrue();

    // Danh mục gốc nổi bật: chỉ active và có thứ tự hiển thị ổn định.
    List<Category> findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();

    // Các con trực tiếp, dùng khi dựng cây hoặc xem chi tiết một danh mục.
    List<Category> findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(Integer parentId);

    long countByParentIsNull();

    long countByParentIsNotNull();
}
