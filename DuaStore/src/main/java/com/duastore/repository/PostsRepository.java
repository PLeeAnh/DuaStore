package com.duastore.repository;

import com.duastore.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {

    List<Post> findByTrangThaiOrderByNgayTaoDesc(String trangThai);
}
