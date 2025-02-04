package com.example.lifeonhana.entity;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_dictionary")
@Getter @Setter
@NoArgsConstructor
public class ArticleDictionary {
	@EmbeddedId
	private ArticleDictionaryId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("articleId")  // ArticleDictionaryId의 articleId 필드와 매핑
	@JoinColumn(name = "article_id")
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("dictionaryId")  // ArticleDictionaryId의 dictionaryId 필드와 매핑
	@JoinColumn(name = "dictionary_id")
	private Dictionary dictionary;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleDictionaryId implements Serializable {
		private Long articleId;     // Article의 ID
		private Long dictionaryId;  // Dictionary의 ID
	}
}
