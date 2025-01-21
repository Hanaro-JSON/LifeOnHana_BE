package com.example.lifeonhana.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

@Service
public class ProductLikeService {
	private final ProductLikeRepository productLikeRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	public ProductLikeService(ProductLikeRepository productLikeRepository, ProductRepository productRepository, UserRepository userRepository) {
		this.productLikeRepository = productLikeRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
	}

	public ProductListResponseDTO<ProductResponseDTO> getProductLikes(String authId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size + 1);
		Slice<ProductLike> productLikes = productLikeRepository.findByUser_AuthIdAndIsLikeTrue(authId, pageable);

		List<ProductResponseDTO> productList =
			productLikes.stream().map(like -> ProductResponseDTO.fromEntity(like.getProduct())).collect(Collectors.toList());

		return new ProductListResponseDTO<>(productList, productLikes.hasNext());
	}

	@Transactional
	public boolean toggleLike(Long productId, String authId) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));

		Optional<ProductLike> existingLike = productLikeRepository.findByUserAndProduct(user, product);

		if (existingLike.isPresent()) {
			productLikeRepository.delete(existingLike.get());
			return false;
		} else {
			ProductLike like = new ProductLike();
			like.setId(new ProductLikeId(user.getUserId(), product.getProductId()));
			like.setUser(user);
			like.setProduct(product);
			like.setIsLike(true);
			productLikeRepository.save(like);
			return true;
		}
	}
}
