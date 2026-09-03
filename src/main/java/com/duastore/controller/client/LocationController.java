package com.duastore.controller.client;

import com.duastore.service.LocationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
/**
 * Controller xử lý các request HTTP liên quan tới địa danh/tỉnh-thành.
 */
public class LocationController {

    private final LocationService locationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String, Object>>> getProvinces() {
        return ResponseEntity.ok(locationService.getProvinces());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<Map<String, Object>>> getDistricts(@RequestParam String provinceCode) {
        return ResponseEntity.ok(locationService.getDistricts(provinceCode));
    }

    @GetMapping("/wards")
    public ResponseEntity<List<Map<String, Object>>> getWards(@RequestParam String districtCode) {
        return ResponseEntity.ok(locationService.getWards(districtCode));
    }

    /*
     * Dùng Photon (photon.komoot.io) làm nguồn geocode thay cho Nominatim trực tiếp:
     * - Nominatim yêu cầu header User-Agent định danh thật (trình duyệt không cho script
     *   set header này) và chặn/giới hạn IP dùng nhiều mà không tuân thủ usage policy —
     *   nên gọi thẳng từ browser rất dễ bị chặn âm thầm.
     * - Kết quả trả về cho client vẫn được format lại đúng hình dạng JSON kiểu Nominatim
     *   (address{...}, display_name, lat, lon) để không phải sửa lại toàn bộ logic parse
     *   ở phía client (checkout.js).
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<String> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        try {
            String url = "https://photon.komoot.io/reverse?lat=" + lat + "&lon=" + lng;
            JsonNode root = fetchPhoton(url);
            JsonNode features = root != null ? root.get("features") : null;
            if (features != null && features.isArray() && !features.isEmpty()) {
                ObjectNode result = photonFeatureToNominatimLike(features.get(0));
                return ResponseEntity.ok().header("Content-Type", "application/json").body(mapper.writeValueAsString(result));
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().header("Content-Type", "application/json").body("{}");
    }

    @GetMapping("/search-geocode")
    public ResponseEntity<String> searchGeocode(@RequestParam String q) {
        try {
            String url = "https://photon.komoot.io/api/?q=" + java.net.URLEncoder.encode(q + ", Việt Nam", java.nio.charset.StandardCharsets.UTF_8) + "&limit=5";
            JsonNode root = fetchPhoton(url);
            JsonNode features = root != null ? root.get("features") : null;
            ArrayNode results = mapper.createArrayNode();
            if (features != null && features.isArray()) {
                for (JsonNode f : features) {
                    results.add(photonFeatureToNominatimLike(f));
                }
            }
            return ResponseEntity.ok().header("Content-Type", "application/json").body(mapper.writeValueAsString(results));
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok().header("Content-Type", "application/json").body("[]");
    }

    private JsonNode fetchPhoton(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .header("User-Agent", "DuaStore/1.0 (contact@duastore.vn)")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? mapper.readTree(response.body()) : null;
    }

    /** Chuyển 1 Photon GeoJSON feature thành object dạng Nominatim (address{...}, display_name, lat, lon). */
    private ObjectNode photonFeatureToNominatimLike(JsonNode feature) {
        JsonNode props = feature.path("properties");
        JsonNode coords = feature.path("geometry").path("coordinates");
        double lon = coords.size() > 0 ? coords.get(0).asDouble() : 0;
        double lat = coords.size() > 1 ? coords.get(1).asDouble() : 0;

        String houseNumber = textOrEmpty(props, "housenumber");
        String street = textOrEmpty(props, "street");
        String locality = textOrEmpty(props, "locality");
        String district = textOrEmpty(props, "district");
        String city = textOrEmpty(props, "city");
        String state = textOrEmpty(props, "state");
        String name = textOrEmpty(props, "name");

        ObjectNode address = mapper.createObjectNode();
        if (!houseNumber.isEmpty()) address.put("house_number", houseNumber);
        if (!street.isEmpty()) address.put("road", street);
        else if (!name.isEmpty()) address.put("road", name);
        if (!locality.isEmpty()) address.put("suburb", locality);
        if (!district.isEmpty()) address.put("county", district);
        if (!city.isEmpty()) address.put("city", city);
        if (!state.isEmpty()) address.put("state", state);

        List<String> parts = new ArrayList<>();
        if (!houseNumber.isEmpty() && !street.isEmpty()) parts.add(houseNumber + " " + street);
        else if (!street.isEmpty()) parts.add(street);
        else if (!name.isEmpty()) parts.add(name);
        if (!locality.isEmpty()) parts.add(locality);
        if (!district.isEmpty()) parts.add(district);
        if (!city.isEmpty()) parts.add(city);
        if (!state.isEmpty() && !state.equals(city)) parts.add(state);
        parts.add("Việt Nam");
        String displayName = String.join(", ", parts);

        ObjectNode result = mapper.createObjectNode();
        result.put("lat", String.valueOf(lat));
        result.put("lon", String.valueOf(lon));
        result.put("display_name", displayName);
        result.set("address", address);
        return result;
    }

    private String textOrEmpty(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : "";
    }

    @GetMapping("/geoip")
    public ResponseEntity<Map<String, Object>> getGeoFromIP() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipwho.is/"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                double lat = 0, lng = 0;
                String city = "";
                if (body.contains("\"latitude\"")) {
                    lat = Double.parseDouble(extractJsonDouble(body, "latitude"));
                }
                if (body.contains("\"longitude\"")) {
                    lng = Double.parseDouble(extractJsonDouble(body, "longitude"));
                }
                if (body.contains("\"city\"")) {
                    city = extractJsonString(body, "city");
                }
                if (lat != 0 && lng != 0) {
                    return ResponseEntity.ok(Map.of("lat", lat, "lng", lng, "city", city));
                }
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok(Map.of("lat", 21.0285, "lng", 105.8542, "city", "Hà Nội"));
    }

    private String extractJsonDouble(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return "0";
        idx = json.indexOf(':', idx) + 1;
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        return json.substring(idx, end).trim();
    }

    private String extractJsonString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return "";
        idx = json.indexOf(':', idx) + 1;
        while (idx < json.length() && json.charAt(idx) != '"') idx++;
        idx++;
        int end = idx;
        while (end < json.length() && json.charAt(end) != '"') end++;
        return json.substring(idx, end);
    }
}
