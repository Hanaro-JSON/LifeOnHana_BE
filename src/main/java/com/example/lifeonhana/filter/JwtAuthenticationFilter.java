package com.example.lifeonhana.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");

		// Authorization 헤더가 없거나 Bearer 토큰이 아닌 경우 통과
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			final String jwt = authHeader.substring(7);
			final String authId = jwtService.extractEmail(jwt);

			// 토큰이 블랙리스트에 있는지 확인
			if (redisService.isBlacklisted(jwt)) {
				setErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Token is blacklisted");
				return;
			}

			if (authId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				if (jwtService.isValidToken(jwt)) {
					// 단순히 ROLE_USER 권한만 부여
					List<SimpleGrantedAuthority> authorities =
						Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						authId,
						null,
						authorities
					);

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				} else {
					setErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
					return;
				}
			}
		} catch (ExpiredJwtException e) {
			setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
			return;
		} catch (Exception e) {
			setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token validation failed");
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
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetails));
	}
}
