package com.duastore.service;

import com.duastore.model.UserSetting;
import com.duastore.repository.UserSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;

    public UserSettingService(UserSettingRepository userSettingRepository) {
        this.userSettingRepository = userSettingRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getSettings(Integer userId) {
        return userSettingRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserSetting::getSettingKey, s -> s.getSettingValue() != null ? s.getSettingValue() : ""));
    }

    @Transactional(readOnly = true)
    public String getSetting(Integer userId, String key) {
        return userSettingRepository.findByUserIdAndSettingKey(userId, key)
                .map(UserSetting::getSettingValue).orElse("");
    }

    public void setSetting(Integer userId, String key, String value) {
        UserSetting setting = userSettingRepository.findByUserIdAndSettingKey(userId, key)
                .orElseGet(() -> {
                    UserSetting s = new UserSetting();
                    s.setUserId(userId);
                    s.setSettingKey(key);
                    return s;
                });
        setting.setSettingValue(value);
        userSettingRepository.save(setting);
    }

    public void setSettings(Integer userId, Map<String, String> settings) {
        settings.forEach((key, value) -> setSetting(userId, key, value));
    }
}
