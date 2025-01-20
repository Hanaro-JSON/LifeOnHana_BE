package com.example.lifeonhana.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductLikeDTO {
	private List<ProductDTO> data;
	private int page;
	private int size;
	private int totalPages;
	private long totalElements;
}
