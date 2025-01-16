package com.example.lifeonhana.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.service.RedisService;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

	@Mock
	private RedisService redisService;

	@InjectMocks
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(jwtService, "secret", "thisIsAVerySecretKeyForTestingPurposesOnly12345");
		ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);
		ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
	}

	@Test
	void generateAccessToken_Success() {
		// When
		String token = jwtService.generateAccessToken("test@example.com", 1L);

		// Then
		assertNotNull(token);
		assertTrue(jwtService.isValidToken(token));
		assertEquals("test@example.com", jwtService.extractAuthId(token));
		assertEquals(1L, jwtService.extractUserId(token));
	}

	@Test
	void generateRefreshToken_Success() {
		// When
		String token = jwtService.generateRefreshToken("test@example.com", 1L);

		// Then
		assertNotNull(token);
		assertTrue(jwtService.isValidToken(token));
		assertEquals("test@example.com", jwtService.extractAuthId(token));
		assertEquals(1L, jwtService.extractUserId(token));
	}

	@Test
	void isValidToken_BlacklistedToken() {
		// Given
		String token = jwtService.generateAccessToken("test@example.com", 1L);
		when(redisService.isBlacklisted(anyString())).thenReturn(true);

		// When & Then
		assertFalse(jwtService.isValidToken(token));
	}

	@Test
	void isValidToken_ExpiredToken() {
		// Given
		ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
		String token = jwtService.generateAccessToken("test@example.com", 1L);

		// When & Then
		assertFalse(jwtService.isValidToken(token));
	}

	@Test
	void getExpirationFromToken_Success() {
		// Given
		String token = jwtService.generateAccessToken("test@example.com", 1L);

		// When
		Long expiration = jwtService.getExpirationFromToken(token);

		// Then
		assertTrue(expiration > 0);
		assertTrue(expiration <= 3600000L);
	}
}
