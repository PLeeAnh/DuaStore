package com.duastore.repository;

import com.duastore.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByMaCodeAndIsActiveTrue(String maCode);
    Optional<Promotion> findByMaCodeIgnoreCase(String maCode);
    List<Promotion> findByIsActiveTrueAndDenNgayBefore(LocalDateTime now);
}
