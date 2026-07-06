package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Address;
import com.duastore.repository.AddressRepository;
import com.duastore.service.GeocodingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/address/api")
public class AddressController {

    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;
    private final GeocodingService geocodingService;

    public AddressController(AddressRepository addressRepository, SecurityUtil securityUtil,
            GeocodingService geocodingService) {
        this.addressRepository = addressRepository;
        this.securityUtil = securityUtil;
        this.geocodingService = geocodingService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> apiSaveAddress(Address address) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        try {
            address.setUserId(userId);
            if (address.getId() == null && addressRepository.countByUserId(userId) >= 10) {
                res.put("success", false);
                res.put("message", "Chỉ được thêm tối đa 10 địa chỉ");
                return ResponseEntity.ok(res);
            }
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                addressRepository.clearDefaultAddressByUserId(userId);
            }
            geocodingService.geocodeIfMissing(address);
            addressRepository.save(address);
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/set-default/{id}")
    public ResponseEntity<Map<String, Object>> apiSetDefault(@PathVariable("id") Integer id) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        addressRepository.findById(id).ifPresent(a -> {
            if (a.getUserId().equals(userId)) {
                addressRepository.clearDefaultAddressByUserId(userId);
                a.setIsDefault(true);
                addressRepository.save(a);
                res.put("success", true);
            }
        });
        res.putIfAbsent("success", false);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> apiDelete(@PathVariable("id") Integer id) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        addressRepository.findById(id).ifPresent(a -> {
            if (a.getUserId().equals(userId)) {
                addressRepository.deleteById(id);
                res.put("success", true);
            }
        });
        res.putIfAbsent("success", false);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> apiGetAddress(@PathVariable("id") Integer id) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        addressRepository.findById(id).ifPresentOrElse(a -> {
            if (!a.getUserId().equals(userId)) {
                res.put("success", false);
                res.put("message", "Không có quyền truy cập");
                return;
            }
            res.put("success", true);
            res.put("id", a.getId());
            res.put("tenNguoiNhan", a.getTenNguoiNhan());
            res.put("soDienThoai", a.getSoDienThoai());
            res.put("tinhThanh", a.getTinhThanh());
            res.put("quanHuyen", a.getQuanHuyen());
            res.put("phuongXa", a.getPhuongXa());
            res.put("diaChiCuThe", a.getDiaChiCuThe());
            res.put("latitude", a.getLatitude());
            res.put("longitude", a.getLongitude());
            res.put("isDefault", a.getIsDefault());
        }, () -> {
            res.put("success", false);
            res.put("message", "Không tìm thấy địa chỉ");
        });
        return ResponseEntity.ok(res);
    }
}
