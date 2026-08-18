package com.duastore.repository;

import com.duastore.model.RefundRequest;
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
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {

    List<RefundRequest> findByUserIdOrderByNgayYeuCauDesc(Integer userId);

    List<RefundRequest> findAllByOrderByNgayYeuCauDesc();

    long countByTrangThai(String trangThai);

    long countByTrangThaiAndNgayYeuCauBetween(String trangThai, LocalDateTime start, LocalDateTime end);

    boolean existsByOrderId(Integer orderId);

    // New methods for glass refund features
    Optional<RefundRequest> findByOrderIdAndLoaiYeuCau(Integer orderId, String loaiYeuCau);

    List<RefundRequest> findByUserIdAndTrangThaiOrderByNgayYeuCauDesc(Integer userId, String trangThai);

    List<RefundRequest> findByLoaiYeuCauAndTrangThaiOrderByNgayYeuCauDesc(String loaiYeuCau, String trangThai);

    List<RefundRequest> findByTrangThaiAndNgayYeuCauBetweenOrderByNgayYeuCauDesc(String trangThai, LocalDateTime start, LocalDateTime end);

    long countByTrangThaiInAndNgayYeuCauBetween(List<String> trangThaiList, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM RefundRequest r WHERE r.orderId = :orderId AND r.trangThai NOT IN ('TU_CHOI', 'TU_CHOI_HOAN_TIEN')")
    Optional<RefundRequest> findActiveRefundByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT r FROM RefundRequest r WHERE r.variantMoiId = :variantId")
    List<RefundRequest> findByVariantMoiId(@Param("variantId") Integer variantId);

    Page<RefundRequest> findByTrangThaiOrderByNgayYeuCauDesc(String trangThai, Pageable pageable);

    Page<RefundRequest> findByUserIdAndTrangThaiInOrderByNgayYeuCauDesc(Integer userId, List<String> trangThaiList, Pageable pageable);
}