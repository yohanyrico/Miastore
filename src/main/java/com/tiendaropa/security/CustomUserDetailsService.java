package com.tiendaropa.security;

import com.tiendaropa.model.User;
import com.tiendaropa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                Set<GrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> {
                                        String roleName = role.name();
                                        if (!roleName.startsWith("ROLE_")) {
                                                roleName = "ROLE_" + roleName;
                                        }
                                        return new SimpleGrantedAuthority(roleName);
                                })
                                .collect(Collectors.toSet());

                return org.springframework.security.core.userdetails.User
                                .builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .disabled(!user.isEnabled())
                                .build();
        }
}
