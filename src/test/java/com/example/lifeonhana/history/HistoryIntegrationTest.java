// package com.example.lifeonhana.history;
//
// import com.example.lifeonhana.entity.History;
// import com.example.lifeonhana.entity.User;
// import com.example.lifeonhana.entity.Wallet;
// import com.example.lifeonhana.repository.HistoryRepository;
// import com.example.lifeonhana.repository.UserRepository;
// import com.example.lifeonhana.repository.WalletRepository;
// import com.example.lifeonhana.service.JwtService;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional;
//
// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.time.YearMonth;
// import java.time.format.DateTimeFormatter;
//
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// @SpringBootTest
// @AutoConfigureMockMvc
// @Transactional
// class HistoryIntegrationTest {
//
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	@Autowired
// 	private HistoryRepository historyRepository;
//
// 	@Autowired
// 	private WalletRepository walletRepository;
//
// 	@Autowired
// 	private JwtService jwtService;
//
// 	private User testUser;
// 	private String authToken;
// 	private String currentYearMonth;
//
// 	@BeforeEach
// 	void setUp() {
// 		// Create test user
// 		testUser = new User();
// 		testUser.setAuthId("test@example.com");
// 		testUser.setName("Test User");
// 		testUser.setBirthday("19900101");
// 		testUser.setIsFirst(true);
// 		testUser = userRepository.save(testUser);
//
// 		// Create wallet
// 		Wallet wallet = new Wallet();
// 		wallet.setUser(testUser);
// 		wallet.setWalletAmount(1000000L);
// 		wallet.setPaymentDay(Wallet.PaymentDay.DAY_1);
// 		wallet.setStartDate(LocalDateTime.now().withDayOfMonth(1));
// 		wallet.setEndDate(LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusSeconds(1));
// 		walletRepository.save(wallet);
//
// 		// Generate JWT token
// 		authToken = "Bearer " + jwtService.generateAccessToken(testUser.getAuthId(), testUser.getUserId());
//
// 		// Set current year-month
// 		currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
//
// 		// Create test transaction history
// 		createTestHistories();
// 	}
//
// 	private void createTestHistories() {
// 		LocalDateTime now = LocalDateTime.now();
//
// 		// Current month expenses
// 		createHistory(History.Category.FOOD, new BigDecimal("50000"), "Lunch", now, true);
// 		createHistory(History.Category.HOBBY, new BigDecimal("100000"), "Movie tickets", now, true);
//
// 		// Current month income
// 		createHistory(History.Category.DEPOSIT, new BigDecimal("3000000"), "Salary", now, false);
// 		createHistory(History.Category.INTEREST, new BigDecimal("5000"), "Savings interest", now, false);
//
// 		// Previous month data
// 		LocalDateTime lastMonth = now.minusMonths(1);
// 		createHistory(History.Category.FOOD, new BigDecimal("45000"), "Dinner", lastMonth, true);
// 	}
//
// 	private void createHistory(History.Category category, BigDecimal amount,
// 		String description, LocalDateTime datetime, boolean isExpense) {
// 		History history = new History();
// 		history.setUser(testUser);
// 		history.setCategory(category);
// 		history.setAmount(amount);
// 		history.setDescription(description);
// 		history.setHistoryDatetime(datetime);
// 		history.setIsExpense(isExpense);
// 		history.setIsFixed(false);
// 		historyRepository.save(history);
// 	}
//
// 	@Test
// 	@DisplayName("Get Monthly Transaction History - Success")
// 	void getHistories_Success() throws Exception {
// 		mockMvc.perform(get("/api/history")
// 				.header("Authorization", authToken)
// 				.param("yearMonth", currentYearMonth)
// 				.param("page", "1")
// 				.param("size", "20"))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.code").value(200))
// 			.andExpect(jsonPath("$.status").value("OK"))
// 			.andExpect(jsonPath("$.message").value("거래 내역 조회 성공"))
// 			.andExpect(jsonPath("$.data.yearMonth").value(currentYearMonth))
// 			.andExpect(jsonPath("$.data.totalIncome").value(3005000))
// 			.andExpect(jsonPath("$.data.totalExpense").value(150000))
// 			.andExpect(jsonPath("$.data.histories").isArray())
// 			.andExpect(jsonPath("$.data.histories.length()").value(4));
// 	}
//
// 	@Test
// 	@DisplayName("Get Monthly Expenses - Success")
// 	void getMonthlyExpenses_Success() throws Exception {
// 		mockMvc.perform(get("/api/history/monthly")
// 				.header("Authorization", authToken))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.code").value(200))
// 			.andExpect(jsonPath("$.status").value("OK"))
// 			.andExpect(jsonPath("$.message").value("월별 지출 내역 조회 성공"))
// 			.andExpect(jsonPath("$.data.averageExpense").exists())
// 			.andExpect(jsonPath("$.data.currentBalance").value(1000000))
// 			.andExpect(jsonPath("$.data.monthlyExpenses").isArray())
// 			.andExpect(jsonPath("$.data.monthlyExpenses[0].month").value(currentYearMonth))
// 			.andExpect(jsonPath("$.data.monthlyExpenses[0].totalExpense").value(150000));
// 	}
//
// 	@Test
// 	@DisplayName("Get Transaction Statistics - Success")
// 	void getStatistics_Success() throws Exception {
// 		mockMvc.perform(get("/api/history/statistics")
// 				.header("Authorization", authToken)
// 				.param("yearMonth", currentYearMonth))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.code").value(200))
// 			.andExpect(jsonPath("$.status").value("OK"))
// 			.andExpect(jsonPath("$.message").value("거래 통계 조회 성공"))
// 			.andExpect(jsonPath("$.data.yearMonth").value(currentYearMonth))
// 			.andExpect(jsonPath("$.data.totalExpense").value(150000))
// 			.andExpect(jsonPath("$.data.totalInterest").value(5000))
// 			.andExpect(jsonPath("$.data.expenseCategories").isArray());
// 	}
//
// 	@Test
// 	@DisplayName("Get Monthly Transaction History - Invalid Year Month Format")
// 	void getHistories_InvalidYearMonth() throws Exception {
// 		mockMvc.perform(get("/api/history")
// 				.header("Authorization", authToken)
// 				.param("yearMonth", "invalid-format")
// 				.param("page", "1")
// 				.param("size", "20"))
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.message").value("Bad Request.\n올바른 년월 형식이 아닙니다. (YYYYMM)"));
// 	}
//
// 	// @Test
// 	// @DisplayName("Get Monthly Transaction History - Invalid Authorization")
// 	// void getHistories_InvalidAuth() throws Exception {
// 	// 	mockMvc.perform(get("/api/history")
// 	// 			.header("Authorization", "Bearer invalid-token")
// 	// 			.param("yearMonth", currentYearMonth)
// 	// 			.param("page", "1")
// 	// 			.param("size", "20"))
// 	// 		.andExpect(status().isUnauthorized());
// 	// }
// 	//
// 	// @Test
// 	// @DisplayName("Get Monthly Transaction History - Missing Authorization")
// 	// void getHistories_MissingAuth() throws Exception {
// 	// 	mockMvc.perform(get("/api/history")
// 	// 			.param("yearMonth", currentYearMonth)
// 	// 			.param("page", "1")
// 	// 			.param("size", "20"))
// 	// 		.andExpect(status().isUnauthorized());
// 	// }
//
// 	@Test
// 	@DisplayName("Get Statistics - Future Month")
// 	void getStatistics_FutureMonth() throws Exception {
// 		String futureYearMonth = YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
//
// 		mockMvc.perform(get("/api/history/statistics")
// 				.header("Authorization", authToken)
// 				.param("yearMonth", futureYearMonth))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.data.totalExpense").value(0))
// 			.andExpect(jsonPath("$.data.totalInterest").value(0));
// 	}
// }
