package com.duastore.controller.admin;

import com.duastore.dto.PostDTO;
import com.duastore.model.Post;
import com.duastore.model.User;
import com.duastore.repository.PostCategoryRepository;
import com.duastore.repository.UserRepository;
import com.duastore.config.security.SecurityUtil;
import com.duastore.service.FileUploadService;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminPostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bai-viet")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới bài viết.
 */
public class AdminPostController {

    private final AdminPostService adminPostService;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final PostCategoryRepository postCategoryRepository;
    private final NotificationHelper notificationHelper;
    private final FileUploadService fileUploadService;

    public AdminPostController(AdminPostService adminPostService,
            UserRepository userRepository,
            SecurityUtil securityUtil,
            PostCategoryRepository postCategoryRepository,
            NotificationHelper notificationHelper,
            FileUploadService fileUploadService) {
        this.adminPostService = adminPostService;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.postCategoryRepository = postCategoryRepository;
        this.notificationHelper = notificationHelper;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer danhMucId,
            Model model) {
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasTrangThai = trangThai != null && !trangThai.isEmpty();

        Page<Post> postPage;
        if (hasKeyword || hasTrangThai || danhMucId != null) {
            postPage = adminPostService.searchPosts(keyword, trangThai, danhMucId, page, size);
        } else {
            postPage = adminPostService.getAllPosts(page, size);
        }

        Map<Integer, String> tacGiaMap = new HashMap<>();
        Set<Integer> tacGiaIds = postPage.getContent().stream()
                .map(Post::getTacGiaId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!tacGiaIds.isEmpty()) {
            userRepository.findAllById(tacGiaIds)
                    .forEach(u -> tacGiaMap.put(u.getId(), u.getHoTen()));
        }

        Map<String, Object> filterParams = new LinkedHashMap<>();
        if (hasKeyword) filterParams.put("keyword", keyword);
        if (hasTrangThai) filterParams.put("trangThai", trangThai);
        if (danhMucId != null) filterParams.put("danhMucId", danhMucId);

        model.addAttribute("title", "bai-viet");
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("tacGiaMap", tacGiaMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "bài viết");
        model.addAttribute("url", "/admin/bai-viet");
        model.addAttribute("filterParams", filterParams);
        return "view/admin/post/post-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CREATE)")
    public String createForm(Model model) {
        User currentUser = securityUtil.getCurrentUser();
        PostDTO dto = new PostDTO();
        if (currentUser != null) {
            dto.setTacGiaId(currentUser.getId());
            model.addAttribute("tacGiaName", currentUser.getHoTen());
        }
        model.addAttribute("title", "bai-viet");
        model.addAttribute("post", dto);
        model.addAttribute("formAction", "/admin/bai-viet/them-moi");
        model.addAttribute("categories", postCategoryRepository.findAllByOrderByThuTuAsc());
        return "view/admin/post/post-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CREATE)")
    public String create(@Valid @ModelAttribute PostDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            User currentUser = securityUtil.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("tacGiaName", currentUser.getHoTen());
            }
            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/them-moi");
            model.addAttribute("categories", postCategoryRepository.findAllByOrderByThuTuAsc());
            return "view/admin/post/post-form";
        }
        try {
            Post saved = adminPostService.save(dto);
            if ("XUAT_BAN".equals(saved.getTrangThai())) {
                notificationHelper.notifyAll(
                        "Bai viet moi: " + saved.getTieuDe(),
                        null, null,
                        "/bai-viet/" + saved.getSlug(),
                        "Doc ngay"
                );
                notificationHelper.notifyStaff(
                        "Bai viet moi da xuat ban: " + saved.getTieuDe(),
                        null, null,
                        "/admin/bai-viet",
                        "Xem bai viet",
                        com.duastore.config.security.PermissionEnum.POST_READ
                );
            }
            ra.addFlashAttribute("successMsg", "Thêm bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            Post post = adminPostService.getPostById(id);
            PostDTO dto = new PostDTO();
            dto.setId(post.getId());
            dto.setTieuDe(post.getTieuDe());
            dto.setSlug(post.getSlug());
            dto.setMetaDescription(post.getMetaDescription());
            dto.setTomTat(post.getTomTat());
            dto.setNoiDung(post.getNoiDung());
            dto.setHinhAnh(post.getHinhAnh());
            dto.setTacGiaId(post.getTacGiaId());
            dto.setTrangThai(post.getTrangThai());
            dto.setLuotXem(post.getLuotXem());
            dto.setFeatured(Boolean.TRUE.equals(post.getFeatured()));
            dto.setNgayXuatBan(post.getNgayXuatBan());
            if (post.getDanhMuc() != null) {
                dto.setDanhMucId(post.getDanhMuc().getId());
            }

            if (post.getTacGiaId() != null) {
                userRepository.findById(post.getTacGiaId())
                        .ifPresent(u -> model.addAttribute("tacGiaName", u.getHoTen()));
            }

            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/sua/" + id);
            model.addAttribute("categories", postCategoryRepository.findAllByOrderByThuTuAsc());
            return "view/admin/post/post-form";
        } catch (Exception e) {
            return "redirect:/admin/bai-viet";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute PostDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            if (dto.getTacGiaId() != null) {
                userRepository.findById(dto.getTacGiaId())
                        .ifPresent(u -> model.addAttribute("tacGiaName", u.getHoTen()));
            }
            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/sua/" + id);
            model.addAttribute("categories", postCategoryRepository.findAllByOrderByThuTuAsc());
            return "view/admin/post/post-form";
        }
        try {
            dto.setId(id);
            adminPostService.save(dto);
            ra.addFlashAttribute("successMsg", "Cập nhật bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPostService.delete(id);
            ra.addFlashAttribute("successMsg", "Đã xóa bài viết (có thể khôi phục từ bộ lọc \"Đã xóa\")");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }

    @PostMapping("/xoa-vinh-vien/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_DELETE)")
    public String deletePermanent(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPostService.deletePermanent(id);
            ra.addFlashAttribute("successMsg", "Đã xóa vĩnh viễn bài viết");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }

    @PostMapping("/upload-hinh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_UPDATE)")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();
        try {
            String url = fileUploadService.save(file);
            res.put("location", url);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/batch-cap-nhat")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_UPDATE)")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdate(@RequestParam List<Integer> ids,
            @RequestParam String trangThai) {
        Map<String, Object> res = new HashMap<>();
        try {
            adminPostService.batchUpdateStatus(ids, trangThai);
            res.put("success", true);
            res.put("message", "Cập nhật " + ids.size() + " bài viết thành công");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }
}
