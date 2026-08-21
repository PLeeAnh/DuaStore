package com.duastore.repository;

import com.duastore.model.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu bài viết, danh mục.
 */
public interface PostCategoryRepository extends JpaRepository<PostCategory, Integer> {

    List<PostCategory> findAllByOrderByThuTuAsc();
}
