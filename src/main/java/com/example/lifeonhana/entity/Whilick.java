package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "whilick")
@Getter @Setter
@NoArgsConstructor
public class Whilick {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long whilickId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id", nullable = false)
	private Article article;

	@Column(nullable = false)
	private Long paragraphId;

	@Column(nullable = false)
	private String paragraph;

	@Column(nullable = false)
	private LocalDateTime startTime;

	@Column(nullable = false)
	private LocalDateTime endTime;
}
