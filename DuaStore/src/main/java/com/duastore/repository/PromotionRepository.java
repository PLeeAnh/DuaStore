package com.duastore.repository;

import com.duastore.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    // CẦN GIỮ LẠI: Hàm này phía Client (OrderService) đang dùng để check mã khi khách mua hàng
    Optional<Promotion> findByMaCodeAndIsActiveTrue(String maCode);
    // CẦN THÊM: Hàm này phía Admin dùng để check trùng mã lúc Thêm/Sửa
    Optional<Promotion> findByMaCodeIgnoreCase(String maCode);
}
