package com.example.lifeonhana.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, Object> redisTemplate;

	@PostConstruct
	public void testConnection() {
		try {
			redisTemplate.opsForValue().set("test", "Hello Redis!");
			String value = (String) redisTemplate.opsForValue().get("test");
			System.out.println("Redis Test Value: " + value);
		} catch (Exception e) {
			System.err.println("Redis Connection Failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void saveRefreshToken(String authId, String token, Long expiration) {
		redisTemplate.opsForValue().set(
			"refresh:" + authId,
			token,
			expiration,
			TimeUnit.MILLISECONDS
		);
	}

	public String getRefreshToken(String authId) {
		return (String) redisTemplate.opsForValue().get("refresh:" + authId);
	}

	public void addToBlacklist(String token, Long expiration) {
		redisTemplate.opsForValue().set(
			"blacklist:" + token,
			"true",
			expiration,
			TimeUnit.MILLISECONDS
		);
	}

	public boolean isBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
	}

	public void deleteRefreshToken(String authId) {
		redisTemplate.delete("refresh:" + authId);
	}
}
