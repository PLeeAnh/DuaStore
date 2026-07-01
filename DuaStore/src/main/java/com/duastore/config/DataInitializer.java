package com.duastore.config;

import com.duastore.model.Permission;
import com.duastore.model.Promotion;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.PermissionRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PermissionRepository permissionRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PromotionRepository promotionRepository,
                           PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedPermissions();
        seedUsers();
        seedPromotions();
    }

    private void seedRoles() {
        if (roleRepository.count() > 0) return;
        for (String name : List.of("SUPER_ADMIN", "ADMIN", "USER")) {
            Role r = new Role();
            r.setName(name);
            roleRepository.save(r);
        }
    }

    private void seedPermissions() {
        Map<String, List<String>> perms = Map.ofEntries(
            Map.entry("DASHBOARD", List.of("READ")),
            Map.entry("PRODUCT", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("ORDER", List.of("READ", "UPDATE")),
            Map.entry("USER", List.of("READ", "UPDATE")),
            Map.entry("CATEGORY", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("PROMOTION", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("REVIEW", List.of("READ", "APPROVE", "HIDE", "DELETE")),
            Map.entry("POST", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("VARIANT", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("ROLE", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("AUDIT_LOG", List.of("READ")),
            Map.entry("NOTIFICATION", List.of("CREATE", "READ", "UPDATE", "DELETE")),
            Map.entry("BANNER", List.of("CREATE", "READ", "UPDATE", "DELETE"))
        );

        for (var entry : perms.entrySet()) {
            for (String action : entry.getValue()) {
                if (permissionRepository.findByModuleAndAction(entry.getKey(), action).isEmpty()) {
                    Permission p = new Permission();
                    p.setModule(entry.getKey());
                    p.setAction(action);
                    permissionRepository.save(p);
                }
            }
        }

        Role adminRole = roleRepository.findByName("ADMIN");
        Set<Permission> allPerms = Set.copyOf(permissionRepository.findAll());
        adminRole.setPermissions(allPerms);
        roleRepository.save(adminRole);
    }

    private void seedUsers() {
        String adminUsername = "admin";
        String adminPassword = "admin@123";

        Role adminRole = roleRepository.findByName("ADMIN");

        User admin = userRepository.findByUsername(adminUsername).orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail("admin@duastore.vn");
            admin.setHoTen("Quản Trị Viên");
            admin.setSoDienThoai("0901234567");
            admin.setIsActive(true);
        }
        admin.setRoles(Set.of(adminRole));
        admin.setPassword(passwordEncoder.encode(adminPassword));
        userRepository.save(admin);
    }

    private void seedPromotions() {
        List<Promotion> existing = promotionRepository.findAll();
        if (!existing.isEmpty()) {
            for (Promotion p : existing) {
                p.setDenNgay(LocalDateTime.now().plusMonths(6));
                p.setTuNgay(LocalDateTime.now().minusDays(1));
                if (p.getDaDung() == null) p.setDaDung(0);
                if (p.getIsActive() == null) p.setIsActive(true);
                promotionRepository.save(p);
            }
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Promotion p1 = new Promotion();
        p1.setMaCode("KHAIHANG");
        p1.setTenChuongTrinh("Khai trương DuaStore");
        p1.setLoaiGiam("PHAN_TRAM");
        p1.setGiaTriGiam(new BigDecimal("15"));
        p1.setDonHangToiThieu(new BigDecimal("200000"));
        p1.setGiamToiDa(new BigDecimal("100000"));
        p1.setSoLanDung(200);
        p1.setDaDung(0);
        p1.setTuNgay(now.minusDays(1));
        p1.setDenNgay(now.plusMonths(6));
        p1.setIsActive(true);

        Promotion p2 = new Promotion();
        p2.setMaCode("FREESHIP");
        p2.setTenChuongTrinh("Miễn phí vận chuyển");
        p2.setLoaiGiam("SO_TIEN");
        p2.setGiaTriGiam(new BigDecimal("30000"));
        p2.setDonHangToiThieu(new BigDecimal("500000"));
        p2.setDaDung(0);
        p2.setTuNgay(now.minusDays(1));
        p2.setDenNgay(now.plusMonths(6));
        p2.setIsActive(true);

        Promotion p3 = new Promotion();
        p3.setMaCode("DECO50K");
        p3.setTenChuongTrinh("Giảm 50k đơn từ 300k");
        p3.setLoaiGiam("SO_TIEN");
        p3.setGiaTriGiam(new BigDecimal("50000"));
        p3.setDonHangToiThieu(new BigDecimal("300000"));
        p3.setSoLanDung(100);
        p3.setDaDung(0);
        p3.setTuNgay(now.minusDays(1));
        p3.setDenNgay(now.plusMonths(6));
        p3.setIsActive(true);

        promotionRepository.saveAll(List.of(p1, p2, p3));
    }
}
