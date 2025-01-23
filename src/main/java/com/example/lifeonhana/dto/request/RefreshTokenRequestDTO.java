package com.example.lifeonhana.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequestDTO(
	@Schema(description = "리프레시 토큰")
	String refreshToken
) {}
