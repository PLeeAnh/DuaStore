package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.client.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
/**
 * Controller xử lý các request HTTP liên quan tới coupon api controller.
 */
public class CouponApiController {

    private final OrderService orderService;
    private final SecurityUtil securityUtil;

    public CouponApiController(OrderService orderService, SecurityUtil securityUtil) {
        this.orderService = orderService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(@RequestBody Map<String, Object> payload) {
        String maCode = (String) payload.get("maCode");
        Object subObj = payload.get("subtotal");
        if (subObj == null) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Thiếu thông tin subtotal"));
        }
        BigDecimal subtotal;
        try {
            subtotal = new BigDecimal(subObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Subtotal không hợp lệ"));
        }
        Map<String, Object> result = orderService.validateCouponForApi(maCode, subtotal, securityUtil.getCurrentUserId());
        return ResponseEntity.ok(result);
    }
}
