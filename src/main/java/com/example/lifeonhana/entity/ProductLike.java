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
	@MapsId("productId")  // This maps to a field we'll add in ProductLikeId
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("userId")    // This maps to a field we'll add in ProductLikeId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private Boolean isLike;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ProductLikeId implements Serializable {
		private Long userId;     // Changed from User entity to Long
		private Long productId;  // Changed from Product entity to Long
	}
}
