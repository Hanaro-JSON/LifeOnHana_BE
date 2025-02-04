package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.entity.Article;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public record ArticleSearchResponseDTO(
    Long articleId,
    String title,
    String category,
    String thumbnailS3Key,
    String publishedAt,
    Boolean isLiked
) {
    public static ArticleSearchResponseDTO from(Article article, boolean isLiked) {
        return new ArticleSearchResponseDTO(
            article.getArticleId(),
            article.getTitle(),
            article.getCategory().toString(),
            article.getThumbnailS3Key(),
            article.getPublishedAt().format(DateTimeFormatter.ISO_DATE),
            isLiked
        );
    }

    public static List<ArticleSearchResponseDTO> fromList(List<Article> articles, Map<Long, Boolean> likeStatusMap) {
        return articles.stream()
            .map(article -> from(article, likeStatusMap.getOrDefault(article.getArticleId(), false)))
            .toList();
    }
} 
