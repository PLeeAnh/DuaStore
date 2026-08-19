package com.duastore.repository;

import com.duastore.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {

    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT COALESCE((SELECT t.balance FROM LoyaltyTransaction t WHERE t.userId = :userId ORDER BY t.id DESC OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY), 0)")
    int findCurrentBalanceByUserId(@Param("userId") Integer userId);

    @Query("SELECT t.userId, COALESCE(MAX(t.balance), 0) FROM LoyaltyTransaction t "
            + "WHERE t.userId IN :userIds GROUP BY t.userId")
    List<Object[]> findCurrentBalanceByUserIds(@Param("userIds") List<Integer> userIds);

    Optional<LoyaltyTransaction> findFirstByUserIdAndReferenceIdAndType(Integer userId, Integer referenceId, String type);

    @Query("SELECT t FROM LoyaltyTransaction t WHERE t.userId = :userId AND t.type = 'EARNED' AND t.createdAt < :threshold AND t.points > 0 ORDER BY t.createdAt ASC")
    List<LoyaltyTransaction> findOldEarnedTransactionsForExpiry(@Param("userId") Integer userId, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT DISTINCT t.userId FROM LoyaltyTransaction t WHERE t.type = 'EARNED' AND t.createdAt < :threshold AND t.points > 0")
    List<Integer> findUserIdsWithOldEarnedTransactions(@Param("threshold") LocalDateTime threshold);
}
