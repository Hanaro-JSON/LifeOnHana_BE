package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.ProductResponseDTO;
import com.example.lifeonhana.service.ProductLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users/liked")
@Tag(name = "User Liked API", description = "좋아요한 상품 목록 api")
public class ProductLikeController {
	ProductLikeService productLikeService;

	public ProductLikeController(ProductLikeService productLikeService) {
		this.productLikeService = productLikeService;
	}

	@GetMapping("/products")
	@Operation(summary = "좋아요한 상품 목록 조회", description = "좋아요한 상품 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "종아요한 상품 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "좋아요한 상품이 없습니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> getProductLikes(@AuthenticationPrincipal String authId,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "10") int limit) {

		ProductListResponseDTO<ProductResponseDTO> productLikePage = productLikeService.getProductLikes(authId, offset, limit);

		if (productLikePage.products().isEmpty()) {
			return ResponseEntity.status(404).body(new ApiResult(200, HttpStatus.OK, "좋아요한 상품이 없습니다.", productLikePage));
		}
		return ResponseEntity.ok(new ApiResult(200, HttpStatus.OK, "좋아요한 상품 목록 조회 성공", productLikePage));
	}
}
