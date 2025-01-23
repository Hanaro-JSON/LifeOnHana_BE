package com.example.lifeonhana.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequestDTO(
	@Schema(description = "인증 ID")
	String authId,
	@Schema(description = "비밀번호")
	String password
) {}
