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

    @Query("SELECT COALESCE(MAX(t.balance), 0) FROM LoyaltyTransaction t WHERE t.userId = :userId")
    int findCurrentBalanceByUserId(@Param("userId") Integer userId);

    Optional<LoyaltyTransaction> findFirstByUserIdAndReferenceIdAndType(Integer userId, Integer referenceId, String type);
}
