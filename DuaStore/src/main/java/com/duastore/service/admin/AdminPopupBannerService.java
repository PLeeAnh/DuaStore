package com.duastore.service.admin;

import com.duastore.dto.PopupBannerFormDTO;
import com.duastore.model.PopupBanner;
import com.duastore.repository.PopupBannerRepository;
import com.duastore.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Transactional
public class AdminPopupBannerService {

    private static final String UPLOAD_DIRECTORY = "popup-banners";

    private final PopupBannerRepository popupBannerRepository;
    private final FileUploadService fileUploadService;

    public AdminPopupBannerService(PopupBannerRepository popupBannerRepository,
                                   FileUploadService fileUploadService) {
        this.popupBannerRepository = popupBannerRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public List<PopupBanner> getAll() {
        return popupBannerRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PopupBanner getById(Integer id) {
        return popupBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy popup banner"));
    }

    @Transactional(readOnly = true)
    public PopupBanner getFirstActive() {
        List<PopupBanner> list = popupBannerRepository.findActiveBanners();
        return list.isEmpty() ? null : list.get(0);
    }

    public PopupBanner save(PopupBannerFormDTO dto) {
        PopupBanner banner = dto.getId() == null ? new PopupBanner() : getById(dto.getId());
        String oldImageUrl = banner.getImageUrl();
        String newImageUrl = null;

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            newImageUrl = fileUploadService.save(dto.getImageFile(), UPLOAD_DIRECTORY);
        }
        if (oldImageUrl == null && newImageUrl == null) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh popup");
        }

        banner.setTitle(dto.getTitle().trim());
        banner.setImageUrl(newImageUrl != null ? newImageUrl : oldImageUrl);
        banner.setLinkUrl(trimToNull(dto.getLinkUrl()));
        banner.setDisplayMode(dto.getDisplayMode());
        banner.setIntervalMinutes(dto.getIntervalMinutes());
        banner.setActive(Boolean.TRUE.equals(dto.getActive()));

        PopupBanner saved;
        try {
            saved = popupBannerRepository.saveAndFlush(banner);
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

    public void delete(Integer id) {
        PopupBanner banner = getById(id);
        String imageUrl = banner.getImageUrl();
        popupBannerRepository.delete(banner);
        popupBannerRepository.flush();
        deleteImageAfterCommit(imageUrl);
    }

    public PopupBanner toggleActive(Integer id) {
        PopupBanner banner = getById(id);
        banner.setActive(!Boolean.TRUE.equals(banner.getActive()));
        return popupBannerRepository.save(banner);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
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
