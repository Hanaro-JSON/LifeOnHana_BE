package com.example.lifeonhana.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.ProductInsightRequest;
import com.example.lifeonhana.dto.response.ProductInsightResponse;
import com.example.lifeonhana.service.ProductInsightService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/anthropic")
@Tag(name = "Product Insight API", description = "상품 선택시 기대효과 API")
public class ProductInsightController {

	private final ProductInsightService productInsightService;

	@Operation(
		summary = "상품 기대효과 분석 조회",
		description = "상품 기대효과 결과를 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
		}
	)
	@PostMapping("/effect")
	public ResponseEntity<ApiResult> getProductInsight(
		@RequestBody ProductInsightRequest request,
		@AuthenticationPrincipal String authId
	) {
		try {
			ProductInsightResponse insightResponse = productInsightService.getProductInsight(request, authId);

			return ResponseEntity.ok(new ApiResult(
				200,
				HttpStatus.OK,
				"상품 분석 성공",
				insightResponse
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResult(
				400,
				HttpStatus.BAD_REQUEST,
				e.getMessage(),
				null
			));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResult(
				500,
				HttpStatus.INTERNAL_SERVER_ERROR,
				"내부 서버 오류 발생: " + e.getMessage(),
				null
			));
		}
	}
}
