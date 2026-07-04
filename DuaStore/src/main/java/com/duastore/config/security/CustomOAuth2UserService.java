package com.duastore.config.security;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            return doLoadUser(userRequest);
        } catch (Exception e) {
            log.error("OAuth2 loadUser failed for registrationId={}: {}", 
                userRequest.getClientRegistration().getRegistrationId(), e.getMessage(), e);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("google_login_failed",
                    "Đăng nhập Google thất bại: " + e.getMessage(), null), e);
        }
    }

    private OAuth2User doLoadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) return oauth2User;

        User user = userRepository.findByEmailWithRoles(email).orElse(null);
        if (user == null) {
            user = new User();

            String rawUsername = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);
            if (rawUsername.length() > 50) rawUsername = rawUsername.substring(0, 50);

            user.setUsername(rawUsername);
            user.setEmail(email);
            user.setHoTen(name != null && !name.isBlank() ? name : email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
            }
            user.setRoles(Set.of(userRole));
            user.setIsActive(true);
            userRepository.save(user);
        } else if (!user.getIsActive()) {
            return oauth2User;
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());

        boolean isSuperAdmin = false;
        boolean isAdmin = false;
        for (Role role : user.getRoles()) {
            String rn = role.getName();
            if ("SUPER_ADMIN".equals(rn)) {
                isSuperAdmin = true;
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else if ("ADMIN".equals(rn)) {
                isAdmin = true;
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            Set<Permission> perms = role.getPermissions();
            if (perms != null) {
                for (Permission p : perms) {
                    authorities.add(new SimpleGrantedAuthority(p.getModule() + "_" + p.getAction()));
                }
            }
        }

        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
