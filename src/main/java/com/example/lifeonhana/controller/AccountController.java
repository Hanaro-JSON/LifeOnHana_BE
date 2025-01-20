package com.example.lifeonhana.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.dto.response.SalaryAccountResponseDTO;
import com.example.lifeonhana.service.AccountService;
import com.example.lifeonhana.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Account", description = "계좌 관련 API")
@RestController
@RequestMapping("/api/account")
public class AccountController {
	private final JwtService jwtService;
	private final AccountService accountService;

	public AccountController(JwtService jwtService, AccountService accountService) {
		this.jwtService = jwtService;
		this.accountService = accountService;
	}

	@Operation(summary = "계좌 목록 조회", description = "계좌 목록 조회")
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

	@GetMapping("/salary")
	@Operation(summary = "하나 월급 통장 계좌 잔액 조회", description = "하나 월급 통장 계좌 정보와 잔액을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "급여 계좌를 찾을 수 없습니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getSalaryAccount(@RequestHeader("Authorization") String token) {
		token = token.substring(7);
		Long userId = jwtService.extractUserId(token);
		
		SalaryAccountResponseDTO response = accountService.getSalaryAccount(userId);
		
		return ResponseEntity.ok(
			ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message("월급 통장 조회 성공")
				.data(response)
				.build()
		);
	}
}
