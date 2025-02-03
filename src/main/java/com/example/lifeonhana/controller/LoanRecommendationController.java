package com.example.lifeonhana.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.LoanRecommendationRequest;
import com.example.lifeonhana.dto.response.LoanProductResponse;
import com.example.lifeonhana.service.LoanRecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/anthropic/loans")
@Tag(name = "Loan Recommendation API", description = "대출 상품 추천 API")
public class LoanRecommendationController {

	private final LoanRecommendationService loanRecommendationService;

	@Operation(
		summary = "대출 상품 추천",
		description = "대출 사유와 금액, 마이데이터를 기반으로 대출 상품을 추천합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "추천 성공", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
		}
	)
	@PostMapping
	public ResponseEntity<ApiResult<List<LoanProductResponse>>> recommendLoanProducts(
		@RequestBody LoanRecommendationRequest request,
		@AuthenticationPrincipal String authId
	) {
		String reason = request.reason();
		BigDecimal amount = request.amount();

		List<LoanProductResponse> recommendedProducts = loanRecommendationService
			.recommendLoanProducts(reason, amount, authId);

		return ResponseEntity.ok(ApiResult.<List<LoanProductResponse>>builder()
			.code(String.valueOf(HttpStatus.OK.value()))
			.status(HttpStatus.OK)
			.message("대출 상품 추천 성공")
			.data(recommendedProducts)
			.build());
	}
}
