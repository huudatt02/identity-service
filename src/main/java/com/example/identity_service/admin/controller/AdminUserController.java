package com.example.identity_service.admin.controller;

import com.example.identity_service.admin.dto.request.AdminUserCreateRequest;
import com.example.identity_service.admin.service.AdminUserService;
import com.example.identity_service.user.dto.response.UserResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return ResponseEntity.ok(adminUserService.searchUsers(search, role, enabled, pageable));
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody AdminUserCreateRequest request) {
        adminUserService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId, @RequestBody String newPassword) {
        adminUserService.changePassword(userId, newPassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> updateRoles(
            @PathVariable UUID userId, @RequestBody Set<String> roleNames) {
        adminUserService.updateRoles(userId, roleNames);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable UUID userId, @RequestBody boolean enabled) {
        adminUserService.updateUserStatus(userId, enabled);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
