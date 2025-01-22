package com.example.lifeonhana.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.AuthRequestDTO;
import com.example.lifeonhana.dto.response.AuthResponseDTO;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtService jwtService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${jwt.refresh.token.expiration}")
	private Long refreshTokenExpiration;

	@Transactional
	public AuthResponseDTO signIn(AuthRequestDTO request) {
		User user = userRepository.findByAuthId(request.getAuthId())
			.orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UnauthorizedException("잘못된 비밀번호입니다.");
		}

		Boolean isFirstLogin = user.getIsFirst();

		if (isFirstLogin) {
			user.setIsFirst(false);
			userRepository.save(user);
		}

		return generateTokens(user, isFirstLogin);
	}

	public AuthResponseDTO refreshToken(String refreshToken) {
		if (!jwtService.isValidToken(refreshToken)) {
			throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
		}

		String authId = jwtService.extractAuthId(refreshToken);
		Long userId = jwtService.extractUserId(refreshToken);

		String storedRefreshToken = redisService.getRefreshToken(authId);
		if (!refreshToken.equals(storedRefreshToken)) {
			throw new UnauthorizedException("토큰이 일치하지 않습니다.");
		}

		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

		return generateTokens(user, user.getIsFirst());
	}

	public void signOut(String token) {
		try {
			String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
			String authId = jwtService.extractAuthId(accessToken);

			Long expiration = jwtService.getExpirationFromToken(accessToken);
			redisService.addToBlacklist(accessToken, expiration);
			redisService.deleteRefreshToken(authId);
		} catch (Exception e) {
			throw new BadRequestException("로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private AuthResponseDTO generateTokens(User user, Boolean isFirstLogin) {
		String accessToken = jwtService.generateAccessToken(user.getAuthId(), user.getUserId());
		String refreshToken = jwtService.generateRefreshToken(user.getAuthId(), user.getUserId());

		redisService.saveRefreshToken(user.getAuthId(), refreshToken, refreshTokenExpiration);

		return AuthResponseDTO.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.userId(String.valueOf(user.getUserId()))
			.isFirst(isFirstLogin)
			.build();
	}
}
