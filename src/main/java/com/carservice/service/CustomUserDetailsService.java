package com.carservice.service;

import com.carservice.entity.Customer;
import com.carservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Customer customer = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String role = customer.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }
        role = role.trim().toUpperCase().replace("ROLE_", "");

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();
    }
}