package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mydata")
@Getter @Setter
@NoArgsConstructor
public class Mydata {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mydataId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, precision = 15)
	private BigDecimal totalAsset;

	@Column(nullable = false, precision = 15)
	private BigDecimal depositAmount;

	@Column(nullable = false, precision = 15)
	private BigDecimal savingsAmount;

	@Column(nullable = false, precision = 15)
	private BigDecimal loanAmount;

	@Column(nullable = false, precision = 15)
	private BigDecimal stockAmount;

	@Column(nullable = false, precision = 15)
	private BigDecimal realEstateAmount;

	@Column(nullable = false)
	private LocalDateTime lastUpdatedAt;

	@Column(nullable = false)
	private Year pensionStartYear;

	@OneToMany(mappedBy = "mydata")
	private List<Account> accounts = new ArrayList<>();
}
