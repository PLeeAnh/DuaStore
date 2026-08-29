package com.duastore.controller.admin;

import com.duastore.model.StockMovement;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.StockMovementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller quản lý lịch sử xuất nhập kho (stock movements).
 * Hiển thị danh sách toàn bộ biến động tồn kho trong hệ thống.
 */
@Controller
@RequestMapping("/admin/xuat-nhap-kho")
public class AdminStockMovementController {

    private final StockMovementService stockMovementService;
    private final UserRepository userRepository;

    public AdminStockMovementController(StockMovementService stockMovementService,
                                         UserRepository userRepository) {
        this.stockMovementService = stockMovementService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<StockMovement> movementPage = stockMovementService.getAll(PageRequest.of(page, size));

        // Lấy tên người thực hiện
        Set<Integer> userIds = movementPage.getContent().stream()
                .map(StockMovement::getUserId)
                .collect(Collectors.toSet());
        Map<Integer, String> userNames = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userNames.put(u.getId(), u.getHoTen()));

        model.addAttribute("movements", movementPage.getContent());
        model.addAttribute("userNames", userNames);
        model.addAttribute("currentPage", movementPage.getNumber());
        model.addAttribute("totalPages", movementPage.getTotalPages());
        model.addAttribute("totalItems", movementPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("title", "xuat-nhap-kho");
        return "view/admin/stock-movement/list";
    }
}
