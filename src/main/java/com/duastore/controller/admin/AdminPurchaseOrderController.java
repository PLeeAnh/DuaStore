package com.duastore.controller.admin;

import com.duastore.model.PurchaseOrder;
import com.duastore.model.PurchaseOrderItem;
import com.duastore.model.Supplier;
import com.duastore.service.admin.AdminPurchaseOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/nhap-hang")
public class AdminPurchaseOrderController {

    private final AdminPurchaseOrderService service;

    public AdminPurchaseOrderController(AdminPurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("title", "nhap-hang");
        model.addAttribute("orders", trangThai != null && !trangThai.isEmpty()
                ? service.listByStatus(trangThai, page, 20)
                : service.listOrders(page, 20));
        model.addAttribute("currentPage", page);
        model.addAttribute("statusCounts", service.getStatusCounts());
        model.addAttribute("totalOrders", service.getTotalOrders());
        model.addAttribute("activeFilter", trangThai);
        return "view/admin/purchase-order/list";
    }

    @GetMapping("/tao-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "nhap-hang");
        model.addAttribute("suppliers", service.listSuppliers());
        model.addAttribute("order", new PurchaseOrder());
        return "view/admin/purchase-order/form";
    }

    @PostMapping("/tao-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String create(@ModelAttribute PurchaseOrder order,
                         @RequestParam(value = "variantId", required = false) List<Integer> variantIds,
                         @RequestParam(value = "tenSanPham", required = false) List<String> tenSanPhams,
                         @RequestParam(value = "soLuong", required = false) List<Integer> soLuongs,
                         @RequestParam(value = "giaNhap", required = false) List<java.math.BigDecimal> giaNhaps,
                         @RequestParam(value = "soLuongNhan", required = false) List<Integer> soLuongNhans,
                         @RequestParam Integer supplierId,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes ra) {
        Supplier supplier = service.findSupplier(supplierId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        order.setSupplier(supplier);

        List<PurchaseOrderItem> items = new ArrayList<>();
        if (tenSanPhams != null) {
            for (int i = 0; i < tenSanPhams.size(); i++) {
                if (tenSanPhams.get(i) == null || tenSanPhams.get(i).isEmpty()) continue;
                PurchaseOrderItem item = new PurchaseOrderItem();
                if (variantIds != null && i < variantIds.size() && variantIds.get(i) != null) {
                    item.setVariantId(variantIds.get(i));
                }
                item.setTenSanPham(tenSanPhams.get(i));
                item.setSoLuong(soLuongs != null && i < soLuongs.size() ? soLuongs.get(i) : 0);
                item.setGiaNhap(giaNhaps != null && i < giaNhaps.size() ? giaNhaps.get(i) : java.math.BigDecimal.ZERO);
                item.setSoLuongNhan(soLuongNhans != null && i < soLuongNhans.size() ? soLuongNhans.get(i) : 0);
                items.add(item);
            }
        }
        service.createOrder(order, items);
        ra.addFlashAttribute("successMsg", "Tạo phiếu nhập thành công!");
        return "redirect:/admin/nhap-hang";
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("title", "nhap-hang");
        model.addAttribute("order", service.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập")));
        return "view/admin/purchase-order/detail";
    }

    @PostMapping("/{id}/duyet")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String approve(@PathVariable Integer id,
                          @AuthenticationPrincipal UserDetails user,
                          RedirectAttributes ra) {
        service.approveOrder(id, null);
        ra.addFlashAttribute("successMsg", "Duyệt phiếu nhập thành công!");
        return "redirect:/admin/nhap-hang/" + id;
    }

    @PostMapping("/{id}/nhap-hang")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String receive(@PathVariable Integer id, RedirectAttributes ra) {
        service.receiveOrder(id);
        ra.addFlashAttribute("successMsg", "Nhập hàng thành công! Tồn kho đã cập nhật.");
        return "redirect:/admin/nhap-hang/" + id;
    }

    @PostMapping("/{id}/huy")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String cancel(@PathVariable Integer id, RedirectAttributes ra) {
        service.cancelOrder(id);
        ra.addFlashAttribute("successMsg", "Hủy phiếu nhập thành công!");
        return "redirect:/admin/nhap-hang/" + id;
    }

    @PostMapping("/{id}/xoa")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.deleteOrder(id);
        ra.addFlashAttribute("successMsg", "Xóa phiếu nhập thành công!");
        return "redirect:/admin/nhap-hang";
    }

    @GetMapping("/nha-cung-cap")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String supplierList(Model model) {
        model.addAttribute("title", "nhap-hang");
        model.addAttribute("suppliers", service.listSuppliers());
        return "view/admin/purchase-order/supplier-list";
    }

    @PostMapping("/nha-cung-cap/luu")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String saveSupplier(@ModelAttribute Supplier supplier, RedirectAttributes ra) {
        service.saveSupplier(supplier);
        ra.addFlashAttribute("successMsg", "Lưu nhà cung cấp thành công!");
        return "redirect:/admin/nhap-hang/nha-cung-cap";
    }

    @PostMapping("/nha-cung-cap/{id}/xoa")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String deleteSupplier(@PathVariable Integer id, RedirectAttributes ra) {
        service.deleteSupplier(id);
        ra.addFlashAttribute("successMsg", "Xóa nhà cung cấp thành công!");
        return "redirect:/admin/nhap-hang/nha-cung-cap";
    }
}
