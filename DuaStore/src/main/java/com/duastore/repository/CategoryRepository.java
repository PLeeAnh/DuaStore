package com.duastore.repository;

import com.duastore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByParentIsNull();
    List<Category> findByIsActiveTrue();
    List<Category> findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(Integer parentId);
}
