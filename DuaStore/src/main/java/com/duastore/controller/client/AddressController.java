package com.duastore.controller.client;

import com.duastore.model.Address;
import com.duastore.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    // 1. Giao diện danh sách địa chỉ: http://localhost:8080/address
    @GetMapping
    public String listAddresses(Model model) {
        Integer userId = 2; // Gán cứng tài khoản Nguyễn Văn An để test
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        model.addAttribute("addresses", addresses);
        return "view/client/address/address-list";
    }

    // 2. Giao diện form thêm mới: http://localhost:8080/address/new
    @GetMapping("/new")
    public String showAddForm(Model model) {
        Address address = new Address();
        address.setUserId(2); // Gán sẵn userId = 2 cho thực thể
        model.addAttribute("address", address);
        return "view/client/address/address-form";
    }

    // 3. Giao diện form sửa địa chỉ
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return "redirect:/address";
        }
        model.addAttribute("address", address);
        return "view/client/address/address-form";
    }

    // 4. Xử lý Lưu dữ liệu (Hỗ trợ cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveAddress(@ModelAttribute("address") Address address) {
        address.setUserId(2); // Đảm bảo luôn luôn lưu cho tài khoản test id=2
        
        // Xử lý Task 22: Nếu người dùng đặt địa chỉ này làm mặc định
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddressByUserId(2); // Gỡ mặc định các địa chỉ cũ
        }
        
        addressRepository.save(address);
        return "redirect:/address"; // Lưu xong tự quay về trang danh sách
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
        addressRepository.clearDefaultAddressByUserId(2);
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null) {
            address.setIsDefault(true);
            addressRepository.save(address);
        }
        return "redirect:/address";
    }
}