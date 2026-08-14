package com.example.identity_service.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
}
