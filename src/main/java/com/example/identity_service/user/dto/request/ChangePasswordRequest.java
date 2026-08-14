package com.example.identity_service.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
