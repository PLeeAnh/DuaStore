package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.dto.CheckoutRequestDTO;
import com.duastore.model.Address;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.model.Promotion;
import com.duastore.model.User;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.service.EmailService;
import com.duastore.service.ShippingFeeService;
import com.duastore.service.client.CartService;
import com.duastore.service.client.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final SecurityUtil securityUtil;
    private final ShippingFeeService shippingFeeService;
    private final EmailService emailService;

    public CheckoutController(OrderService orderService, CartService cartService,
                              AddressRepository addressRepository,
                              PromotionRepository promotionRepository,
                              SecurityUtil securityUtil,
                              ShippingFeeService shippingFeeService,
                              EmailService emailService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.securityUtil = securityUtil;
        this.shippingFeeService = shippingFeeService;
        this.emailService = emailService;
    }

    private Integer getUserId() {
        return securityUtil.getCurrentUserId();
    }

    @GetMapping
    public String showCheckout(Model model) {
        Integer userId = getUserId();
        List<CartItemDTO> cartItems = cartService.getItems(userId);
        if (cartItems.isEmpty()) return "redirect:/gio-hang";

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        BigDecimal subtotal = cartService.total(cartItems);
        BigDecimal phiShip = addresses.isEmpty()
                ? new BigDecimal("10000")
                : shippingFeeService.calculateFee(addresses.get(0), "SHIP");

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("tienGiam", BigDecimal.ZERO);
        model.addAttribute("tongTam", subtotal.add(phiShip));
        model.addAttribute("storeLat", shippingFeeService.getStoreLat());
        model.addAttribute("storeLng", shippingFeeService.getStoreLng());
        model.addAttribute("checkoutRequest", new CheckoutRequestDTO());
        model.addAttribute("title", "Thanh toán");
        return "view/client/checkout";
    }

    @GetMapping("/shipping-fee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShippingFee(
            @RequestParam Integer addressId,
            @RequestParam(defaultValue = "SHIP") String method) {
        Map<String, Object> res = new HashMap<>();
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
            BigDecimal fee = shippingFeeService.calculateFee(address, method);
            res.put("fee", fee);
            res.put("success", true);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
                                   BindingResult result, Model model) {
        Integer userId = getUserId();

        if (result.hasErrors()) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            BigDecimal phiShip = addresses.isEmpty()
                    ? new BigDecimal("10000")
                    : shippingFeeService.calculateFee(addresses.get(0), "SHIP");
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("phiVanChuyen", phiShip);
            model.addAttribute("tienGiam", BigDecimal.ZERO);
            model.addAttribute("tongTam", subtotal.add(phiShip));
            model.addAttribute("storeLat", shippingFeeService.getStoreLat());
            model.addAttribute("storeLng", shippingFeeService.getStoreLng());
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }

        try {
            Order order = orderService.processCheckout(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu()
            );

            try {
                User user = order.getUser();
                String tt = "CHUYEN_KHOAN".equals(order.getPhuongThucTT()) ? "Chuyển khoản" : "COD";
                String gh = "NHAN_TAI_CONG".equals(order.getPhuongThucGiaoHang()) ? "Nhận tại cửa hàng" : "Giao hàng tiêu chuẩn";

                StringBuilder itemsHtml = new StringBuilder();
                for (OrderItem item : order.getOrderItems()) {
                    itemsHtml.append("<div style=\"display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f0f0f0;\">")
                            .append("<div><div style=\"font-size:14px;color:#424242;\">").append(item.getTenSanPham()).append("</div>")
                            .append("<div style=\"font-size:12px;color:#9e9e9e;\">").append(item.getTenBienThe()).append(" x ").append(item.getSoLuong()).append("</div></div>")
                            .append("<div style=\"font-size:14px;font-weight:600;color:#424242;\">").append(String.format("%,.0fđ", item.getThanhTien())).append("</div></div>");
                }

                emailService.sendOrderSuccessEmail(
                        user.getEmail(), user.getHoTen(), order.getMaDon(),
                        order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        order.getSnapDiaChi(), tt, gh,
                        String.format("%,.0fđ", order.getTongThanhToan()),
                        itemsHtml.toString()
                );
            } catch (Exception ignored) {}

            return "redirect:/checkout/thanh-cong/" + order.getId();
        } catch (RuntimeException e) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            BigDecimal phiShip = addresses.isEmpty()
                    ? new BigDecimal("10000")
                    : shippingFeeService.calculateFee(addresses.get(0), "SHIP");
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("phiVanChuyen", phiShip);
            model.addAttribute("tienGiam", BigDecimal.ZERO);
            model.addAttribute("tongTam", subtotal.add(phiShip));
            model.addAttribute("storeLat", shippingFeeService.getStoreLat());
            model.addAttribute("storeLng", shippingFeeService.getStoreLng());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }
    }

    @PostMapping("/ap-dung-ma")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyPromo(@RequestParam String maCode,
                                                           @RequestParam BigDecimal subtotal) {
        Map<String, Object> res = new HashMap<>();
        try {
            Promotion promo = promotionRepository.findByMaCodeAndIsActiveTrue(maCode.toUpperCase().trim())
                    .orElse(null);
            if (promo == null) {
                res.put("success", false);
                res.put("message", "Mã giảm giá không tồn tại hoặc đã ngừng hoạt động");
                return ResponseEntity.ok(res);
            }
            orderService.validatePromotion(promo, subtotal);
            BigDecimal tienGiam = orderService.calculateDiscount(promo, subtotal);
            res.put("success", true);
            res.put("tienGiam", tienGiam);
            res.put("message", "Áp dụng mã thành công! Giảm " +
                    (tienGiam.compareTo(BigDecimal.ZERO) > 0
                            ? String.format("%,.0fđ", tienGiam) : "0đ"));
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/thanh-cong/{id}")
    public String orderSuccess(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            model.addAttribute("order", orderService.convertToDTO(order));
            model.addAttribute("title", "Đặt hàng thành công");
            return "view/client/order-success";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }
}
