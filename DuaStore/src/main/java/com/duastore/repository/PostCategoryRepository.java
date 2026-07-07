package com.duastore.repository;

import com.duastore.model.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Integer> {

    List<PostCategory> findAllByOrderByThuTuAsc();
}
