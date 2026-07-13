package com.duastore.service;

import com.duastore.dto.BannerFormDTO;
import com.duastore.model.Banner;
import com.duastore.repository.BannerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class BannerService {

    private static final Logger log = LoggerFactory.getLogger(BannerService.class);
    private static final String UPLOAD_DIRECTORY = "banners";

    private final BannerRepository bannerRepository;
    private final FileUploadService fileUploadService;

    public BannerService(BannerRepository bannerRepository, FileUploadService fileUploadService) {
        this.bannerRepository = bannerRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public List<Banner> getAllForAdmin() {
        return bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "activeBanners", unless = "#result.isEmpty()")
    public List<Banner> getActiveForClient() {
        try {
            return bannerRepository.findActiveForDisplay(LocalDateTime.now());
        } catch (RuntimeException ex) {
            // Home phải tiếp tục hoạt động ngay cả khi migration DB chưa được áp dụng.
            log.error("Không thể tải banner, tạm ẩn khu vực banner: {}", ex.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public Banner getById(Integer id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy banner"));
    }

    @CacheEvict(value = "activeBanners", allEntries = true)
    public Banner save(BannerFormDTO dto) {
        Banner banner = dto.getId() == null ? new Banner() : getById(dto.getId());
        String oldImageUrl = banner.getImageUrl();
        String newImageUrl = null;

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            newImageUrl = fileUploadService.save(dto.getImageFile(), UPLOAD_DIRECTORY);
        }
        if (oldImageUrl == null && newImageUrl == null) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh banner");
        }

        banner.setTitle(dto.getTitle().trim());
        banner.setImageUrl(newImageUrl != null ? newImageUrl : oldImageUrl);
        banner.setLinkUrl(trimToNull(dto.getLinkUrl()));
        banner.setActive(Boolean.TRUE.equals(dto.getActive()));
        banner.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        banner.setStartDate(dto.getStartDate());
        banner.setEndDate(dto.getEndDate());
        banner.setDescription(trimToNull(dto.getDescription()));

        Banner saved;
        try {
            saved = bannerRepository.saveAndFlush(banner);
        } catch (RuntimeException ex) {
            if (newImageUrl != null) {
                fileUploadService.delete(newImageUrl, UPLOAD_DIRECTORY);
            }
            throw ex;
        }
        if (newImageUrl != null && oldImageUrl != null) {
            deleteImageAfterCommit(oldImageUrl);
        }
        return saved;
    }

    @CacheEvict(value = "activeBanners", allEntries = true)
    public void delete(Integer id) {
        Banner banner = getById(id);
        String imageUrl = banner.getImageUrl();
        bannerRepository.delete(banner);
        bannerRepository.flush();
        deleteImageAfterCommit(imageUrl);
    }

    @CacheEvict(value = "activeBanners", allEntries = true)
    public Banner toggleActive(Integer id) {
        Banner banner = getById(id);
        banner.setActive(!Boolean.TRUE.equals(banner.getActive()));
        return bannerRepository.save(banner);
    }

    @CacheEvict(value = "activeBanners", allEntries = true)
    public void reorder(List<Integer> bannerIds) {
        for (int i = 0; i < bannerIds.size(); i++) {
            Banner banner = getById(bannerIds.get(i));
            banner.setDisplayOrder(i);
            bannerRepository.save(banner);
        }
        bannerRepository.flush();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void deleteImageAfterCommit(String imageUrl) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileUploadService.delete(imageUrl, UPLOAD_DIRECTORY);
                }
            });
        } else {
            fileUploadService.delete(imageUrl, UPLOAD_DIRECTORY);
        }
    }
}
