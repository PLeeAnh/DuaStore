package com.duastore.repository;

import com.duastore.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Integer> {

    Optional<SiteSetting> findBySettingKey(String settingKey);

    List<SiteSetting> findBySettingGroup(String settingGroup);
}
