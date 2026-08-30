package com.duastore.service;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User superAdminUser;
    private User adminUser;
    private User normalUser;
    private Role superAdminRole;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        superAdminRole = roleRepository.findByName("PRODUCT_OWNER");
        if (superAdminRole == null) {
            superAdminRole = new Role();
            superAdminRole.setName("PRODUCT_OWNER");
            roleRepository.save(superAdminRole);
        }

        adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }

        superAdminUser = createUser("super", "super@test.com", "PRODUCT_OWNER", true, Set.of(superAdminRole));
        adminUser = createUser("admin_test", "admin_test@test.com", "Admin", true, Set.of(adminRole, userRole));
        normalUser = createUser("user", "user@test.com", "User", true, Set.of(userRole));
    }

    private User createUser(String username, String email, String hoTen, boolean active, Set<Role> roles) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setHoTen(hoTen);
        u.setPassword(passwordEncoder.encode("password"));
        u.setIsActive(active);
        u.setRoles(roles);
        return userRepository.save(u);
    }

    @Test
    void toggleStatus_cannotLockSelf() {
        assertThatThrownBy(() -> adminUserService.toggleStatus(superAdminUser.getId(), superAdminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể tự khóa");
    }

    @Test
    void toggleStatus_cannotLockLastSuperAdmin() {
        assertThatThrownBy(() -> adminUserService.toggleStatus(superAdminUser.getId(), adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("khóa tài khoản PRODUCT_OWNER cuối cùng");
    }

    @Test
    void updateUserRoles_cannotRemoveOwnSuperAdmin() {
        User anotherSuper = createUser("super2", "super2@test.com", "Super2", true, Set.of(superAdminRole));
        assertThatThrownBy(() -> adminUserService.updateUserRoles(anotherSuper.getId(),
                List.of(userRole.getId()), anotherSuper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tự gỡ vai trò PRODUCT_OWNER");
    }

    @Test
    void updateUserRoles_adminCannotAssignSuperAdmin() {
        assertThatThrownBy(() -> adminUserService.updateUserRoles(normalUser.getId(),
                List.of(superAdminRole.getId()), adminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chỉ PRODUCT_OWNER");
    }

    @Test
    void updateUserRoles_superAdminCanAssignSuperAdmin() {
        adminUserService.updateUserRoles(normalUser.getId(),
                List.of(superAdminRole.getId()), superAdminUser);
    }

    @Test
    void toggleStatus_adminCanLockNormalUser() {
        adminUserService.toggleStatus(normalUser.getId(), adminUser);
    }

    @Test
    void updateUser_cannotLockSelf() {
        assertThatThrownBy(() -> adminUserService.updateUser(superAdminUser.getId(),
                "Super", "super@test.com", null, false, superAdminUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể tự khóa");
    }
}
