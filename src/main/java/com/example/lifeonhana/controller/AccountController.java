package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.AccountTransferRequest;
import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.dto.response.AccountTransferResponse;
import com.example.lifeonhana.dto.response.SalaryAccountResponseDTO;
import com.example.lifeonhana.service.AccountService;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.global.exception.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@Tag(name = "Account", description = "계좌 관련 API")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
	private final JwtService jwtService;
	private final AccountService accountService;

	@Operation(summary = "계좌 목록 조회",
		description = "계좌 목록을 조회합니다.",
		responses = {
		@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json")),
		@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
		@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
	})
	@GetMapping
	public ResponseEntity<ApiResult> getAccounts(@RequestHeader("Authorization") String token) {

		token = token.substring(7);
		Long userId = jwtService.extractUserId(token);

		AccountListResponseDTO response = accountService.getAccounts(userId);

		return ResponseEntity.ok(
			ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("계좌 목록 조회 성공")
				.data(response)
				.build()
		);
	}

	@Operation(summary = "급여 계좌 잔액 조회", description = "사용자의 급여 계좌 정보와 잔액을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
		@ApiResponse(responseCode = "404", description = "급여 계좌를 찾을 수 없습니다.")
	})
	@GetMapping("/salary")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getSalaryAccount(
		@Parameter(hidden = true)
		@AuthenticationPrincipal String authId
	) {
		if (authId == null || authId.isEmpty()) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}

		SalaryAccountResponseDTO response = accountService.getSalaryAccount(authId);
		
		return ResponseEntity.ok(
			ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("월급 통장 조회 성공")
				.data(response)
				.build()
		);
	}

	@Operation(
		summary = "계좌 이체",
		description = "출금 계좌에서 입금 계좌로 금액을 이체합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "이체 성공", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음", content = @Content(mediaType = "application/json"))
		}
	)
	@PostMapping("/transfer")
	public ResponseEntity<ApiResult> transfer(
		@AuthenticationPrincipal String authId,
		@RequestBody AccountTransferRequest transferRequest
	) {
		if (authId == null || authId.isEmpty()) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		AccountTransferResponse response = accountService.transfer(authId, transferRequest);
		return ResponseEntity.ok(
			ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message("이체가 완료되었습니다.")
				.data(response)
				.build()
		);
	}
}
