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
	private Category category;

	@Column(nullable = false, precision = 15)
	private BigDecimal amount;

	@Column(nullable = false, length = 100)
	private String description;

	@Column(nullable = false)
	private LocalDateTime historyDatetime;

	@Column(nullable = false)
	private Boolean isFixed = false;

	@Column(nullable = false)
	private Boolean isExpense;

	public enum Category {
		FOOD, SNACK, EDUCATION, HOBBY, HEALTH, FIXED_EXPENSE, TRAVEL, DEPOSIT, INTEREST, ETC
	}
}
