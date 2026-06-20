package com.duastore.repository;

import com.duastore.model.OrderAssignment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment, Integer> {
    @EntityGraph(attributePaths = {"admin"})
    Optional<OrderAssignment> findByOrderId(Integer orderId);
    List<OrderAssignment> findByAdminId(Integer adminId, Sort sort);
    List<OrderAssignment> findByAdminIdAndTrangThai(Integer adminId, String trangThai);
    long countByAdminIdAndTrangThai(Integer adminId, String trangThai);

    @Query("SELECT oa FROM OrderAssignment oa WHERE oa.admin.id = :adminId AND oa.trangThai = :trangThai")
    List<OrderAssignment> findActiveByAdminId(@Param("adminId") Integer adminId, @Param("trangThai") String trangThai);

    @Query("SELECT oa.admin.id, COUNT(oa) FROM OrderAssignment oa WHERE oa.trangThai = 'DANG_XU_LY' GROUP BY oa.admin.id ORDER BY COUNT(oa) ASC")
    List<Object[]> findAdminLoadAsc();
}
