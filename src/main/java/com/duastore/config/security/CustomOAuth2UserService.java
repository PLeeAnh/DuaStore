package com.duastore.config.security;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.model.UserAuthProvider;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserAuthProviderRepository;
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
/**
 * Service chứa nghiệp vụ (business logic) xử lý đăng nhập OAuth2, xác thực đăng nhập, người dùng.
 */
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;

    public CustomOAuth2UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            UserAuthProviderRepository userAuthProviderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userAuthProviderRepository = userAuthProviderRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            return doLoadUser(userRequest);
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 loadUser failed for registrationId={}: {}",
                    userRequest.getClientRegistration().getRegistrationId(), e.getMessage(), e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("google_login_failed",
                            "Dang nhap Google that bai: " + e.getMessage(), null), e);
        }
    }

    private OAuth2User doLoadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleSub = (String) attributes.get("sub");

        log.info("Google OAuth2 login attempt: email={}", email);

        if (email == null) {
            log.warn("Google OAuth2: email is null, returning raw oauth2User");
            return oauth2User;
        }

        User user = userRepository.findByEmailWithRoles(email).orElse(null);

        if (user == null) {
            log.info("Google OAuth2: creating new user for email={}", email);
            user = new User();

            String rawUsername = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);
            if (rawUsername.length() > 50) {
                rawUsername = rawUsername.substring(0, 50);
            }

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
            log.info("Google OAuth2: new user created with id={}", user.getId());

            try {
                UserAuthProvider passProv = new UserAuthProvider();
                passProv.setUserId(user.getId());
                passProv.setProvider("PASSWORD");
                userAuthProviderRepository.save(passProv);
            } catch (Exception ex) {
                log.warn("Google OAuth2: could not save PASSWORD auth provider (DB may need migration): {}", ex.getMessage());
            }

        } else if (!user.getIsActive()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_locked",
                            "Tai khoan cua ban da bi khoa. Vui long lien he quan tri vien.", null));
        }

        if (googleSub != null) {
            try {
                boolean alreadyLinked = userAuthProviderRepository.existsByUserIdAndProvider(user.getId(), "GOOGLE");
                if (!alreadyLinked) {
                    UserAuthProvider googleProv = new UserAuthProvider();
                    googleProv.setUserId(user.getId());
                    googleProv.setProvider("GOOGLE");
                    googleProv.setProviderSub(googleSub);
                    userAuthProviderRepository.save(googleProv);
                    log.info("Google OAuth2: saved GOOGLE auth provider for userId={}", user.getId());
                }
            } catch (Exception ex) {
                log.warn("Google OAuth2: could not save GOOGLE auth provider (DB may need migration): {}", ex.getMessage());
            }
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());

        for (Role role : user.getRoles()) {
            String rn = role.getName();
            if ("SUPER_ADMIN".equals(rn)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else if ("ADMIN".equals(rn)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if ("USER".equals(rn)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            Set<Permission> perms = role.getPermissions();
            if (perms != null) {
                for (Permission p : perms) {
                    authorities.add(new SimpleGrantedAuthority(p.getModule() + "_" + p.getAction()));
                }
            }
        }

        log.info("Google OAuth2: login successful for email={}", email);
        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
