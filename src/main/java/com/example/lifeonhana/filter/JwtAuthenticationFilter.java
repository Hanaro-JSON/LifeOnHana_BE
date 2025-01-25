package com.example.lifeonhana.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final RedisService redisService;

	@Override
	public void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			final String jwt = authHeader.substring(7);

			// 토큰 유효성 먼저 검사
			if (!jwtService.isValidToken(jwt)) {
				setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
				return;
			}

			// 토큰이 유효한 경우에만 authId 추출
			final String authId = jwtService.extractAuthId(jwt);

			// 블랙리스트 확인
			if (redisService.isBlacklisted(jwt)) {
				setErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Token is blacklisted");
				return;
			}

			if (authId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				List<SimpleGrantedAuthority> authorities =
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					authId,
					null,
					authorities
				);

				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		} catch (ExpiredJwtException e) {
			setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
			return;
		} catch (IllegalArgumentException | SecurityException e) {
			setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
			return;
		} catch (Exception e) {
			log.error("JWT 처리 중 예외 발생", e);
			setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("status", status);
		errorDetails.put("message", message);
		errorDetails.put("data", null);
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetails));
	}
}
