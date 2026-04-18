package com.nexusdev.service;

import com.nexusdev.model.User;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class NexusUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Find user in your database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found: " + username));

        // Convert role "admin" → "ROLE_ADMIN"
        // Convert role "member" → "ROLE_MEMBER"
        // Spring Security requires "ROLE_" prefix
        String role = "ROLE_" + user.getRole().toUpperCase();

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),  // must be BCrypt hashed
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}