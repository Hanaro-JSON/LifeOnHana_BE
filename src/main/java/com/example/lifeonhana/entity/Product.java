package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter @Setter
@NoArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Category category;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	private String feature;

	private String target;

	@Column(nullable = false)
	private String link;

	@Column(precision = 15)
	private BigDecimal minAmount;

	@Column(precision = 15)
	private BigDecimal maxAmount;

	@Column(precision = 5, scale = 2)
	private BigDecimal basicInterestRate;

	@Column(precision = 5, scale = 2)
	private BigDecimal maxInterestRate;

	private Integer minPeriod;

	private Integer maxPeriod;

	private Long minCreditScore;

	@OneToMany(mappedBy = "product")
	private List<ArticleProduct> articleProducts = new ArrayList<>();

	@OneToMany(mappedBy = "product")
	private List<ProductLike> productLikes = new ArrayList<>();

	public enum Category {
		LOAN, SAVINGS, LIFE
	}
}
