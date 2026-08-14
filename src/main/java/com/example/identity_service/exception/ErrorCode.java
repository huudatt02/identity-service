package com.example.identity_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "User ID not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Role not found"),
    OLD_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "Old password is incorrect"),

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "Invalid token type"),
    USER_ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "User account is disabled"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Email not verified"),

    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "Token is blacklisted"),
    MISSING_JTI_CLAIM(HttpStatus.BAD_REQUEST, "Missing jti claim in token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    FAILED_TO_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");

    private final HttpStatus status;
    private final String message;
}
