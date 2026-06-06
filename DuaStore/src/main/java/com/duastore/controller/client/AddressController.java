package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Address;
import com.duastore.repository.AddressRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/address")
public class AddressController {

    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;

    public AddressController(AddressRepository addressRepository, SecurityUtil securityUtil) {
        this.addressRepository = addressRepository;
        this.securityUtil = securityUtil;
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
    public String showAddForm(Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        Address address = new Address();
        address.setUserId(userId);
        model.addAttribute("address", address);
        return "view/client/address/address-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return "redirect:/address";
        }
        model.addAttribute("address", address);
        return "view/client/address/address-form";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute("address") Address address) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        address.setUserId(userId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddressByUserId(userId);
        }

        addressRepository.save(address);
        return "redirect:/address";
    }

    // 5. Xử lý Xóa địa chỉ
    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Integer id) {
        addressRepository.deleteById(id);
        return "redirect:/address";
    }

    // 6. Xử lý đặt địa chỉ mặc định nhanh ngoài danh sách
    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable("id") Integer id) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) return "redirect:/dang-nhap";
        addressRepository.clearDefaultAddressByUserId(userId);
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null) {
            address.setIsDefault(true);
            addressRepository.save(address);
        }
        return "redirect:/address";
    }
}