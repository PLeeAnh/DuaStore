package com.duastore.repository;

import com.duastore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.fraudWarning IS NOT NULL AND o.fraudWarning <> ''")
    long countByUserIdAndFraudWarningPresent(@Param("userId") Integer userId);

    Optional<Order> findByMaVanDon(String maVanDon);

    Optional<Order> findByMaDon(String maDon);

    /**
     * Nap san User + OrderItems trong 1 query — dung cho cac cho can dua Order sang
     * AsyncEmailService (chay o thread khac, khong con Hibernate session): neu de
     * lazy-load tu nhien, truy cap order.getUser()/getOrderItems() tren thread async
     * se nem loi "session is null" vi session request-scope da dong truoc do.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithUserAndItems(@Param("id") Integer id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderItems WHERE o.maDon = :maDon")
    Optional<Order> findByMaDonWithUserAndItems(@Param("maDon") String maDon);

    /**
     * Chuyen sang DA_THANH_TOAN mot cach ATOMIC — tra ve so dong bi anh huong (0 hoac 1).
     * Dung thay cho "SELECT...FOR UPDATE" (@Lock PESSIMISTIC_WRITE): da xac nhan bang test
     * thuc te (2 request webhook gui dong thoi) rang pessimistic lock qua Hibernate + SQL
     * Server o moi truong nay KHONG chan duoc race — ca hai request van doc thay CHUA_THANH_TOAN
     * va cung "thanh cong". UPDATE ... WHERE dieu kien la thao tac nguyen tu that su o muc DB
     * engine (giong cach decrementStock tru ton kho an toan duoi concurrency), khong phu thuoc
     * vao viec ORM dich dung lock hint hay khong. Goi tra ve 1 -> chinh request nay la request
     * THAT SU chuyen trang thai (chi request do moi duoc log/gui email/phan cong); tra ve 0 ->
     * don da duoc request khac xu ly roi, bo qua.
     */
    @Modifying
    @Query("UPDATE Order o SET o.trangThaiTT = 'DA_THANH_TOAN' WHERE o.id = :id AND o.trangThaiTT <> 'DA_THANH_TOAN'")
    int markPaidIfUnpaid(@Param("id") Integer id);

    /**
     * Chuyen DA_GIAO -> DA_HOAN_THANH mot cach ATOMIC — cung ly do voi markPaidIfUnpaid:
     * khach co the double-click "Da nhan duoc hang", hoac khach tu xac nhan ngay luc admin
     * cung dang doi trang thai don sang Hoan thanh — ca hai deu co the doc thay DA_GIAO va
     * cung tinh la "lan dau" neu khong co UPDATE dieu kien nay, dan toi cong diem tich luy
     * va gui email cam on 2 lan. Tra ve so dong bi anh huong (0 hoac 1).
     */
    @Modifying
    @Query("UPDATE Order o SET o.trangThaiDon = 'DA_HOAN_THANH', o.trangThaiTT = 'DA_THANH_TOAN' WHERE o.id = :id AND o.trangThaiDon = 'DA_GIAO'")
    int markCompletedIfDelivered(@Param("id") Integer id);

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
