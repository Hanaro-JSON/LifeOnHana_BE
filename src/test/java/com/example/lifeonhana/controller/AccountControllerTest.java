package com.example.lifeonhana.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.lifeonhana.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import com.example.lifeonhana.dto.request.AccountTransferRequest;
import com.example.lifeonhana.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.repository.AccountRepository;
import com.example.lifeonhana.repository.UserRepository;

@SpringBootTest(
    properties = "spring.profiles.active=test",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() {
        // 실제 테스트 데이터로 토큰 생성
        User testUser = userRepository.findById(3L)
            .orElseThrow(() -> new RuntimeException("테스트 사용자가 없습니다."));
        validToken = "Bearer " + jwtService.generateAccessToken("user1@example.com", 3L);
    }

    @Test
    @Order(1)
    @DisplayName("계좌 목록 조회 - 성공")
    void getAccounts_Success() throws Exception {
        Account account = accountRepository.findById(11L).orElseThrow();
        mockMvc.perform(get("/api/account")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data.mainAccount.accountId").value(11))
            .andExpect(jsonPath("$.data.mainAccount.accountNumber").value("11111111111111"))
            .andExpect(jsonPath("$.data.mainAccount.balance").value(account.getBalance().doubleValue()))
            .andDo(print());
    }

    @Test
    @Order(2)
    @DisplayName("계좌 이체 - 성공")
    void transfer_Success() throws Exception {
        BigDecimal transferAmount = new BigDecimal("1000");
        Account fromAccountBefore = accountRepository.findById(11L).orElseThrow();
        Account toAccountBefore = accountRepository.findById(12L).orElseThrow();

        BigDecimal fromAccountBe = new BigDecimal(fromAccountBefore.getBalance().doubleValue());
        BigDecimal toAccountBe = new BigDecimal(toAccountBefore.getBalance().doubleValue());
        AccountTransferRequest transferRequest = new AccountTransferRequest(
                11L, 12L, transferAmount
        );

        mockMvc.perform(post("/api/account/transfer")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value("1000"))
                .andDo(print());

        Account fromAccountAfter = accountRepository.findById(11L).orElseThrow();
        Account toAccountAfter = accountRepository.findById(12L).orElseThrow();

        System.out.println("From account after: " + fromAccountAfter.getBalance());
        System.out.println("To account after: " + toAccountAfter.getBalance());

        assertEquals(fromAccountBe.subtract(transferAmount).setScale(2, RoundingMode.HALF_UP), fromAccountAfter.getBalance().setScale(2, RoundingMode.HALF_UP));
        assertEquals(toAccountBe.add(transferAmount).setScale(2, RoundingMode.HALF_UP), toAccountAfter.getBalance().setScale(2, RoundingMode.HALF_UP));
    }


    @Test
    @DisplayName("급여 계좌 조회 - 성공")
    void getSalaryAccount_Success() throws Exception {
        Account account = accountRepository.findById(11L).orElseThrow();
        mockMvc.perform(get("/api/account/salary")
                .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data.accountId").value(11))
            .andExpect(jsonPath("$.data.balance").value(account.getBalance().doubleValue()));
    }

    @Test
    @DisplayName("계좌 목록 조회 - 유저 없음")
    void getAccounts_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/account")
                        .header("Authorization", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A003"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근")
    void unauthorized_Access() throws Exception {
        mockMvc.perform(get("/api/account")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A001"))
            .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.customMessage").value("인증이 필요합니다"));
    }

    @Test
    @DisplayName("계좌 이체 - 잔액 부족")
    void transfer_InsufficientBalance() throws Exception {
        AccountTransferRequest transferRequest = new AccountTransferRequest(
            14L, 12L, new BigDecimal("10000000")
        );

        mockMvc.perform(post("/api/account/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AC002"));
    }

    @Test
    @DisplayName("잘못된 계좌 이체 요청")
    void transfer_InvalidRequest() throws Exception {
        String invalidRequest = "{\"fromAccountId\": 1, \"invalidField\": \"value\"}";

        mockMvc.perform(post("/api/account/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("AC001"));
    }

    @Test
    @DisplayName("존재하지 않는 계좌로 이체")
    void transfer_AccountNotFound() throws Exception {
        AccountTransferRequest transferRequest = new AccountTransferRequest(
            11L, 999L, new BigDecimal("50000")
        );

        mockMvc.perform(post("/api/account/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("AC001"));
    }

    @Test
    @DisplayName("음수 금액 이체 시도")
    void transfer_NegativeAmount() throws Exception {
        AccountTransferRequest transferRequest = new AccountTransferRequest(
            11L, 12L, new BigDecimal("-50000")
        );

        mockMvc.perform(post("/api/account/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AC004"));
    }

    @Test
    @DisplayName("동일 계좌 이체 시도")
    void transfer_SameAccount() throws Exception {
        AccountTransferRequest transferRequest = new AccountTransferRequest(
            11L, 11L, new BigDecimal("50000")
        );

        mockMvc.perform(post("/api/account/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AC005"));
    }

    @Test
    @DisplayName("잘못된 토큰 형식")
    void getAccounts_InvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/account")
                .header("Authorization", "InvalidToken"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A003"));
    }

    @Test
    @DisplayName("만료된 토큰")
    void getAccounts_ExpiredToken() throws Exception {
        String expiredToken = "Bearer expired.token.here";
        mockMvc.perform(get("/api/account")
                .header("Authorization", expiredToken))
            .andExpect(status().isUnauthorized());
    }
}