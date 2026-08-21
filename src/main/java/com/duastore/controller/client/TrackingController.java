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
/**
 * Controller xử lý các request HTTP liên quan tới theo dõi trạng thái đơn hàng/vận đơn.
 */
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
            @RequestParam String phone, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        boolean valid = trackingService.verifyOrder(maDon, phone);
        if (valid) {
            session.setAttribute("trackingVerified", maDon);
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
    public ResponseEntity<Map<String, Object>> getTrackingData(@RequestParam String code,
            HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!canAccess(code, session)) {
            res.put("success", false);
            res.put("message", "Vui lòng xác minh mã đơn và SĐT trước khi xem chi tiết");
            return ResponseEntity.ok(res);
        }
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
    public ResponseEntity<Map<String, Object>> getCarrierStatus(@RequestParam String code,
            HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!canAccess(code, session)) {
            res.put("success", false);
            res.put("message", "Vui lòng xác minh mã đơn và SĐT trước khi xem chi tiết");
            return ResponseEntity.ok(res);
        }
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

    private boolean canAccess(String maDon, HttpSession session) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId != null) {
            try {
                var order = orderService.getOrderByMaDon(maDon);
                if (order.getUser() != null && order.getUser().getId().equals(userId)) {
                    return true;
                }
            } catch (RuntimeException ignored) {
            }
        }
        Object verified = session.getAttribute("trackingVerified");
        return maDon.equals(verified);
    }
}
