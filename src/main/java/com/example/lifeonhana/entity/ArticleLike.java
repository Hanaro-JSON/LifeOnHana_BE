package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_like")
@Getter @Setter
@NoArgsConstructor
public class ArticleLike {
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id")
	private Article article;

	@Column(nullable = false)
	private Boolean isLike;
}
