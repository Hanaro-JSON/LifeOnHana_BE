package com.example.lifeonhana.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponseDTO(
	@Schema(description = "액세스 토큰")
	String accessToken,
	@Schema(description = "리프레시 토큰")
	String refreshToken,
	@Schema(description = "사용자 ID")
	String userId,
	@Schema(description = "최초 로그인 여부")
	Boolean isFirst
) {}
