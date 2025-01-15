package com.example.lifeonhana.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_like")
@Getter @Setter
@NoArgsConstructor
public class ArticleLike {
	@EmbeddedId
	private ArticleLikeId id;

	@Column(nullable = false)
	private Boolean isLike;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleLikeId implements Serializable {
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "user_id")
		private User user;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "article_id")
		private Article article;
	}
}
