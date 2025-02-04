package com.example.lifeonhana.controller;

import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.ProductRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

@SpringBootTest(
	properties = "spring.profiles.active=test",
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
class ProductInsightControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	private String validToken;

	@BeforeEach
	void setUp() {
		User testUser = userRepository.findById(3L)
			.orElseThrow(() -> new RuntimeException("테스트 사용자가 없습니다."));
		validToken = "Bearer " + jwtService.generateAccessToken(testUser.getAuthId(), testUser.getUserId());
		System.out.println("Generated authToken: " + validToken);
	}

	@Test
	@DisplayName("상품 기대효과 분석 조회 - 성공")
	void getProductInsight_Success() throws Exception {
		Article article = articleRepository.findById(11L)
			.orElseThrow(() -> new RuntimeException("테스트 기사가 없습니다."));
		Product product = productRepository.findById(5L)
			.orElseThrow(() -> new RuntimeException("테스트 상품이 없습니다."));

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> requestData = Map.of(
			"articleId", article.getArticleId(),
			"productId", product.getProductId()
		);

		mockMvc.perform(post("/api/anthropic/effect")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestData))
				.header("Authorization", validToken))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("상품 분석 성공"))
			.andExpect(jsonPath("$.data.analysisResult").exists())
			.andExpect(jsonPath("$.data.productLink").exists())
			.andExpect(jsonPath("$.data.productName").exists())
			.andExpect(jsonPath("$.data.isLiked").exists());
	}

	@Test
	@DisplayName("상품 기대효과 분석 조회 - 잘못된 요청")
	void getProductInsight_BadRequest() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> invalidRequestData = Map.of(
			"articleId", 99999,
			"productId", 99999
		);

		mockMvc.perform(post("/api/anthropic/effect")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequestData))
				.header("Authorization", validToken))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(400))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("상품 기대효과 분석 조회 - 권한 없음")
	void getProductInsight_Unauthorized() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> requestData = Map.of(
			"articleId", 11,
			"productId", 5
		);

		mockMvc.perform(post("/api/anthropic/effect")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestData)))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(401))
			.andExpect(jsonPath("$.message").exists());
	}
}
