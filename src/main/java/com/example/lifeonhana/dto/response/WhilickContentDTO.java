package com.example.lifeonhana.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record WhilickContentDTO(
	Long articleId,
	String title,
	List<WhilickTextDTO> text,
	String ttsUrl,
	Float totalDuration,
	Integer likeCount,
	Boolean isLiked
) {}
