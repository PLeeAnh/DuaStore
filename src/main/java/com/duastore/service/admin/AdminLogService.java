package com.duastore.service.admin;

import com.duastore.model.AdminActionLog;
import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.OrderEventType;
import com.duastore.model.User;
import com.duastore.repository.AdminActionLogRepository;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý nhật ký hệ thống.
 */
public class AdminLogService {

    private final AdminActionLogRepository logRepository;
    private final OrderAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogService orderStatusLogService;

    public AdminLogService(AdminActionLogRepository logRepository,
            OrderAssignmentRepository assignmentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderStatusLogService orderStatusLogService) {
        this.logRepository = logRepository;
        this.assignmentRepository = assignmentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderStatusLogService = orderStatusLogService;
    }

    public void ghiLog(User admin, String hanhDong, String loaiEntity, Integer entityId,
            String giaTriCu, String giaTriMoi, String moTa) {
        AdminActionLog log = new AdminActionLog();
        log.setAdmin(admin);
        log.setHanhDong(Objects.requireNonNull(hanhDong));
        log.setLoaiEntity(Objects.requireNonNull(loaiEntity));
        log.setEntityId(Objects.requireNonNull(entityId));
        log.setGiaTriCu(giaTriCu);
        log.setGiaTriMoi(giaTriMoi);
        log.setMoTa(moTa);
        logRepository.save(log);
    }

    public void ghiLogDonHang(User admin, Integer orderId, String hanhDong,
            String giaTriCu, String giaTriMoi, String moTa,
            HttpServletRequest request) {
        AdminActionLog log = new AdminActionLog();
        log.setAdmin(admin);
        log.setHanhDong(hanhDong);
        log.setLoaiEntity("ORDER");
        log.setEntityId(orderId);
        log.setGiaTriCu(giaTriCu);
        log.setGiaTriMoi(giaTriMoi);
        log.setMoTa(moTa);
        if (request != null) {
            log.setIpAddress(request.getRemoteAddr());
        }
        logRepository.save(log);
    }

    public List<AdminActionLog> getLogsByOrder(Integer orderId) {
        return logRepository.findByLoaiEntityAndEntityIdWithAdmin("ORDER", orderId);
    }

    public void tuDongPhanDon(Order order) {
        if (order == null) {
            return;
        }
        if (assignmentRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        List<User> admins = userRepository.findAllActiveAdmins();
        if (admins.isEmpty()) {
            return;
        }

        User adminChon = admins.get(0);
        long minLoad = Long.MAX_VALUE;

        for (User admin : admins) {
            long load = assignmentRepository.countByAdminIdAndTrangThai(admin.getId(), "DANG_XU_LY");
            if (load < minLoad) {
                minLoad = load;
                adminChon = admin;
            }
        }

        phanDonCho(order, adminChon);
    }

    public void phanDonCho(Order order, User admin) {
        if (order == null || admin == null) {
            return;
        }
        if (assignmentRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrder(order);
        assignment.setAdmin(admin);
        assignment.setTrangThai("DANG_XU_LY");
        assignmentRepository.save(assignment);

        orderStatusLogService.ghiLog(order, OrderEventType.ASSIGN_ADMIN, admin, null, null,
                "Phân cho " + admin.getHoTen());

        ghiLogDonHang(admin, order.getId(), "PHAN_DON",
                null, admin.getHoTen(),
                "Tự động phân đơn cho " + admin.getHoTen(), null);
    }

    @Transactional(readOnly = true)
    public OrderAssignment getAssignmentByOrder(Integer orderId) {
        return assignmentRepository.findByOrderId(orderId).orElse(null);
    }

    @Transactional(readOnly = true)
    public long countPendingForAdmin(Integer adminId) {
        return assignmentRepository.countByAdminIdAndTrangThai(adminId, "DANG_XU_LY");
    }

    @Transactional(readOnly = true)
    public Page<AdminActionLog> getAllLogs(int page, int size) {
        return logRepository.findAllWithAdmin(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<User> getActiveAdmins() {
        return userRepository.findAllActiveAdmins();
    }

    @Transactional
    public void reassignAdmin(Order order, User currentAdmin, User newAdmin, HttpServletRequest request) {
        if (order == null || newAdmin == null) {
            return;
        }
        OrderAssignment existing = assignmentRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null) {
            String oldName = existing.getAdmin().getHoTen();
            existing.setAdmin(newAdmin);
            assignmentRepository.save(existing);
            orderStatusLogService.ghiLog(order, OrderEventType.ASSIGN_ADMIN, currentAdmin, null, null,
                    "Phân công lại từ " + oldName + " → " + newAdmin.getHoTen());
            ghiLogDonHang(currentAdmin, order.getId(), "PHAN_CONG_LAI",
                    oldName, newAdmin.getHoTen(),
                    "Phân công lại từ " + oldName + " → " + newAdmin.getHoTen(), request);
        } else {
            OrderAssignment assignment = new OrderAssignment();
            assignment.setOrder(order);
            assignment.setAdmin(newAdmin);
            assignment.setTrangThai("DANG_XU_LY");
            assignmentRepository.save(assignment);
            orderStatusLogService.ghiLog(order, OrderEventType.ASSIGN_ADMIN, currentAdmin, null, null,
                    "Phân cho " + newAdmin.getHoTen());
            ghiLogDonHang(currentAdmin, order.getId(), "PHAN_CONG",
                    null, newAdmin.getHoTen(),
                    "Phân cho " + newAdmin.getHoTen(), request);
        }
    }
}
