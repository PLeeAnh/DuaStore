package com.duastore.config.security;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

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
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) return oauth2User;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4));
            user.setHoTen(name != null ? name : email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            Role userRole = roleRepository.findByName("USER");
            user.setRoles(Set.of(userRole));
            user.setIsActive(true);
            userRepository.save(user);
        } else if (!user.getIsActive()) {
            return oauth2User;
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getName()) || "SUPER_ADMIN".equals(r.getName()));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
