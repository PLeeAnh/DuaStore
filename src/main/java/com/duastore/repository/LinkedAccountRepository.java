package com.duastore.repository;

import com.duastore.model.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu tài khoản mạng xã hội liên kết.
 */
public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, Integer> {

    List<LinkedAccount> findByUserId(Integer userId);

    boolean existsByUserIdAndLinkedUserId(Integer userId, Integer linkedUserId);

    void deleteByUserIdAndLinkedUserId(Integer userId, Integer linkedUserId);
}
