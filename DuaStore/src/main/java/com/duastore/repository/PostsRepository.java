package com.duastore.repository;

import com.duastore.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostsRepository extends JpaRepository<Post, Integer> {

    List<Post> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    Page<Post> findByTrangThai(String trangThai, Pageable pageable);

    Page<Post> findByTrangThaiAndTieuDeContainingIgnoreCase(String trangThai, String keyword, Pageable pageable);

    Page<Post> findByTieuDeContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Post> findByTrangThaiAndFeaturedTrue(String trangThai, Pageable pageable);

    Page<Post> findByTrangThaiAndDanhMucId(String trangThai, Integer danhMucId, Pageable pageable);

    Page<Post> findByTrangThaiAndDanhMucIdAndTieuDeContainingIgnoreCase(String trangThai, Integer danhMucId, String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.trangThai = :trangThai AND p.ngayXuatBan <= :now ORDER BY p.ngayXuatBan DESC")
    Page<Post> findScheduledPosts(@Param("trangThai") String trangThai, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.trangThai = 'NHAP' AND p.ngayXuatBan IS NOT NULL AND p.ngayXuatBan <= :now")
    List<Post> findPostsToAutoPublish(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Post p WHERE p.trangThai = :trangThai AND p.danhMuc.id = :danhMucId AND p.id <> :excludeId")
    List<Post> findRelatedPosts(@Param("trangThai") String trangThai, @Param("danhMucId") Integer danhMucId, @Param("excludeId") Integer excludeId, Pageable pageable);

    Page<Post> findByDanhMucId(Integer danhMucId, Pageable pageable);

    Page<Post> findByDanhMucIdAndTieuDeContainingIgnoreCase(Integer danhMucId, String keyword, Pageable pageable);

    Optional<Post> findBySlugAndTrangThai(String slug, String trangThai);
}
