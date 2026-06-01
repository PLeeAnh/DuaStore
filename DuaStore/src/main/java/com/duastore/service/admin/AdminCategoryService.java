package com.duastore.service.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.model.Category;
import com.duastore.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getThuTuHienThi, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Category::getId))
                .toList();
    }

    public List<Category> findAvailableParents(Integer currentId) {
        return findAll().stream()
                .filter(Category::isActive)
                .filter(c -> currentId == null || !c.getId().equals(currentId))
                .toList();
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setTenDanhMuc(category.getTenDanhMuc());
        dto.setMoTa(category.getMoTa());
        dto.setThuTuHienThi(category.getThuTuHienThi());
        dto.setActive(category.isActive());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setTenDanhMucCha(category.getParent().getTenDanhMuc());
        }
        return dto;
    }

    public Category save(CategoryDTO dto) {
        Category category = dto.getId() == null
                ? new Category()
                : categoryRepository.findById(dto.getId()).orElse(new Category());

        category.setTenDanhMuc(dto.getTenDanhMuc());
        category.setMoTa(dto.getMoTa());
        category.setThuTuHienThi(dto.getThuTuHienThi() == null ? 0 : dto.getThuTuHienThi());
        category.setActive(dto.isActive());

        if (dto.getParentId() != null) {
            category.setParent(categoryRepository.findById(dto.getParentId()).orElse(null));
        } else {
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    public boolean softDelete(Integer id) {
        Category category = findById(id);
        if (category == null) {
            return false;
        }
        category.setActive(false);
        categoryRepository.save(category);
        return true;
    }
}
