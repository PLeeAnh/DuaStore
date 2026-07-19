package com.duastore.controller.admin;

import com.duastore.service.admin.AdminCopilotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/phan-tich/api")
public class AdminCopilotController {

    private final AdminCopilotService copilotService;

    public AdminCopilotController(AdminCopilotService copilotService) {
        this.copilotService = copilotService;
    }

    @PostMapping("/copilot")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ANALYTICS_READ)")
    public ResponseEntity<Map<String, Object>> ask(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("query", "").trim();
        if (query.isEmpty()) {
            return ResponseEntity.ok(Map.of("answer", "Vui lòng nhập câu hỏi của bạn."));
        }
        Map<String, Object> result = copilotService.answer(query);
        return ResponseEntity.ok(result);
    }
}
