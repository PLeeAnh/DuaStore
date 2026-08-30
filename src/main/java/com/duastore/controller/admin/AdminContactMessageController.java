package com.duastore.controller.admin;

import com.duastore.config.security.PermissionEnum;
import com.duastore.config.security.SecurityUtil;
import com.duastore.model.ContactMessage;
import com.duastore.model.ContactReply;
import com.duastore.repository.ContactReplyRepository;
import com.duastore.service.ContactMessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tin-nhan-lien-he")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới liên hệ.
 */
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;
    private final ContactReplyRepository contactReplyRepository;
    private final SecurityUtil securityUtil;

    public AdminContactMessageController(ContactMessageService contactMessageService,
                                          ContactReplyRepository contactReplyRepository,
                                          SecurityUtil securityUtil) {
        this.contactMessageService = contactMessageService;
        this.contactReplyRepository = contactReplyRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CONTACT_MESSAGE_READ)")
    public String list(@RequestParam(required = false) String bo,
                       @RequestParam(required = false) String loai,
                       Model model) {
        model.addAttribute("title", "tin-nhan-lien-he");
        Map<String, String> labels = ContactMessageService.labelMap();

        List<ContactMessage> all = contactMessageService.findAll();
        List<ContactMessage> filtered = all;

        if ("rac".equals(bo)) {
            filtered = all.stream().filter(m -> Boolean.TRUE.equals(m.getIsSpam())).collect(Collectors.toList());
        } else if ("thuong".equals(bo)) {
            filtered = all.stream().filter(m -> !Boolean.TRUE.equals(m.getIsSpam())).collect(Collectors.toList());
        } else if ("chua-doc".equals(bo)) {
            filtered = all.stream().filter(m -> !Boolean.TRUE.equals(m.getIsRead())).collect(Collectors.toList());
        }

        if (StringUtils.hasText(loai)) {
            filtered = filtered.stream()
                    .filter(m -> loai.equals(m.getPhanLoaiKey()))
                    .collect(Collectors.toList());
        }

        long countThuong = all.stream().filter(m -> !Boolean.TRUE.equals(m.getIsSpam())).count();
        long countRac = all.stream().filter(m -> Boolean.TRUE.equals(m.getIsSpam())).count();
        long countChuaDoc = all.stream().filter(m -> !Boolean.TRUE.equals(m.getIsRead())).count();

        // Đếm reply cho mỗi tin nhắn
        Map<Integer, Long> replyCountMap = new HashMap<>();
        Map<Integer, List<ContactReply>> repliesMap = new HashMap<>();
        for (ContactMessage m : filtered) {
            List<ContactReply> replies = contactReplyRepository.findByContactIdOrderByCreatedAtAsc(m.getId());
            repliesMap.put(m.getId(), replies);
            replyCountMap.put(m.getId(), (long) replies.size());
        }

        model.addAttribute("messages", filtered);
        model.addAttribute("labels", labels);
        model.addAttribute("bo", bo);
        model.addAttribute("loai", loai == null ? "" : loai);
        model.addAttribute("countThuong", countThuong);
        model.addAttribute("countRac", countRac);
        model.addAttribute("countChuaDoc", countChuaDoc);
        model.addAttribute("countAll", (long) all.size());
        model.addAttribute("replyCountMap", replyCountMap);
        model.addAttribute("repliesMap", repliesMap);
        return "view/admin/contact/list";
    }

    @PostMapping("/danh-dau/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CONTACT_MESSAGE_UPDATE)")
    public String markRead(@PathVariable Integer id, RedirectAttributes ra) {
        ContactMessage m = contactMessageService.toggleRead(id);
        if (m != null) {
            Boolean nowRead = m.getIsRead();
            ra.addFlashAttribute("successMsg", Boolean.TRUE.equals(nowRead) ? "Đã đánh dấu đã đọc" : "Đã chuyển về chưa đọc");
        } else {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy tin nhắn");
        }
        return "redirect:/admin/tin-nhan-lien-he";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CONTACT_MESSAGE_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (contactMessageService.delete(id)) {
            ra.addFlashAttribute("successMsg", "Đã xóa tin nhắn");
        } else {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy tin nhắn");
        }
        return "redirect:/admin/tin-nhan-lien-he";
    }

    /** API phản hồi tin nhắn liên hệ — AJAX */
    @PostMapping("/{id}/tra-loi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CONTACT_MESSAGE_UPDATE)")
    @ResponseBody
    public ResponseEntity<?> reply(@PathVariable Integer id, @RequestParam String content) {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nội dung không được để trống"));
        }
        ContactMessage message = contactMessageService.getById(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        Integer adminId = securityUtil.getCurrentUserId();
        String adminName = securityUtil.getCurrentUser().getHoTen();

        ContactReply reply = ContactReply.builder()
                .contactId(id)
                .content(content.trim())
                .createdBy(adminId)
                .build();
        contactReplyRepository.save(reply);

        // Đánh dấu đã đọc khi reply
        contactMessageService.toggleRead(id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", reply.getId());
        result.put("content", reply.getContent());
        result.put("adminName", adminName);
        result.put("createdAt", reply.getCreatedAt().toString());
        return ResponseEntity.ok(result);
    }
}