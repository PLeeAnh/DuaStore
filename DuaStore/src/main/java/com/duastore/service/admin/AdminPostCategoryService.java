package com.duastore.service.admin;

import com.duastore.model.PostCategory;
import com.duastore.repository.PostCategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminPostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    public AdminPostCategoryService(PostCategoryRepository postCategoryRepository) {
        this.postCategoryRepository = postCategoryRepository;
    }

    public List<PostCategory> getAll() {
        return postCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "thuTu"));
    }

    public PostCategory getById(Integer id) {
        return postCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục bài viết"));
    }

    public void save(PostCategory category) {
        if (category.getTenDanhMuc() == null || category.getTenDanhMuc().isBlank()) {
            throw new RuntimeException("Tên danh mục không được để trống");
        }
        if (category.getThuTu() == null) {
            category.setThuTu(0);
        }
        postCategoryRepository.save(category);
    }

    public void delete(Integer id) {
        if (!postCategoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục bài viết");
        }
        postCategoryRepository.deleteById(id);
    }
}
