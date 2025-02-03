package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

import com.example.lifeonhana.global.exception.InsufficientBalanceException;

@Entity
@Table(name = "account")
@Getter @Setter
@NoArgsConstructor
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mydata_id", nullable = false)
	private Mydata mydata;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Bank bank;

	@Column(nullable = false)
	private String accountNumber;

	@Column(nullable = false)
	private String accountName;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ServiceAccount serviceAccount;

	public enum Bank {
		HANA, SHINHAN, NH, TOSS, KB, IBK, KAKAO, NAVER, WOORI
	}

	public enum ServiceAccount {
		SALARY, WALLET, OTHER
	}

	public void withdraw(BigDecimal amount) {
		this.balance = this.balance.subtract(amount);
	}

	public void deposit(BigDecimal amount) {
		this.balance = this.balance.add(amount);
	}
}
