package com.example.lifeonhana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ArticleDetailResponse;
import com.example.lifeonhana.service.ArticleService;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Article API", description = "기사 관련 API")
public class ArticleController {

	private final ArticleService articleService;

	@Operation(
		summary = "기사 상세 조회",
		description = "기사의 세부 정보를 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
		}
	)
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
}
