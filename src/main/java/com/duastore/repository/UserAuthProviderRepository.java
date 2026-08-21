package com.duastore.repository;

import com.duastore.model.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu người dùng, xác thực đăng nhập.
 */
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Integer> {

    List<UserAuthProvider> findByUserId(Integer userId);

    Optional<UserAuthProvider> findByProviderAndProviderSub(String provider, String providerSub);

    boolean existsByUserIdAndProvider(Integer userId, String provider);
}
