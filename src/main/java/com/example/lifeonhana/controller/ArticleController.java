package com.example.lifeonhana.controller;

import com.example.lifeonhana.dto.response.ArticleListItemResponse;
import com.example.lifeonhana.entity.Article;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Slice;

import java.util.Map;
import java.util.HashMap;

import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;

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
	public ResponseEntity<ApiResult<ArticleDetailResponse>> getArticleDetails(
			@PathVariable Long articleId, 
			@AuthenticationPrincipal String authId) {
		try {
			ArticleDetailResponse articleResponse = articleService.getArticleDetails(articleId, authId);
			return ResponseEntity.ok(ApiResult.<ArticleDetailResponse>builder()
					.code(String.valueOf(HttpStatus.OK.value()))
					.status(HttpStatus.OK)
					.message("기사 상세 조회 성공")
					.data(articleResponse)
					.build());
		} catch (BaseException e) {
			return createErrorResponse(ErrorCode.ARTICLE_NOT_FOUND);
		} catch (IllegalArgumentException e) {
			return createErrorResponse(ErrorCode.INVALID_REQUEST);
		} catch (Exception e) {
			return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(
		summary = "기사 목록 조회",
		description = "카테고리별 기사 목록을 페이징하여 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "기사 목록 조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 카테고리"),
		@ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@SecurityRequirement(name = "bearerAuth")
	@GetMapping
	public ResponseEntity<ApiResult<Map<String, Object>>> getArticles(
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@Parameter(hidden = true) @AuthenticationPrincipal String authId) {
		try {
			validateAuthentication(authId);
			
			if (category != null && !category.isEmpty()) {
				try {
					Article.Category.valueOf(category.toUpperCase());
				} catch (IllegalArgumentException e) {
					return createErrorResponse(ErrorCode.INVALID_CATEGORY);
				}
			}

			Slice<ArticleListItemResponse> response = articleService.getArticles(category, page - 1, size, authId);
			
			Map<String, Object> data = new HashMap<>();
			data.put("articles", response.getContent());
			data.put("hasNext", response.hasNext());

			return createSuccessResponse("기사 목록 조회 성공", data);
		} catch (BaseException e) {
			return createErrorResponse(ErrorCode.UNAUTHORIZED);
		} catch (Exception e) {
			return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
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
	public ResponseEntity<ApiResult<Map<String, Object>>> searchArticles(
			@Parameter(description = "검색 키워드")
			@RequestParam(name = "query", required = false) String query,
			@Parameter(description = "페이지 번호", example = "0")
			@RequestParam(defaultValue = "0") @Min(value = 0) int page,
			@Parameter(description = "페이지 크기", example = "20")
			@RequestParam(defaultValue = "20") @Min(value = 1) @Max(value = MAX_LIMIT) int size,
			@Parameter(hidden = true) @AuthenticationPrincipal String authId
	) {
		try {
			validateAuthentication(authId);
			
			if (query != null && (query.length() < 2 || query.length() > 100)) {
				return createErrorResponse(ErrorCode.VALIDATION_FAILED);
			}

			Slice<ArticleSearchResponseDTO> response = articleService.searchArticles(query, page, size, authId);
			
			Map<String, Object> data = new HashMap<>();
			data.put("articles", response.getContent());
			data.put("hasNext", response.hasNext());

			return createSuccessResponse("기사 검색 성공", data);
		} catch (BaseException e) {
			return createErrorResponse(ErrorCode.UNAUTHORIZED);
		} catch (Exception e) {
			return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private ResponseEntity<ApiResult<Map<String, Object>>> createSuccessResponse(String message, Map<String, Object> data) {
		return ResponseEntity.ok(ApiResult.<Map<String, Object>>builder()
				.code(String.valueOf(HttpStatus.OK.value()))
				.status(HttpStatus.OK)
				.message(message)
				.data(data)
				.build());
	}

	private <T> ResponseEntity<ApiResult<T>> createErrorResponse(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(ApiResult.<T>builder()
						.status(errorCode.getHttpStatus())
						.code(errorCode.getCode())
						.message(errorCode.getMessage())
						.build());
	}

	private void validateAuthentication(String authId) {
		if (authId == null || authId.isEmpty()) {
			throw new BaseException(ErrorCode.UNAUTHORIZED);
		}
	}
}
