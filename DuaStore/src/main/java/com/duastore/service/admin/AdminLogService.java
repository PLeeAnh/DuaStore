package com.duastore.service.admin;

import com.duastore.model.AdminActionLog;
import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.User;
import com.duastore.repository.AdminActionLogRepository;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminLogService {

    private final AdminActionLogRepository logRepository;
    private final OrderAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminLogService(AdminActionLogRepository logRepository,
                           OrderAssignmentRepository assignmentRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository) {
        this.logRepository = logRepository;
        this.assignmentRepository = assignmentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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
        return logRepository.findByLoaiEntityAndEntityId("ORDER", orderId,
                Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    public void tuDongPhanDon(Order order) {
        if (order == null) return;
        if (assignmentRepository.findByOrderId(order.getId()).isPresent()) return;

        List<User> admins = userRepository.findAllActiveAdmins();
        if (admins.isEmpty()) return;

        User adminChon = admins.get(0);
        long minLoad = Long.MAX_VALUE;

        for (User admin : admins) {
            long load = assignmentRepository.countByAdminIdAndTrangThai(admin.getId(), "DANG_XU_LY");
            if (load < minLoad) {
                minLoad = load;
                adminChon = admin;
            }
        }

        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrder(order);
        assignment.setAdmin(adminChon);
        assignment.setTrangThai("DANG_XU_LY");
        assignmentRepository.save(assignment);

        ghiLogDonHang(adminChon, order.getId(), "PHAN_DON",
                null, adminChon.getHoTen(),
                "Tự động phân đơn cho " + adminChon.getHoTen(), null);
    }

    @Transactional(readOnly = true)
    public OrderAssignment getAssignmentByOrder(Integer orderId) {
        return assignmentRepository.findByOrderId(orderId).orElse(null);
    }

    @Transactional(readOnly = true)
    public long countPendingForAdmin(Integer adminId) {
        return assignmentRepository.countByAdminIdAndTrangThai(adminId, "DANG_XU_LY");
    }
}
