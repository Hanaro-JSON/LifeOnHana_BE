package com.example.lifeonhana.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ArticleDetailResponse;
import com.example.lifeonhana.dto.response.ArticleListResponse;
import com.example.lifeonhana.service.ArticleService;
import com.example.lifeonhana.service.JwtService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

	private final ArticleService articleService;
	private final JwtService jwtService;

	@GetMapping("/{articleId}")
	public ResponseEntity<ApiResult> getArticleDetails(@PathVariable Long articleId) {
		try {
			// Service에서 기사 상세 데이터 가져오기
			ArticleDetailResponse articleResponse = articleService.getArticleDetails(articleId);

			// 성공 응답 생성
			ApiResult result = ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message("기사 상세 조회 성공")
				.data(articleResponse)
				.build();

			return ResponseEntity.ok(result);

		} catch (IllegalArgumentException e) {
			// 잘못된 요청에 대한 응답
			ApiResult result = ApiResult.builder()
				.code(400)
				.status(HttpStatus.BAD_REQUEST)
				.message(e.getMessage())
				.build();

			return ResponseEntity.badRequest().body(result);

		} catch (Exception e) {
			// 내부 서버 오류에 대한 응답
			ApiResult result = ApiResult.builder()
				.code(500)
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.message("Unexpected error occurred")
				.build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}

	@GetMapping
	public ResponseEntity<ApiResult> getArticles(
		@RequestParam(value = "category", required = false) String category,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "20") int size,
		@Parameter(hidden = true)
		@AuthenticationPrincipal String authId
	) {
		try {
			ArticleListResponse response = articleService.getArticles(
				category, 
				page - 1, 
				size,
				authId
			);
			
			return ResponseEntity.ok(ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message("기사 목록 조회 성공")
				.data(response)
				.build());
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResult.builder()
					.code(500)
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.message("기사 목록 조회 중 오류가 발생했습니다.")
					.build());
		}
	}
}
