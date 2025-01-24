package com.example.lifeonhana.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.example.lifeonhana.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.example.lifeonhana.service.JwtService;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.UserRepository;

@SpringBootTest(
    properties = "spring.profiles.active=test",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JwtService jwtService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() {
        // 실제 테스트 데이터로 토큰 생성
        User testUser = userRepository.findById(3L)
            .orElseThrow(() -> new RuntimeException("테스트 사용자가 없습니다."));
        validToken = "Bearer " + jwtService.generateAccessToken(testUser.getAuthId(), testUser.getUserId());
    }

    @Test
    @Order(1)
    @DisplayName("기사 상세 조회 - 성공")
    @WithMockUser
    void getArticleDetails_Success() throws Exception {
        Long articleId = 11L;  // 실제 DB에 있는 게시글 ID

        mockMvc.perform(get("/api/articles/{id}", articleId)
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.articleId").value(articleId))
            .andExpect(jsonPath("$.data.title").value("주택시장 금리 상승! 버티는 게 답일까?"))
            .andExpect(jsonPath("$.data.category").value("REAL_ESTATE"));
    }

    @Test
    @Order(2)
    @DisplayName("기사 목록 조회 - 성공")
    @WithMockUser
    void getArticles_Success() throws Exception {
        mockMvc.perform(get("/api/articles")
                .param("category", "REAL_ESTATE")
                .param("page", "0")
                .param("size", "20")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("기사 목록 조회 성공"))
            .andExpect(jsonPath("$.data.articles[0].title").value("테스트 기사"))
            .andDo(print());
    }

    @Test
    @Order(3)
    @DisplayName("기사 검색 - 성공")
    @WithMockUser
    void searchArticles_Success() throws Exception {
        mockMvc.perform(get("/api/articles/search")
                .param("query", "테스트")
                .param("page", "0")
                .param("size", "20")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.articles[0].title").value("테스트 기사"))
            .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 기사 조회")
    @WithMockUser
    void getArticleDetails_NotFound() throws Exception {
        Long nonExistentArticleId = 999L;

        mockMvc.perform(get("/api/articles/{id}", nonExistentArticleId)
                .header("Authorization", validToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("잘못된 카테고리로 기사 목록 조회")
    @WithMockUser
    void getArticles_InvalidCategory() throws Exception {
        mockMvc.perform(get("/api/articles")
                .param("category", "INVALID_CATEGORY")
                .header("Authorization", validToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근")
    void unauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/articles/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("검색어 최소 길이 검증")
    @WithMockUser
    void searchQuery_MinLength() throws Exception {
        mockMvc.perform(get("/api/articles/search")
                .param("query", "a")
                .header("Authorization", validToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("검색어 최대 길이 검증")
    @WithMockUser
    void searchQuery_MaxLength() throws Exception {
        String longQuery = "a".repeat(101);
        mockMvc.perform(get("/api/articles/search")
                .param("query", longQuery)
                .header("Authorization", validToken))
            .andExpect(status().isBadRequest());
    }
} 