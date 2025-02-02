package com.example.lifeonhana.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.ProductResponseDTO;
import com.example.lifeonhana.service.ProductLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Liked API", description = "좋아요한 상품 목록 api")
public class ProductLikeController {
	private final ProductLikeService productLikeService;

	public ProductLikeController(ProductLikeService productLikeService) {
		this.productLikeService = productLikeService;
	}

	@GetMapping("/liked/products")
	@Operation(summary = "좋아요한 상품 목록 조회", description = "좋아요한 상품 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", ref = "#/components/responses/Success"),
		@ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult<ProductListResponseDTO<ProductResponseDTO>>> getProductLikes(
		@AuthenticationPrincipal String authId,
		@RequestParam(defaultValue = "0") int offset,
		@RequestParam(defaultValue = "10") int limit) 
	{
		ProductListResponseDTO<ProductResponseDTO> response = productLikeService.getProductLikes(authId, offset, limit);
		return ResponseEntity.ok(ApiResult.success(ErrorCode.LIKED_PRODUCT_LIST_SUCCESS, response));
	}

	@PostMapping("/{productId}/like")
	@Operation(summary = "상품 좋아요 토글", description = "상품 좋아요 상태를 변경합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", ref = "#/components/responses/Success"),
		@ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
	})
	public ResponseEntity<ApiResult<Map<String, Boolean>>> toggleLike(
		@PathVariable Long productId,
		@AuthenticationPrincipal String authId) 
	{
		boolean isLiked = productLikeService.toggleLike(productId, authId);
		return ResponseEntity.ok(
			ApiResult.success(
				isLiked ? ErrorCode.PRODUCT_LIKE_ADDED : ErrorCode.PRODUCT_LIKE_REMOVED,
				Map.of("isLiked", isLiked)
			)
		);
	}
}
