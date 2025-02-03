package com.example.lifeonhana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.WhilickResponseDTO;
import com.example.lifeonhana.service.WhilickService;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@Tag(name = "Whilick", description = "칼럼 쇼츠 관련 API")
public class WhilickController {
	private final WhilickService whilickService;

	@Operation(summary = "칼럼 쇼츠 조회", description = "페이지네이션을 적용한 칼럼 쇼츠 목록 조회")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "404", description = "컨텐츠를 찾을 수 없음")
	})
	@GetMapping({"/shorts", "/shorts/{articleId}"})
	public ResponseEntity<ApiResult<WhilickResponseDTO>> getShorts(
		@PathVariable(required = false) Long articleId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestHeader("Authorization") String token
	) {
		validatePaginationParams(page, size);
		
		WhilickResponseDTO response = (articleId != null)
			? whilickService.getShortsByArticleId(articleId, size, token)
			: whilickService.getShorts(page, size, token);

		return ResponseEntity.ok(
			ApiResult.success(ErrorCode.SHORTS_READ_SUCCESS, response)
		);
	}

	private void validatePaginationParams(int page, int size) {
		if (page < 0 || size <= 0 || size > 100) {
			throw new BaseException(ErrorCode.INVALID_PAGINATION_PARAMS);
		}
	}
}
