package com.duastore.service;

import com.duastore.model.Address;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý đơn vị vận chuyển Giao Hàng Nhanh (GHN), vận chuyển.
 */
public class GHNShippingService {

    private static final Logger log = LoggerFactory.getLogger(GHNShippingService.class);
    private static final String API_BASE = "https://dev-online-gateway.ghn.vn/shiip/public-api";

    private static final int DEFAULT_WEIGHT = 500;
    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 10;
    private static final int DEFAULT_LENGTH = 10;
    private static final int SERVICE_TYPE_STANDARD = 2;

    private final SiteSettingService siteSettingService;
    private final RestTemplate restTemplate;

    public GHNShippingService(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
        this.restTemplate = new RestTemplate();
    }

    public boolean isEnabled() {
        String token = getToken();
        return token != null && !token.isBlank();
    }

    public BigDecimal calculateFee(Address address) {
        if (!isEnabled()) {
            return null;
        }
        try {
            Integer toDistrictId = resolveDistrictId(address);
            String toWardCode = resolveWardCode(address);
            if (toDistrictId == null) {
                return null;
            }

            Integer serviceId = findServiceId(toDistrictId);
            if (serviceId == null) {
                return null;
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("service_id", serviceId);
            body.put("service_type_id", SERVICE_TYPE_STANDARD);
            body.put("to_district_id", toDistrictId);
            body.put("to_ward_code", toWardCode);
            body.put("weight", DEFAULT_WEIGHT);
            body.put("length", DEFAULT_LENGTH);
            body.put("width", DEFAULT_WIDTH);
            body.put("height", DEFAULT_HEIGHT);

            Map<String, Object> resp = post("/v2/shipping-order/fee", body);
            if (resp != null && resp.get("data") instanceof Map<?, ?> data) {
                Number total = (Number) data.get("total");
                if (total != null) {
                    return BigDecimal.valueOf(total.longValue());
                }
            }
        } catch (Exception e) {
            log.warn("GHN fee calculation failed: {}", e.getMessage());
        }
        return null;
    }

    public String createOrder(Order order, Address address) {
        if (!isEnabled() || address == null) {
            return null;
        }
        try {
            Integer toDistrictId = resolveDistrictId(address);
            String toWardCode = resolveWardCode(address);
            if (toDistrictId == null) {
                return null;
            }

            Integer serviceId = findServiceId(toDistrictId);
            if (serviceId == null) {
                return null;
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("to_name", address.getTenNguoiNhan());
            body.put("to_phone", address.getSoDienThoai());
            body.put("to_address", buildAddressLine(address));
            body.put("to_ward_code", toWardCode);
            body.put("to_district_id", toDistrictId);
            body.put("weight", DEFAULT_WEIGHT);
            body.put("length", DEFAULT_LENGTH);
            body.put("width", DEFAULT_WIDTH);
            body.put("height", DEFAULT_HEIGHT);
            body.put("service_id", serviceId);
            body.put("service_type_id", SERVICE_TYPE_STANDARD);
            body.put("payment_type_id", 2);
            body.put("required_note", "CHOTHUHANG");
            body.put("note", order.getGhiChu() != null ? order.getGhiChu() : "");
            body.put("client_order_code", order.getMaDon());

            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                Map<String, Object> i = new LinkedHashMap<>();
                i.put("name", item.getTenSanPham() + " - " + item.getTenBienThe());
                i.put("quantity", item.getSoLuong());
                i.put("price", item.getDonGia().longValue());
                items.add(i);
            }
            body.put("items", items);

            Map<String, Object> resp = post("/v2/shipping-order/create", body);
            if (resp != null && resp.get("data") instanceof Map<?, ?> data) {
                String orderCode = (String) data.get("order_code");
                if (orderCode != null) {
                    log.info("GHN order created: {} for order {}", orderCode, order.getMaDon());
                    return orderCode;
                }
            }
        } catch (Exception e) {
            log.warn("GHN order creation failed for {}: {}", order.getMaDon(), e.getMessage());
        }
        return null;
    }

    public Map<String, Object> getOrderDetail(String ghnOrderCode) {
        if (!isEnabled() || ghnOrderCode == null) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of("order_code", ghnOrderCode);
            return post("/v2/shipping-order/detail", body);
        } catch (Exception e) {
            log.warn("GHN order detail failed: {}", e.getMessage());
            return null;
        }
    }

    private Integer findServiceId(Integer toDistrictId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("shop_id", getShopId());
        body.put("to_district_id", toDistrictId);
        Map<String, Object> resp = post("/v2/shipping-order/available-services", body);
        if (resp != null && resp.get("data") instanceof List<?> list && !list.isEmpty()) {
            if (list.get(0) instanceof Map<?, ?> sv) {
                Number serviceId = (Number) sv.get("service_id");
                return serviceId != null ? serviceId.intValue() : null;
            }
        }
        return null;
    }

    private Integer resolveDistrictId(Address address) {
        if (address.getGhnDistrictId() != null) {
            return address.getGhnDistrictId();
        }
        String val = siteSettingService.getValue("ghn_default_district_id", "1444");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveWardCode(Address address) {
        if (address.getGhnWardCode() != null) {
            return address.getGhnWardCode();
        }
        return siteSettingService.getValue("ghn_default_ward_code", "21012");
    }

    private String buildAddressLine(Address a) {
        return (a.getDiaChiCuThe() != null ? a.getDiaChiCuThe() + ", " : "")
                + (a.getPhuongXa() != null ? a.getPhuongXa() + ", " : "")
                + (a.getQuanHuyen() != null ? a.getQuanHuyen() + ", " : "")
                + (a.getTinhThanh() != null ? a.getTinhThanh() : "");
    }

    private String getToken() {
        return siteSettingService.getValue("ghn_token", "");
    }

    private Integer getShopId() {
        String val = siteSettingService.getValue("ghn_shop_id", "");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isTestMode() {
        return "true".equals(siteSettingService.getValue("ghn_test_mode", "true"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Map<String, Object> body) {
        String token = getToken();
        if (token.isBlank()) {
            return null;
        }

        String url = (isTestMode() ? "https://dev-online-gateway.ghn.vn/shiip/public-api" : API_BASE) + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", token);
        if (getShopId() != null) {
            headers.set("ShopId", getShopId().toString());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return resp.getBody();
        }
        return null;
    }
}
