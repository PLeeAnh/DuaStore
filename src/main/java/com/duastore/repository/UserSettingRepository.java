package com.duastore.repository;

import com.duastore.model.UserSetting;
import com.duastore.model.UserSettingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, UserSettingId> {
    List<UserSetting> findByUserId(Integer userId);
    Optional<UserSetting> findByUserIdAndSettingKey(Integer userId, String settingKey);
}
