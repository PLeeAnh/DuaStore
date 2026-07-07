package com.duastore.repository;

import com.duastore.model.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        if (permissionRepository.count() > 0) return;
        for (String module : List.of("DASHBOARD", "PRODUCT", "ORDER", "USER",
                "CATEGORY", "PROMOTION", "REVIEW", "POST", "VARIANT", "ROLE", "AUDIT_LOG")) {
            Permission p = new Permission();
            p.setModule(module);
            p.setAction("READ");
            permissionRepository.save(p);
        }
    }

    @Test
    void findAllModules_returnsAllModules() {
        List<String> modules = permissionRepository.findAllModules();
        assertThat(modules).isNotEmpty();
        assertThat(modules).contains(
                "AUDIT_LOG", "CATEGORY", "DASHBOARD", "ORDER", "POST",
                "PRODUCT", "PROMOTION", "REVIEW", "ROLE", "USER", "VARIANT"
        );
    }
}
