package com.duastore.repository;

import com.duastore.model.SavedCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedCartItemRepository extends JpaRepository<SavedCartItem, Integer> {
    List<SavedCartItem> findByUserIdOrderByNgayLuuDesc(Integer userId);
    Optional<SavedCartItem> findByUserIdAndVariantId(Integer userId, Integer variantId);
    int countByUserId(Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
