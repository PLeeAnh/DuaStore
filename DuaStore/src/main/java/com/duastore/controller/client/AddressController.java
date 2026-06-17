package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Address;
import com.duastore.repository.AddressRepository;
import com.duastore.service.GeocodingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/address")
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

    @GetMapping
    public String listAddresses(Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        model.addAttribute("addresses", addresses);
        return "view/client/address/address-list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model,
                              @RequestParam(required = false) String returnUrl) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        Address address = new Address();
        address.setUserId(userId);
        model.addAttribute("address", address);
        model.addAttribute("returnUrl", returnUrl);
        return "view/client/address/address-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model,
                               @RequestParam(required = false) String returnUrl) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return "redirect:/address";
        }
        model.addAttribute("address", address);
        model.addAttribute("returnUrl", returnUrl);
        return "view/client/address/address-form";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute("address") Address address,
                              @RequestParam(required = false) String returnUrl) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        address.setUserId(userId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddressByUserId(userId);
        }

        geocodingService.geocodeIfMissing(address);
        addressRepository.save(address);
        return (returnUrl != null && !returnUrl.isBlank()) ? "redirect:" + returnUrl : "redirect:/address";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Integer id) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        addressRepository.findById(id).ifPresent(a -> {
            if (a.getUserId().equals(userId)) addressRepository.deleteById(id);
        });
        return "redirect:/address";
    }

    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable("id") Integer id) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        addressRepository.clearDefaultAddressByUserId(userId);
        addressRepository.findById(id).ifPresent(a -> {
            if (a.getUserId().equals(userId)) {
                a.setIsDefault(true);
                addressRepository.save(a);
            }
        });
        return "redirect:/address";
    }

    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSaveAddress(@ModelAttribute Address address) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        try {
            address.setUserId(userId);
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

    @PostMapping("/api/set-default/{id}")
    @ResponseBody
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

    @PostMapping("/api/delete/{id}")
    @ResponseBody
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
}