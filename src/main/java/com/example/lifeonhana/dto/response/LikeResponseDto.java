package com.example.lifeonhana.dto.response;

import lombok.Builder;

@Builder
public record LikeResponseDto (
	boolean isLiked,
	int likeCount
){}
