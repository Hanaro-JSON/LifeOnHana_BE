package com.example.lifeonhana.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
	private String accessToken;
	private String refreshToken;
	private String userId;
	private Boolean isFirst;
}
