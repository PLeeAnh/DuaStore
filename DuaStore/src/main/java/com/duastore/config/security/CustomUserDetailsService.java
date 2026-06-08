package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                getAuthorities(user.getRole())
        );
    }

    public UserDetails loadUserByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                getAuthorities(user.getRole())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
