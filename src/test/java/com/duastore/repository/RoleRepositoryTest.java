package com.duastore.repository;

import com.duastore.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("USER") != null) return;
        Role r = new Role();
        r.setName("USER");
        roleRepository.save(r);
        Role a = new Role();
        a.setName("ADMIN");
        roleRepository.save(a);
        Role s = new Role();
        s.setName("SUPER_ADMIN");
        roleRepository.save(s);
    }

    @Test
    void findByName_USER_returnsRole() {
        Role role = roleRepository.findByName("USER");
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("USER");
    }

    @Test
    void findByName_ADMIN_returnsRole() {
        Role role = roleRepository.findByName("ADMIN");
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("ADMIN");
    }

    @Test
    void findByName_SUPER_ADMIN_returnsRole() {
        Role role = roleRepository.findByName("SUPER_ADMIN");
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void findByName_nonExistent_returnsNull() {
        Role role = roleRepository.findByName("NONEXISTENT");
        assertThat(role).isNull();
    }
}
