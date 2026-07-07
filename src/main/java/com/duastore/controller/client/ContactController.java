package com.duastore.controller.client;

import com.duastore.model.StoreInfo;
import com.duastore.service.admin.AdminStoreInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final AdminStoreInfoService storeInfoService;

    public ContactController(AdminStoreInfoService storeInfoService) {
        this.storeInfoService = storeInfoService;
    }

    @GetMapping("/lien-he")
    public String contactForm(Model model) {
        model.addAttribute("title", "Liên hệ");
        StoreInfo store = storeInfoService.findDefault();
        if (store != null) {
            model.addAttribute("store", store);
            model.addAttribute("storeLat", store.getLatitude());
            model.addAttribute("storeLng", store.getLongitude());
        }
        return "view/client/contact";
    }

    @PostMapping("/lien-he")
    public String contactSubmit(@RequestParam String hoTen, @RequestParam String email,
                                @RequestParam String noiDung, RedirectAttributes ra) {
        if (!StringUtils.hasText(hoTen) || hoTen.trim().length() > 100) {
            ra.addFlashAttribute("errorMsg", "Họ tên không hợp lệ (1-100 ký tự)");
            return "redirect:/lien-he";
        }
        if (!StringUtils.hasText(email) || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
            ra.addFlashAttribute("errorMsg", "Email không hợp lệ");
            return "redirect:/lien-he";
        }
        if (!StringUtils.hasText(noiDung) || noiDung.trim().length() < 10 || noiDung.trim().length() > 2000) {
            ra.addFlashAttribute("errorMsg", "Nội dung phải từ 10-2000 ký tự");
            return "redirect:/lien-he";
        }
        ra.addFlashAttribute("successMsg", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        return "redirect:/lien-he";
    }
}
