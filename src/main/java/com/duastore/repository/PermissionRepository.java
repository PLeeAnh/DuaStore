package com.duastore.repository;

import com.duastore.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu quyền hạn (permission).
 */
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByModuleAndAction(String module, String action);

    List<Permission> findAllByOrderByModuleAscActionAsc();

    @Query("SELECT DISTINCT p.module FROM Permission p ORDER BY p.module")
    List<String> findAllModules();
}
