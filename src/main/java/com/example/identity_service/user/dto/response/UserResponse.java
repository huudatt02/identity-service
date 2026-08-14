package com.example.identity_service.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private boolean enabled;
    private boolean emailVerified;
    private Set<String> roles;
}
