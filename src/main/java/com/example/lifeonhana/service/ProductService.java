package com.example.lifeonhana.service;

import java.util.List;
import java.util.Map;

import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import com.example.lifeonhana.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.LifeProductResponseDTO;
import com.example.lifeonhana.dto.response.LoanProductDetailResponseDTO;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.SavingProductResponseDTO;
import com.example.lifeonhana.dto.response.SimpleProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.ProductLikeRepository;
import com.example.lifeonhana.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductLikeRepository productLikeRepository;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	public ProductListResponseDTO<SimpleProductResponseDTO> getProducts(String category, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Slice<Product> productSlice;

		if (category != null && !category.isBlank()) {
			Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
			productSlice = productRepository.findByCategory(productCategory, pageable);
		} else {
			productSlice = productRepository.findAll(pageable);
		}

		List<SimpleProductResponseDTO> products =
			productSlice.getContent().stream().map(SimpleProductResponseDTO::fromEntity).toList();
		boolean hasNext = productSlice.hasNext();

		return new ProductListResponseDTO<>(products, hasNext);
	}


	public SavingProductResponseDTO getSavingsProduct(Long productId,String authId) {
		User user = getUser(authId);
		Product product = productRepository.findByProductId(productId)
				.orElseThrow(() -> new BadRequestException("존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.SAVINGS) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		String userLikesKey = "user:" + user.getUserId() + ":productLikes";
		Map<Object, Object> likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);

		if (likedProductsMap.isEmpty()) {
			List<ProductLike> dbLikes = productLikeRepository.findByUserAndIsLikeTrue(user);
			for (ProductLike like : dbLikes) {
				redisTemplate.opsForHash().put(userLikesKey,
						like.getProduct().getProductId().toString(), true);
			}
			likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);
		}

		Boolean redisProduct = (Boolean) redisTemplate.opsForHash().get(userLikesKey, product.getProductId().toString());
		boolean isLike = Boolean.TRUE.equals(redisProduct);

		return SavingProductResponseDTO.fromEntity(product, isLike);
	}

	public LoanProductDetailResponseDTO getLoanProduct(Long productId,String authId) {
		User user = getUser(authId);
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BadRequestException(
			"존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.LOAN) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		String userLikesKey = "user:" + user.getUserId() + ":productLikes";
		Map<Object, Object> likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);

		if (likedProductsMap.isEmpty()) {
			List<ProductLike> dbLikes = productLikeRepository.findByUserAndIsLikeTrue(user);
			for (ProductLike like : dbLikes) {
				redisTemplate.opsForHash().put(userLikesKey,
						like.getProduct().getProductId().toString(), true);
			}
			likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);
		}

		Boolean redisProduct = (Boolean) redisTemplate.opsForHash().get(userLikesKey, product.getProductId().toString());
		boolean isLike = Boolean.TRUE.equals(redisProduct);

		return LoanProductDetailResponseDTO.fromEntity(product, isLike);
	}

	public LifeProductResponseDTO getLifeProduct(Long productId,String authId) {
		User user = getUser(authId);
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BadRequestException(
			"존재하지 않는 id 입니다."));

		if(product.getCategory() != Product.Category.LIFE) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		String userLikesKey = "user:" + user.getUserId() + ":productLikes";
		Map<Object, Object> likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);

		if (likedProductsMap.isEmpty()) {
			List<ProductLike> dbLikes = productLikeRepository.findByUserAndIsLikeTrue(user);
			for (ProductLike like : dbLikes) {
				redisTemplate.opsForHash().put(userLikesKey,
						like.getProduct().getProductId().toString(), true);
			}
			likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);
		}

		Boolean redisProduct = (Boolean) redisTemplate.opsForHash().get(userLikesKey, product.getProductId().toString());
		boolean isLike = Boolean.TRUE.equals(redisProduct);

		return LifeProductResponseDTO.fromEntity(product, isLike);
	}

	private User getUser(String authId) {
		return userRepository.findByAuthId(authId)
				.orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다. authId: " + authId));
	}
}
