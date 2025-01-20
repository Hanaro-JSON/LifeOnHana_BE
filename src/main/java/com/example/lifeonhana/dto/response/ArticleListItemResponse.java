package com.example.lifeonhana.dto.response;

public record ArticleListItemResponse(
    Long articleId,
    String title,
    String category,
    String thumbnailS3Key,
    String publishedAt,
    Boolean isLiked
) {} 