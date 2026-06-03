package com.duastore.service.client;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String usernameOrEmail, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        User user = userOpt.get();

        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
        }

        if (!user.getPassword().equals(rawPassword)) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        return user;
    }

    public User register(String username, String email, String password, String hoTen, String soDienThoai) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setHoTen(hoTen);
        user.setSoDienThoai(soDienThoai);
        user.setRole("USER");
        user.setIsActive(true);
        return userRepository.save(user);
    }
}
