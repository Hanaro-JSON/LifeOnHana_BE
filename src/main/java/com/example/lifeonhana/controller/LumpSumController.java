package com.example.lifeonhana.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.request.LumpSumRequestDTO;
import com.example.lifeonhana.dto.response.LumpSumResponseDTO;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.service.LumpSumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/lumpsum")
@Tag(name = "Lump Sum API", description = "목돈 신청 API")
public class LumpSumController {
	private final LumpSumService lumpSumService;

	@PostMapping("")
	@Operation(summary = "목돈 신청", description = "목돈 신청을 처리합니다.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "목돈 인출 요청 정보",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LumpSumRequestDTO.class)
			)))
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "목돈 인출 신청 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = LumpSumResponseDTO.class),
				examples = @ExampleObject(
					value = """
                    {
                    	"code": 200,
                        "status": "OK",
                        "message": "목돈 인출 신청 성공",
                        "data": {
                            "lumpSumId": "1",
                            "balance": 10000,
                            "requestedAt": "2025-01-24T15:00:00"
                        }
                    }
                    """
				)
			)),
		@ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
	})
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResult<LumpSumResponseDTO>> createLumpSum(
		@AuthenticationPrincipal String authId,
		@RequestBody LumpSumRequestDTO requestDTO) {
		LumpSumResponseDTO responseDTO = lumpSumService.createLumpSum(authId, requestDTO);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResult.success(ErrorCode.LUMP_SUM_CREATED, responseDTO));
	}
}
