package com.example.lifeonhana.service;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.AuthRequestDTO;
import com.example.lifeonhana.dto.response.AuthResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;

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
		User user = userRepository.findByAuthId(request.authId())
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BaseException(ErrorCode.INVALID_PASSWORD);
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
			throw new BaseException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		String authId = jwtService.extractAuthId(refreshToken);
		Long userId = jwtService.extractUserId(refreshToken);

		String storedRefreshToken = redisService.getRefreshToken(authId);
		if (!refreshToken.equals(storedRefreshToken)) {
			throw new BaseException(ErrorCode.TOKEN_MISMATCH);
		}

		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

		return generateTokens(user, user.getIsFirst());
	}

	public void signOut(String token) {
		try {
			String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;

			if (!jwtService.isValidToken(accessToken)) {
				throw new BaseException(ErrorCode.TOKEN_MISMATCH);
			}

			String authId = jwtService.extractAuthId(accessToken);
			Long expiration = jwtService.getExpirationFromToken(accessToken);

			redisService.addToBlacklist(accessToken, expiration);
			redisService.deleteRefreshToken(authId);
		} catch (IllegalArgumentException | SecurityException e) {
			throw new BaseException(ErrorCode.TOKEN_MISMATCH);
		} catch (BaseException e) {
			throw e;
		} catch (Exception e) {
			throw new BaseException(ErrorCode.LOGOUT_ERROR);
		}
	}

	private AuthResponseDTO generateTokens(User user, Boolean isFirstLogin) {
		String accessToken = jwtService.generateAccessToken(user.getAuthId(), user.getUserId());
		String refreshToken = jwtService.generateRefreshToken(user.getAuthId(), user.getUserId());

		redisService.saveRefreshToken(user.getAuthId(), refreshToken, refreshTokenExpiration);

		return new AuthResponseDTO(
			accessToken,
			refreshToken,
			String.valueOf(user.getUserId()),
			isFirstLogin
		);
	}
}
