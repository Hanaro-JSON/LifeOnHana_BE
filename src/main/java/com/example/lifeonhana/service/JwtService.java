package com.example.lifeonhana.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	@Value("${jwt.secret}")
	private String secret;

	@Getter
	@Value("${jwt.access.token.expiration}")
	private Long accessTokenExpiration;

	@Getter
	@Value("${jwt.refresh.token.expiration}")
	private Long refreshTokenExpiration;

	private final RedisService redisService;

	public JwtService(RedisService redisService) {
		this.redisService = redisService;
	}

	public String generateAToken(String subject) {}
}
