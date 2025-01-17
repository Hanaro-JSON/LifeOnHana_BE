package com.example.lifeonhana.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.lifeonhana.entity.History.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class HistoryDTO {

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class HistoryResponseDTO {
		@JsonProperty("yearMonth")
		private String yearMonth;

		@JsonProperty("totalIncome")
		private BigDecimal totalIncome;

		@JsonProperty("totalExpense")
		private BigDecimal totalExpense;

		@JsonProperty("histories")
		private List<HistoryDetailDTO> histories;

		@JsonProperty("page")
		private int page;

		@JsonProperty("size")
		private int size;

		@JsonProperty("totalPages")
		private int totalPages;

		@JsonProperty("totalElements")
		private long totalElements;

		@Getter
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		public static class	HistoryDetailDTO {
			@JsonProperty("historyId")
			private Long historyId;

			@JsonProperty("category")
			private Category category;

			@JsonProperty("amount")
			private BigDecimal amount;

			@JsonProperty("description")
			private String description;

			@JsonProperty("historyDateTime")
			private LocalDateTime historyDateTime;

			@JsonProperty("isFixed")
			private Boolean isFixed;

			@JsonProperty("isExpense")
			private Boolean isExpense;
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthlyExpenseResponseDTO {
		@JsonProperty("averageExpense")
		private Integer averageExpense;

		@JsonProperty("currentBalance")
		private Integer currentBalance;

		@JsonProperty("monthlyExpenses")
		private List<MonthlyExpenseDetailDTO> monthlyExpenses;

		@Getter
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		public static class MonthlyExpenseDetailDTO {
			@JsonProperty("month")
			private String month;

			@JsonProperty("totalExpense")
			private Integer totalExpense;
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StatisticsResponseDTO {
		@JsonProperty("yearMonth")
		private String yearMonth;

		@JsonProperty("totalExpense")
		private Integer totalExpense;

		@JsonProperty("totalInterest")
		private Integer totalInterest;

		@JsonProperty("expenseCategories")
		private List<CategoryStatDTO> expenseCategories;

		@Getter
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		public static class CategoryStatDTO {
			@JsonProperty("category")
			private Category category;

			@JsonProperty("amount")
			private BigDecimal amount;

			@JsonProperty("percentage")
			private Integer percentage;
		}
	}
}
