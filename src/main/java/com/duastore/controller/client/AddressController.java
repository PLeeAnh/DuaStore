package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Address;
import com.duastore.repository.AddressRepository;
import com.duastore.service.GeocodingService;
import com.duastore.service.LocationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/address/api")
/**
 * Controller xử lý các request HTTP liên quan tới địa chỉ giao hàng.
 */
public class AddressController {

    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;
    private final GeocodingService geocodingService;
    private final LocationService locationService;

    public AddressController(AddressRepository addressRepository, SecurityUtil securityUtil,
            GeocodingService geocodingService, LocationService locationService) {
        this.addressRepository = addressRepository;
        this.securityUtil = securityUtil;
        this.geocodingService = geocodingService;
        this.locationService = locationService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> apiSaveAddress(@Valid Address address, BindingResult bindingResult) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            res.put("success", false);
            res.put("message", errorMsg);
            return ResponseEntity.ok(res);
        }

        try {
            address.setUserId(userId);
            if (address.getId() == null && addressRepository.countByUserId(userId) >= 10) {
                res.put("success", false);
                res.put("message", "Chỉ được thêm tối đa 10 địa chỉ");
                return ResponseEntity.ok(res);
            }
            String dc = address.getDiaChiCuThe();
            if (dc != null && isLocationNameConflict(dc, address.getTinhThanh(), address.getQuanHuyen(), address.getPhuongXa())) {
                res.put("success", false);
                res.put("message", "Số nhà, đường không được nhập tên tỉnh/quận/phường");
                return ResponseEntity.ok(res);
            }
            Integer existingId = address.getId() != null ? address.getId() : 0;
            if (addressRepository.countByUserIdAndTenNguoiNhanAndSoDienThoaiAndTinhThanhAndQuanHuyenAndPhuongXaAndDiaChiCuTheAndIdNot(
                    userId, address.getTenNguoiNhan(), address.getSoDienThoai(), address.getTinhThanh(), address.getQuanHuyen(), address.getPhuongXa(), address.getDiaChiCuThe(), existingId) > 0) {
                res.put("success", false);
                res.put("message", "Địa chỉ này đã tồn tại");
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
                try {
                    addressRepository.deleteById(id);
                    res.put("success", true);
                } catch (DataIntegrityViolationException e) {
                    res.put("success", false);
                    res.put("message", "Không thể xoá địa chỉ vì có đơn hàng liên quan");
                }
            }
        });
        res.putIfAbsent("success", false);
        return ResponseEntity.ok(res);
    }

    private boolean isLocationNameConflict(String dc, String tinh, String quan, String phuong) {
        String s = dc.toLowerCase().trim();
        if (matchName(s, tinh) || matchName(s, quan) || matchName(s, phuong))
            return true;
        List<Map<String, Object>> provinces = locationService.getProvinces();
        for (Map<String, Object> p : provinces) {
            if (matchName(s, (String) p.get("name")))
                return true;
        }
        return false;
    }

    private boolean matchName(String input, String locationName) {
        if (input == null || locationName == null) return false;
        String n = locationName.toLowerCase().trim();
        return input.equals(n) || n.contains(input) || input.contains(n);
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
