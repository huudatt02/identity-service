package com.example.identity_service.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:access:";
    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jti, Duration ttl) {
        redisTemplate.opsForValue().set(buildKey(jti), "true", ttl);
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(jti)));
    }

    public String buildKey(String jti) {
        return PREFIX + jti;
    }
}
