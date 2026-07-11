package com.duastore.repository;

import com.duastore.model.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    List<PriceHistory> findByVariantIdOrderByNgayThayDoiDesc(Integer variantId);
    List<PriceHistory> findByProductIdOrderByNgayThayDoiDesc(Integer productId);
    Page<PriceHistory> findAllByOrderByNgayThayDoiDesc(Pageable pageable);
}
