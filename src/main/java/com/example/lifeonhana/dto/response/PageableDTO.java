package com.example.lifeonhana.dto.response;

public record PageableDTO(
	int pageNumber,
	int pageSize,
	int totalPages,
	long totalElements,
	boolean first,
	boolean last
) {}
