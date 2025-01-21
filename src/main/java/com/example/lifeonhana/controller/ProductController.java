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
import com.example.lifeonhana.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products List API", description = "상품 목록 api")
public class ProductController {

	private final ProductService productService;

	@GetMapping("")
	@Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "상품 목록 조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getProducts(
		@RequestParam(value = "category", required = false) String category,
		@RequestParam(defaultValue = "1") int offset,
		@RequestParam(defaultValue = "20") int limit) {
		ProductListResponseDTO<SimpleProductResponseDTO> productResponse = productService.getProducts(category, offset,
			limit);
		return ResponseEntity.ok(new ApiResult(200, HttpStatus.OK, "상품 목록 조회 성공", productResponse));
	}

	@GetMapping("/savings/{productId}")
	@Operation(summary = "예적금 상품 상세 조회", description = "예적금 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "예적금 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getSavings(
		@PathVariable Long productId) {
		SavingProductResponseDTO savingsResponse = productService.getSavingsProduct(productId);

		return ResponseEntity.ok().body(new ApiResult(200, HttpStatus.OK, "예적금 상품 상세 조회 성공", savingsResponse));
	}

	@GetMapping("/loans/{productId}")
	@Operation(summary = "대출 상품 상세 조회", description = "대출 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "대출 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getLoans(
		@PathVariable Long productId
	) {
		LoanProductDetailResponseDTO loanResponse = productService.getLoanProduct(productId);

		return ResponseEntity.ok().body(new ApiResult(200, HttpStatus.OK, "대출 상품 상세 조회 성공" , loanResponse));
	}

	@GetMapping("/life/{productId}")
	@Operation(summary = "라이프 상품 상세 조회", description = "라이프 상품을 상세 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "라이프 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "존재하지 않는 id 입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getLife(@PathVariable Long productId) {
		LifeProductResponseDTO lifeResponse = productService.getLifeProduct(productId);

		return ResponseEntity.ok().body(new ApiResult(200, HttpStatus.OK, "라이프 상품 상세 조회 성공", lifeResponse));
	}
}
