package com.example.identity_service.config;

import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.user.entity.Role;
import com.example.identity_service.user.entity.User;
import com.example.identity_service.user.repository.RoleRepository;
import com.example.identity_service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            createRoleIfNotExists("USER");
            createRoleIfNotExists("ADMIN");
            createAdminIfNotExists();
        };
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }

    private void createAdminIfNotExists() {
        String email = "admin@gmail.com";

        if (userRepository.existsByEmail(email)) {
            return;
        }

        Role adminRole =
                roleRepository
                        .findByName("ADMIN")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User admin = new User();
        admin.setEmail(email);
        admin.setFullName("Administrator");
        admin.setPhoneNumber("0338851230");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
    }
}
