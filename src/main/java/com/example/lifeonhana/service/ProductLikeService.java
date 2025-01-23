package com.example.lifeonhana.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.ProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.entity.ProductLikeId;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.ProductLikeRepository;
import com.example.lifeonhana.repository.ProductRepository;
import com.example.lifeonhana.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductLikeService {
	private final ProductLikeRepository productLikeRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	public ProductListResponseDTO<ProductResponseDTO> getProductLikes(String authId, int page, int size) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
			
		String userLikesKey = "user:" + user.getUserId() + ":productLikes";
		Map<Object, Object> likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);

		// Redis에 데이터가 없으면 DB에서 초기 데이터 로드
		if (likedProductsMap.isEmpty()) {
			List<ProductLike> dbLikes = productLikeRepository.findByUserAndIsLikeTrue(user);
			for (ProductLike like : dbLikes) {
				redisTemplate.opsForHash().put(userLikesKey, 
					like.getProduct().getProductId().toString(), true);
			}
			likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);
		}

		List<Long> likedProductIds = likedProductsMap.entrySet().stream()
			.filter(entry -> Boolean.TRUE.equals(entry.getValue()))
			.map(entry -> Long.parseLong(entry.getKey().toString()))
			.collect(Collectors.toList());

		if (likedProductIds.isEmpty()) {
			return new ProductListResponseDTO<>(Collections.emptyList(), false);
		}

		Pageable pageable = PageRequest.of(page, size + 1);
		Slice<Product> productsSlice = productRepository.findByProductIdIn(likedProductIds, pageable);

		List<ProductResponseDTO> productList = productsSlice.getContent().stream()
			.map(ProductResponseDTO::fromEntity)
			.collect(Collectors.toList());

		return new ProductListResponseDTO<>(productList, productsSlice.hasNext());
	}

	@Transactional
	public boolean toggleLike(Long productId, String authId) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		
		String userLikesKey = "user:" + user.getUserId() + ":productLikes";

		Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, productId.toString());
		if (isLiked == null) isLiked = false;

		boolean newLikeStatus = !isLiked;
		redisTemplate.opsForHash().put(userLikesKey, productId.toString(), newLikeStatus);
		
		// 변경된 좋아요 정보를 동기화 대상으로 표시
		redisTemplate.opsForSet().add("changedProductLikes", 
			user.getUserId() + ":" + productId);

		return newLikeStatus;
	}
}
