package com.example.lifeonhana.controller;

import com.example.lifeonhana.dto.request.AuthRequestDTO;
import com.example.lifeonhana.dto.response.AuthResponseDTO;
import com.example.lifeonhana.dto.request.RefreshTokenRequestDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RedisService redisService;

	@Autowired
	private ObjectMapper objectMapper;

	private User testUser;
	private static final String TEST_AUTH_ID = "testuser@example.com";
	private static final String TEST_PASSWORD = "password123";
	private static final String TEST_NAME = "Test User";
	private static final String TEST_BIRTHDAY = "1990-01-01";

	@BeforeAll
	void setUp() {
		// Create test user
		testUser = new User();
		testUser.setAuthId(TEST_AUTH_ID);
		testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
		testUser.setName(TEST_NAME);
		testUser.setBirthday(TEST_BIRTHDAY);
		testUser.setIsFirst(true);
		testUser = userRepository.save(testUser);
	}

	@AfterAll
	void tearDown() {
		// Clean up test data
		userRepository.delete(testUser);
	}

	@BeforeEach
	void resetUser() {
		testUser.setIsFirst(true);
		userRepository.save(testUser);
	}

	@Test
	@DisplayName("첫 로그인 성공 테스트 - isFirst true 반환")
	void firstSignInSuccess() throws Exception {
		// Prepare request
		AuthRequestDTO request = new AuthRequestDTO(TEST_AUTH_ID, TEST_PASSWORD);

		// Perform first sign in request
		MvcResult result = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.data.userId").value(testUser.getUserId().toString()))
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andExpect(jsonPath("$.data.refreshToken").exists())
			.andExpect(jsonPath("$.data.isFirst").value(true))  // 첫 로그인이므로 true
			.andReturn();

		// Extract tokens for further tests
		AuthResponseDTO response = objectMapper.readValue(
			objectMapper.readTree(result.getResponse().getContentAsString())
				.get("data")
				.toString(),
			AuthResponseDTO.class
		);
		assertNotNull(response.accessToken());
		assertNotNull(response.refreshToken());
		assertTrue(response.isFirst());
	}

	@Test
	@DisplayName("두 번째 로그인 성공 테스트 - isFirst false 반환")
	void secondSignInSuccess() throws Exception {

		AuthRequestDTO firstRequest = new AuthRequestDTO(TEST_AUTH_ID, TEST_PASSWORD);

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstRequest)))
			.andExpect(status().isOk());

		// Second sign in should return isFirst as false
		MvcResult secondResult = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.data.userId").value(testUser.getUserId().toString()))
			.andExpect(jsonPath("$.data.isFirst").value(false))  // 두 번째 로그인이므로 false
			.andReturn();

		AuthResponseDTO response = objectMapper.readValue(
			objectMapper.readTree(secondResult.getResponse().getContentAsString())
				.get("data")
				.toString(),
			AuthResponseDTO.class
		);
		assertFalse(response.isFirst());
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호")
	void signInFailureWrongPassword() throws Exception {

		AuthRequestDTO request = new AuthRequestDTO(TEST_AUTH_ID, "wrongpassword");

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("잘못된 비밀번호입니다."));
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 사용자")
	void signInFailureUserNotFound() throws Exception {
		AuthRequestDTO request = new AuthRequestDTO("nonexistent@example.com", TEST_PASSWORD);

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Not Found.\n사용자를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("토큰 갱신 테스트")
	void refreshTokenTest() throws Exception {

		AuthRequestDTO signInRequest = new AuthRequestDTO(TEST_AUTH_ID, TEST_PASSWORD);

		MvcResult signInResult = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signInRequest)))
			.andExpect(status().isOk())
			.andReturn();

		AuthResponseDTO signInResponse = objectMapper.readValue(
			objectMapper.readTree(signInResult.getResponse().getContentAsString())
				.get("data")
				.toString(),
			AuthResponseDTO.class
		);

		// Then, try to refresh token
		RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(signInResponse.refreshToken());

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(refreshRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("토큰 갱신 성공"))
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andExpect(jsonPath("$.data.refreshToken").exists());
	}
	@Test
	@DisplayName("로그아웃 성공 테스트")
	void signOutSuccess() throws Exception {

		AuthRequestDTO signInRequest = new AuthRequestDTO(TEST_AUTH_ID, TEST_PASSWORD);

		MvcResult signInResult = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signInRequest)))
			.andExpect(status().isOk())
			.andReturn();

		AuthResponseDTO signInResponse = objectMapper.readValue(
			objectMapper.readTree(signInResult.getResponse().getContentAsString())
				.get("data")
				.toString(),
			AuthResponseDTO.class
		);

		// Then, try to sign out
		mockMvc.perform(post("/api/auth/signout")
				.header("Authorization", "Bearer " + signInResponse.accessToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("로그아웃 성공"));

		// Verify token is blacklisted
		assertTrue(redisService.isBlacklisted(signInResponse.accessToken()));
	}

	@Test
	@DisplayName("로그아웃 실패 - 유효하지 않은 토큰")
	void signOutFailureInvalidToken() throws Exception {
		mockMvc.perform(post("/api/auth/signout")
				.header("Authorization", "Bearer invalid_token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.message").value("Invalid token"));
	}

	@Test
	@DisplayName("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
	void refreshTokenWithInvalidToken() throws Exception {
		RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO("invalid.refresh.token");

		mockMvc.perform(post("/api/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(refreshRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(401))
			.andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
	}
}
