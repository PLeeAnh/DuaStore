package com.duastore.controller.admin;

import com.duastore.dto.FooterLinkDTO;
import com.duastore.service.admin.AdminFooterLinkService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/footer-links")
public class AdminFooterLinkController {

    private final AdminFooterLinkService adminFooterLinkService;

    public AdminFooterLinkController(AdminFooterLinkService adminFooterLinkService) {
        this.adminFooterLinkService = adminFooterLinkService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_READ)")
    public String list(Model model) {
        model.addAttribute("links", adminFooterLinkService.findAll());
        model.addAttribute("title", "footer-links");
        return "view/admin/footerlink/list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("link", new FooterLinkDTO());
        model.addAttribute("formAction", "/admin/footer-links/them-moi");
        model.addAttribute("title", "footer-links");
        return "view/admin/footerlink/form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_CREATE)")
    public String create(@Valid @ModelAttribute("link") FooterLinkDTO dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/admin/footer-links/them-moi");
            model.addAttribute("title", "footer-links");
            return "view/admin/footerlink/form";
        }
        try {
            adminFooterLinkService.save(dto);
            ra.addFlashAttribute("successMsg", "Thêm liên kết thành công");
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("formAction", "/admin/footer-links/them-moi");
            model.addAttribute("title", "footer-links");
            return "view/admin/footerlink/form";
        }
        return "redirect:/admin/footer-links";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            FooterLinkDTO dto = adminFooterLinkService.findById(id);
            model.addAttribute("link", dto);
            model.addAttribute("formAction", "/admin/footer-links/sua/" + id);
            model.addAttribute("title", "footer-links");
            return "view/admin/footerlink/form";
        } catch (Exception e) {
            return "redirect:/admin/footer-links";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute("link") FooterLinkDTO dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/admin/footer-links/sua/" + id);
            model.addAttribute("title", "footer-links");
            return "view/admin/footerlink/form";
        }
        try {
            dto.setId(id);
            adminFooterLinkService.save(dto);
            ra.addFlashAttribute("successMsg", "Cập nhật liên kết thành công");
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("formAction", "/admin/footer-links/sua/" + id);
            model.addAttribute("title", "footer-links");
            return "view/admin/footerlink/form";
        }
        return "redirect:/admin/footer-links";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FOOTER_LINK_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminFooterLinkService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa liên kết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/footer-links";
    }
}
