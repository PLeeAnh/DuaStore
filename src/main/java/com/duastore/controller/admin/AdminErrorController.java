package com.duastore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới admin error controller.
 */
public class AdminErrorController {

    @GetMapping("/admin/error/403")
    public String accessDenied() {
        return "view/admin/error/403";
    }
}
