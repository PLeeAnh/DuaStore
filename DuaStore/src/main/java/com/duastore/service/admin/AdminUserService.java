package com.duastore.service.admin;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import java.util.Set;

@Service
@Transactional
public class AdminUserService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Không tìm thấy người dùng"
                ));
    }

    public void updateUser(
            Integer id,
            String hoTen,
            String email,
            String soDienThoai,
            Boolean isActive,
            User currentAdmin
    ) {

        User user = getUserById(id);

        if (hoTen != null && !hoTen.isBlank()) {
            user.setHoTen(hoTen.trim());
        }

        if (email != null && !email.isBlank()) {
            user.setEmail(email.trim());
        }

        user.setSoDienThoai(soDienThoai);

        if (isActive != null) {

            validateStatusChange(
                    user,
                    isActive,
                    currentAdmin
            );

            user.setIsActive(isActive);
        }

        userRepository.save(user);
    }

    public void updateUserRoles(
            Integer id,
            List<Integer> roleIds,
            User currentAdmin
    ) {

        User user = getUserById(id);

        Set<Role> newRoles = roleIds == null || roleIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(
                        roleRepository.findAllById(roleIds)
                );

        validateRoleAssignment(
                user,
                newRoles,
                currentAdmin
        );

        user.setRoles(newRoles);

        userRepository.save(user);
    }

    public void toggleStatus(
            Integer id,
            User currentAdmin
    ) {

        User user = getUserById(id);

        validateStatusChange(
                user,
                !user.getIsActive(),
                currentAdmin
        );

        user.setIsActive(
                !user.getIsActive()
        );

        userRepository.save(user);
    }

    public User createUser(
            String username,
            String hoTen,
            String email,
            String password,
            String soDienThoai,
            Boolean isActive,
            List<Integer> roleIds,
            User currentAdmin
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setHoTen(hoTen.trim());
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setSoDienThoai(soDienThoai);
        user.setIsActive(isActive != null ? isActive : true);

        Set<Role> roles = roleIds == null || roleIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(roleRepository.findAllById(roleIds));

        validateRoleAssignment(user, roles, currentAdmin);

        user.setRoles(roles);

        return userRepository.save(user);
    }

    private void validateStatusChange(
            User targetUser,
            boolean newStatus,
            User currentAdmin
    ) {

        if (currentAdmin != null
                && currentAdmin.getId().equals(targetUser.getId())
                && !newStatus) {
            throw new IllegalArgumentException(
                    "Không thể tự khóa tài khoản của chính mình"
            );
        }

        boolean isSuperAdmin
                = hasRole(targetUser, SUPER_ADMIN);

        if (isSuperAdmin
                && targetUser.getIsActive()
                && !newStatus) {

            long superAdminCount
                    = countSuperAdmins();

            if (superAdminCount <= 1) {
                throw new IllegalArgumentException(
                        "Không thể khóa tài khoản SUPER_ADMIN cuối cùng"
                );
            }
        }
    }

    public void validateRoleAssignment(
            User targetUser,
            Set<Role> newRoles,
            User currentAdmin
    ) {

        boolean isCurrentSuperAdmin
                = currentAdmin != null
                && hasRole(currentAdmin, SUPER_ADMIN);

        boolean oldHasSuperAdmin
                = hasRole(targetUser, SUPER_ADMIN);

        boolean newHasSuperAdmin
                = newRoles.stream()
                        .anyMatch(role
                                -> SUPER_ADMIN.equals(
                                role.getName()
                        )
                        );

        if (oldHasSuperAdmin
                && !newHasSuperAdmin) {

            if (currentAdmin != null
                    && currentAdmin.getId().equals(
                            targetUser.getId()
                    )) {
                throw new IllegalArgumentException(
                        "Không thể tự gỡ vai trò SUPER_ADMIN của chính mình"
                );
            }

            if (countSuperAdmins() <= 1) {
                throw new IllegalArgumentException(
                        "Không thể gỡ vai trò SUPER_ADMIN cuối cùng"
                );
            }
        }

        if (newHasSuperAdmin
                && !oldHasSuperAdmin
                && !isCurrentSuperAdmin) {
            throw new IllegalArgumentException(
                    "Chỉ SUPER_ADMIN mới được gán vai trò SUPER_ADMIN"
            );
        }
    }

    private boolean hasRole(
            User user,
            String roleName
    ) {

        return user.getRoles()
                .stream()
                .anyMatch(role
                        -> roleName.equals(
                        role.getName()
                )
                );
    }

    @Transactional(readOnly = true)
    public long countSuperAdmins() {
        return userRepository.countActiveByRoleName(
                SUPER_ADMIN
        );
    }

    @Transactional(readOnly = true)
    public long countActiveByRole(
            String roleName
    ) {
        return userRepository.countActiveByRoleName(
                roleName
        );
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
