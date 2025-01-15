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

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class ArticleDictionaryId implements Serializable {
		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "article_id")
		private Article article;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "dictionary_id")
		private Dictionary dictionary;
	}
}
