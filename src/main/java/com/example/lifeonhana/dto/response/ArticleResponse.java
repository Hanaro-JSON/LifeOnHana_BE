package com.example.lifeonhana.dto.response;

import java.time.LocalDateTime;

public record ArticleResponse(
	Long articleId,
	String title,
	String category,
	String thumbnailS3Key,
	LocalDateTime publishedAt
) {
}
