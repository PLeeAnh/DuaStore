package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.*;
import com.duastore.repository.*;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminCustomerService;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminUserService;
import com.duastore.service.LoyaltyPointsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/khach-hang")
public class AdminCustomersController {

    private final AdminCustomerService adminCustomerService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewsRepository reviewsRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final OrderItemRepository orderItemRepository;
    private final LoyaltyPointsService loyaltyPointsService;
    private final NotificationHelper notificationHelper;
    private final SecurityUtil securityUtil;
    private final AdminUserService adminUserService;
    private final AdminLogService adminLogService;

    public AdminCustomersController(AdminCustomerService adminCustomerService,
            UserRepository userRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            WishlistRepository wishlistRepository,
            ReviewsRepository reviewsRepository,
            UserVoucherRepository userVoucherRepository,
            OrderItemRepository orderItemRepository,
            LoyaltyPointsService loyaltyPointsService,
            NotificationHelper notificationHelper,
            SecurityUtil securityUtil,
            AdminUserService adminUserService,
            AdminLogService adminLogService) {
        this.adminCustomerService = adminCustomerService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewsRepository = reviewsRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.orderItemRepository = orderItemRepository;
        this.loyaltyPointsService = loyaltyPointsService;
        this.notificationHelper = notificationHelper;
        this.securityUtil = securityUtil;
        this.adminUserService = adminUserService;
        this.adminLogService = adminLogService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_READ)")
    public String list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String spendingTier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        model.addAttribute("title", "khach-hang");

        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "ngayTao"));
        Page<User> userPage = adminCustomerService.searchCustomers(keyword, status, city, spendingTier, pageable);

        List<Integer> userIds = userPage.getContent().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, Long> orderCountMap = adminCustomerService.getOrderCountMap(userIds);
        Map<Integer, Integer> loyaltyBalanceMap = adminCustomerService.getLoyaltyBalanceMap(userIds);

        model.addAttribute("customers", userPage.getContent());
        model.addAttribute("orderCountMap", orderCountMap);
        model.addAttribute("loyaltyBalanceMap", loyaltyBalanceMap);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("city", city);
        model.addAttribute("spendingTier", spendingTier);
        model.addAttribute("cities", adminCustomerService.getAllDistinctCities());
        model.addAttribute("searching", keyword != null || status != null || city != null || spendingTier != null);

        // Stats for cards (only count users with role USER)
        long activeCount = userRepository.countByRoleAndIsActive("USER", true);
        long lockedCount = userRepository.countByRoleAndIsActive("USER", false);
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        long newThisMonth = userRepository.countByRoleAndNgayTaoBetween("USER", startOfMonth, LocalDateTime.now());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("lockedCount", lockedCount);
        model.addAttribute("newThisMonth", newThisMonth);

        return "view/admin/customer/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_READ)")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy khách hàng");
            return "redirect:/admin/khach-hang";
        }
        model.addAttribute("title", "khach-hang");
        model.addAttribute("customer", user);

        Page<Order> orders = orderRepository.findByUserId(id, PageRequest.of(0, 50, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "ngayDat")));
        model.addAttribute("orders", orders.getContent());

        List<Order> allOrders = orderRepository.findAllByUserId(id);
        long totalOrders = allOrders.size();
        long completedOrders = allOrders.stream().filter(o -> "DA_HOAN_THANH".equals(o.getTrangThaiDon()) || "DA_GIAO".equals(o.getTrangThaiDon())).count();
        long cancelledOrders = allOrders.stream().filter(o -> "DA_HUY".equals(o.getTrangThaiDon())).count();
        BigDecimal totalSpent = allOrders.stream()
                .filter(o -> "DA_GIAO".equals(o.getTrangThaiDon()) || "DA_HOAN_THANH".equals(o.getTrangThaiDon()))
                .map(Order::getTongThanhToan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrderValue = completedOrders > 0 ? totalSpent.divide(BigDecimal.valueOf(completedOrders), 0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("avgOrderValue", avgOrderValue);

        Optional<Order> lastOrder = allOrders.stream().findFirst();
        model.addAttribute("lastOrderDate", lastOrder.map(Order::getNgayDat).orElse(null));

        double cancelRate = totalOrders > 0 ? (cancelledOrders * 100.0 / totalOrders) : 0;
        model.addAttribute("cancelRate", cancelRate);

        Map<String, Integer> productCounts = new HashMap<>();
        for (Order o : allOrders) {
            if ("DA_GIAO".equals(o.getTrangThaiDon()) || "DA_HOAN_THANH".equals(o.getTrangThaiDon())) {
                var items = orderItemRepository.findByOrderId(o.getId());
                for (var item : items) {
                    productCounts.merge(item.getTenSanPham(), item.getSoLuong(), Integer::sum);
                }
            }
        }
        String topProduct = productCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");
        model.addAttribute("topProduct", topProduct);

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(id);
        model.addAttribute("addresses", addresses);

        List<UserVoucher> vouchers = userVoucherRepository.findByUserIdOrderBySavedAtDesc(id);
        model.addAttribute("vouchers", vouchers);

        List<Wishlist> wishlist = wishlistRepository.findByUserIdOrderByNgayThemDesc(id);
        model.addAttribute("wishlistItems", wishlist);

        List<Review> reviews = reviewsRepository.findByUserIdOrderByNgayTaoDesc(id);
        model.addAttribute("reviews", reviews);

        model.addAttribute("notes", adminCustomerService.getNotes(id));
        model.addAttribute("tags", adminCustomerService.getTags(id));
        model.addAttribute("loyaltyBalance", loyaltyPointsService.getBalance(id));
        model.addAttribute("loyaltyHistory", adminCustomerService.getLoyaltyHistory(id));

        return "view/admin/customer/detail";
    }

    @PostMapping("/{id}/api/notes")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_UPDATE)")
    public ResponseEntity<?> addNote(@PathVariable Integer id, @RequestParam String content) {
        String adminName = securityUtil.getCurrentUser().getHoTen();
        CustomerNote note = adminCustomerService.addNote(id, content, adminName);
        Map<String, Object> result = new HashMap<>();
        result.put("id", note.getId());
        result.put("content", note.getContent());
        result.put("createdBy", note.getCreatedBy());
        result.put("createdAt", note.getCreatedAt().toString());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}/api/notes/{noteId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_UPDATE)")
    public ResponseEntity<?> deleteNote(@PathVariable Integer id, @PathVariable Integer noteId) {
        adminCustomerService.deleteNote(noteId, id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/{id}/api/tags")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_UPDATE)")
    public ResponseEntity<?> addTag(@PathVariable Integer id, @RequestParam String tag) {
        CustomerTag ct = adminCustomerService.addTag(id, tag);
        if (ct == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tag đã tồn tại"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", ct.getId());
        result.put("tag", ct.getTag());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/toggle-status")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_UPDATE)")
    public String toggleStatus(@RequestParam Integer id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy khách hàng");
            return "redirect:/admin/khach-hang";
        }
        User currentAdmin;
        try {
            currentAdmin = securityUtil.getCurrentUser();
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không xác định được người thao tác");
            return "redirect:/admin/khach-hang";
        }
        try {
            adminUserService.toggleStatus(id, currentAdmin);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/khach-hang";
        }
        boolean nowActive = !user.getIsActive();
        String statusMsg = nowActive ? "đã được kích hoạt" : "đã bị khóa";
        adminLogService.ghiLog(currentAdmin,
                "Khóa/kích hoạt tài khoản khách hàng #" + id,
                "CUSTOMER", id, null, null, statusMsg);
        notificationHelper.notifyAll(
                "Tài khoản của bạn " + statusMsg,
                null, null, null, null,
                user.getId()
        );
        String adminName;
        try {
            adminName = securityUtil.getCurrentUser().getHoTen();
        } catch (Exception e) {
            adminName = "";
        }
        notificationHelper.notifyStaff(
                "Admin " + adminName + " " + statusMsg + " tài khoản khách hàng " + user.getHoTen(),
                "CUSTOMER", user.getId(),
                "/admin/khach-hang/" + user.getId(),
                "Xem khách hàng"
        );
        ra.addFlashAttribute("successMsg", nowActive ? "Đã kích hoạt khách hàng" : "Đã khóa khách hàng");
        return "redirect:/admin/khach-hang";
    }

    @DeleteMapping("/{id}/api/tags/{tagId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_UPDATE)")
    public ResponseEntity<?> removeTag(@PathVariable Integer id, @PathVariable Integer tagId) {
        adminCustomerService.removeTag(tagId, id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
