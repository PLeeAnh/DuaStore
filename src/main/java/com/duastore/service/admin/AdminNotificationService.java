package com.duastore.service.admin;

import com.duastore.dto.AdminNotificationDTO;
import com.duastore.model.Notification;
import com.duastore.model.Product;
import com.duastore.model.Promotion;
import com.duastore.repository.NotificationRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý thông báo.
 */
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    public AdminNotificationService(NotificationRepository notificationRepository,
            ProductRepository productRepository,
            PromotionRepository promotionRepository) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findAdminNotifications(pageable);
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(Integer id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
    }

    public Notification save(AdminNotificationDTO dto) {
        Notification notification;
        if (dto.getId() != null) {
            notification = getNotificationById(dto.getId());
            notification.setContent(dto.getContent());
        } else {
            notification = new Notification();
            notification.setContent(dto.getContent());
            notification.setIsActive(true);
        }

        notification.setLinkType(null);
        notification.setLinkId(null);
        notification.setLinkUrl(null);
        notification.setLinkLabel(null);

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            notification.setLinkType("PRODUCT");
            notification.setLinkId(dto.getProductId());
            notification.setLinkUrl("/san-pham/" + dto.getProductId());
            notification.setLinkLabel(product.getTenSanPham());
        } else if (dto.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(dto.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá"));
            notification.setLinkType("PROMOTION");
            notification.setLinkId(dto.getPromotionId());
            notification.setLinkUrl("/khuyen-mai/" + dto.getPromotionId());
            notification.setLinkLabel(promotion.getMaCode() + " - " + promotion.getTenChuongTrinh());
        }

        return notificationRepository.save(notification);
    }

    public void deleteNotification(Integer id) {
        Notification n = getNotificationById(id);
        n.setIsActive(false);
        notificationRepository.save(n);
    }
}
