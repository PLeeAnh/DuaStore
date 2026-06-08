package com.duastore.controller.admin;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nguoi-dung")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String role,
                       Model model) {
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayTao");
        Page<User> userPage;
        if (role != null && !role.isEmpty()) {
            userPage = userRepository.findByRole(role, PageRequest.of(page, 20, sort));
        } else {
            userPage = userRepository.findAllBy(PageRequest.of(page, 20, sort));
        }
        model.addAttribute("title", "nguoi-dung");
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("role", role);
        return "view/admin/user/user-list";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam Integer id, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setIsActive(!user.getIsActive());
            userRepository.save(user);
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        }
        return "redirect:/admin/nguoi-dung";
    }
}
