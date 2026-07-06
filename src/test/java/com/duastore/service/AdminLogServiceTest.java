package com.duastore.service;

import com.duastore.model.AdminActionLog;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.AdminActionLogRepository;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AdminLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminLogServiceTest {

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private AdminActionLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
        }

        adminUser = userRepository.findByUsername("admin_log_test").orElse(null);
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin_log_test");
            adminUser.setEmail("admin_log_test@test.com");
            adminUser.setHoTen("Admin Logger");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setIsActive(true);
            adminUser.setRoles(Set.of(adminRole));
            userRepository.save(adminUser);
        }
    }

    @Test
    void ghiLog_suaRole_storesGiaTriCuAndGiaTriMoi() {
        adminLogService.ghiLog(adminUser, "SUA_USER", "USER", 1,
                "USER", "USER,ADMIN",
                "Cập nhật vai trò của Test User: USER -> USER, ADMIN");

        List<AdminActionLog> logs = logRepository.findAll();
        assertThat(logs).isNotEmpty();

        AdminActionLog log = logs.get(0);
        assertThat(log.getHanhDong()).isEqualTo("SUA_USER");
        assertThat(log.getLoaiEntity()).isEqualTo("USER");
        assertThat(log.getGiaTriCu()).isEqualTo("USER");
        assertThat(log.getGiaTriMoi()).isEqualTo("USER,ADMIN");
        assertThat(log.getAdmin().getId()).isEqualTo(adminUser.getId());
    }

    @Test
    void ghiLog_khoaUser_storesTrueToFalse() {
        adminLogService.ghiLog(adminUser, "KHOA_USER", "USER", 1,
                "true", "false",
                "Khóa tài khoản Test User");

        List<AdminActionLog> logs = logRepository.findAll();
        assertThat(logs).isNotEmpty();

        AdminActionLog log = logs.get(0);
        assertThat(log.getHanhDong()).isEqualTo("KHOA_USER");
        assertThat(log.getGiaTriCu()).isEqualTo("true");
        assertThat(log.getGiaTriMoi()).isEqualTo("false");
    }

    @Test
    void ghiLog_taoRole_storesNewValue() {
        adminLogService.ghiLog(adminUser, "TAO_ROLE", "ROLE", 1,
                null, "MANAGER",
                "Tạo vai trò MANAGER");

        List<AdminActionLog> logs = logRepository.findAll();
        AdminActionLog log = logs.get(0);
        assertThat(log.getHanhDong()).isEqualTo("TAO_ROLE");
        assertThat(log.getGiaTriCu()).isNull();
        assertThat(log.getGiaTriMoi()).isEqualTo("MANAGER");
    }

    @Test
    void ghiLog_setsAdminCorrectly() {
        adminLogService.ghiLog(adminUser, "TEST", "TEST", 99,
                null, null, "Test log");

        List<AdminActionLog> logs = logRepository.findAll();
        AdminActionLog log = logs.get(0);
        assertThat(log.getAdmin().getId()).isEqualTo(adminUser.getId());
        assertThat(log.getAdmin().getHoTen()).isEqualTo("Admin Logger");
    }
}
