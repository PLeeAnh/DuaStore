package com.duastore.controller.client;

import com.duastore.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
/**
 * Controller xử lý các request HTTP liên quan tới địa danh/tỉnh-thành.
 */
public class LocationController {

    private final LocationService locationService;

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
