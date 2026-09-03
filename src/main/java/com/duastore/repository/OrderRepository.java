package com.duastore.repository;

import com.duastore.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu đơn hàng.
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Modifying
    @Query("UPDATE Order o SET o.fraudWarning = :warning WHERE o.id = :id")
    void setFraudWarning(@Param("id") Integer id, @Param("warning") String warning);

    Optional<Order> findByMaVanDon(String maVanDon);

    Optional<Order> findByMaDon(String maDon);

    /** Khoa bang PESSIMISTIC_WRITE de cap nhat thanh toan / trang thai an toan duoi concurrency. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Integer id);

    Page<Order> findByUserId(Integer userId, Pageable pageable);

    List<Order> findAllByUserId(Integer userId);

    Page<Order> findByUserIdAndTrangThaiDon(Integer userId, String trangThaiDon, Pageable pageable);

    long countByUserId(Integer userId);

    Page<Order> findAllBy(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.ngayDat BETWEEN :start AND :end")
    long countByNgayDatBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.tongThanhToan), 0) FROM Order o WHERE (o.trangThaiDon = 'DA_GIAO' OR o.trangThaiDon = 'DA_HOAN_THANH') AND o.trangThaiTT = 'DA_THANH_TOAN' AND o.ngayDat BETWEEN :start AND :end")
    BigDecimal sumTongThanhToanByTrangThaiDonAndNgayDatBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o ORDER BY o.ngayDat DESC")
    List<Order> findTop10ByOrderByNgayDatDesc(Pageable pageable);

    @Query(value = "SELECT o FROM Order o ORDER BY o.ngayDat DESC",
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllByOrderByNgayDatDesc(Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY CASE WHEN o.trangThaiDon = 'CHO_XAC_NHAN' THEN 0 ELSE 1 END, o.ngayDat DESC")
    Page<Order> findAllOrderByPriority(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id IN :ids ORDER BY CASE WHEN o.trangThaiDon = 'CHO_XAC_NHAN' THEN 0 ELSE 1 END, o.ngayDat DESC")
    Page<Order> findByIdsWithPriority(@Param("ids") List<Integer> ids, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.trangThaiDon = :trangThaiDon AND o.id IN :ids")
    long countByTrangThaiDonAndIdIn(@Param("trangThaiDon") String trangThaiDon, @Param("ids") List<Integer> ids);

    @Query("SELECT o.trangThaiDon, COUNT(o) FROM Order o GROUP BY o.trangThaiDon")
    List<Object[]> countGroupByTrangThaiDon();

    long countByTrangThaiDon(String trangThaiDon);

    @Query("SELECT o FROM Order o WHERE o.trangThaiDon IN :statuses AND o.ngayDat BETWEEN :start AND :end")
    List<Order> findByTrangThaiDonInAndNgayDatBetween(@Param("statuses") List<String> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.trangThaiDon IN :statuses AND o.ngayDat >= :since")
    List<Order> findCompletedOrdersSince(@Param("statuses") List<String> statuses,
            @Param("since") LocalDateTime since);

    @Query("SELECT o FROM Order o WHERE o.trangThaiDon = :trangThaiDon "
            + "AND (o.trangThaiTT IS NULL OR o.trangThaiTT <> 'DA_THANH_TOAN') "
            + "AND o.ngayDat < :before")
    List<Order> findPendingUnpaidOrdersBefore(@Param("trangThaiDon") String trangThaiDon,
            @Param("before") LocalDateTime before);

    @Query("SELECT o FROM Order o WHERE "
            + "(:q IS NULL OR o.maDon LIKE %:q% OR o.snapTenNguoiNhan LIKE %:q%) AND "
            + "(:trangThai IS NULL OR :trangThai = 'CHUA_HOAN_THANH' OR o.trangThaiDon = :trangThai) AND "
            + "(:trangThai IS NULL OR :trangThai <> 'CHUA_HOAN_THANH' OR o.trangThaiDon <> 'DA_HOAN_THANH') AND "
            + "(:trangThaiTT IS NULL OR o.trangThaiTT = :trangThaiTT) AND "
            + "(:fromDate IS NULL OR o.ngayDat >= :fromDate) AND "
            + "(:toDate IS NULL OR o.ngayDat < :toDate) AND "
            + "(:chuaGan IS NULL OR :chuaGan = false OR NOT EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id)) AND "
            + "(:assignedAdminId IS NULL OR EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id AND a.admin.id = :assignedAdminId)) ")
    Page<Order> searchOrders(@Param("q") String q, @Param("trangThai") String trangThai,
            @Param("trangThaiTT") String trangThaiTT,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("chuaGan") Boolean chuaGan,
            @Param("assignedAdminId") Integer assignedAdminId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id IN :ids AND "
            + "(:q IS NULL OR o.maDon LIKE %:q% OR o.snapTenNguoiNhan LIKE %:q%) AND "
            + "(:trangThai IS NULL OR :trangThai = 'CHUA_HOAN_THANH' OR o.trangThaiDon = :trangThai) AND "
            + "(:trangThai IS NULL OR :trangThai <> 'CHUA_HOAN_THANH' OR o.trangThaiDon <> 'DA_HOAN_THANH') AND "
            + "(:trangThaiTT IS NULL OR o.trangThaiTT = :trangThaiTT) AND "
            + "(:fromDate IS NULL OR o.ngayDat >= :fromDate) AND "
            + "(:toDate IS NULL OR o.ngayDat < :toDate) AND "
            + "(:chuaGan IS NULL OR :chuaGan = false OR NOT EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id)) AND "
            + "(:assignedAdminId IS NULL OR EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id AND a.admin.id = :assignedAdminId)) ")
    Page<Order> searchOrdersByIds(@Param("ids") List<Integer> ids,
            @Param("q") String q,
            @Param("trangThai") String trangThai,
            @Param("trangThaiTT") String trangThaiTT,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("chuaGan") Boolean chuaGan,
            @Param("assignedAdminId") Integer assignedAdminId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE NOT EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id)")
    List<Order> findUnassignedOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE NOT EXISTS (SELECT a FROM OrderAssignment a WHERE a.order.id = o.id)")
    long countUnassignedOrders();

    @Query("SELECT o.user.id, COUNT(o) FROM Order o WHERE o.user.id IN :userIds GROUP BY o.user.id")
    List<Object[]> countByUserIds(@Param("userIds") List<Integer> userIds);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.trangThaiDon = :trangThai AND o.ngayDat BETWEEN :start AND :end")
    long countByTrangThaiDonAndNgayDatBetween(@Param("trangThai") String trangThai,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o.phuongThucTT, COUNT(o) FROM Order o WHERE o.ngayDat BETWEEN :start AND :end GROUP BY o.phuongThucTT")
    List<Object[]> countGroupByPhuongThucTTAndNgayDatBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT AVG(o.tongThanhToan) FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO', 'DA_HOAN_THANH') AND o.ngayDat BETWEEN :start AND :end")
    BigDecimal avgTongThanhToanByNgayDatBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.ngayDat BETWEEN :start AND :end ORDER BY o.ngayDat DESC")
    List<Order> findByNgayDatBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.phuongThucTT = :phuongThuc AND o.ngayDat BETWEEN :start AND :end")
    long countByPhuongThucTTAndNgayDatBetween(@Param("phuongThuc") String phuongThuc,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.trangThaiDon = :trangThai AND o.ngayDat < :before")
    long countByTrangThaiDonAndNgayDatBefore(@Param("trangThai") String trangThai,
            @Param("before") LocalDateTime before);

    @Query("SELECT o.promotion.id, COUNT(o) FROM Order o WHERE o.promotion IS NOT NULL GROUP BY o.promotion.id")
    List<Object[]> countOrdersByPromotion();

    @Modifying
    @Query("UPDATE Order o SET o.promotion = null WHERE o.promotion.id = :id")
    int clearPromotionReference(@Param("id") Integer id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.snapSoDienThoai = :phone AND o.trangThaiDon = 'DA_HUY' AND o.ngayDat >= :since")
    long countCancelledByPhoneSince(@Param("phone") String phone, @Param("since") LocalDateTime since);

    @Query("SELECT o FROM Order o WHERE "
            + "(:q IS NULL OR :q = '' OR LOWER(o.maDon) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(o.snapTenNguoiNhan) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(o.snapSoDienThoai) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY o.ngayDat DESC")
    List<Order> searchOrdersAutocomplete(@Param("q") String q, Pageable pageable);

    @Query("SELECT o.user.id, SUM(o.tongThanhToan) FROM Order o "
            + "WHERE o.user.id IN :ids AND (o.trangThaiDon = 'DA_GIAO' OR o.trangThaiDon = 'DA_HOAN_THANH') AND o.trangThaiTT = 'DA_THANH_TOAN' "
            + "GROUP BY o.user.id")
    List<Object[]> sumTotalSpentByUserIds(@Param("ids") List<Integer> ids);

    @Query("SELECT o.phuongThucTT, SUM(o.tongThanhToan), COUNT(o) FROM Order o WHERE o.trangThaiDon IN ('DA_GIAO', 'DA_HOAN_THANH') AND o.trangThaiTT = 'DA_THANH_TOAN' AND o.ngayDat BETWEEN :start AND :end GROUP BY o.phuongThucTT")
    List<Object[]> sumRevenueGroupByPhuongThucTT(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
