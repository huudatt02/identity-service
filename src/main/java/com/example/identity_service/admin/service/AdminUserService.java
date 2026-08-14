package com.example.identity_service.admin.service;

import static com.example.identity_service.user.specification.UserSpecification.*;

import com.example.identity_service.admin.dto.request.AdminUserCreateRequest;
import com.example.identity_service.user.dto.request.UserCreateRequest;
import com.example.identity_service.user.dto.response.UserResponse;
import com.example.identity_service.user.entity.Role;
import com.example.identity_service.user.entity.User;
import com.example.identity_service.user.mapper.UserMapper;
import com.example.identity_service.user.repository.RoleRepository;
import com.example.identity_service.user.repository.UserRepository;
import com.example.identity_service.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUserByAdmin(AdminUserCreateRequest request) {
        Set<Role> roles =
                request.getRoles().stream()
                        .map(
                                roleName ->
                                        roleRepository
                                                .findByName(roleName)
                                                .orElseThrow(
                                                        () ->
                                                                new IllegalArgumentException(
                                                                        "Role not found: "
                                                                                + roleName)))
                        .collect(Collectors.toSet());
        UserCreateRequest userCreateRequest = userMapper.toUserCreationRequest(request);
        userService.createUser(userCreateRequest, roles);
    }

    public void changePassword(UUID userId, String newPassword) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateRoles(UUID userId, Set<String> roleNames) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(roleNames));
        if (roles.size() != roleNames.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    public void updateUserStatus(UUID userId, boolean enabled) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public Page<UserResponse> searchUsers(
            String search, String role, Boolean enabled, Pageable pageable) {
        Specification<User> specification =
                Specification.allOf(search(search), hasRole(role), hasEnabled(enabled));
        return userRepository.findAll(specification, pageable).map(userMapper::toUserResponse);
    }
}
