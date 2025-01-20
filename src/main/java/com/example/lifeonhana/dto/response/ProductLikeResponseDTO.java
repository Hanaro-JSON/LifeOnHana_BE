package com.example.lifeonhana.dto.response;

import java.util.List;

public record ProductLikeResponseDTO(
	List<ProductResponseDTO> data,
	int page,
	int size,
	int totalPages,
	long totalElements) {
}
