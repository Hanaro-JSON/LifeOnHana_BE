package com.example.lifeonhana.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.ProductDTO;
import com.example.lifeonhana.dto.ProductLikeDTO;
import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.repository.ProductLikeRepository;

@Service
public class ProductLikeService {
	private final ProductLikeRepository productLikeRepository;
	private final JwtService jwtService;
	public ProductLikeService(ProductLikeRepository productLikeRepository, JwtService jwtService) {
		this.productLikeRepository = productLikeRepository;
		this.jwtService = jwtService;
	}

	private Long getUserIdFromToken(String token) {
		String accessToken = token.replace("Bearer ", "");
		return jwtService.extractUserId(accessToken);
	}

	public ProductLikeDTO getProductLikes(String token, Pageable pageable) {
		Long userId = getUserIdFromToken(token);
		Page<ProductLike> productLikes = productLikeRepository.findByUser_UserIdAndIsLikeTrue(userId, pageable);
		List<ProductDTO> productList =
			productLikes.stream().map(like -> ProductDTO.fromEntity(like.getProduct())).collect(Collectors.toList());

		return ProductLikeDTO.builder()
			.data(productList)
			.page(productLikes.getNumber() + 1)
			.size(productLikes.getSize())
			.totalPages(productLikes.getTotalPages())
			.totalElements(productLikes.getTotalElements())
			.build();
	}
}
