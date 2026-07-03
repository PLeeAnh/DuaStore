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

    @Transactional
    public Role save(Integer id, String name, String moTa, List<Integer> permissionIds) {
        Role role = (id != null) ? roleRepository.findById(id).orElse(new Role()) : new Role();
        if ("SUPER_ADMIN".equals(role.getName()) && !"SUPER_ADMIN".equals(name)) {
            throw new IllegalArgumentException("Không thể đổi tên vai trò SUPER_ADMIN");
        }
        role.setName(name);
        role.setMoTa(moTa);
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
        if (role == null) return false;
        if ("SUPER_ADMIN".equals(role.getName()) || "ADMIN".equals(role.getName()) || "USER".equals(role.getName())) {
            return false;
        }
        role.setPermissions(new HashSet<>());
        roleRepository.save(role);
        roleRepository.delete(role);
        return true;
    }
}
