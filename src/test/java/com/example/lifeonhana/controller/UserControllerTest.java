package com.example.lifeonhana.controller;

import com.example.lifeonhana.config.SecurityConfig;
import com.example.lifeonhana.dto.response.UserResponseDTO;
import com.example.lifeonhana.dto.response.MyDataResponseDTO;
import com.example.lifeonhana.dto.response.UserNicknameResponseDTO;
import com.example.lifeonhana.filter.JwtAuthenticationFilter;
import com.example.lifeonhana.service.RedisService;
import com.example.lifeonhana.service.UserService;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({JwtAuthenticationFilter.class, SecurityConfig.class})
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private RedisService redisService;

	@Autowired
	private ObjectMapper objectMapper;

	private final String AUTH_ID = "test-auth-id";
	private UserResponseDTO userResponseDTO;
	private MyDataResponseDTO myDataResponseDTO;
	private UserNicknameResponseDTO userNicknameResponseDTO;

	@BeforeEach
	void setUp() {
		// UserResponseDTO 설정
		userResponseDTO = new UserResponseDTO(
			1L,
			"Test User",
			AUTH_ID,
			"1990-01-01",
			false
		);

		// MyDataResponseDTO 설정
		myDataResponseDTO = new MyDataResponseDTO(
			"2050",
			BigDecimal.valueOf(10000000),
			BigDecimal.valueOf(8000000),
			BigDecimal.valueOf(5000000),
			50,
			BigDecimal.valueOf(2000000),
			20,
			BigDecimal.valueOf(2000000),
			20,
			BigDecimal.valueOf(1000000),
			10,
			BigDecimal.ZERO,
			0,
			LocalDateTime.now(),
			new MyDataResponseDTO.SalaryAccountDTO(
				"123-456-789",
				BigDecimal.valueOf(1000000),
				"HANA"
			),
			BigDecimal.valueOf(500000)
		);

		// UserNicknameResponseDTO 설정
		userNicknameResponseDTO = UserNicknameResponseDTO.builder()
			.nickname("투자의 달인 Test User")
			.build();

		// JWT 검증 로직 모킹
		given(jwtService.extractAuthId(anyString())).willReturn(AUTH_ID);
		given(jwtService.isValidToken(anyString())).willReturn(true);
		given(redisService.isBlacklisted(anyString())).willReturn(false);
	}

	@Test
	@DisplayName("사용자 정보 조회 성공")
	void getUserInfo_Success() throws Exception {
		// given
		given(userService.getUserInfo(AUTH_ID)).willReturn(userResponseDTO);

		// when & then
		mockMvc.perform(get("/api/users/info")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("U200"))
			.andExpect(jsonPath("$.data.userId").value(1))
			.andExpect(jsonPath("$.data.name").value("Test User"));
	}

	// @Test
	// @DisplayName("사용자 정보 조회 실패 - 인증 없음")
	// void getUserInfo_Unauthorized() throws Exception {
	// 	mockMvc.perform(get("/api/users/info"))
	// 		.andExpect(status().isUnauthorized())
	// 		.andExpect(jsonPath("$.status").value(401))
	// 		.andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
	// }

	@Test
	@DisplayName("사용자 정보 조회 실패 - 사용자 없음")
	void getUserInfo_UserNotFound() throws Exception {
		// given
		given(userService.getUserInfo(AUTH_ID))
			.willThrow(new BaseException(ErrorCode.USER_NOT_FOUND, AUTH_ID));

		// when & then
		mockMvc.perform(get("/api/users/info")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("마이데이터 조회 성공 - 정상 응답 검증")
	void getMyData_Success() throws Exception {
		// given
		MyDataResponseDTO mockResponse = new MyDataResponseDTO(
			"2050",
			BigDecimal.valueOf(10000000),
			BigDecimal.valueOf(8000000),
			BigDecimal.valueOf(5000000),
			50,
			BigDecimal.valueOf(2000000),
			20,
			BigDecimal.valueOf(2000000),
			20,
			BigDecimal.valueOf(1000000),
			10,
			BigDecimal.ZERO,
			0,
			LocalDateTime.now(),
			new MyDataResponseDTO.SalaryAccountDTO("123-456-789", BigDecimal.valueOf(1000000), "HANA"),
			BigDecimal.valueOf(500000)
		);
		
		given(userService.getMyData(AUTH_ID)).willReturn(mockResponse);

		// when & then
		mockMvc.perform(get("/api/users/mydata")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(ErrorCode.MYDATA_INFO_SUCCESS.getCode()))
			.andExpect(jsonPath("$.data.pensionStart").value("2050"))
			.andExpect(jsonPath("$.data.totalAsset").value(10000000))
			.andExpect(jsonPath("$.data.salaryAccount.accountNumber").value("123-456-789"));
	}

	@Test
	@DisplayName("마이데이터 조회 실패 - 데이터 없음")
	void getMyData_NotFound() throws Exception {
		// given
		given(userService.getMyData(AUTH_ID))
			.willThrow(new BaseException(ErrorCode.MYDATA_NOT_FOUND, AUTH_ID));

		// when & then
		mockMvc.perform(get("/api/users/mydata")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$.code").value(ErrorCode.MYDATA_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("마이데이터 조회 - 내부 서버 오류")
	void getMyData_InternalServerError() throws Exception {
		given(userService.getMyData(AUTH_ID))
			.willThrow(new RuntimeException("DB Connection Error"));

		mockMvc.perform(get("/api/users/mydata")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
	}

	@Test
	@DisplayName("사용자 칭호 조회 성공")
	void getUserNickname_Success() throws Exception {
		// given
		given(userService.getUserNickname(AUTH_ID)).willReturn(userNicknameResponseDTO);

		// when & then
		mockMvc.perform(get("/api/users/nickname")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("N200"))
			.andExpect(jsonPath("$.data.nickname").value("투자의 달인 Test User"));
	}



	@Test
	@DisplayName("사용자 칭호 조회 실패 - 좋아요 기록 없음")
	void getUserNickname_NoLikes() throws Exception {
		// given
		given(userService.getUserNickname(AUTH_ID))
			.willThrow(new BaseException(ErrorCode.NO_LIKED_ARTICLES));

		// when & then
		mockMvc.perform(get("/api/users/nickname")
				.header("Authorization", "Bearer test-token")
				.principal(() -> AUTH_ID))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.NO_LIKED_ARTICLES.getCode()));
	}

}
