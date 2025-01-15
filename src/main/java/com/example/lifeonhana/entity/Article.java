package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "article")
@Getter @Setter
@NoArgsConstructor
public class Article {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long articleId;

	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Category category;

	@Column(nullable = false)
	private String thumbnailS3Key;

	@Column(nullable = false)
	private String ttsS3Key;

	@Column(nullable = false)
	private String shorts;

	@Column(nullable = false, columnDefinition = "json")
	private String content;

	@Column(nullable = false)
	private Integer likeCount;

	@Column(nullable = false)
	private LocalDateTime publishedAt;

	@OneToMany(mappedBy = "article")
	private List<ArticleLike> articleLikes = new ArrayList<>();

	@OneToMany(mappedBy = "article")
	private List<ArticleDictionary> articleDictionaries = new ArrayList<>();

	@OneToMany(mappedBy = "article")
	private List<ArticleProduct> articleProducts = new ArrayList<>();

	@OneToMany(mappedBy = "article")
	private List<Whilick> whilicks = new ArrayList<>();

	public enum Category {
		REAL_ESTATE, INVESTMENT, INHERITANCE_GIFT, TRAVEL, CULTURE, HOBBY
	}
}
