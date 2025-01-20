package com.example.lifeonhana.auth;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lifeonhana.dto.request.AuthRequestDTO;
import com.example.lifeonhana.dto.response.AuthResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.service.AuthService;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.service.RedisService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private RedisService redisService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	private User testUser;
	private AuthRequestDTO testRequest;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setUserId(1L);
		testUser.setAuthId("test@example.com");
		testUser.setPassword("hashedPassword");

		testRequest = new AuthRequestDTO();
		testRequest.setAuthId("test@example.com");
		testRequest.setPassword("password123");

		ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
	}

	@Test
	void signIn_Success() {
		// Given
		when(userRepository.findByAuthId(anyString())).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
		when(jwtService.generateAccessToken(anyString(), anyLong())).thenReturn("access-token");
		when(jwtService.generateRefreshToken(anyString(), anyLong())).thenReturn("refresh-token");

		// When
		AuthResponseDTO response = authService.signIn(testRequest);

		// Then
		assertNotNull(response);
		assertEquals("access-token", response.getAccessToken());
		assertEquals("refresh-token", response.getRefreshToken());
		assertEquals("1", response.getUserId());

		verify(redisService).saveRefreshToken(anyString(), anyString(), anyLong());
	}

	@Test
	void signIn_UserNotFound() {
		// Given
		when(userRepository.findByAuthId(anyString())).thenReturn(Optional.empty());

		// When & Then
		assertThrows(NotFoundException.class, () -> authService.signIn(testRequest));
	}

	@Test
	void signIn_InvalidPassword() {
		// Given
		when(userRepository.findByAuthId(anyString())).thenReturn(Optional.of(testUser));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

		// When & Then
		assertThrows(UnauthorizedException.class, () -> authService.signIn(testRequest));
	}

	@Test
	void refreshToken_Success() {
		// Given
		String refreshToken = "valid-refresh-token";
		when(jwtService.isValidToken(anyString())).thenReturn(true);
		when(jwtService.extractAuthId(anyString())).thenReturn("test@example.com");
		when(jwtService.extractUserId(anyString())).thenReturn(1L);
		when(redisService.getRefreshToken(anyString())).thenReturn(refreshToken);
		when(userRepository.findByAuthId(anyString())).thenReturn(Optional.of(testUser));
		when(jwtService.generateAccessToken(anyString(), anyLong())).thenReturn("new-access-token");
		when(jwtService.generateRefreshToken(anyString(), anyLong())).thenReturn("new-refresh-token");

		// When
		AuthResponseDTO response = authService.refreshToken(refreshToken);

		// Then
		assertNotNull(response);
		assertEquals("new-access-token", response.getAccessToken());
		assertEquals("new-refresh-token", response.getRefreshToken());
	}

	@Test
	void signOut_Success() {
		// Given
		String token = "Bearer access-token";
		when(jwtService.extractAuthId(anyString())).thenReturn("test@example.com");
		when(jwtService.getExpirationFromToken(anyString())).thenReturn(3600000L);

		// When
		authService.signOut(token);

		// Then
		verify(redisService).addToBlacklist(anyString(), anyLong());
		verify(redisService).deleteRefreshToken(anyString());
	}
}
