package com.duastore.repository;

import com.duastore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserIdOrderByNgayThemDesc(Integer userId);
    Optional<CartItem> findByUserIdAndVariantId(Integer userId, Integer variantId);
    int countByUserId(Integer userId);
    void deleteByIdAndUserId(Integer id, Integer userId);
}
