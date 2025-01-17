package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.LikeResponseDto;
import com.example.lifeonhana.service.ArticleLikeService;
import com.example.lifeonhana.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name="ArticleLike API", description = "칼럼 좋아요 관련 api")
public class ArticleLikeController {
	private final JwtService jwtService;
	private final ArticleLikeService articleLikeService;

	@Operation(summary = "칼럼 좋아요 생성 && 취소", description = "칼럼 좋아요 생성 && 취소")
	@PostMapping("/{articleId}/like")
	public ResponseEntity<ApiResult> toggleLike(
		@PathVariable Long articleId,
		@RequestHeader("Authorization") String token) {

		token = token.substring(7);
		Long userId = jwtService.extractUserId(token);

		LikeResponseDto responseDto = articleLikeService.toggleLike(userId, articleId);

		return ResponseEntity.ok(
			ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message(responseDto.isLiked() ? "좋아요 성공" : "좋아요 취소 성공")
				.data(responseDto)
				.build()
		);
	}

	@Operation(summary = "칼럼 좋아요 수 && 여부 조회", description = "칼럼 좋아요 수 && 여부 조회")
	@GetMapping("/{articleId}/like")
	public ResponseEntity<ApiResult> getLikeInfo(
		@PathVariable Long articleId,
		@RequestHeader("Authorization") String token) {

		token = token.substring(7);
		Long userId = jwtService.extractUserId(token);

		LikeResponseDto responseDto = articleLikeService.getLikeInfo(userId, articleId);

		return ResponseEntity.ok(
			ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message("게시글 좋아요 정보 조회 성공")
				.data(responseDto)
				.build()
		);
	}
}
