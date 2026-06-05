package com.duastore.controller.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.dto.CheckoutRequestDTO;
import com.duastore.model.Address;
import com.duastore.model.Order;
import com.duastore.repository.AddressRepository;
import com.duastore.service.client.CartService;
import com.duastore.service.client.OrderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressRepository addressRepository;

    public CheckoutController(OrderService orderService, CartService cartService,
                              AddressRepository addressRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
    }

    private Integer getUserId() {
        return 2;
    }

    @GetMapping
    public String showCheckout(Model model) {
        Integer userId = getUserId();
        List<CartItemDTO> cartItems = cartService.getItems(userId);
        if (cartItems.isEmpty()) return "redirect:/gio-hang";

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        BigDecimal subtotal = cartService.total(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("checkoutRequest", new CheckoutRequestDTO());
        model.addAttribute("title", "Thanh toán");
        return "view/client/checkout";
    }

    @PostMapping
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
                                   BindingResult result, Model model) {
        Integer userId = getUserId();

        if (result.hasErrors()) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }

        try {
            Order order = orderService.processCheckout(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu()
            );
            return "redirect:/checkout/thanh-cong/" + order.getId();
        } catch (RuntimeException e) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }
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
