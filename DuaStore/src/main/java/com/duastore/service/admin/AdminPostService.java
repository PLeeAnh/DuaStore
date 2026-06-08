package com.duastore.service.admin;

import com.duastore.dto.PostDTO;
import com.duastore.model.Post;
import com.duastore.repository.PostsRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminPostService {

    private final PostsRepository postsRepository;
    private final FileUploadService fileUploadService;

    public AdminPostService(PostsRepository postsRepository,
                            FileUploadService fileUploadService) {
        this.postsRepository = postsRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return postsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Integer id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
    }

    public Post save(PostDTO dto) {
        Post post;
        if (dto.getId() != null) {
            post = getPostById(dto.getId());
            post.setTieuDe(dto.getTieuDe());
            post.setTomTat(dto.getTomTat());
            post.setNoiDung(dto.getNoiDung());
            post.setTrangThai(dto.getTrangThai());
            if (dto.getTacGiaId() != null) {
                post.setTacGiaId(dto.getTacGiaId());
            }
        } else {
            post = new Post();
            post.setTieuDe(dto.getTieuDe());
            post.setTomTat(dto.getTomTat());
            post.setNoiDung(dto.getNoiDung());
            post.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "NHAP");
            post.setTacGiaId(dto.getTacGiaId());
            post.setLuotXem(0);
        }

        if (dto.getHinhAnhFile() != null && !dto.getHinhAnhFile().isEmpty()) {
            String path = fileUploadService.save(dto.getHinhAnhFile());
            if (path != null) {
                post.setHinhAnh(path);
            }
        }

        return postsRepository.save(post);
    }

    public void delete(Integer id) {
        Post post = getPostById(id);
        post.setTrangThai("AN");
        postsRepository.save(post);
    }
}
