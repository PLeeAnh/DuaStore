package com.duastore.repository;

import com.duastore.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    Optional<User> findByResetToken(String resetToken);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.isActive = true AND r.name = :role")
    long countByRoleAndIsActiveTrue(@Param("role") String role);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u")
    Page<User> findAllBy(Pageable pageable);

    @Query(value = "SELECT DISTINCT u FROM User u JOIN FETCH u.roles r WHERE r.name = :role",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :role")
    Page<User> findByRole(@Param("role") String role, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('ADMIN', 'SUPER_ADMIN') AND u.isActive = true")
    java.util.List<User> findAllActiveAdmins();

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.isActive = true")
    long countActiveByRoleName(@Param("role") String role);
}
