package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary")
@Getter @Setter
@NoArgsConstructor
public class Salary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long salaryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Long salaryAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentDay paymentDay;

	@Column(nullable = false)
	private LocalDateTime startDate;

	@Column(nullable = false)
	private LocalDateTime endDate;

	public enum PaymentDay {
		DAY_1("1"),
		DAY_15("15");

		private final String value;

		PaymentDay(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
