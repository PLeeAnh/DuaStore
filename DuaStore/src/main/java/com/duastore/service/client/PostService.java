package com.duastore.service.client;

import com.duastore.model.Post;
import com.duastore.model.User;
import com.duastore.repository.PostsRepository;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostsRepository postsRepository;
    private final UserRepository userRepository;

    public PostService(PostsRepository postsRepository, UserRepository userRepository) {
        this.postsRepository = postsRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> getPublishedPosts() {
        return postsRepository.findByTrangThaiOrderByNgayTaoDesc("XUAT_BAN");
    }

    @Transactional(readOnly = true)
    public Post getPostById(Integer id) {
        Post post = postsRepository.findById(id).orElse(null);
        if (post == null || !"XUAT_BAN".equals(post.getTrangThai())) return null;
        return post;
    }

    @Transactional
    public void incrementLuotXem(Integer id) {
        Post post = postsRepository.findById(id).orElse(null);
        if (post != null) {
            post.setLuotXem(post.getLuotXem() + 1);
            postsRepository.save(post);
        }
    }

    @Transactional(readOnly = true)
    public String getTenTacGia(Integer tacGiaId) {
        if (tacGiaId == null) return null;
        return userRepository.findById(tacGiaId)
                .map(User::getHoTen)
                .orElse(null);
    }
}
