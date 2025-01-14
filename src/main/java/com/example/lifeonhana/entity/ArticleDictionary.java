package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_dictionary")
@Getter @Setter
@NoArgsConstructor
public class ArticleDictionary {
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id")
	private Article article;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dictionary_id")
	private Dictionary dictionary;
}
