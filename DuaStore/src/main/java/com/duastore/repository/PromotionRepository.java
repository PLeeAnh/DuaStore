package com.duastore.repository;

import com.duastore.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByMaCodeAndIsActiveTrue(String maCode);
    Optional<Promotion> findByMaCodeIgnoreCase(String maCode);
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND UPPER(p.maCode) = UPPER(:maCode)")
    Optional<Promotion> findByMaCodeIgnoreCaseAndIsActiveTrue(String maCode);
    List<Promotion> findByIsActiveTrueAndDenNgayBefore(LocalDateTime now);
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.tuNgay IS NULL OR p.tuNgay <= :now) AND (p.denNgay IS NULL OR p.denNgay >= :now)")
    List<Promotion> findActiveNow(LocalDateTime now);
    List<Promotion> findByIsActiveTrue();
}
