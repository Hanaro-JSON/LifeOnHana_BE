package com.example.lifeonhana.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_like")
@Getter @Setter
@NoArgsConstructor
public class ProductLike {
	@EmbeddedId
	private ProductLikeId id;

	@Column(nullable = false)
	private Boolean isLike;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ProductLikeId implements Serializable {
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "user_id")
		private User user;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "product_id")
		private Product product;
	}
}
