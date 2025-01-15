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

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("articleId")  // ArticleLikeId의 articleId 필드와 매핑
	@JoinColumn(name = "article_id")
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("userId")    // ArticleLikeId의 userId 필드와 매핑
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private Boolean isLike;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleLikeId implements Serializable {
		private Long userId;     // User의 ID 값만 저장
		private Long articleId;  // Article의 ID 값만 저장
	}
}
