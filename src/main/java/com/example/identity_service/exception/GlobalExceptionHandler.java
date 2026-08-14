package com.example.identity_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ErrorResponse response =
                new ErrorResponse(
                        errorCode.getStatus().value(),
                        errorCode.name(),
                        errorCode.getMessage(),
                        Instant.now());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
