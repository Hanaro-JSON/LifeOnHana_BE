package com.example.lifeonhana.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.service.AccountService;
import com.example.lifeonhana.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Account", description = "계좌 관련 API")
@RestController
public class AccountController {
	private final JwtService jwtService;
	private final AccountService accountService;

	public AccountController(JwtService jwtService, AccountService accountService) {
		this.jwtService = jwtService;
		this.accountService = accountService;
	}

	@Operation(summary = "계좌 목록 조회", description = "계좌 목록 조회")
	@GetMapping("/api/account")
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
}
