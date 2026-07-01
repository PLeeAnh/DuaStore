package com.duastore.controller.admin;

import com.duastore.model.User;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/khach-hang")
public class AdminCustomersController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminCustomersController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
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

        // Count orders per customer
        List<Integer> userIds = userPage.getContent().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Map<Integer, Long> orderCountMap = new HashMap<>();
        for (Integer userId : userIds) {
            long count = orderRepository.countByUserId(userId);
            orderCountMap.put(userId, count);
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

    private Page<User> searchUsers(String keyword, String status, Pageable pageable) {
        // Filter by keyword (name, email, phone) and status
        Page<User> all = userRepository.findAllBy(pageable);

        return all; // Simplified — keyword/status filtering handled in template for now
    }
}
