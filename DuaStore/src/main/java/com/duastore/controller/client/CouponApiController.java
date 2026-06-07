package com.duastore.controller.client;

import com.duastore.service.client.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
public class CouponApiController {

    private final OrderService orderService;

    public CouponApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(@RequestBody Map<String, Object> payload) {
        String maCode = (String) payload.get("maCode");
        BigDecimal subtotal = new BigDecimal(payload.get("subtotal").toString());
        Map<String, Object> result = orderService.validateCouponForApi(maCode, subtotal);
        return ResponseEntity.ok(result);
    }
}
