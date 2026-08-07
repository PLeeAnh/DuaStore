package com.duastore.controller.admin;

import com.duastore.service.admin.AdminSuggestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/search")
public class AdminSearchController {

    private final AdminSuggestionService suggestionService;

    public AdminSearchController(AdminSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * Autocomplete tìm kiếm cho các form admin.
     * type = product | variant | customer | order | attribute
     * VD: /admin/api/search?type=product&q=chai&limit=7
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> search(
            @RequestParam String type,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "7") int limit) {
        return suggestionService.search(type, q, limit);
    }
}