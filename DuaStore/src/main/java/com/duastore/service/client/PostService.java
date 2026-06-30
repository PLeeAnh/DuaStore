package com.duastore.service.client;

import com.duastore.model.Post;
import com.duastore.model.PostTag;
import com.duastore.model.User;
import com.duastore.repository.PostsRepository;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Page<Post> getPublishedPostsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return postsRepository.findByTrangThai("XUAT_BAN", pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPublishedPostsByCategory(int danhMucId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return postsRepository.findByTrangThaiAndDanhMucId("XUAT_BAN", danhMucId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getFeaturedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return postsRepository.findByTrangThaiAndFeaturedTrue("XUAT_BAN", pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPublishedPosts(String keyword, Integer danhMucId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        if (danhMucId != null && keyword != null && !keyword.isBlank()) {
            return postsRepository.findByTrangThaiAndDanhMucIdAndTieuDeContainingIgnoreCase("XUAT_BAN", danhMucId, keyword, pageable);
        }
        if (danhMucId != null) {
            return postsRepository.findByTrangThaiAndDanhMucId("XUAT_BAN", danhMucId, pageable);
        }
        if (keyword != null && !keyword.isBlank()) {
            return postsRepository.findByTrangThaiAndTieuDeContainingIgnoreCase("XUAT_BAN", keyword, pageable);
        }
        return getPublishedPostsPaged(page, size);
    }

    @Transactional(readOnly = true)
    public Post getPostBySlugOrId(String slugOrId) {
        try {
            Integer id = Integer.parseInt(slugOrId);
            Post post = postsRepository.findById(id).orElse(null);
            if (post != null && "XUAT_BAN".equals(post.getTrangThai())) return post;
        } catch (NumberFormatException ignored) {}
        return postsRepository.findBySlugAndTrangThai(slugOrId, "XUAT_BAN").orElse(null);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Integer id) {
        Post post = postsRepository.findById(id).orElse(null);
        if (post == null || !"XUAT_BAN".equals(post.getTrangThai())) return null;
        return post;
    }

    @Transactional(readOnly = true)
    public List<Post> getRelatedPosts(Post post, int limit) {
        if (post.getDanhMuc() == null || post.getDanhMuc().getId() == null) return List.of();
        return postsRepository.findRelatedPosts("XUAT_BAN", post.getDanhMuc().getId(), post.getId(),
                PageRequest.of(0, limit));
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

    @Transactional(readOnly = true)
    public Set<String> getTagNames(Set<PostTag> tags) {
        if (tags == null) return Set.of();
        return tags.stream().map(PostTag::getTenTag).collect(Collectors.toSet());
    }
}
