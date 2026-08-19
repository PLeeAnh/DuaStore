package com.duastore.service.admin;

import com.duastore.model.Permission;
import com.duastore.model.Role;
import com.duastore.repository.PermissionRepository;
import com.duastore.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminRoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public AdminRoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> findAll() {
        List<Role> roles = roleRepository.findAll();
        roles.sort(Comparator.comparing(Role::getId));
        return roles;
    }

    public Role findById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscActionAsc();
    }

    public Map<String, List<Permission>> getPermissionsGroupedByModule() {
        List<Permission> all = getAllPermissions();
        Map<String, List<Permission>> grouped = new LinkedHashMap<>();
        for (Permission p : all) {
            grouped.computeIfAbsent(p.getModule(), k -> new java.util.ArrayList<>()).add(p);
        }
        return grouped;
    }

    private static final Set<String> PROTECTED_NAMES = Set.of("SUPER_ADMIN", "ADMIN", "USER");

    @Transactional
    public Role save(Integer id, String name, String moTa, Boolean isActive, List<Integer> permissionIds) {
        Role role = (id != null) ? roleRepository.findById(id).orElse(new Role()) : new Role();
        String newName = name != null ? name.trim().toUpperCase() : null;
        if ("SUPER_ADMIN".equals(role.getName()) && !"SUPER_ADMIN".equals(newName)) {
            throw new IllegalArgumentException("Không thể đổi tên vai trò SUPER_ADMIN");
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Tên vai trò không được để trống");
        }
        if (PROTECTED_NAMES.contains(newName)) {
            throw new IllegalArgumentException("Không thể tạo hoặc đổi tên vai trò hệ thống: " + newName);
        }
        if (role.getId() == null && roleRepository.findByName(newName) != null) {
            throw new IllegalArgumentException("Vai trò \"" + newName + "\" đã tồn tại");
        }
        role.setName(newName);
        role.setMoTa(com.duastore.util.HtmlSanitizer.sanitize(moTa));
        if (isActive != null) {
            role.setIsActive(isActive);
        }
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(permissionIds));
            role.setPermissions(perms);
        } else {
            role.setPermissions(new HashSet<>());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public boolean delete(Integer id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return false;
        }
        if ("SUPER_ADMIN".equals(role.getName()) || "ADMIN".equals(role.getName()) || "USER".equals(role.getName())) {
            return false;
        }
        role.setPermissions(new HashSet<>());
        roleRepository.save(role);
        roleRepository.delete(role);
        return true;
    }
}
