package com.duastore.service;

import com.duastore.model.Post;
import com.duastore.repository.PostsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostScheduledService {

    private final PostsRepository postsRepository;

    public PostScheduledService(PostsRepository postsRepository) {
        this.postsRepository = postsRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void autoPublishScheduledPosts() {
        List<Post> posts = postsRepository.findPostsToAutoPublish(LocalDateTime.now());
        for (Post p : posts) {
            p.setTrangThai("XUAT_BAN");
            postsRepository.save(p);
        }
    }
}
