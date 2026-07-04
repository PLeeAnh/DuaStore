package com.duastore.repository;

import com.duastore.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {

    List<RefundRequest> findByUserIdOrderByNgayYeuCauDesc(Integer userId);

    List<RefundRequest> findAllByOrderByNgayYeuCauDesc();

    long countByTrangThai(String trangThai);

    long countByTrangThaiAndNgayYeuCauBetween(String trangThai, LocalDateTime start, LocalDateTime end);
}
