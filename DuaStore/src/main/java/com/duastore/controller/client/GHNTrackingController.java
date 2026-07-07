package com.duastore.controller.client;

import com.duastore.model.Order;
import com.duastore.repository.OrderRepository;
import com.duastore.service.GHNShippingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/don-hang")
public class GHNTrackingController {

    private final OrderRepository orderRepository;
    private final GHNShippingService ghnShippingService;

    public GHNTrackingController(OrderRepository orderRepository, GHNShippingService ghnShippingService) {
        this.orderRepository = orderRepository;
        this.ghnShippingService = ghnShippingService;
    }

    @GetMapping("/tracking/{maVanDon}")
    @PreAuthorize("isAuthenticated()")
    public String tracking(@PathVariable String maVanDon, Model model) {
        Order order = maVanDon != null && !maVanDon.isBlank()
                ? orderRepository.findByMaVanDon(maVanDon).orElse(null) : null;

        Map<String, Object> ghnData = maVanDon != null && !maVanDon.isBlank()
                ? ghnShippingService.getOrderDetail(maVanDon) : null;

        model.addAttribute("order", order);
        model.addAttribute("ghnData", ghnData);
        model.addAttribute("maVanDon", maVanDon);
        model.addAttribute("noTrackingCode", maVanDon == null || maVanDon.isBlank());
        model.addAttribute("title", "Theo dõi vận chuyển");
        return "view/client/order/tracking";
    }
}
