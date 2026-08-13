package com.duastore.controller.client;

import com.duastore.model.PopupBanner;
import com.duastore.repository.PopupBannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/popup-banner")
public class PopupBannerApiController {

    private final PopupBannerRepository popupBannerRepository;

    public PopupBannerApiController(PopupBannerRepository popupBannerRepository) {
        this.popupBannerRepository = popupBannerRepository;
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActive() {
        java.util.List<PopupBanner> list = popupBannerRepository.findActiveBanners();
        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of("active", false));
        }
        PopupBanner banner = list.get(0);
        return ResponseEntity.ok(Map.of(
                "active", true,
                "id", banner.getId(),
                "imageUrl", banner.getImageUrl(),
                "linkUrl", banner.getLinkUrl() != null ? banner.getLinkUrl() : "",
                "displayMode", banner.getDisplayMode(),
                "intervalMinutes", banner.getIntervalMinutes() != null ? banner.getIntervalMinutes() : 0
        ));
    }
}
