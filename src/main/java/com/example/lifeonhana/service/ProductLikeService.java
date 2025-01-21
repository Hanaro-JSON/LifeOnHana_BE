package com.example.lifeonhana.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.ProductResponseDTO;
import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.repository.ProductLikeRepository;

@Service
public class ProductLikeService {
	private final ProductLikeRepository productLikeRepository;
	public ProductLikeService(ProductLikeRepository productLikeRepository) {
		this.productLikeRepository = productLikeRepository;
	}

	public ProductListResponseDTO<ProductResponseDTO> getProductLikes(String authId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size + 1);
		Slice<ProductLike> productLikes = productLikeRepository.findByUser_AuthIdAndIsLikeTrue(authId, pageable);

		List<ProductResponseDTO> productList =
			productLikes.stream().map(like -> ProductResponseDTO.fromEntity(like.getProduct())).collect(Collectors.toList());

		return new ProductListResponseDTO<>(productList, productLikes.hasNext());
	}
}
