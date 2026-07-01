package com.duastore.controller.admin;

import com.duastore.model.CampaignStatus;
import com.duastore.model.PromotionCampaign;
import com.duastore.service.admin.AdminCampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/chien-dich")
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    public AdminCampaignController(AdminCampaignService adminCampaignService) {
        this.adminCampaignService = adminCampaignService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) CampaignStatus status,
                       Model model) {
        Page<PromotionCampaign> campaignPage = adminCampaignService.searchCampaigns(keyword, status, page, size);
        model.addAttribute("campaigns", campaignPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", campaignPage.getTotalPages());
        model.addAttribute("totalItems", campaignPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "chiến dịch");
        model.addAttribute("url", "/admin/chien-dich");
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        Map<String, Object> filterParams = new HashMap<>();
        if (keyword != null) filterParams.put("keyword", keyword);
        if (status != null) filterParams.put("status", status);
        model.addAttribute("filterParams", filterParams);
        model.addAttribute("title", "chien-dich");
        return "view/admin/campaign/campaign-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("campaign", new PromotionCampaign());
        model.addAttribute("formAction", "/admin/chien-dich/them-moi");
        model.addAttribute("title", "chien-dich");
        return "view/admin/campaign/campaign-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_CREATE)")
    public String create(@Valid @ModelAttribute("campaign") PromotionCampaign campaign, BindingResult result,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/them-moi");
            return "view/admin/campaign/campaign-form";
        }
        try {
            if (startDate != null && !startDate.isBlank()) {
                campaign.setStartDate(LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
            if (endDate != null && !endDate.isBlank()) {
                campaign.setEndDate(LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/them-moi");
            model.addAttribute("errorMsg", "Định dạng ngày không hợp lệ");
            return "view/admin/campaign/campaign-form";
        }
        try {
            adminCampaignService.saveCampaign(campaign);
            ra.addFlashAttribute("successMsg", "Thêm chiến dịch thành công");
        } catch (Exception e) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/them-moi");
            model.addAttribute("errorMsg", e.getMessage());
            return "view/admin/campaign/campaign-form";
        }
        return "redirect:/admin/chien-dich";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            PromotionCampaign c = adminCampaignService.getCampaignById(id);
            model.addAttribute("campaign", c);
            model.addAttribute("formAction", "/admin/chien-dich/sua/" + id);
            model.addAttribute("title", "chien-dich");
            return "view/admin/campaign/campaign-form";
        } catch (Exception e) {
            return "redirect:/admin/chien-dich";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute("campaign") PromotionCampaign campaign,
                        BindingResult result,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/sua/" + id);
            return "view/admin/campaign/campaign-form";
        }
        try {
            if (startDate != null && !startDate.isBlank()) {
                campaign.setStartDate(LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
            if (endDate != null && !endDate.isBlank()) {
                campaign.setEndDate(LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/sua/" + id);
            model.addAttribute("errorMsg", "Định dạng ngày không hợp lệ");
            return "view/admin/campaign/campaign-form";
        }
        try {
            campaign.setId(id);
            adminCampaignService.saveCampaign(campaign);
            ra.addFlashAttribute("successMsg", "Cập nhật chiến dịch thành công");
        } catch (Exception e) {
            model.addAttribute("title", "chien-dich");
            model.addAttribute("campaign", campaign);
            model.addAttribute("formAction", "/admin/chien-dich/sua/" + id);
            model.addAttribute("errorMsg", e.getMessage());
            return "view/admin/campaign/campaign-form";
        }
        return "redirect:/admin/chien-dich";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminCampaignService.deleteCampaign(id);
            ra.addFlashAttribute("successMsg", "Đã kết thúc chiến dịch");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/chien-dich";
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_UPDATE)")
    public String togglePause(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminCampaignService.togglePause(id);
            ra.addFlashAttribute("successMsg", "Đã cập nhật trạng thái chiến dịch");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/chien-dich";
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CAMPAIGN_CREATE)")
    public String clone(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminCampaignService.cloneCampaign(id);
            ra.addFlashAttribute("successMsg", "Đã nhân bản chiến dịch");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/chien-dich";
    }
}
