package com.duastore.repository;

import com.duastore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu danh mục.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByParentIsNull();

    List<Category> findByIsActiveTrue();

    List<Category> findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(Integer parentId);

    long countByParentIsNull();

    long countByParentIsNotNull();

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
