package com.duastore.service.client;

import com.duastore.model.Category;
import com.duastore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class CategoryService {

    @Value("${homepage.categories.limit:7}")
    private int categoryLimit;

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(value = "featuredCategories", unless = "#result.isEmpty()")
    public List<Category> getFeaturedCategories() {
        // Cache kết quả khác rỗng để giảm số lần truy vấn khi nhiều người cùng mở trang chủ.
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc()
                .stream()
                .limit(categoryLimit)
                .collect(Collectors.toList());
    }
}
