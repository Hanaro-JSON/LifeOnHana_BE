package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lump_sum")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

	@Column
	private String reasonDetail;

	@Column(nullable = false)
	private LocalDateTime requestDate;

	public enum Source {
		SALARY, OTHER, LOAN
	}

	public enum Reason {
		CHILDREN, MEDICAL, HOUSING, BUSINESS_INVESTMENT,
		VEHICLE_TRANSPORT, LEISURE, DEBT_REPAYMENT, OTHER
	}
}
