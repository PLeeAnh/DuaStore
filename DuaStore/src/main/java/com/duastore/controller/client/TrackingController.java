package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.TrackingDataDTO;
import com.duastore.service.client.OrderService;
import com.duastore.service.client.TrackingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/tracking")
public class TrackingController {

    private final TrackingService trackingService;
    private final OrderService orderService;
    private final SecurityUtil securityUtil;

    public TrackingController(TrackingService trackingService,
            OrderService orderService,
            SecurityUtil securityUtil) {
        this.trackingService = trackingService;
        this.orderService = orderService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public String showVerifyForm() {
        return "view/client/tracking/verify";
    }

    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOrder(@RequestParam String maDon,
            @RequestParam String phone) {
        Map<String, Object> res = new HashMap<>();
        boolean valid = trackingService.verifyOrder(maDon, phone);
        if (valid) {
            res.put("success", true);
            res.put("redirect", "/tracking/" + maDon);
        } else {
            res.put("success", false);
            res.put("message", "Mã đơn hoặc SĐT không chính xác");
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{maDon}")
    public String showTracking(@PathVariable String maDon, Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        boolean canView = false;
        if (userId != null) {
            try {
                var order = orderService.getOrderByMaDon(maDon);
                if (order.getUser().getId().equals(userId)) {
                    canView = true;
                }
            } catch (RuntimeException ignored) {}
        }
        model.addAttribute("maDon", maDon);
        model.addAttribute("canView", canView);
        model.addAttribute("title", "Theo dõi đơn hàng");
        return "view/client/tracking/tracking";
    }

    @GetMapping("/api/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTrackingData(@RequestParam String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            TrackingDataDTO data = trackingService.getTrackingData(code);
            res.put("success", true);
            res.put("data", data);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/api/carrier-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCarrierStatus(@RequestParam String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            Map<String, Object> carrierData = trackingService.pollCarrierStatus(code);
            res.put("success", true);
            res.put("data", carrierData);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }
}
