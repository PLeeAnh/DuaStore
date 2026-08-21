package com.duastore.repository;

import com.duastore.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu cấu hình hệ thống.
 */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Integer> {

    Optional<SiteSetting> findBySettingKey(String settingKey);

    List<SiteSetting> findBySettingGroup(String settingGroup);

    List<SiteSetting> findBySettingKeyStartingWith(String prefix);
}
