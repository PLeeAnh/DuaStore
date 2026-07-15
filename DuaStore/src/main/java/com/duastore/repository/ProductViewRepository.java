package com.duastore.repository;

import com.duastore.model.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    List<ProductView> findByUserIdOrderByViewedAtDesc(Integer userId);

    long countByUserIdAndProductId(Integer userId, Integer productId);

    @Query("SELECT pv.productId, COUNT(pv) AS cnt FROM ProductView pv " +
            "WHERE pv.userId = :userId GROUP BY pv.productId ORDER BY cnt DESC")
    List<Object[]> findMostViewedProductIdsByUserId(@Param("userId") Integer userId);

    @Query("SELECT pv.productId FROM ProductView pv WHERE pv.userId = :userId AND pv.viewedAt > :since")
    List<Integer> findProductIdsViewedByUserSince(@Param("userId") Integer userId, @Param("since") LocalDateTime since);

    @Query("SELECT pv.userId, COUNT(pv) AS cnt FROM ProductView pv " +
            "WHERE pv.viewedAt > :since GROUP BY pv.userId")
    List<Object[]> countViewsByUserSince(@Param("since") LocalDateTime since);
}
