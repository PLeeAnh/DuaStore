package com.duastore.repository;

import com.duastore.model.Permission;
import com.duastore.model.Role;
import com.duastore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername("testuser").isPresent()) return;

        Role userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }

        User u = new User();
        u.setUsername("testuser");
        u.setEmail("testuser@test.com");
        u.setHoTen("Test User");
        u.setPassword(passwordEncoder.encode("password"));
        u.setIsActive(true);
        u.setRoles(Set.of(userRole));
        userRepository.save(u);

        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
            for (String mod : Set.of("PRODUCT", "USER")) {
                if (permissionRepository.findByModuleAndAction(mod, "READ").isEmpty()) {
                    Permission p = new Permission();
                    p.setModule(mod);
                    p.setAction("READ");
                    permissionRepository.save(p);
                }
            }
            adminRole.setPermissions(Set.copyOf(permissionRepository.findAll()));
            roleRepository.save(adminRole);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User a = new User();
            a.setUsername("admin");
            a.setEmail("admin@test.com");
            a.setHoTen("Admin");
            a.setPassword(passwordEncoder.encode("admin"));
            a.setIsActive(true);
            a.setRoles(Set.of(adminRole));
            userRepository.save(a);
        }
    }

    @Test
    void findByUsernameOrEmail_withUsername_returnsUser() {
        Optional<User> result = userRepository.findByUsernameOrEmail("testuser");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsernameOrEmail_withEmail_returnsUser() {
        Optional<User> result = userRepository.findByUsernameOrEmail("testuser@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("testuser@test.com");
    }

    @Test
    void findByUsernameOrEmail_withNonExistent_returnsEmpty() {
        Optional<User> result = userRepository.findByUsernameOrEmail("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameOrEmail_rolesAreLoaded() {
        Optional<User> result = userRepository.findByUsernameOrEmail("testuser");
        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).isNotEmpty();
    }

    @Test
    void findByUsernameOrEmail_permissionsLoaded() {
        Optional<User> result = userRepository.findByUsernameOrEmail("admin");
        assertThat(result).isPresent();
        User user = result.get();
        boolean hasPerms = user.getRoles().stream()
                .anyMatch(r -> r.getPermissions() != null && !r.getPermissions().isEmpty());
        assertThat(hasPerms).isTrue();
    }
}
