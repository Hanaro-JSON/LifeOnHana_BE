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

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("productId")
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private Boolean isLike;

	public ProductLike(Product product, User user, Boolean isLike) {
		this.id = new ProductLikeId(user.getUserId(), product.getProductId());
		this.product = product;
		this.user = user;
		this.isLike = isLike;
	}
}
