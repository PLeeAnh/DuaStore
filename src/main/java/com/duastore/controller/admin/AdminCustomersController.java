package com.duastore.controller.admin;

import com.duastore.model.*;
import com.duastore.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/khach-hang")
public class AdminCustomersController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewsRepository reviewsRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminCustomersController(UserRepository userRepository,
                                     OrderRepository orderRepository,
                                     AddressRepository addressRepository,
                                     WishlistRepository wishlistRepository,
                                     ReviewsRepository reviewsRepository,
                                     UserVoucherRepository userVoucherRepository,
                                     OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewsRepository = reviewsRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_READ)")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("title", "khach-hang");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<User> userPage;

        boolean searching = (keyword != null && !keyword.isBlank())
                || (status != null && !status.isBlank());

        if (searching) {
            userPage = searchUsers(keyword, status, pageable);
        } else {
            userPage = userRepository.findAllBy(pageable);
        }

        List<Integer> userIds = userPage.getContent().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, Long> orderCountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            orderRepository.countByUserIds(userIds).forEach(row ->
                orderCountMap.put((Integer) row[0], (Long) row[1]));
        }

        model.addAttribute("customers", userPage.getContent());
        model.addAttribute("orderCountMap", orderCountMap);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("searching", searching);

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

        // Orders
        Page<Order> orders = orderRepository.findByUserId(id, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "ngayDat")));
        model.addAttribute("orders", orders.getContent());

        // Order stats
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

        // Last order
        Optional<Order> lastOrder = allOrders.stream().findFirst();
        model.addAttribute("lastOrderDate", lastOrder.map(Order::getNgayDat).orElse(null));

        // Cancellation rate
        double cancelRate = totalOrders > 0 ? (cancelledOrders * 100.0 / totalOrders) : 0;
        model.addAttribute("cancelRate", cancelRate);

        // Top product
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

        // Addresses
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(id);
        model.addAttribute("addresses", addresses);

        // Vouchers
        List<UserVoucher> vouchers = userVoucherRepository.findByUserIdOrderBySavedAtDesc(id);
        model.addAttribute("vouchers", vouchers);

        // Wishlist
        List<Wishlist> wishlist = wishlistRepository.findByUserIdOrderByNgayThemDesc(id);
        model.addAttribute("wishlistItems", wishlist);

        // Reviews
        List<Review> reviews = reviewsRepository.findByUserIdOrderByNgayTaoDesc(id);
        model.addAttribute("reviews", reviews);

        return "view/admin/customer/detail";
    }

    private Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        return userRepository.searchByKeywordAndStatus(keyword, status, pageable);
    }
}
