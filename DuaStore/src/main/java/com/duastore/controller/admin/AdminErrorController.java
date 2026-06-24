package com.duastore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminErrorController {

    @GetMapping("/admin/error/403")
    public String accessDenied() {
        return "view/admin/error/403";
    }
}
