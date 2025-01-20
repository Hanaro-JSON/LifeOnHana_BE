package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.AuthRequestDTO;
import com.example.lifeonhana.dto.response.AuthResponseDTO;
import com.example.lifeonhana.dto.request.RefreshTokenRequestDTO;
import com.example.lifeonhana.service.AuthService;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.UnauthorizedException;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {
	private final AuthService authService;
	private final JwtService jwtService;

	@Operation(summary = "로그인", description = "사용자 인증 및 토큰 발급")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공",
			content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@PostMapping("/signin")
	public ResponseEntity<ApiResult> signIn(@RequestBody AuthRequestDTO request) {
		try {
			AuthResponseDTO authResponse = authService.signIn(request);
			return ResponseEntity.ok(ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("로그인 성공")
				.data(authResponse)
				.build());
		} catch (UnauthorizedException e) {
			throw e;
		}
	}

	@Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰 발급")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
			content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
	})
	@PostMapping("/refresh")
	public ResponseEntity<ApiResult> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
		try {
			AuthResponseDTO newTokens = authService.refreshToken(request.getRefreshToken());
			return ResponseEntity.ok(ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("토큰 갱신 성공")
				.data(newTokens)
				.build());
		} catch (UnauthorizedException e) {
			throw e;
		}
	}

	@Operation(summary = "로그아웃", description = "사용자 로그아웃 및 토큰 무효화")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
	})
	@PostMapping("/signout")
	public ResponseEntity<ApiResult> signOut(@RequestHeader("Authorization") String token) {
		try {
			authService.signOut(token);
			return ResponseEntity.ok(ApiResult.builder()
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
