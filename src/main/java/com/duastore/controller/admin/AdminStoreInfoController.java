package com.duastore.controller.admin;

import com.duastore.config.security.PermissionEnum;
import com.duastore.model.StoreInfo;
import com.duastore.service.admin.AdminStoreInfoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;
import java.util.List;

@Controller
@RequestMapping("/admin/dia-chi")
public class AdminStoreInfoController {

    private final AdminStoreInfoService service;

    public AdminStoreInfoController(AdminStoreInfoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_READ)")
    public String list(Model model) {
        List<StoreInfo> list = service.findAll();
        model.addAttribute("title", "dia-chi");
        model.addAttribute("stores", list);
        return "view/admin/store/store-info-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "dia-chi");
        model.addAttribute("store", new StoreInfo());
        return "view/admin/store/store-info-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_CREATE)")
    public String create(@ModelAttribute("store") StoreInfo store, RedirectAttributes ra) {
        service.save(store);
        ra.addFlashAttribute("successMsg", "Thêm địa chỉ cửa hàng thành công");
        return "redirect:/admin/dia-chi";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        StoreInfo store = service.findById(id);
        if (store == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy địa chỉ");
            return "redirect:/admin/dia-chi";
        }
        model.addAttribute("title", "dia-chi");
        model.addAttribute("store", store);
        return "view/admin/store/store-info-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_UPDATE)")
    public String edit(@PathVariable Integer id, @ModelAttribute("store") StoreInfo store, RedirectAttributes ra) {
        store.setId(id);
        service.save(store);
        ra.addFlashAttribute("successMsg", "Cập nhật địa chỉ cửa hàng thành công");
        return "redirect:/admin/dia-chi";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("successMsg", "Đã xóa địa chỉ cửa hàng");
        return "redirect:/admin/dia-chi";
    }

    @GetMapping("/api/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_READ)")
    @ResponseBody
    public ResponseEntity<StoreInfo> getApi(@PathVariable Integer id) {
        StoreInfo store = service.findById(id);
        if (store == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(store);
    }
}
