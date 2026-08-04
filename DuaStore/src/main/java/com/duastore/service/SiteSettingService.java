package com.duastore.service;

import com.duastore.model.SiteSetting;
import com.duastore.repository.SiteSettingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiteSettingService {

    // Giá trị mặc định thông tin cửa hàng khi admin chưa lưu cấu hình.
    public static final Map<String, String> STORE_DEFAULTS = new LinkedHashMap<>();

    // Giá trị mặc định cấu hình SMTP khi admin chưa lưu (form admin /admin/email-smtp).
    public static final Map<String, String> EMAIL_DEFAULTS = new LinkedHashMap<>();

    static {
        STORE_DEFAULTS.put("store_address", "Phố Tôn Thất Thuyết, Phan Bội Châu, Phường Hồng Bàng, Thành phố Hải Phòng, 18000, Việt Nam");
        STORE_DEFAULTS.put("store_phone", "0983595240");
        STORE_DEFAULTS.put("store_email", "anhpltp00872@gmail.com");
        STORE_DEFAULTS.put("store_business_hours",
                "{\"mon\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"tue\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"wed\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"thu\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"fri\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"sat\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]},"
                + "\"sun\":{\"open\":true,\"allDay\":false,\"slots\":[{\"open\":\"08:00\",\"close\":\"19:00\"}]}}");
        EMAIL_DEFAULTS.put("email_host", "smtp.gmail.com");
        EMAIL_DEFAULTS.put("email_port", "587");
        EMAIL_DEFAULTS.put("email_encryption", "tls");
        EMAIL_DEFAULTS.put("email_username", "hhi658724@gmail.com");
        EMAIL_DEFAULTS.put("email_password", "zkhj vycf jtyg tyzr");
        EMAIL_DEFAULTS.put("email_from", "hhi658724@gmail.com");
        EMAIL_DEFAULTS.put("email_from_name", "DuaStore");
    }

    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Cacheable(value = "siteSettings", key = "#key", unless = "#result == null")
    public String getValue(String key) {
        return siteSettingRepository.findBySettingKey(key)
                .map(SiteSetting::getSettingValue)
                .orElse(null);
    }

    public String getValue(String key, String defaultValue) {
        return siteSettingRepository.findBySettingKey(key)
                .map(SiteSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public Map<String, String> getGroup(String group) {
        Map<String, String> result = new HashMap<>();
        siteSettingRepository.findBySettingGroup(group)
                .forEach(s -> result.put(s.getSettingKey(), s.getSettingValue()));
        return result;
    }

    @Transactional
    @CacheEvict(value = "siteSettings", key = "#key")
    public void save(String key, String value, String group) {
        SiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElseGet(() -> {
                    SiteSetting s = new SiteSetting();
                    s.setSettingKey(key);
                    return s;
                });
        setting.setSettingValue(value);
        setting.setSettingGroup(group);
        siteSettingRepository.save(setting);
    }

    @Transactional
    public void saveGroup(Map<String, String> settings, String group) {
        for (var entry : settings.entrySet()) {
            save(entry.getKey(), entry.getValue(), group);
        }
    }

    @Transactional
    public void saveGroupFromParams(Map<String, List<String>> params, String group) {
        for (var entry : params.entrySet()) {
            String key = entry.getKey();
            if ("_csrf".equals(key) || key.isEmpty() || key.endsWith("_text")) {
                continue;
            }
            String value = "";
            List<String> values = entry.getValue();
            if (values != null) {
                for (String v : values) {
                    if (v != null && !v.isEmpty()) {
                        value = v;
                    }
                }
            }
            save(key, value, group);
        }
    }

    @Transactional
    public void deleteByPrefix(String prefix) {
        siteSettingRepository.findBySettingKeyStartingWith(prefix)
                .forEach(siteSettingRepository::delete);
    }
}
