package com.duastore.controller.client;

import com.duastore.model.ContactMessage;
import com.duastore.model.StoreInfo;
import com.duastore.service.AsyncEmailService;
import com.duastore.service.ContactMessageService;
import com.duastore.service.SiteSettingService;
import com.duastore.service.admin.AdminStoreInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
/**
 * Controller xử lý các request HTTP liên quan tới liên hệ.
 */
public class ContactController {

    private final AdminStoreInfoService storeInfoService;
    private final AsyncEmailService asyncEmailService;
    private final SiteSettingService siteSettingService;
    private final ContactMessageService contactMessageService;

    @Value("${store.latitude}")
    private double defaultStoreLat;

    @Value("${store.longitude}")
    private double defaultStoreLng;

    public ContactController(AdminStoreInfoService storeInfoService,
            AsyncEmailService asyncEmailService,
            SiteSettingService siteSettingService,
            ContactMessageService contactMessageService) {
        this.storeInfoService = storeInfoService;
        this.asyncEmailService = asyncEmailService;
        this.siteSettingService = siteSettingService;
        this.contactMessageService = contactMessageService;
    }

    @GetMapping("/lien-he")
    public String contactForm(Model model) {
        model.addAttribute("title", "Liên hệ");
        StoreInfo store = storeInfoService.findDefault();
        if (store != null) {
            model.addAttribute("store", store);
        }
        Double lat = store != null ? store.getLatitude() : null;
        Double lng = store != null ? store.getLongitude() : null;
        model.addAttribute("storeLat", lat != null ? lat : defaultStoreLat);
        model.addAttribute("storeLng", lng != null ? lng : defaultStoreLng);
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

        ContactMessage saved = contactMessageService.save(hoTen.trim(), email.trim(), noiDung.trim());

        // Tin rác: chỉ lưu vào hộp thư để admin rà soát, KHÔNG gửi email về hộp admin.
        boolean isSpam = Boolean.TRUE.equals(saved.getIsSpam());
        if (!isSpam) {
            String loaiLabel = ContactMessageService.labelMap().getOrDefault(saved.getPhanLoai(), "Khác");
            String storeEmail = siteSettingService.getValue("store_email");
            if (storeEmail == null || storeEmail.isBlank()) {
                storeEmail = SiteSettingService.STORE_DEFAULTS.get("store_email");
            }
            if (storeEmail != null && !storeEmail.isBlank()) {
                asyncEmailService.sendRaw(storeEmail,
                        "[DuaStore][" + loaiLabel + "] Tin nhắn từ " + hoTen.trim(),
                        "<div style=\"font-family:Arial,sans-serif;padding:20px;\">"
                        + "<h3 style=\"color:#1D4ED8;\">Tin nhắn liên hệ mới <small>(" + loaiLabel + ")</small></h3>"
                        + "<p><b>Họ tên:</b> " + htmlEscape(hoTen.trim()) + "</p>"
                        + "<p><b>Email:</b> " + htmlEscape(email.trim()) + "</p>"
                        + "<p><b>Nội dung:</b></p>"
                        + "<p style=\"white-space:pre-wrap;background:#f8fafc;border-left:4px solid #1D4ED8;padding:12px 16px;border-radius:6px;\">"
                        + htmlEscape(noiDung.trim()) + "</p></div>");
            }
            String contactPhone = siteSettingService.getValue("store_phone");
            if (contactPhone == null || contactPhone.isBlank()) {
                contactPhone = SiteSettingService.STORE_DEFAULTS.getOrDefault("store_phone", "0901 234 567");
            }
            asyncEmailService.sendRaw(email.trim(),
                    "[DuaStore] Cảm ơn bạn đã liên hệ",
                    "<div style=\"font-family:Arial,sans-serif;padding:20px;\">"
                    + "<h3 style=\"color:#1D4ED8;\">DuaStore</h3>"
                    + "<p>Xin chào <b>" + htmlEscape(hoTen.trim()) + "</b>,</p>"
                    + "<p>Chúng tôi đã nhận được tin nhắn của bạn. DuaStore sẽ phản hồi trong thời gian sớm nhất.</p>"
                    + "<p style=\"background:#f8fafc;border-left:4px solid #1D4ED8;padding:12px 16px;border-radius:6px;white-space:pre-wrap;\">"
                    + htmlEscape(noiDung.trim()) + "</p>"
                    + "<p style=\"color:#64748b;\">Mọi thắc mắc vui lòng liên hệ <b>" + htmlEscape(contactPhone) + "</b>.</p></div>");
        }

        ra.addFlashAttribute("successMsg", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        return "redirect:/lien-he";
    }

    private String htmlEscape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
