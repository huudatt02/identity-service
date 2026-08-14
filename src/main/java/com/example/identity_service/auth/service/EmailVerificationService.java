package com.example.identity_service.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String PREFIX = "email_verification:";
    private final StringRedisTemplate redisTemplate;

    public void saveToken(String token, UUID userID) {
        redisTemplate.opsForValue().set(buildKey(token), userID.toString(), Duration.ofMinutes(15));
    }

    public String getUserId(String token) {
        return redisTemplate.opsForValue().get(buildKey(token));
    }

    public void deleteToken(String token) {
        redisTemplate.delete(buildKey(token));
    }

    public String buildKey(String token) {
        return PREFIX + token;
    }
}
