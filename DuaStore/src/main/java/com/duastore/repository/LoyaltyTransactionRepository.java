package com.duastore.repository;

import com.duastore.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {

    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT COALESCE((SELECT t.balance FROM LoyaltyTransaction t WHERE t.userId = :userId ORDER BY t.id DESC OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY), 0)")
    int findCurrentBalanceByUserId(@Param("userId") Integer userId);

    Optional<LoyaltyTransaction> findFirstByUserIdAndReferenceIdAndType(Integer userId, Integer referenceId, String type);
}
