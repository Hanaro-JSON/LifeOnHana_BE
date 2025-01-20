package com.example.lifeonhana.dto.response;

import java.util.List;

public record ArticleDetailResponse(
	Long articleId,
	String title,
	String category,
	String thumbnailS3Key,
	String content,
	String publishedAt,
	boolean isLiked,
	int likeCount,
	List<RelatedProduct> relatedProducts
) {
	public static record RelatedProduct(
		Long productId,
		String name,
		String category,
		String link
	) {}
}
