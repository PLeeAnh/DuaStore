package com.duastore.repository;

import com.duastore.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {

    @Query("SELECT f FROM FlashSale f WHERE f.isActive = true AND f.ngayBatDau <= :now AND f.ngayKetThuc >= :now")
    List<FlashSale> findActiveNow(LocalDateTime now);

    List<FlashSale> findByProductIdInAndIsActiveTrue(List<Integer> productIds);
}
