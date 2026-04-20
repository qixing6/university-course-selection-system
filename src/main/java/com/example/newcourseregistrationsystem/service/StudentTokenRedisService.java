package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.config.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class StudentTokenRedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public StudentTokenRedisService(StringRedisTemplate stringRedisTemplate, JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    private String key(long studentDbId) {
        return jwtProperties.getRedisTokenKeyPrefix() + studentDbId;
    }

    public void saveToken(long studentDbId, String token) {
        long seconds = Math.max(1L, jwtProperties.getExpire() / 1000);
        stringRedisTemplate.opsForValue().set(key(studentDbId), token, Duration.ofSeconds(seconds));
    }

    public boolean matchesStoredToken(long studentDbId, String token) {
        String cached = stringRedisTemplate.opsForValue().get(key(studentDbId));
        return token.equals(cached);
    }

    public void removeToken(long studentDbId) {
        stringRedisTemplate.delete(key(studentDbId));
    }
}
