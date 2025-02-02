package com.example.lifeonhana.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.LifeProductResponseDTO;
import com.example.lifeonhana.dto.response.LoanProductDetailResponseDTO;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.SavingProductResponseDTO;
import com.example.lifeonhana.dto.response.SimpleProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.repository.ProductLikeRepository;
import com.example.lifeonhana.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductLikeRepository productLikeRepository;

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


	public SavingProductResponseDTO getSavingsProduct(Long productId) {
		Product product = productRepository.findByProductId(productId)
				.orElseThrow(() -> new BaseException(ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getCategory() != Product.Category.SAVINGS) {
			throw new BaseException(ErrorCode.PRODUCT_NOT_FOUND);
		}

		boolean isLike = productLikeRepository.existsById_ProductIdAndIsLikeTrue(productId);
		return SavingProductResponseDTO.fromEntity(product, isLike);
	}

	public LoanProductDetailResponseDTO getLoanProduct(Long productId) {
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BaseException(
			ErrorCode.PRODUCT_NOT_FOUND));

		if (product.getCategory() != Product.Category.LOAN) {
			throw new BaseException(ErrorCode.PRODUCT_NOT_FOUND);
		}

		boolean isLike = productLikeRepository.existsById_ProductIdAndIsLikeTrue(productId);
		return LoanProductDetailResponseDTO.fromEntity(product, isLike);
	}

	public LifeProductResponseDTO getLifeProduct(Long productId) {
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BaseException(
			ErrorCode.PRODUCT_NOT_FOUND));

		if(product.getCategory() != Product.Category.LIFE) {
			throw new BaseException(ErrorCode.PRODUCT_NOT_FOUND);
		}

		boolean isLike = productLikeRepository.existsById_ProductIdAndIsLikeTrue(productId);
		return LifeProductResponseDTO.fromEntity(product, isLike);
	}
}
