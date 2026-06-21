    package com.duastore.controller.client;

    import com.duastore.config.security.SecurityUtil;
    import com.duastore.dto.CartItemDTO;
    import com.duastore.repository.ProductVariantRepository;
    import com.duastore.service.client.CartService;
    import jakarta.servlet.http.HttpSession;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.ResponseBody;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Controller
    public class CartController {

        private final CartService cartService;
        private final ProductVariantRepository variantRepository;
        private final SecurityUtil securityUtil; // Thêm SecurityUtil để đồng bộ ID người dùng

        public CartController(CartService cartService, ProductVariantRepository variantRepository, SecurityUtil securityUtil) {
            this.cartService = cartService;
            this.variantRepository = variantRepository;
            this.securityUtil = securityUtil;
        }

        @GetMapping("/gio-hang")
        public String cart(HttpSession session, Model model) {
            Integer userId = currentUserId();
            if (userId == null) {
                return "redirect:/login"; // Nếu chưa đăng nhập thì chuyển hướng sang trang login
            }

            List<CartItemDTO> items = cartService.getItems(userId);
            model.addAttribute("title", "gio-hang");
            model.addAttribute("cartItems", items);

            // Đvector ĐÃ SỬA LỖI 1: Đổi từ "cartTotal" thành "totalPrice" để khớp 100% với file cart.html
            model.addAttribute("totalPrice", cartService.total(items)); 

            // Bổ sung thêm cartCount để đồng bộ hiển thị số lượng trên Badge của Navbar
            model.addAttribute("cartCount", cartService.count(userId));

            return "view/client/cart/cart";
        }

        @PostMapping("/gio-hang/cap-nhat/{id}")
        public String update(@PathVariable Integer id,
                             @RequestParam Integer soLuong,
                             HttpSession session) {
            Integer userId = currentUserId();
            if (userId != null) {
                cartService.updateQuantity(userId, id, soLuong);
            }
            return "redirect:/gio-hang";
        }

        @GetMapping("/gio-hang/xoa/{id}")
        public String remove(@PathVariable Integer id, HttpSession session) {
            Integer userId = currentUserId();
            if (userId != null) {
                cartService.remove(userId, id);
            }
            return "redirect:/gio-hang";
        }

        @PostMapping("/api/cart/add")
        @ResponseBody
        public Map<String, Object> add(@RequestBody Map<String, Integer> body, HttpSession session) {
            Integer variantId = body.get("variantId");
            if (variantId == null && body.get("productId") != null) {
                variantId = variantRepository.findByProductIdAndIsDefaultTrue(body.get("productId"))
                        .or(() -> variantRepository.findByProductIdAndIsActiveTrue(body.get("productId")).stream().findFirst())
                        .map(v -> v.getId())
                        .orElse(null);
            }
            Integer quantity = body.get("soLuong") != null ? body.get("soLuong") : body.get("quantity");
            CartService.CartResult result = cartService.add(currentUserId(), variantId, quantity);
            return response(result);
        }

        @GetMapping("/api/cart/count")
        @ResponseBody
        public Map<String, Object> count(HttpSession session) {
            Map<String, Object> data = new HashMap<>();
            data.put("cartCount", cartService.count(currentUserId()));
            return data;
        }

        private Map<String, Object> response(CartService.CartResult result) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.success());
            data.put("message", result.message());
            data.put("cartCount", result.cartCount());
            return data;
        }

        // Đvector ĐÃ SỬA LỖI 2: Lấy User ID động từ SecurityUtil thay vì fix cứng return 2 như trước
        private Integer currentUserId() {
            return securityUtil.getCurrentUserId();
        }
    }