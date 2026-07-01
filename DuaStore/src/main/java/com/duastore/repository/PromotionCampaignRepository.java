package com.duastore.repository;

import com.duastore.model.CampaignStatus;
import com.duastore.model.PromotionCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionCampaignRepository extends JpaRepository<PromotionCampaign, Integer> {

    List<PromotionCampaign> findAllByOrderByStartDateDesc();

    Page<PromotionCampaign> findByStatus(CampaignStatus status, Pageable pageable);

    Page<PromotionCampaign> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT c FROM PromotionCampaign c WHERE c.status = 'ACTIVE' " +
           "AND c.startDate <= :now AND (c.endDate IS NULL OR c.endDate >= :now)")
    List<PromotionCampaign> findActiveNow(@Param("now") LocalDateTime now);
}
