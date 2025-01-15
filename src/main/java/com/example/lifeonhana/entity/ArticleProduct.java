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

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleProductId implements Serializable {
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "article_id")
		private Article article;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "product_id")
		private Product product;
	}
}
