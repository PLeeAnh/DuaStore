package com.duastore.service.admin;

import com.duastore.dto.PostDTO;
import com.duastore.model.Post;
import com.duastore.repository.PostCategoryRepository;
import com.duastore.repository.PostsRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional
public class AdminPostService {

    private final PostsRepository postsRepository;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final PostCategoryRepository postCategoryRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public AdminPostService(PostsRepository postsRepository,
            FileUploadService fileUploadService,
            UserRepository userRepository,
            PostCategoryRepository postCategoryRepository) {
        this.postsRepository = postsRepository;
        this.fileUploadService = fileUploadService;
        this.userRepository = userRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ROOT).replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return postsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String keyword, String trangThai, Integer danhMucId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasTrangThai = trangThai != null && !trangThai.isEmpty();
        if (hasTrangThai && danhMucId != null) {
            return postsRepository.findByTrangThaiAndDanhMucIdAndTieuDeContainingIgnoreCase(trangThai, danhMucId,
                    hasKeyword ? keyword : "", pageable);
        }
        if (hasTrangThai) {
            return postsRepository.findByTrangThaiAndTieuDeContainingIgnoreCase(trangThai,
                    hasKeyword ? keyword : "", pageable);
        }
        if (danhMucId != null) {
            if (hasKeyword) {
                return postsRepository.findByDanhMucIdAndTieuDeContainingIgnoreCase(danhMucId, keyword, pageable);
            }
            return postsRepository.findByDanhMucId(danhMucId, pageable);
        }
        return postsRepository.findByTieuDeContainingIgnoreCase(hasKeyword ? keyword : "", pageable);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Integer id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
    }

    public Post save(PostDTO dto) {
        if (dto.getTacGiaId() != null && !userRepository.existsById(dto.getTacGiaId())) {
            throw new RuntimeException("Tác giả không tồn tại");
        }

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

            if (dto.getHinhAnhFile() != null && !dto.getHinhAnhFile().isEmpty()) {
                if (post.getHinhAnh() != null) {
                    fileUploadService.delete(post.getHinhAnh(), "posts");
                }
                String path = fileUploadService.save(dto.getHinhAnhFile());
                if (path != null) {
                    post.setHinhAnh(path);
                }
            }
        } else {
            post = new Post();
            post.setTieuDe(dto.getTieuDe());
            post.setTomTat(dto.getTomTat());
            post.setNoiDung(dto.getNoiDung());
            post.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "NHAP");
            post.setTacGiaId(dto.getTacGiaId());
            post.setLuotXem(0);

            if (dto.getHinhAnhFile() != null && !dto.getHinhAnhFile().isEmpty()) {
                String path = fileUploadService.save(dto.getHinhAnhFile());
                if (path != null) {
                    post.setHinhAnh(path);
                }
            }
        }

        post.setSlug(dto.getSlug() != null && !dto.getSlug().isBlank() ? dto.getSlug() : toSlug(dto.getTieuDe()));
        post.setMetaDescription(dto.getMetaDescription());
        post.setFeatured(Boolean.TRUE.equals(dto.getFeatured()));
        post.setNgayXuatBan(dto.getNgayXuatBan());

        if (dto.getDanhMucId() != null) {
            postCategoryRepository.findById(dto.getDanhMucId()).ifPresent(post::setDanhMuc);
        } else {
            post.setDanhMuc(null);
        }

        if ("XUAT_BAN".equals(post.getTrangThai()) && post.getNgayXuatBan() == null) {
            post.setNgayXuatBan(LocalDateTime.now());
        }

        return postsRepository.save(post);
    }

    public void delete(Integer id) {
        Post post = getPostById(id);
        post.setTrangThai("AN");
        postsRepository.save(post);
    }

    public void deletePermanent(Integer id) {
        Post post = getPostById(id);
        if (post.getHinhAnh() != null) {
            fileUploadService.delete(post.getHinhAnh(), "posts");
        }
        postsRepository.delete(post);
    }

    public void batchUpdateStatus(List<Integer> ids, String trangThai) {
        List<Post> posts = postsRepository.findAllById(ids);
        for (Post p : posts) {
            p.setTrangThai(trangThai);
        }
        postsRepository.saveAll(posts);
    }
}
