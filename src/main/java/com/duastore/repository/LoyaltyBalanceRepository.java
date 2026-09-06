package com.duastore.repository;

import com.duastore.model.LoyaltyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác số dư điểm tích lũy hiện tại.
 */
public interface LoyaltyBalanceRepository extends JpaRepository<LoyaltyBalance, Integer> {

    /** Cong/tru diem mot cach nguyen tu (delta co the am, dung cho hoan diem/EARN/EXPIRE). */
    @Modifying
    @Query("UPDATE LoyaltyBalance b SET b.balance = b.balance + :delta WHERE b.userId = :userId")
    int addDelta(@Param("userId") Integer userId, @Param("delta") int delta);

    /**
     * Tru diem mot cach NGUYEN TU chi khi du diem — day la lop bao ve chinh chong
     * double-redeem. Tra ve 0 = khong du diem (khong tru), 1 = tru thanh cong.
     */
    @Modifying
    @Query("UPDATE LoyaltyBalance b SET b.balance = b.balance - :points "
            + "WHERE b.userId = :userId AND b.balance >= :points")
    int deductIfEnough(@Param("userId") Integer userId, @Param("points") int points);
}
