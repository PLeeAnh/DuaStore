package com.duastore.controller.client;

import com.duastore.model.Post;
import com.duastore.service.client.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/blog")
    public String list(Model model) {
        model.addAttribute("title", "blog");
        model.addAttribute("posts", postService.getPublishedPosts());
        return "view/client/post/blog-list";
    }

    @GetMapping("/blog/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) return "redirect:/blog";

        postService.incrementLuotXem(id);

        model.addAttribute("title", post.getTieuDe());
        model.addAttribute("post", post);
        model.addAttribute("tacGia", postService.getTenTacGia(post.getTacGiaId()));
        return "view/client/post/blog-detail";
    }
}
