package com.duastore.controller.client;

import com.duastore.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
