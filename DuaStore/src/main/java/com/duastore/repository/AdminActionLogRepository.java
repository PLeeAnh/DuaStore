package com.duastore.repository;

import com.duastore.model.AdminActionLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Integer> {
    List<AdminActionLog> findByLoaiEntityAndEntityId(String loaiEntity, Integer entityId, Sort sort);
    List<AdminActionLog> findByAdminId(Integer adminId, Sort sort);
}
