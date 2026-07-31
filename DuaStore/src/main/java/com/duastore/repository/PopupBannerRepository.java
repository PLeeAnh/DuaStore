package com.duastore.repository;

import com.duastore.model.PopupBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopupBannerRepository extends JpaRepository<PopupBanner, Integer> {

    List<PopupBanner> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM PopupBanner p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<PopupBanner> findActiveBanners();

    @Query("SELECT p FROM PopupBanner p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<PopupBanner> findFirstActiveBanner();
}
