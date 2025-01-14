package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dictionary")
@Getter @Setter
@NoArgsConstructor
public class Dictionary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dictionaryId;

	@Column(nullable = false, length = 100)
	private String word;

	@Column(nullable = false)
	private String description;

	@OneToMany(mappedBy = "dictionary")
	private List<ArticleDictionary> articleDictionaries = new ArrayList<>();
}
