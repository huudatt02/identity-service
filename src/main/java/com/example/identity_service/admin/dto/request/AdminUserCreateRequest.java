package com.example.identity_service.admin.dto.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserCreateRequest {

    private String fullName;
    private String phone;
    private String email;
    private String password;
    private Set<String> roles;
}
