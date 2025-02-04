package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.LifeProductResponseDTO;
import com.example.lifeonhana.dto.response.LoanProductDetailResponseDTO;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.SavingProductResponseDTO;
import com.example.lifeonhana.dto.response.SimpleProductResponseDTO;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products List API", description = "상품 목록 api")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

	private final ProductService productService;

	@Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "상품 목록 조회 성공",
			content = @Content(schema = @Schema(implementation = ProductListResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
	})
	@GetMapping("")
	public ResponseEntity<ApiResult<ProductListResponseDTO<SimpleProductResponseDTO>>> getProducts(
		@Parameter(
			description = "상품 카테고리",
			schema = @Schema(allowableValues = {"SAVING", "LOAN", "LIFE"}),
			example = "SAVING"
		)
		@RequestParam(value = "category", required = false) String category,

		@Parameter(description = "페이지 번호", example = "1")
		@RequestParam(defaultValue = "1") @Min(1) @Max(100) int offset,

		@Parameter(description = "페이지당 항목 수", example = "20")
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
		ProductListResponseDTO<SimpleProductResponseDTO> response = productService.getProducts(category, offset, limit);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.PRODUCT_LIST_SUCCESS, response));
	}

	@GetMapping("/savings/{productId}")
	@Operation(summary = "예적금 상품 상세 조회", description = "예적금 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "예적금 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult<SavingProductResponseDTO>> getSavings(
		@PathVariable Long productId) {
		SavingProductResponseDTO response = productService.getSavingsProduct(productId);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.SAVING_PRODUCT_DETAIL_SUCCESS, response));
	}

	@GetMapping("/loans/{productId}")
	@Operation(summary = "대출 상품 상세 조회", description = "대출 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "대출 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult<LoanProductDetailResponseDTO>> getLoans(
		@PathVariable Long productId) {
		LoanProductDetailResponseDTO response = productService.getLoanProduct(productId);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.LOAN_PRODUCT_DETAIL_SUCCESS, response));
	}

	@GetMapping("/life/{productId}")
	@Operation(summary = "라이프 상품 상세 조회", description = "라이프 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "라이프 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult<LifeProductResponseDTO>> getLife(
		@PathVariable Long productId) {
		LifeProductResponseDTO response = productService.getLifeProduct(productId);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.LIFE_PRODUCT_DETAIL_SUCCESS, response));
	}
}
