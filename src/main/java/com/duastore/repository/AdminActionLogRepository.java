package com.duastore.repository;

import com.duastore.model.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * phía quản trị (admin) — Repository (Spring Data JPA) truy vấn/thao tác dữ liệu nhật ký hệ thống.
 */
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Integer> {

    List<AdminActionLog> findByLoaiEntityAndEntityId(String loaiEntity, Integer entityId, Sort sort);

    @Query("SELECT l FROM AdminActionLog l JOIN FETCH l.admin WHERE l.loaiEntity = ?1 AND l.entityId = ?2 ORDER BY l.ngayTao DESC")
    List<AdminActionLog> findByLoaiEntityAndEntityIdWithAdmin(String loaiEntity, Integer entityId);

    List<AdminActionLog> findByAdminId(Integer adminId, Sort sort);

    @Query(value = "SELECT l FROM AdminActionLog l JOIN FETCH l.admin ORDER BY l.ngayTao DESC",
            countQuery = "SELECT COUNT(l) FROM AdminActionLog l")
    Page<AdminActionLog> findAllWithAdmin(Pageable pageable);
}
