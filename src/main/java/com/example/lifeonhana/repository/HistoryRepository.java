package com.example.lifeonhana.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.lifeonhana.entity.History;
import com.example.lifeonhana.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

	@Query("SELECT h FROM History h " +
		"WHERE h.user = :user " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"ORDER BY h.historyDatetime DESC")
	Page<History> findAllByUserAndYearMonth(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate,
		Pageable pageable
	);

	// Page<History> findByUserAndHistoryDatetimeBetweenOrderByHistoryDatetimeDesc(
	// 	User user,
	// 	LocalDateTime startDate,
	// 	LocalDateTime endDate,
	// 	Pageable pageable
	// );

	@Query("SELECT COALESCE(SUM(h.amount), 0) FROM History h " +
		"WHERE h.user = :user " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"AND h.isExpense = false")
	BigDecimal calculateTotalIncome(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	// BigDecimal findSumOfAmountByUserAndHistoryDatetimeBetweenAndIsExpenseFalse(
	// 	User user,
	// 	LocalDateTime startDate,
	// 	LocalDateTime endDate
	// );

	@Query("SELECT COALESCE(SUM(h.amount), 0) FROM History h " +
		"WHERE h.user = :user " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"AND h.isExpense = true " +
		"AND h.category != 'INTEREST'")
	BigDecimal calculateTotalExpense(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT FUNCTION('DATE_FORMAT', h.historyDatetime, '%Y%m') as month, " +
		"CAST(SUM(h.amount) AS integer) as amount " +
		"FROM History h " +
		"WHERE h.user = :user " +
		"AND h.isExpense = true " +
		"AND h.category != 'INTEREST' " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"GROUP BY FUNCTION('DATE_FORMAT', h.historyDatetime, '%Y%m') " +
		"ORDER BY FUNCTION('DATE_FORMAT', h.historyDatetime, '%Y%m') DESC")
	List<Object[]> findMonthlyExpenses(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT CAST(COALESCE(AVG(monthly.total), 0) AS integer) FROM (" +
		"SELECT SUM(h.amount) as total " +
		"FROM History h " +
		"WHERE h.user = :user " +
		"AND h.isExpense = true " +
		"AND h.category != 'INTEREST' " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"GROUP BY FUNCTION('DATE_FORMAT', h.historyDatetime, '%Y-%m')) monthly")
	Integer calculateAverageMonthlyExpense(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT h.category as category, COALESCE(SUM(h.amount), 0) as amount " +
		"FROM History h " +
		"WHERE h.user = :user " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"AND h.isExpense = true " +
		"AND h.category != 'INTEREST' " +
		"GROUP BY h.category")
	List<CategoryAmountProjection> findExpenseStatsByCategory(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT COALESCE(SUM(h.amount), 0) " +
		"FROM History h " +
		"WHERE h.user = :user " +
		"AND h.historyDatetime BETWEEN :startDate AND :endDate " +
		"AND h.category = 'INTEREST' " +
		"AND h.isExpense = false")
	BigDecimal calculateTotalInterest(
		@Param("user") User user,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	// BigDecimal findSumOfAmountByUserAndHistoryDatetimeBetweenAndCategoryAndIsExpenseFalse(
	// 	User user,
	// 	LocalDateTime startDate,
	// 	LocalDateTime endDate,
	// 	History.Category category
	// );

	interface CategoryAmountProjection {
		History.Category getCategory();
		BigDecimal getAmount();
	}
}
