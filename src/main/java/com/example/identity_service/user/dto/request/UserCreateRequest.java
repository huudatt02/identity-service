package com.example.identity_service.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
}
