package com.example.lifeonhana.dto.response;

import java.util.List;

public record ProductListResponseDTO<T>(
	List<T> products,
	int page,
	int size,
	int totalPages,
	long totalElements
) {
}
