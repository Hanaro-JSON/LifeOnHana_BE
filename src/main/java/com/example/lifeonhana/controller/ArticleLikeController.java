package com.example.lifeonhana.controller;

import java.util.Map;

import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ArticleResponse;
import com.example.lifeonhana.dto.response.LikeResponseDto;
import com.example.lifeonhana.service.ArticleLikeService;
import com.example.lifeonhana.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name="ArticleLike API", description = "칼럼 좋아요 관련 api")
public class ArticleLikeController {
	private final JwtService jwtService;
	private final ArticleLikeService articleLikeService;

	@Operation(
			summary = "칼럼 좋아요 생성 및 취소",
			description = "칼럼 좋아요를 생성하거나 취소합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "요청 성공", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
			}
	)
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

	@Operation(
			summary = "칼럼 좋아요 수 및 여부 조회",
			description = "칼럼의 좋아요 수와 사용자의 좋아요 여부를 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
			}
	)
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

	@Operation(
			summary = "좋아요한 기사 목록 조회",
			description = "사용자가 좋아요한 기사 목록을 조회합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
					@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
			}
	)
	@GetMapping("/liked")
	public ResponseEntity<ApiResult> getLikedArticles(
			@RequestHeader("Authorization") String token,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@RequestParam(value = "category", required = false) String category) {

		token = token.substring(7);
		Long userId = jwtService.extractUserId(token);

		Slice<ArticleResponse> articlesSlice = articleLikeService.getLikedArticles(userId, page, size, category);

		return ResponseEntity.ok(ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message(articlesSlice.hasContent() ? "좋아요한 기사 목록 조회 성공" : "좋아요한 기사가 없습니다.")
				.data(Map.of(
						"articles", articlesSlice.getContent(),
						"hasNext", articlesSlice.hasNext()
				))
				.build());
	}
}