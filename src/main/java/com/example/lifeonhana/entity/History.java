package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
@Getter @Setter
@NoArgsConstructor
public class History {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long historyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private HistoryCategory category;

	@Column(nullable = false, precision = 15)
	private BigDecimal amount;

	@Column(nullable = false, length = 100)
	private String description;

	@Column(nullable = false)
	private LocalDateTime historyDatetime;

	@Column(nullable = false)
	private Boolean isFixed;

	@Column(nullable = false)
	private Boolean isExpense;

	public enum HistoryCategory {
		FOOD("식비"),
		COFFEE_SNACK("커피/간식"),
		EDUCATION("교육"),
		HOBBY_LEISURE("취미/여가"),
		HEALTH("건강"),
		FIXED_EXPENSE("고정지출"),
		TRAVEL("여행"),
		OTHER("기타"),
		DEPOSIT("입금"),
		INTEREST("이자");

		private final String value;

		HistoryCategory(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
