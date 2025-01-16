package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.WalletDTO;
import com.example.lifeonhana.service.WalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/wallet")
@Tag(name="Wallet API", description = "하나지갑 관련 api")
public class WalletController {

	WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@PostMapping("")
	@Operation(summary = "하나지갑 정보 등록", description = "하나지갑 정보를 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "등록 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "하나지갑 정보를 등록할 수 없습니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> createWallet(@RequestHeader ("Authorization") String token,
		@RequestBody WalletDTO wallet) {
		WalletDTO walletDTO = walletService.creatWallet(wallet, token);
		return ResponseEntity.ok(new ApiResult(200, HttpStatus.OK, "하나지갑 정보 등록 성공", walletDTO));
	}

	@GetMapping("")
	@Operation(summary = "하나지갑 정보 조회", description = "하나지갑 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "하나지갑 정보를 찾을 수 없습니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getWallet(@RequestHeader("Authorization") String token) {
		WalletDTO wallet = walletService.getUserWallet(token);
		return ResponseEntity.ok(new ApiResult(200, HttpStatus.OK, "하나지갑 정보 조회 성공", wallet));
	}

	@PutMapping("")
	@Operation(summary = "하나지갑 정보 수정", description = "하나지갑 정보를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "하나지갑 정보를 찾을 수 없습니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> putWallet(@RequestHeader("Authorization") String token, @RequestBody WalletDTO wallet) {
		WalletDTO walletDTO = walletService.updateWallet(wallet, token);
		return ResponseEntity.ok(new ApiResult(HttpStatus.OK.value(), HttpStatus.OK, "하나지갑 정보 수정 성공", walletDTO));
	}

}
