package com.duastore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý địa danh/tỉnh-thành.
 */
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final String BASE = "https://provinces.open-api.vn/api";
    private static final String DATA_FILE = "static/data/provinces.json";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, List<Map<String, Object>>> districtCache = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> wardCache = new ConcurrentHashMap<>();
    private List<Map<String, Object>> provinceList;
    private boolean fileLoaded = false;

    @PostConstruct
    public void init() {
        try {
            InputStream is = new ClassPathResource(DATA_FILE).getInputStream();
            List<Map<String, Object>> allData = mapper.readValue(is, new TypeReference<>() {
            });
            provinceList = new ArrayList<>();
            for (Map<String, Object> p : allData) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("code", p.get("code"));
                entry.put("name", p.get("name"));
                provinceList.add(entry);
                Object code = p.get("code");
                Object raw = p.get("districts");
                if (code != null && raw instanceof List<?> list) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> dists = (List<Map<String, Object>>) list;
                    districtCache.put(code.toString(), dists);
                    for (Map<String, Object> d : dists) {
                        Object dCode = d.get("code");
                        Object wRaw = d.get("wards");
                        if (dCode != null && wRaw instanceof List<?> wList) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> wards = (List<Map<String, Object>>) wList;
                            wardCache.put(dCode.toString(), wards);
                        }
                    }
                }
            }
            fileLoaded = true;
            log.info("Da tai {} tinh/thanh tu file data", provinceList.size());
        } catch (Exception e) {
            log.warn("Khong the doc file fallback, dung API: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> getProvinces() {
        if (fileLoaded) {
            return provinceList;
        }
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(BASE + "/p/", String.class);
            List<Map<String, Object>> list = mapper.readValue(resp.getBody(), new TypeReference<>() {
            });
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> p : list) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("code", p.get("code"));
                entry.put("name", p.get("name"));
                result.add(entry);
            }
            return result;
        } catch (Exception e) {
            log.error("Loi lay tinh/thanh tu API: {}", e.getMessage());
            return getProvinceFallback();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDistricts(String provinceCode) {
        if (districtCache.containsKey(provinceCode) && !districtCache.get(provinceCode).isEmpty()) {
            return districtCache.get(provinceCode);
        }
        if (districtCache.containsKey(provinceCode) && !fileLoaded) {
            return districtCache.get(provinceCode);
        }
        if (districtCache.containsKey(provinceCode)) {
            return districtCache.get(provinceCode);
        }
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(BASE + "/p/" + provinceCode + "?depth=2", String.class);
            Map<String, Object> province = mapper.readValue(resp.getBody(), new TypeReference<>() {
            });
            Object raw = province.get("districts");
            if (raw instanceof List<?> list) {
                List<Map<String, Object>> dists = (List<Map<String, Object>>) list;
                districtCache.put(provinceCode, dists);
                return dists;
            }
            return List.of();
        } catch (Exception e) {
            log.error("Loi lay quan/huyen cho province {}: {}", provinceCode, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getWards(String districtCode) {
        if (wardCache.containsKey(districtCode) && !wardCache.get(districtCode).isEmpty()) {
            return wardCache.get(districtCode);
        }
        if (wardCache.containsKey(districtCode)) {
            return wardCache.get(districtCode);
        }
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(BASE + "/d/" + districtCode + "?depth=2", String.class);
            Map<String, Object> district = mapper.readValue(resp.getBody(), new TypeReference<>() {
            });
            Object raw = district.get("wards");
            if (raw instanceof List<?> list) {
                List<Map<String, Object>> wards = (List<Map<String, Object>>) list;
                wardCache.put(districtCode, wards);
                return wards;
            }
            return List.of();
        } catch (Exception e) {
            log.error("Loi lay phuong/xa cho district {}: {}", districtCode, e.getMessage());
            return List.of();
        }
    }

    private List<Map<String, Object>> getProvinceFallback() {
        String[][] data = {
            {"1", "Thành phố Hà Nội"}, {"2", "Tỉnh Hà Giang"}, {"4", "Tỉnh Cao Bằng"},
            {"6", "Tỉnh Bắc Kạn"}, {"8", "Tỉnh Tuyên Quang"}, {"10", "Tỉnh Lào Cai"},
            {"11", "Tỉnh Điện Biên"}, {"12", "Tỉnh Lai Châu"}, {"14", "Tỉnh Sơn La"},
            {"15", "Tỉnh Yên Bái"}, {"17", "Tỉnh Hoà Bình"}, {"19", "Tỉnh Thái Nguyên"},
            {"20", "Tỉnh Lạng Sơn"}, {"22", "Tỉnh Quảng Ninh"}, {"24", "Tỉnh Bắc Giang"},
            {"25", "Tỉnh Phú Thọ"}, {"26", "Tỉnh Vĩnh Phúc"}, {"27", "Tỉnh Bắc Ninh"},
            {"30", "Tỉnh Hải Dương"}, {"31", "Thành phố Hải Phòng"}, {"33", "Tỉnh Hưng Yên"},
            {"34", "Tỉnh Thái Bình"}, {"35", "Tỉnh Hà Nam"}, {"36", "Tỉnh Nam Định"},
            {"37", "Tỉnh Ninh Bình"}, {"38", "Tỉnh Thanh Hóa"}, {"40", "Tỉnh Nghệ An"},
            {"42", "Tỉnh Hà Tĩnh"}, {"44", "Tỉnh Quảng Bình"}, {"45", "Tỉnh Quảng Trị"},
            {"46", "Tỉnh Thừa Thiên Huế"}, {"48", "Thành phố Đà Nẵng"}, {"49", "Tỉnh Quảng Nam"},
            {"51", "Tỉnh Quảng Ngãi"}, {"52", "Tỉnh Bình Định"}, {"54", "Tỉnh Phú Yên"},
            {"55", "Tỉnh Khánh Hòa"}, {"56", "Tỉnh Ninh Thuận"}, {"58", "Tỉnh Bình Thuận"},
            {"60", "Tỉnh Kon Tum"}, {"62", "Tỉnh Gia Lai"}, {"64", "Tỉnh Đắk Lắk"},
            {"66", "Tỉnh Đắk Nông"}, {"67", "Tỉnh Lâm Đồng"}, {"68", "Tỉnh Bình Phước"},
            {"70", "Tỉnh Tây Ninh"}, {"72", "Tỉnh Bình Dương"}, {"74", "Tỉnh Đồng Nai"},
            {"75", "Tỉnh Bà Rịa - Vũng Tàu"}, {"77", "Thành phố Hồ Chí Minh"},
            {"79", "Tỉnh Long An"}, {"80", "Tỉnh Tiền Giang"}, {"82", "Tỉnh Bến Tre"},
            {"83", "Tỉnh Trà Vinh"}, {"84", "Tỉnh Vĩnh Long"}, {"86", "Tỉnh Đồng Tháp"},
            {"87", "Tỉnh An Giang"}, {"89", "Tỉnh Kiên Giang"}, {"91", "Tỉnh Cần Thơ"},
            {"92", "Tỉnh Hậu Giang"}, {"93", "Tỉnh Sóc Trăng"}, {"94", "Tỉnh Bạc Liêu"},
            {"95", "Tỉnh Cà Mau"}
        };
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] row : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", Integer.parseInt(row[0]));
            m.put("name", row[1]);
            result.add(m);
        }
        return result;
    }
}
