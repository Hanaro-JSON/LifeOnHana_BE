package com.example.lifeonhana.dto.response;

public record ProductInsightResponse(
	String analysisResult,
	String productLink,
	String productName,
	boolean isLiked
) {
}
