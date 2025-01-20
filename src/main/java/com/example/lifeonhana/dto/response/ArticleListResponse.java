package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.dto.response.ArticleListItemResponse;

import java.util.List;

public record ArticleListResponse(
    List<ArticleListItemResponse> articles,
    int page,
    int size,
    int totalPages,
    long totalElements
) {} 