package com.example.lifeonhana.jwt;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.lifeonhana.filter.JwtAuthenticationFilter;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private RedisService redisService;

	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private MockFilterChain filterChain;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, redisService);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
		objectMapper = new ObjectMapper();
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
		// Given
		String token = "valid-token";
		request.addHeader("Authorization", "Bearer " + token);

		when(redisService.isBlacklisted(token)).thenReturn(false);
		when(jwtService.extractAuthId(token)).thenReturn("test@example.com");
		when(jwtService.isValidToken(token)).thenReturn(true);

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(redisService).isBlacklisted(token);
		verify(jwtService).extractAuthId(token);
		verify(jwtService).isValidToken(token);
		assert SecurityContextHolder.getContext().getAuthentication() != null;
		assert SecurityContextHolder.getContext().getAuthentication().getAuthorities()
			.stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
	}

	@Test
	void doFilterInternal_WithBlacklistedToken_ShouldReturnForbidden() throws ServletException, IOException {
		// Given
		String token = "blacklisted-token";
		request.addHeader("Authorization", "Bearer " + token);

		when(redisService.isBlacklisted(token)).thenReturn(true);

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(redisService).isBlacklisted(token);
		verify(jwtService, never()).isValidToken(anyString());
		assert response.getStatus() == HttpServletResponse.SC_FORBIDDEN;

		String responseBody = response.getContentAsString();
		assert responseBody.contains("Token is blacklisted");
	}

	@Test
	void doFilterInternal_WithExpiredToken_ShouldReturnUnauthorized() throws ServletException, IOException {
		// Given
		String token = "expired-token";
		request.addHeader("Authorization", "Bearer " + token);

		when(jwtService.extractAuthId(token)).thenThrow(new ExpiredJwtException(null, null, "Token is expired"));

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(jwtService).extractAuthId(token);
		verify(redisService, never()).isBlacklisted(anyString());
		verify(jwtService, never()).isValidToken(anyString());
		assert response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED;

		String responseBody = response.getContentAsString();
		assert responseBody.contains("Token is expired");
	}

	@Test
	void doFilterInternal_WithInvalidToken_ShouldReturnBadRequest() throws ServletException, IOException {
		// Given
		String token = "invalid-token";
		request.addHeader("Authorization", "Bearer " + token);

		when(redisService.isBlacklisted(token)).thenReturn(false);
		when(jwtService.extractAuthId(token)).thenReturn("test@example.com");
		when(jwtService.isValidToken(token)).thenReturn(false);

		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(redisService).isBlacklisted(token);
		verify(jwtService).isValidToken(token);
		assert response.getStatus() == HttpServletResponse.SC_BAD_REQUEST;

		String responseBody = response.getContentAsString();
		assert responseBody.contains("Invalid token");
	}

	@Test
	void doFilterInternal_WithNoToken_ShouldContinueChain() throws ServletException, IOException {
		// When
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Then
		verify(redisService, never()).isBlacklisted(anyString());
		verify(jwtService, never()).extractAuthId(anyString());
		verify(jwtService, never()).isValidToken(anyString());
		assert SecurityContextHolder.getContext().getAuthentication() == null;
	}
}
