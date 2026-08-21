package com.duastore.controller.admin;

import com.duastore.model.PriceHistory;
import com.duastore.service.admin.PriceHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/admin/lich-su-gia")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới lịch sử thay đổi giá.
 */
public class AdminPriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    public AdminPriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRICE_HISTORY_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<PriceHistory> historyPage = priceHistoryService.getAllPaged(PageRequest.of(page, size));
        model.addAttribute("title", "lich-su-gia");
        model.addAttribute("histories", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("totalItems", historyPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "bản ghi");
        model.addAttribute("url", "/admin/lich-su-gia");
        model.addAttribute("filterParams", Map.of());
        return "view/admin/price-history/list";
    }
}
