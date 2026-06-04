package com.duastore.controller.admin;

import com.duastore.dto.PostDTO;
import com.duastore.model.Post;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AdminPostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Post> postPage = adminPostService.getAllPosts(page, 20);

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
        return "view/admin/post/post-list";
    }

    @GetMapping("/them-moi")
    public String createForm(Model model) {
        model.addAttribute("title", "bai-viet");
        model.addAttribute("post", new PostDTO());
        model.addAttribute("formAction", "/admin/bai-viet/them-moi");
        model.addAttribute("users", userRepository.findAll());
        return "view/admin/post/post-form";
    }

    @PostMapping("/them-moi")
    public String create(@ModelAttribute PostDTO dto, RedirectAttributes ra) {
        try {
            adminPostService.save(dto);
            ra.addFlashAttribute("successMsg", "Thêm bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bai-viet";
    }

    @GetMapping("/sua/{id}")
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
    public String edit(@PathVariable Integer id, @ModelAttribute PostDTO dto, RedirectAttributes ra) {
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
