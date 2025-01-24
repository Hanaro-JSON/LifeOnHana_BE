package com.example.lifeonhana.controller;

import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.ProductRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.service.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    properties = "spring.profiles.active=test",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
class ProductLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private String validToken;

    @BeforeEach
    void setUp() {
        // 실제 테스트 데이터로 토큰 생성
        User testUser = userRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException("테스트 사용자가 없습니다."));
        validToken = "Bearer " + jwtService.generateAccessToken(testUser.getAuthId(), testUser.getUserId());
    }

    @Test
    @Order(1)
    @DisplayName("좋아요한 상품 목록 조회 - 성공")
    @WithMockUser
    void getProductLikes_Success() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .header("Authorization", validToken)
                .param("offset", "0")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("좋아요한 상품 목록 조회 성공"))
            .andExpect(jsonPath("$.data.products").exists())
            .andDo(print());
    }

    @Test
    @Order(2)
    @DisplayName("상품 좋아요 토글 - 좋아요 생성")
    @WithMockUser
    void toggleLike_Create() throws Exception {
        Long productId = productRepository.findAll().get(0).getProductId();
        
        mockMvc.perform(post("/api/users/" + productId + "/like")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("좋아요 성공"))
            .andExpect(jsonPath("$.data.isLiked").value(true))
            .andDo(print());
    }

    @Test
    @Order(3)
    @DisplayName("상품 좋아요 토글 - 좋아요 취소")
    @WithMockUser
    void toggleLike_Cancel() throws Exception {
        Long productId = productRepository.findAll().get(0).getProductId();
        
        // 먼저 좋아요를 생성
        mockMvc.perform(post("/api/users/" + productId + "/like")
                .header("Authorization", validToken));
                
        // 다시 요청하여 좋아요 취소
        mockMvc.perform(post("/api/users/" + productId + "/like")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("좋아요 취소 성공"))
            .andExpect(jsonPath("$.data.isLiked").value(false))
            .andDo(print());
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 - 인증되지 않은 사용자")
    void getProductLikes_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .param("offset", "0")
                .param("limit", "10"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 - 잘못된 페이지 파라미터")
    @WithMockUser
    void getProductLikes_InvalidParameters() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .header("Authorization", validToken)
                .param("offset", "-1")
                .param("limit", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 좋아요 토글 - 존재하지 않는 상품")
    @WithMockUser
    void toggleLike_ProductNotFound() throws Exception {
        mockMvc.perform(post("/api/users/999/like")
                .header("Authorization", validToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("상품 좋아요 토글 - 인증되지 않은 사용자")
    void toggleLike_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/users/1/like"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("상품 좋아요 토글 - 잘못된 상품 ID 형식")
    @WithMockUser
    void toggleLike_InvalidProductId() throws Exception {
        mockMvc.perform(post("/api/users/invalid/like")
                .header("Authorization", validToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 - 페이지네이션 테스트")
    @WithMockUser
    void getProductLikes_Pagination() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .header("Authorization", validToken)
                .param("offset", "0")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.hasNext").exists())
            .andDo(print());
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 - 최대 제한 초과 요청")
    @WithMockUser
    void getProductLikes_ExceedMaxLimit() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .header("Authorization", validToken)
                .param("offset", "0")
                .param("limit", "1001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("페이지 크기가 최대 제한을 초과했습니다."));
    }

    @Test
    @DisplayName("좋아요한 상품 목록 조회 - 정렬 파라미터 테스트")
    @WithMockUser
    void getProductLikes_WithSorting() throws Exception {
        mockMvc.perform(get("/api/users/liked/products")
                .header("Authorization", validToken)
                .param("offset", "0")
                .param("limit", "10")
                .param("sort", "interestRate,desc"))
            .andExpect(status().isOk());
    }
}