package com.example.lifeonhana.controller;

import com.example.lifeonhana.dto.response.WhilickResponseDTO;
import com.example.lifeonhana.dto.response.WhilickContentDTO;
import com.example.lifeonhana.dto.response.WhilickTextDTO;
import com.example.lifeonhana.dto.response.PageableDTO;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.service.WhilickService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@Transactional
class WhilickControllerTest {

	@InjectMocks
	private WhilickController whilickController;

	@Mock
	private WhilickService whilickService;

	@Mock
	private JwtService jwtService;

	private static final String VALID_TOKEN = "Bearer valid-token";
	private static final String VALID_AUTH_ID = "test-auth-id";
	private WhilickResponseDTO mockResponse;

	@BeforeEach
	void setUp() {
		// Mock WhilickTextDTO 생성
		List<WhilickTextDTO> textList = List.of(
			new WhilickTextDTO(1L, "첫 번째 문단입니다.", 0.0f, 5.5f),
			new WhilickTextDTO(2L, "두 번째 문단입니다.", 5.5f, 10.0f)
		);

		// Mock WhilickContentDTO 생성
		List<WhilickContentDTO> contentList = List.of(
			WhilickContentDTO.builder()
				.articleId(1L)
				.title("테스트 아티클")
				.text(textList)
				.ttsUrl("https://example.com/tts/1.mp3")
				.totalDuration(10.0f)
				.likeCount(10)
				.isLiked(false)
				.build()
		);

		// Mock PageableDTO 생성
		PageableDTO pageableDTO = new PageableDTO(
			0,
			10,
			1,
			1L,
			true,
			true
		);

		// Mock WhilickResponseDTO 생성
		mockResponse = new WhilickResponseDTO(contentList, pageableDTO);
	}

	@Test
	@DisplayName("정상적인 쇼츠 조회 - articleId 없음")
	void getShorts_WithoutArticleId_Success() {
		// Given
		when(jwtService.extractAuthId("valid-token")).thenReturn(VALID_AUTH_ID);
		when(whilickService.getShorts(anyInt(), anyInt(), eq(VALID_AUTH_ID)))
			.thenReturn(mockResponse);

		// When
		ResponseEntity<?> response = whilickController.getShorts(
			null, 0, 10, VALID_TOKEN);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	@DisplayName("정상적인 쇼츠 조회 - articleId 있음")
	void getShorts_WithArticleId_Success() {
		// Given
		Long articleId = 1L;
		when(jwtService.extractAuthId("valid-token")).thenReturn(VALID_AUTH_ID);
		when(whilickService.getShortsByArticleId(anyLong(), anyInt(), eq(VALID_AUTH_ID)))
			.thenReturn(mockResponse);

		// When
		ResponseEntity<?> response = whilickController.getShorts(
			articleId, 0, 10, VALID_TOKEN);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	@DisplayName("인증되지 않은 사용자 쇼츠 조회 실패")
	void getShorts_Unauthorized() {
		// Given
		String invalidToken = "Bearer invalid-token";
		when(jwtService.extractAuthId("invalid-token"))
			.thenThrow(new UnauthorizedException("Unauthorized access"));

		// Then
		assertThatThrownBy(() ->
			whilickController.getShorts(null, 0, 10, invalidToken))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessageContaining("Unauthorized access");
	}

	@Test
	@DisplayName("존재하지 않는 articleId로 쇼츠 조회 실패")
	void getShorts_NotFound() {
		// Given
		Long nonExistentArticleId = 999L;
		when(jwtService.extractAuthId("valid-token")).thenReturn(VALID_AUTH_ID);
		when(whilickService.getShortsByArticleId(anyLong(), anyInt(), eq(VALID_AUTH_ID)))
			.thenThrow(new NotFoundException("Article not found"));

		// Then
		assertThatThrownBy(() ->
			whilickController.getShorts(nonExistentArticleId, 0, 10, VALID_TOKEN))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("Article not found");
	}
}
