package com.duastore.repository;

import com.duastore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu người dùng.
 */
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

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('ADMIN', 'PRODUCT_OWNER', 'STAFF') AND u.isActive = true")
    java.util.List<User> findAllActiveAdmins();

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.isActive = true")
    long countActiveByRoleName(@Param("role") String role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.isActive = :isActive")
    long countByRoleAndIsActive(@Param("role") String role, @Param("isActive") boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.ngayTao BETWEEN :start AND :end")
    long countByNgayTaoBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.ngayTao BETWEEN :start AND :end")
    long countByRoleAndNgayTaoBetween(@Param("role") String role, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE "
            + "(:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u WHERE "
            + "(:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)")
    Page<User> searchByKeywordAndStatus(@Param("keyword") String keyword,
            @Param("status") String status,
            @Param("role") String role,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles "
            + "WHERE (:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:city IS NULL OR u.id IN (SELECT a.userId FROM Address a WHERE a.tinhThanh = :city)) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u "
            + "WHERE (:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:city IS NULL OR u.id IN (SELECT a.userId FROM Address a WHERE a.tinhThanh = :city)) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)")
    Page<User> searchByKeywordStatusAndCity(@Param("keyword") String keyword,
            @Param("status") String status,
            @Param("city") String city,
            @Param("role") String role,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles "
            + "WHERE (:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:spendingTier IS NULL OR (:spendingTier = 'vip' AND u.id IN "
            + "(SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) >= 10000000)) "
            + "OR (:spendingTier = 'medium' AND u.id IN "
            + "(SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) BETWEEN 2000000 AND 9999999)) "
            + "OR (:spendingTier = 'new' AND (u.id NOT IN "
            + "(SELECT DISTINCT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH')) "
            + "OR u.id IN (SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) < 2000000)))) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u "
            + "WHERE (:keyword IS NULL OR LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:status IS NULL OR (:status = 'active' AND u.isActive = true) OR (:status = 'inactive' AND u.isActive = false)) "
            + "AND (:spendingTier IS NULL OR (:spendingTier = 'vip' AND u.id IN "
            + "(SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) >= 10000000)) "
            + "OR (:spendingTier = 'medium' AND u.id IN "
            + "(SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) BETWEEN 2000000 AND 9999999)) "
            + "OR (:spendingTier = 'new' AND (u.id NOT IN "
            + "(SELECT DISTINCT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH')) "
            + "OR u.id IN (SELECT o.user.id FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO','DA_HOAN_THANH') GROUP BY o.user.id HAVING SUM(o.tongThanhToan) < 2000000)))) "
            + "AND (:role IS NULL OR :role MEMBER OF u.roles)")
    Page<User> searchByKeywordStatusAndSpending(@Param("keyword") String keyword,
            @Param("status") String status,
            @Param("spendingTier") String spendingTier,
            @Param("role") String role,
            Pageable pageable);

    @Query("SELECT u.id, u.hoTen, COUNT(o) as orderCount, COALESCE(SUM(o.tongThanhToan), 0) as totalSpent "
            + "FROM Order o JOIN o.user u "
            + "WHERE o.trangThaiDon IN ('DA_GIAO', 'DA_HOAN_THANH') AND o.ngayDat BETWEEN :start AND :end "
            + "GROUP BY u.id, u.hoTen ORDER BY totalSpent DESC")
    List<Object[]> findTopCustomersByRevenue(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT u.id, COUNT(o), COALESCE(SUM(o.tongThanhToan), 0) "
            + "FROM Order o JOIN o.user u "
            + "WHERE o.trangThaiDon IN ('DA_GIAO', 'DA_HOAN_THANH') "
            + "GROUP BY u.id")
    List<Object[]> findCustomerLifetimeStats();

    @Query("SELECT COUNT(o), COALESCE(SUM(o.tongThanhToan), 0), MAX(o.ngayDat) "
            + "FROM Order o JOIN o.user u "
            + "WHERE o.trangThaiDon IN ('DA_GIAO', 'DA_HOAN_THANH') "
            + "GROUP BY u.id")
    List<Object[]> findRFMData();
}
