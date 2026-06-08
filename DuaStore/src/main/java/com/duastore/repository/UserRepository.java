package com.duastore.repository;

import com.duastore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.role = :role")
    long countByRoleAndIsActiveTrue(@Param("role") String role);

    Page<User> findAllBy(Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);
}
