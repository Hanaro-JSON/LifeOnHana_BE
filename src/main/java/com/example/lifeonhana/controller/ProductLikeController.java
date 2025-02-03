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
	ProductLikeService productLikeService;

	public ProductLikeController(ProductLikeService productLikeService) {
		this.productLikeService = productLikeService;
	}

	@GetMapping("/liked/products")
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

	@Operation(
		summary = "상품 좋아요 생성 및 취소",
		description = "상품에 좋아요를 생성하거나 취소합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(mediaType = "application/json"))
		}
	)
	@PostMapping("/{productId}/like")
	public ResponseEntity<ApiResult> toggleLike(
		@PathVariable Long productId,
		@AuthenticationPrincipal String authId
	) {
		boolean isLiked = productLikeService.toggleLike(productId, authId);

		String message = isLiked ? "좋아요 성공" : "좋아요 취소 성공";

		return ResponseEntity.ok(new ApiResult(
			200,
			HttpStatus.OK,
			message,
			Map.of("isLiked", isLiked)
		));
	}
}
