package com.duastore.service.admin;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User getUserById(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return user;
    }

    public void updateUser(Integer id, String hoTen, String email, String soDienThoai, Boolean isActive, User currentAdmin) {
        User user = getUserById(id);
        if (hoTen != null) user.setHoTen(hoTen);
        if (email != null) user.setEmail(email);
        user.setSoDienThoai(soDienThoai);

        if (isActive != null) {
            if (currentAdmin.getId().equals(id) && !isActive) {
                throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình");
            }
            boolean isSuperAdmin = user.getRoles().stream()
                    .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));
            if (isSuperAdmin && !isActive) {
                long superAdminCount = countSuperAdmins();
                if (superAdminCount <= 1) {
                    throw new IllegalArgumentException("Không thể khóa tài khoản SUPER_ADMIN cuối cùng");
                }
            }
            user.setIsActive(isActive);
        }

        userRepository.save(user);
    }

    public void updateUserRoles(Integer id, List<Integer> roleIds, User currentAdmin) {
        User user = getUserById(id);
        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            newRoles = new HashSet<>(roleRepository.findAllById(roleIds));
        }

        validateRoleAssignment(user, newRoles, currentAdmin);

        user.setRoles(newRoles);
        userRepository.save(user);
    }

    public void toggleStatus(Integer id, User currentAdmin) {
        User user = getUserById(id);

        if (currentAdmin.getId().equals(id)) {
            throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình");
        }

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));
        if (isSuperAdmin && user.getIsActive()) {
            long superAdminCount = countSuperAdmins();
            if (superAdminCount <= 1) {
                throw new IllegalArgumentException("Không thể khóa tài khoản SUPER_ADMIN cuối cùng");
            }
        }

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public void validateRoleAssignment(User targetUser, Set<Role> newRoles, User currentAdmin) {
        String currentAdminName = (currentAdmin != null) ? currentAdmin.getHoTen() : "Unknown";

        boolean isCurrentSuperAdmin = currentAdmin != null && currentAdmin.getRoles().stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));

        boolean newHasSuperAdmin = newRoles.stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));

        boolean oldHasSuperAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));

        if (oldHasSuperAdmin && !newHasSuperAdmin) {
            if (currentAdmin.getId().equals(targetUser.getId())) {
                throw new IllegalArgumentException("Không thể tự gỡ vai trò SUPER_ADMIN của chính mình");
            }
            long superAdminCount = countSuperAdmins();
            if (superAdminCount <= 1) {
                throw new IllegalArgumentException("Không thể gỡ vai trò SUPER_ADMIN của người dùng cuối cùng");
            }
        }

        if (newHasSuperAdmin && !oldHasSuperAdmin) {
            if (!isCurrentSuperAdmin) {
                throw new IllegalArgumentException("Chỉ SUPER_ADMIN mới có thể gán vai trò SUPER_ADMIN");
            }
        }
    }

    public long countSuperAdmins() {
        return userRepository.countActiveByRoleName("SUPER_ADMIN");
    }

    public long countActiveByRole(String roleName) {
        return userRepository.countActiveByRoleName(roleName);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
