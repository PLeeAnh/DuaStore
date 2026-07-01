package com.duastore.controller.client;

import com.duastore.model.Post;
import com.duastore.model.PostCategory;
import com.duastore.repository.PostCategoryRepository;
import com.duastore.service.client.PostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PostController {

    private final PostService postService;
    private final PostCategoryRepository postCategoryRepository;

    public PostController(PostService postService, PostCategoryRepository postCategoryRepository) {
        this.postService = postService;
        this.postCategoryRepository = postCategoryRepository;
    }

    @GetMapping("/blog")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size,
                        @RequestParam(required = false) Integer danhMuc,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        model.addAttribute("title", "blog");
        Page<Post> postPage;
        if (keyword != null || danhMuc != null) {
            postPage = postService.searchPublishedPosts(keyword, danhMuc, page, size);
        } else {
            postPage = postService.getPublishedPostsPaged(page, size);
        }
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("keyword", keyword);
        model.addAttribute("postCategories", postCategoryRepository.findAllByOrderByThuTuAsc());
        model.addAttribute("featuredPosts", postService.getFeaturedPosts(0, 5).getContent());
        return "view/client/post/blog-list";
    }

    @GetMapping("/blog/{slugOrId}")
    public String detail(@PathVariable String slugOrId, Model model) {
        Post post = postService.getPostBySlugOrId(slugOrId);
        if (post == null) return "redirect:/blog";

        postService.incrementLuotXem(post.getId());

        model.addAttribute("title", post.getTieuDe());
        model.addAttribute("post", post);
        model.addAttribute("tacGia", postService.getTenTacGia(post.getTacGiaId()));
        model.addAttribute("tagNames", postService.getTagNames(post.getTags()));
        model.addAttribute("relatedPosts", postService.getRelatedPosts(post, 4));
        return "view/client/post/blog-detail";
    }
}
