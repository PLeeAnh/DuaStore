package com.duastore.service.admin;

import com.duastore.model.CampaignStatus;
import com.duastore.model.PromotionCampaign;
import com.duastore.repository.PromotionCampaignRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminCampaignService {

    private final PromotionCampaignRepository campaignRepository;

    public AdminCampaignService(PromotionCampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Transactional(readOnly = true)
    public Page<PromotionCampaign> getAllCampaigns(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return campaignRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PromotionCampaign> searchCampaigns(String keyword, CampaignStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (keyword != null && !keyword.isBlank()) {
            return campaignRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        if (status != null) {
            return campaignRepository.findByStatus(status, pageable);
        }
        return getAllCampaigns(page, size);
    }

    @Transactional(readOnly = true)
    public PromotionCampaign getCampaignById(Integer id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));
    }

    public PromotionCampaign saveCampaign(PromotionCampaign campaign) {
        if (campaign.getStartDate() != null && campaign.getEndDate() != null
                && campaign.getStartDate().isAfter(campaign.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        return campaignRepository.save(campaign);
    }

    public void deleteCampaign(Integer id) {
        PromotionCampaign c = getCampaignById(id);
        c.setStatus(CampaignStatus.ENDED);
        campaignRepository.save(c);
    }

    public void togglePause(Integer id) {
        PromotionCampaign c = getCampaignById(id);
        if (c.getStatus() == CampaignStatus.ACTIVE) {
            c.setStatus(CampaignStatus.PAUSED);
        } else if (c.getStatus() == CampaignStatus.PAUSED) {
            c.setStatus(CampaignStatus.ACTIVE);
        }
        campaignRepository.save(c);
    }

    public PromotionCampaign cloneCampaign(Integer id) {
        PromotionCampaign original = getCampaignById(id);
        PromotionCampaign clone = new PromotionCampaign();
        clone.setName(original.getName() + " (Copy)");
        clone.setBanner(original.getBanner());
        clone.setStartDate(original.getStartDate());
        clone.setEndDate(original.getEndDate());
        clone.setStatus(CampaignStatus.DRAFT);
        return campaignRepository.save(clone);
    }

    @Transactional(readOnly = true)
    public List<PromotionCampaign> getActiveCampaigns() {
        return campaignRepository.findActiveNow(LocalDateTime.now());
    }
}
