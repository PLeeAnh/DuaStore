package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.OrderStatusDTO;
import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.User;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.AsyncEmailService;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminOrderService;
import com.duastore.service.admin.FraudDetectionService;
import com.duastore.service.admin.OrderNoteService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.service.client.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/don-hang")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới đơn hàng.
 */
public class AdminOrderController {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderController.class);

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;
    private final AdminLogService adminLogService;
    private final SecurityUtil securityUtil;
    private final OrderStatusLogService orderStatusLogService;
    private final OrderNoteService orderNoteService;
    private final NotificationHelper notificationHelper;
    private final OrderRepository orderRepository;
    private final com.duastore.repository.ProductRepository productRepository;
    private final com.duastore.repository.ProductVariantRepository variantRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AsyncEmailService asyncEmailService;
    private final UserRepository userRepository;
    private final OrderAssignmentRepository assignmentRepository;

    public AdminOrderController(AdminOrderService adminOrderService,
            OrderService orderService,
            AdminLogService adminLogService,
            SecurityUtil securityUtil,
            OrderStatusLogService orderStatusLogService,
            OrderNoteService orderNoteService,
            NotificationHelper notificationHelper,
            OrderRepository orderRepository,
            com.duastore.repository.ProductRepository productRepository,
            com.duastore.repository.ProductVariantRepository variantRepository,
            FraudDetectionService fraudDetectionService,
            AsyncEmailService asyncEmailService,
            UserRepository userRepository,
            OrderAssignmentRepository assignmentRepository) {
        this.adminOrderService = adminOrderService;
        this.orderService = orderService;
        this.adminLogService = adminLogService;
        this.securityUtil = securityUtil;
        this.orderStatusLogService = orderStatusLogService;
        this.orderNoteService = orderNoteService;
        this.notificationHelper = notificationHelper;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.asyncEmailService = asyncEmailService;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public String listOrders(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean tatCa,
            @RequestParam(defaultValue = "false") boolean chuaGan,
            @RequestParam(required = false) Integer assignedAdminId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String trangThai,
            @RequestParam(name = "trangThaiTT", required = false) String trangThaiTT,
            @RequestParam(name = "fromDate", required = false) String fromDateStr,
            @RequestParam(name = "toDate", required = false) String toDateStr,
            @RequestParam(required = false, defaultValue = "ngayDat") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            Model model) {
        User admin = securityUtil.getCurrentUser();
        if (admin == null) {
            return "redirect:/dang-nhap";
        }

        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        String filterTT = (trangThai != null && !trangThai.isBlank()) ? trangThai : "CHUA_HOAN_THANH";
        String filterTTTT = (trangThaiTT != null && !trangThaiTT.isBlank()) ? trangThaiTT : null;

        java.time.LocalDateTime fromDate = parseDateStart(fromDateStr);
        java.time.LocalDateTime toDate = parseDateEndExclusive(toDateStr);

        Page<Order> orderPage;
        if (tatCa) {
            orderPage = adminOrderService.getAllOrders(page, size, query, filterTT, filterTTTT, fromDate, toDate, chuaGan, assignedAdminId, sort, dir);
        } else {
            orderPage = adminOrderService.getMyOrders(admin.getId(), page, size, query, filterTT, filterTTTT, fromDate, toDate, chuaGan, assignedAdminId, sort, dir);
        }
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());

        // Bản đồ orderId → nhân viên phụ trách để hiển thị trong bảng
        Map<Integer, OrderAssignment> assignmentMap = new HashMap<>();
        if (!orderDTOs.isEmpty()) {
            List<Integer> ids = orderDTOs.stream().map(OrderDTO::getId).collect(Collectors.toList());
            for (OrderAssignment a : assignmentRepository.findByOrderIdIn(ids)) {
                assignmentMap.put(a.getOrder().getId(), a);
            }
        }
        model.addAttribute("assignmentMap", assignmentMap);
        model.addAttribute("currentAdminId", admin.getId());

        // Thống kê khối lượng công việc từng nhân viên (đơn đang xử lý)
        Map<Integer, Long> staffLoad = new HashMap<>();
        Map<Integer, User> staffUsers = new HashMap<>();
        List<Object[]> loadRows = assignmentRepository.countActiveByAdmin();
        List<Integer> staffIds = loadRows.stream()
                .map(r -> ((Number) r[0]).intValue())
                .collect(Collectors.toList());
        if (!staffIds.isEmpty()) {
            for (User u : userRepository.findAllById(staffIds)) {
                staffUsers.put(u.getId(), u);
            }
        }
        for (Object[] r : loadRows) {
            staffLoad.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }
        model.addAttribute("staffLoad", staffLoad);
        model.addAttribute("staffUsers", staffUsers);
        model.addAttribute("unassignedCount", orderRepository.countUnassignedOrders());

        model.addAttribute("orders", orderDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "đơn hàng");
        model.addAttribute("url", "/admin/don-hang");
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("tatCa", tatCa);
        if (query != null) {
            filterParams.put("q", query);
        }
        if (filterTT != null) {
            filterParams.put("trangThai", filterTT);
        }
        if (filterTTTT != null) {
            filterParams.put("trangThaiTT", filterTTTT);
        }
        if (fromDate != null) {
            filterParams.put("fromDate", fromDateStr);
        }
        if (toDate != null) {
            filterParams.put("toDate", toDateStr);
        }
        if (chuaGan) {
            filterParams.put("chuaGan", true);
        }
        if (assignedAdminId != null) {
            filterParams.put("assignedAdminId", assignedAdminId);
        }
        model.addAttribute("filterParams", filterParams);
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("chuaGan", chuaGan);
        model.addAttribute("assignedAdminId", assignedAdminId);
        model.addAttribute("q", q);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("trangThaiTT", trangThaiTT);
        model.addAttribute("fromDate", fromDateStr);
        model.addAttribute("toDate", toDateStr);
        model.addAttribute("title", "don-hang");
        model.addAttribute("orderTab", "don-hang");

        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("pendingOrdersCount", orderRepository.countByTrangThaiDon("CHO_XAC_NHAN"));
        model.addAttribute("completedOrdersCount", orderRepository.countByTrangThaiDon("DA_HOAN_THANH"));
        model.addAttribute("cancelledOrdersCount", orderRepository.countByTrangThaiDon("DA_HUY"));
        return "view/admin/order/order-list";
    }

    private java.time.LocalDateTime parseDateStart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateStr).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private java.time.LocalDateTime parseDateEndExclusive(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateStr).plusDays(1).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/{id}/debug")
    @ResponseBody
    @Profile("dev")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public ResponseEntity<Map<String, Object>> debugOrder(@PathVariable Integer id) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Order order = adminOrderService.getOrderById(id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            result.put("order", orderDTO);
            var logs = adminLogService.getLogsByOrder(id);
            result.put("logsCount", logs.size());
            var assignment = adminLogService.getAssignmentByOrder(id);
            result.put("hasAssignment", assignment != null);
            var statusLogs = orderStatusLogService.getLogsByOrder(id);
            result.put("statusLogsCount", statusLogs.size());
            for (var sl : statusLogs) {
                if (sl.getNguoiThucHien() != null) {
                    sl.getNguoiThucHien().getHoTen();
                }
            }
            var notes = orderNoteService.getNotesByOrder(id);
            result.put("notesCount", notes.size());
            for (var n : notes) {
                n.getAdmin().getHoTen();
            }
            result.put("success", true);
        } catch (Exception e) {
            log.error("DEBUG Loi khi lay data don hang #{}: {}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("type", e.getClass().getName());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public String orderDetail(@PathVariable Integer id, Model model) {
        try {
            User admin = securityUtil.getCurrentUser();
            Order order = adminOrderService.getOrderById(id);
            if (order.getFraudWarning() == null) {
                fraudDetectionService.analyzeAndPersist(order);
                order = adminOrderService.getOrderById(id);
            }
            String rawWarning = order.getFraudWarning();
            List<String> fraudWarnings = rawWarning != null ? Arrays.asList(rawWarning.split(";;")) : null;
            model.addAttribute("fraudWarnings", fraudWarnings);

            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            var logs = adminLogService.getLogsByOrder(id);
            var assignment = adminLogService.getAssignmentByOrder(id);

            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("logs", logs);
            model.addAttribute("assignment", assignment);
            List<com.duastore.model.OrderStatusLog> allLogs = orderStatusLogService.getLogsByOrder(id);
            model.addAttribute("statusLogs", allLogs);
            model.addAttribute("paymentLogs", allLogs.stream().filter(l -> l.getLoaiSuKien() == com.duastore.model.OrderEventType.PAYMENT_CONFIRMED).toList());
            model.addAttribute("currentStep", com.duastore.util.OrderStatusUtil.getStepIndex(order.getTrangThaiDon()));
            model.addAttribute("notes", orderNoteService.getNotesByOrder(id));
            model.addAttribute("activeAdmins", adminLogService.getActiveAdmins());
            OrderStatusDTO statusDTO = new OrderStatusDTO();
            statusDTO.setTrangThaiDon(order.getTrangThaiDon());
            statusDTO.setTrangThaiTT(order.getTrangThaiTT());
            model.addAttribute("statusDTO", statusDTO);
            model.addAttribute("title", "don-hang");
            return "view/admin/order/order-detail";
        } catch (Exception e) {
            log.error("Loi khi xem chi tiet don hang #{}: {}", id, e.getMessage(), e);
            return "redirect:/admin/don-hang";
        }
    }

    @PostMapping("/{id}/cap-nhat-trang-thai")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public String updateStatus(@PathVariable Integer id,
            @Valid @ModelAttribute("statusDTO") OrderStatusDTO dto,
            BindingResult result,
            RedirectAttributes ra,
            HttpServletRequest request) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Dữ liệu không hợp lệ");
            return "redirect:/admin/don-hang/" + id;
        }
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng");
                return "redirect:/admin/don-hang/" + id;
            }

            Order order = adminOrderService.getOrderById(id);

            if ("DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
                ra.addFlashAttribute("errorMsg", "Đơn đã hoàn thành, không thể thay đổi");
                return "redirect:/admin/don-hang/" + id;
            }

            String oldStatus = order.getTrangThaiDon();

            boolean wasUnpaid = "CHUA_THANH_TOAN".equals(order.getTrangThaiTT());
            String newStatus = dto.getTrangThaiDon();

            if (!"DA_HUY".equals(newStatus) && order.getUser() != null && order.getUser().getEmail() != null) {
                String statusName = AdminOrderService.getStatusName(newStatus);
                // Email gui async, khong chan thao tac cap nhat trang thai.
                asyncEmailService.sendOrderStatus(
                        order.getUser().getEmail(), order.getUser().getHoTen(),
                        order.getMaDon(), statusName);
            }

            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, newStatus, oldStatus, admin, request);
            if (!"DA_HUY".equals(newStatus)) {
                if (wasUnpaid && "DA_HOAN_THANH".equals(newStatus)) {
                    ra.addFlashAttribute("warningMsg", "Khách hàng chưa thanh toán. Bạn đã xác nhận thanh toán thay khách hàng.");
                }
                try {
                    String statusName = AdminOrderService.getStatusName(newStatus);
                    notificationHelper.notifyAll(
                            "Đơn hàng " + order.getMaDon() + " đã chuyển sang trạng thái: " + statusName,
                            "ORDER", order.getId(),
                            "/tai-khoan/don-hang/" + order.getId(),
                            order.getMaDon(),
                            order.getUser() != null ? order.getUser().getId() : null
                    );
                } catch (Exception ignored) {
                }
            }
            String msg;
            if ("DA_HUY".equals(newStatus)) {
                msg = "Đã xóa đơn hàng";
            } else {
                msg = "Cập nhật trạng thái thành công";
            }
            if (stockMsg != null) {
                msg += ". " + stockMsg;
            }
            ra.addFlashAttribute("successMsg", msg);
            if ("DA_HUY".equals(newStatus)) {
                return "redirect:/admin/don-hang";
            }
            Order updatedOrder = adminOrderService.getOrderById(id);
            if (dto.getTrangThaiTT() != null && !dto.getTrangThaiTT().isBlank()
                    && !dto.getTrangThaiTT().equals(updatedOrder.getTrangThaiTT())) {
                adminOrderService.updatePaymentStatusWithLog(id, dto.getTrangThaiTT(), updatedOrder.getTrangThaiTT(), admin, request);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/api/{id}/cap-nhat-trang-thai")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public ResponseEntity<Map<String, Object>> updateStatusInline(@PathVariable Integer id,
            @RequestParam String trangThai,
            HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                result.put("success", false);
                result.put("message", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(result);
            }

            Order order = adminOrderService.getOrderById(id);

            if ("DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
                result.put("success", false);
                result.put("message", "Đơn đã hoàn thành, không thể thay đổi");
                return ResponseEntity.ok(result);
            }

            String oldStatus = order.getTrangThaiDon();
            boolean wasUnpaid = "CHUA_THANH_TOAN".equals(order.getTrangThaiTT());

            if (!"DA_HUY".equals(trangThai) && order.getUser() != null && order.getUser().getEmail() != null) {
                String statusName = AdminOrderService.getStatusName(trangThai);
                asyncEmailService.sendOrderStatus(
                        order.getUser().getEmail(), order.getUser().getHoTen(),
                        order.getMaDon(), statusName);
            }

            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, trangThai, oldStatus, admin, request);
            if (!"DA_HUY".equals(trangThai)) {
                try {
                    String statusName = AdminOrderService.getStatusName(trangThai);
                    notificationHelper.notifyAll(
                            "Đơn hàng " + order.getMaDon() + " đã chuyển sang trạng thái: " + statusName,
                            "ORDER", order.getId(),
                            "/tai-khoan/don-hang/" + order.getId(),
                            order.getMaDon(),
                            order.getUser() != null ? order.getUser().getId() : null
                    );
                } catch (Exception ignored) {
                }
            }
            String msg;
            if ("DA_HUY".equals(trangThai)) {
                msg = "Đã xóa đơn hàng";
            } else {
                msg = "Cập nhật trạng thái thành công";
            }
            if (stockMsg != null) {
                msg += ". " + stockMsg;
            }
            if (wasUnpaid && "DA_HOAN_THANH".equals(trangThai)) {
                msg += " Khách chưa thanh toán — bạn đã xác nhận thay khách.";
            }

            result.put("success", true);
            result.put("message", msg);
            result.put("oldStatus", oldStatus);
            result.put("newStatus", trangThai);
            if ("DA_HUY".equals(trangThai)) {
                result.put("deleted", true);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/batch-cap-nhat-trang-thai")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public ResponseEntity<Map<String, Object>> batchUpdateStatus(@RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                result.put("success", false);
                result.put("message", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(result);
            }

            @SuppressWarnings("unchecked")
            List<Integer> ids = ((List<Integer>) body.get("ids"));
            String status = (String) body.get("status");

            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "Danh sách đơn hàng trống");
                return ResponseEntity.ok(result);
            }
            if (status == null || status.isBlank()) {
                result.put("success", false);
                result.put("message", "Trạng thái không hợp lệ");
                return ResponseEntity.ok(result);
            }

            int updated = 0;
            List<String> errors = new java.util.ArrayList<>();

            for (Integer id : ids) {
                try {
                    Order order = adminOrderService.getOrderById(id);
                    String oldStatus = order.getTrangThaiDon();

                    if (!"DA_HUY".equals(status) && order.getUser() != null
                            && order.getUser().getEmail() != null && !order.getUser().getEmail().isBlank()) {
                        asyncEmailService.sendOrderStatus(
                                order.getUser().getEmail(), order.getUser().getHoTen(),
                                order.getMaDon(), AdminOrderService.getStatusName(status));
                    }

                    String stockMsg = adminOrderService.updateOrderStatusWithLog(id, status, oldStatus, admin, request);
                    updated++;
                    log.info("Batch update order #{}: {} -> {}", id, oldStatus, status);

                    if (!"DA_HUY".equals(status)) {
                        try {
                            String statusName = AdminOrderService.getStatusName(status);
                            notificationHelper.notifyAll(
                                    "Đơn hàng " + order.getMaDon() + " đã chuyển sang trạng thái: " + statusName,
                                    "ORDER", order.getId(),
                                    "/tai-khoan/don-hang/" + order.getId(),
                                    order.getMaDon(),
                                    order.getUser() != null ? order.getUser().getId() : null
                            );
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    log.error("Batch update failed for order #{}: {}", id, e.getMessage());
                    errors.add("Đơn #" + id + ": " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("updated", updated);
            result.put("errors", errors);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/san-pham/{productId}/quick-view")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public ResponseEntity<Map<String, Object>> quickViewProduct(@PathVariable Integer productId) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            var product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy sản phẩm");
                return ResponseEntity.ok(result);
            }
            result.put("success", true);

            Map<String, Object> pData = new LinkedHashMap<>();
            pData.put("id", product.getId());
            pData.put("tenSanPham", product.getTenSanPham());
            pData.put("hinhAnh", product.getHinhAnhChinh());
            pData.put("moTa", product.getMoTa());
            result.put("product", pData);

            var variants = variantRepository.findByProductIdAndIsActiveTrue(productId);
            List<Map<String, Object>> vList = new java.util.ArrayList<>();
            for (var v : variants) {
                Map<String, Object> vData = new LinkedHashMap<>();
                vData.put("id", v.getId());
                vData.put("tenBienThe", v.getTenBienThe());
                vData.put("giaGoc", v.getGiaGoc());
                vData.put("giaKhuyenMai", v.getGiaKhuyenMai());
                vData.put("soLuongTon", v.getSoLuongTon());
                vData.put("hinhAnh", v.getHinhAnh());
                vList.add(vData);
            }
            result.put("variants", vList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/{id}/cap-nhat-ma-van-don")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public ResponseEntity<Map<String, Object>> updateMaVanDon(@PathVariable Integer id,
            @RequestParam("maVanDon") String maVanDon) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy đơn hàng");
                return ResponseEntity.ok(result);
            }
            order.setMaVanDon(maVanDon);
            orderRepository.save(order);
            result.put("success", true);
            result.put("message", "Cập nhật mã vận đơn thành công");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/phan-cong")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public String reassignAdmin(@PathVariable Integer id,
            @RequestParam("adminId") Integer adminId,
            RedirectAttributes ra,
            HttpServletRequest request) {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                return "redirect:/dang-nhap";
            }
            Order order = adminOrderService.getOrderById(id);
            User newAdmin = new User();
            newAdmin.setId(adminId);
            User assignedUser = userRepository.findById(adminId).orElse(null);

            if (assignedUser != null && assignedUser.getEmail() != null && !assignedUser.getEmail().isBlank()) {
                asyncEmailService.sendOrderAssigned(
                        assignedUser.getEmail(),
                        assignedUser.getHoTen() != null ? assignedUser.getHoTen() : assignedUser.getEmail(),
                        order.getMaDon(),
                        order.getSnapTenNguoiNhan() != null ? order.getSnapTenNguoiNhan() : "Khách hàng",
                        admin.getHoTen() != null ? admin.getHoTen() : admin.getUsername());
                log.info("Gửi email phân công đơn {} tới {} <{}>", order.getMaDon(),
                        assignedUser.getHoTen(), assignedUser.getEmail());
            } else {
                log.warn("Không gửi email phân công: adminId={} không có email hoặc không tìm thấy", adminId);
            }

            adminLogService.reassignAdmin(order, admin, newAdmin, request);
            ra.addFlashAttribute("successMsg", "Đã phân công lại đơn hàng");
        } catch (Exception e) {
            log.error("Lỗi reassignAdmin đơn #{}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/{id}/ghi-chu")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public String addNote(@PathVariable Integer id,
            @RequestParam("noiDung") String noiDung,
            @RequestParam(value = "tag", required = false, defaultValue = "") String tag,
            RedirectAttributes ra) {
        if (noiDung == null || noiDung.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Nội dung ghi chú không được để trống");
            return "redirect:/admin/don-hang/" + id;
        }
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng");
                return "redirect:/admin/don-hang/" + id;
            }
            Order order = adminOrderService.getOrderById(id);
            String tagVal = (tag == null || tag.isBlank()) ? null : tag.trim();
            orderNoteService.addNote(order, admin, noiDung.trim(), tagVal);
            ra.addFlashAttribute("successMsg", "Đã thêm ghi chú");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/{id}/xac-nhan-chuyen-khoan")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public ResponseEntity<Map<String, Object>> xacNhanChuyenKhoan(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                result.put("success", false);
                result.put("message", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(result);
            }

            Order order = adminOrderService.getOrderById(id);

            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                result.put("success", false);
                result.put("message", "Đơn hàng không phải thanh toán chuyển khoản");
                return ResponseEntity.ok(result);
            }

            if ("DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
                result.put("success", false);
                result.put("message", "Đơn hàng đã được xác nhận thanh toán rồi");
                return ResponseEntity.ok(result);
            }

            // Cap nhat trang thai thanh toan
            order.setTrangThaiTT("DA_THANH_TOAN");
            orderRepository.save(order);
            orderStatusLogService.ghiLog(order, com.duastore.model.OrderEventType.PAYMENT_CONFIRMED,
                    admin, "CHUA_THANH_TOAN", "DA_THANH_TOAN",
                    "Admin " + admin.getHoTen() + " xác nhận đã nhận chuyển khoản");

            adminLogService.ghiLogDonHang(admin, order.getId(), "XAC_NHAN_CHUYEN_KHOAN",
                    "CHUA_THANH_TOAN", "DA_THANH_TOAN",
                    "Xác nhận chuyển khoản đơn hàng " + order.getMaDon(), request);

            // Gui email xac nhan cho khach
            if (order.getUser() != null && order.getUser().getEmail() != null) {
                asyncEmailService.sendOrderSuccess(order);
            }

            // Thong bao den khach qua WebSocket
            try {
                notificationHelper.notifyAll(
                        "Đơn hàng " + order.getMaDon() + " đã được xác nhận thanh toán",
                        "ORDER", order.getId(),
                        "/tai-khoan/don-hang/" + order.getId(),
                        order.getMaDon(),
                        order.getUser() != null ? order.getUser().getId() : null
                );
            } catch (Exception ignored) {
            }

            result.put("success", true);
            result.put("message", "Đã xác nhận chuyển khoản cho đơn " + order.getMaDon());
        } catch (Exception e) {
            log.error("Lỗi xác nhận chuyển khoản đơn #{}: {}", id, e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/auto-assign")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public ResponseEntity<Map<String, Object>> autoAssignOrders(
            @RequestParam(value = "adminId", required = false) Integer adminId,
            HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                result.put("success", false);
                result.put("message", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(result);
            }

            List<Order> unassignedOrders = orderRepository.findUnassignedOrders();
            if (unassignedOrders.isEmpty()) {
                result.put("success", true);
                result.put("assigned", 0);
                result.put("message", "Không có đơn hàng chưa được phân công");
                return ResponseEntity.ok(result);
            }

            List<User> admins = userRepository.findAllActiveAdmins();
            if (admins.isEmpty()) {
                result.put("success", false);
                result.put("message", "Không có nhân viên nào đang hoạt động");
                return ResponseEntity.ok(result);
            }

            int assigned = 0;
            for (Order order : unassignedOrders) {
                if (adminId != null) {
                    User targetAdmin = userRepository.findById(adminId).orElse(null);
                    if (targetAdmin != null) {
                        adminLogService.phanDonCho(order, targetAdmin);
                        assigned++;
                    }
                } else {
                    User leastLoaded = null;
                    long minLoad = Long.MAX_VALUE;
                    for (User a : admins) {
                        long load = assignmentRepository.countByAdminIdAndTrangThai(a.getId(), "DANG_XU_LY");
                        if (load < minLoad) {
                            minLoad = load;
                            leastLoaded = a;
                        }
                    }
                    if (leastLoaded != null) {
                        adminLogService.phanDonCho(order, leastLoaded);
                        assigned++;
                    }
                }
            }

            result.put("success", true);
            result.put("assigned", assigned);
            result.put("message", "Đã phân công " + assigned + " đơn hàng");
        } catch (Exception e) {
            log.error("Lỗi autoAssignOrders: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
