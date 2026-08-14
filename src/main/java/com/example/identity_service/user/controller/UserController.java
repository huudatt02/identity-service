package com.example.identity_service.user.controller;

import com.example.identity_service.user.dto.request.ChangePasswordRequest;
import com.example.identity_service.user.dto.request.UserUpdateRequest;
import com.example.identity_service.user.entity.User;
import com.example.identity_service.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateRequest request) {
        User entity = userService.updateUser(request);
        return ResponseEntity.ok(entity);
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<User> changePassword(@RequestBody ChangePasswordRequest request) {
        User entity = userService.changePassword(request);
        return ResponseEntity.ok(entity);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.noContent().build();
    }
}
