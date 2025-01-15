package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResponse;
import com.example.lifeonhana.dto.AuthRequestDTO;
import com.example.lifeonhana.dto.AuthResponseDTO;
import com.example.lifeonhana.dto.RefreshTokenRequestDTO;
import com.example.lifeonhana.service.AuthService;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	private final JwtService jwtService;

	@PostMapping("/signin")
	public ResponseEntity<ApiResponse> signIn(@RequestBody AuthRequestDTO request) {
		try {
			AuthResponseDTO authResponse = authService.signIn(request);
			return ResponseEntity.ok(ApiResponse.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("로그인 성공")
				.data(authResponse)
				.build());
		} catch (UnauthorizedException e) {
			throw e;
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
		try {
			AuthResponseDTO newTokens = authService.refreshToken(request.getRefreshToken());
			return ResponseEntity.ok(ApiResponse.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("토큰 갱신 성공")
				.data(newTokens)
				.build());
		} catch (UnauthorizedException e) {
			throw e;
		}
	}

	@PostMapping("/signout")
	public ResponseEntity<ApiResponse> signOut(@RequestHeader("Authorization") String token) {
		try {
			authService.signOut(token);
			return ResponseEntity.ok(ApiResponse.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("로그아웃 성공")
				.data(null)
				.build());
		} catch (BadRequestException | UnauthorizedException e) {
			throw e;
		}
	}
}
