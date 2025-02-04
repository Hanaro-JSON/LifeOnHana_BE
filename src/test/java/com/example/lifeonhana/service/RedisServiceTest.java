package com.example.lifeonhana.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private RedisService redisService;

	@Test
	void testConnection_Success() {
		// Given
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("test")).thenReturn("Hello Redis!");

		// When
		redisService.testConnection();

		// Then
		verify(valueOperations).set("test", "Hello Redis!");
		verify(valueOperations).get("test");
	}

	@Test
	void saveRefreshToken_Success() {
		// Given
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// When
		redisService.saveRefreshToken("test@example.com", "refresh-token", 604800000L);

		// Then
		verify(valueOperations).set(
			eq("refresh:test@example.com"),
			eq("refresh-token"),
			eq(604800000L),
			eq(TimeUnit.MILLISECONDS)
		);
	}

	@Test
	void getRefreshToken_Success() {
		// Given
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("refresh:test@example.com")).thenReturn("refresh-token");

		// When
		String result = redisService.getRefreshToken("test@example.com");

		// Then
		assertEquals("refresh-token", result);
	}

	@Test
	void addToBlacklist_Success() {
		// Given
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// When
		redisService.addToBlacklist("token", 3600000L);

		// Then
		verify(valueOperations).set(
			eq("blacklist:token"),
			eq("true"),
			eq(3600000L),
			eq(TimeUnit.MILLISECONDS)
		);
	}

	@Test
	void isBlacklisted_True() {
		// Given
		when(redisTemplate.hasKey("blacklist:token")).thenReturn(true);

		// When
		boolean result = redisService.isBlacklisted("token");

		// Then
		assertTrue(result);
	}

	@Test
	void isBlacklisted_False() {
		// Given
		when(redisTemplate.hasKey("blacklist:token")).thenReturn(false);

		// When
		boolean result = redisService.isBlacklisted("token");

		// Then
		assertFalse(result);
	}

	@Test
	void deleteRefreshToken_Success() {
		// Given
		when(redisTemplate.delete("refresh:test@example.com")).thenReturn(true);

		// When
		redisService.deleteRefreshToken("test@example.com");

		// Then
		verify(redisTemplate).delete("refresh:test@example.com");
	}
}
