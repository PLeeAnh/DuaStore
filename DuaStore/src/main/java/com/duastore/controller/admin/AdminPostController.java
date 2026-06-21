package com.duastore.controller.admin;

import com.duastore.dto.PostDTO;
import com.duastore.model.Post;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AdminPostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/bai-viet")
public class AdminPostController {

    private final AdminPostService adminPostService;
    private final UserRepository userRepository;

    public AdminPostController(AdminPostService adminPostService,
                               UserRepository userRepository) {
        this.adminPostService = adminPostService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<Post> postPage = adminPostService.getAllPosts(page, size);

        Map<Integer, String> tacGiaMap = new HashMap<>();
        for (Post p : postPage.getContent()) {
            if (p.getTacGiaId() != null) {
                userRepository.findById(p.getTacGiaId())
                        .ifPresent(u -> tacGiaMap.put(p.getTacGiaId(), u.getHoTen()));
            }
        }

        model.addAttribute("title", "bai-viet");
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("tacGiaMap", tacGiaMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "bài viết");
        model.addAttribute("url", "/admin/bai-viet");
        model.addAttribute("filterParams", new HashMap<>());
        return "view/admin/post/post-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "bai-viet");
        model.addAttribute("post", new PostDTO());
        model.addAttribute("formAction", "/admin/bai-viet/them-moi");
        model.addAttribute("users", userRepository.findAll());
        return "view/admin/post/post-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CREATE)")
    public String create(@Valid @ModelAttribute PostDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/them-moi");
            model.addAttribute("users", userRepository.findAll());
            return "view/admin/post/post-form";
        }
        try {
            adminPostService.save(dto);
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
            dto.setTomTat(post.getTomTat());
            dto.setNoiDung(post.getNoiDung());
            dto.setHinhAnh(post.getHinhAnh());
            dto.setTacGiaId(post.getTacGiaId());
            dto.setTrangThai(post.getTrangThai());
            dto.setLuotXem(post.getLuotXem());

            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/sua/" + id);
            model.addAttribute("users", userRepository.findAll());
            return "view/admin/post/post-form";
        } catch (Exception e) {
            return "redirect:/admin/bai-viet";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute PostDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "bai-viet");
            model.addAttribute("post", dto);
            model.addAttribute("formAction", "/admin/bai-viet/sua/" + id);
            model.addAttribute("users", userRepository.findAll());
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
            ra.addFlashAttribute("successMsg", "Xóa bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }
}
