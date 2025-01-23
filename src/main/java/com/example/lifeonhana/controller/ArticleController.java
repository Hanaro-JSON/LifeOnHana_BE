package com.example.lifeonhana.controller;

import com.example.lifeonhana.dto.response.ArticleListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import com.example.lifeonhana.service.JwtService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.lifeonhana.dto.response.ArticleSearchResponseDTO;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Slice;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Article API", description = "기사 관련 API")
public class ArticleController {

	private final ArticleService articleService;
	private final JwtService jwtService;
	private static final int MAX_LIMIT = 200;

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
	public ResponseEntity<ApiResult> getArticleDetails(@PathVariable Long articleId ,@AuthenticationPrincipal String authId) {
		try {
			// Service에서 기사 상세 데이터 가져오기
			ArticleDetailResponse articleResponse = articleService.getArticleDetails(articleId,authId);

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
			Slice<ArticleListItemResponse> response = articleService.getArticles(category, page - 1, size, authId);
			
			Map<String, Object> data = new HashMap<>();
			data.put("articles", response.getContent());
			data.put("hasNext", response.hasNext());

			return ResponseEntity.ok(ApiResult.builder()
				.code(200)
				.status(HttpStatus.OK)
				.message("기사 목록 조회 성공")
				.data(data)
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

	@Operation(summary = "기사 검색", description = "키워드로 기사를 검색합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기사 검색 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
	})
	@GetMapping("/search")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult> searchArticles(
			@Parameter(description = "검색 키워드")
			@RequestParam(name = "query", required = false) String query,
			@Parameter(description = "페이지 번호", example = "0")
			@RequestParam(defaultValue = "0") @Min(value = 0) int page,
			@Parameter(description = "페이지 크기", example = "20")
			@RequestParam(defaultValue = "20") @Min(value = 1) @Max(value = MAX_LIMIT) int size,
			@Parameter(hidden = true) @AuthenticationPrincipal String authId
	) {
		validateAuthentication(authId);
		Slice<ArticleSearchResponseDTO> response = articleService.searchArticles(query, page, size, authId);
		
		Map<String, Object> data = new HashMap<>();
		data.put("articles", response.getContent());
		data.put("hasNext", response.hasNext());

		return createSuccessResponse("기사 검색 성공", data);
	}

	private void validateAuthentication(String authId) {
		if (authId == null || authId.isEmpty()) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
	}

	private ResponseEntity<ApiResult> createSuccessResponse(String message, Object data) {
		return ResponseEntity.ok(ApiResult.builder()
				.code(HttpStatus.OK.value())
				.status(HttpStatus.OK)
				.message(message)
				.data(data)
				.build());
	}
}
