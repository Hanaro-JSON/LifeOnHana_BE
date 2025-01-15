package com.example.lifeonhana.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_product")
@Getter @Setter
@NoArgsConstructor
public class ArticleProduct {
	@EmbeddedId
	private ArticleProductId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("articleId")  // ArticleProductId의 articleId 필드와 매핑
	@JoinColumn(name = "article_id")
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("productId")  // ArticleProductId의 productId 필드와 매핑
	@JoinColumn(name = "product_id")
	private Product product;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleProductId implements Serializable {
		private Long articleId;  // Article의 ID 값만 저장
		private Long productId;  // Product의 ID 값만 저장
	}
}
