package com.example.identity_service.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = "refresh:";
    private final StringRedisTemplate redisTemplate;

    public void store(String jti, String userId, Duration ttl) {
        redisTemplate.opsForValue().set(buildKey(jti), userId, ttl);
    }

    public String getUserId(String jti) {
        return redisTemplate.opsForValue().get(buildKey(jti));
    }

    public boolean exists(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(jti)));
    }

    public void revoke(String jti) {
        redisTemplate.delete(buildKey(jti));
    }

    private String buildKey(String jti) {
        return PREFIX + jti;
    }
}
