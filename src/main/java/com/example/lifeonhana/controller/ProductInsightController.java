package com.example.lifeonhana.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.ProductInsightRequest;
import com.example.lifeonhana.dto.response.ProductInsightResponse;
import com.example.lifeonhana.service.ProductInsightService;
import com.example.lifeonhana.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/anthropic")
@Tag(name = "Product Insight API", description = "상품 선택시 기대효과 API")
public class ProductInsightController {

	private final ProductInsightService productInsightService;

	@PostMapping("/effect")
	@Operation(summary = "상품 기대효과 분석 조회", description = "상품 기대효과 결과를 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "분석 성공", 
			content = @Content(schema = @Schema(implementation = ProductInsightResponse.class))),
		@ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
		@ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
		@ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
	})
	public ResponseEntity<ApiResult<ProductInsightResponse>> getProductInsight(
		@RequestBody ProductInsightRequest request,
		@AuthenticationPrincipal String authId) 
	{
		ProductInsightResponse response = productInsightService.getProductInsight(request, authId);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.INSIGHT_ANALYSIS_SUCCESS, response));
	}
}
