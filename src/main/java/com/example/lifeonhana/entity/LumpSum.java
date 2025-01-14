package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lump_sum")
@Getter @Setter
@NoArgsConstructor
public class LumpSum {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long lumpSumId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, precision = 15)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Source source;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Reason reason;

	private String reasonDetail;

	@Column(nullable = false)
	private LocalDateTime requestDate;

	public enum Source {
		SALARY,
		INVESTMENT,
		INHERITANCE,
		OTHER
	}

	public enum Reason {
		INVESTMENT,
		SAVINGS,
		EXPENDITURE,
		OTHER
	}
}
