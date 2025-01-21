package com.example.lifeonhana.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.LifeProductResponseDTO;
import com.example.lifeonhana.dto.response.LoanProductDetailResponseDTO;
import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.SavingProductResponseDTO;
import com.example.lifeonhana.dto.response.SimpleProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public ProductListResponseDTO<SimpleProductResponseDTO> getProducts(String category, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Product> productPage;
		if (category != null && !category.isBlank()) {
			Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
			productPage = productRepository.findByCategory(productCategory, pageable);
		} else {
			productPage = productRepository.findAll(pageable);
		}

		List<SimpleProductResponseDTO> products =
			productPage.getContent().stream().map(SimpleProductResponseDTO::fromEntity).toList();
		return new ProductListResponseDTO<>(
			products, page,size, productPage.getTotalPages(), productPage.getTotalElements()
		);
	}


	public SavingProductResponseDTO getSavingsProduct(Long productId) {
		Product product = productRepository.findByProductId(productId)
				.orElseThrow(() -> new BadRequestException("존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.SAVINGS) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		return SavingProductResponseDTO.fromEntity(product);
	}

	public LoanProductDetailResponseDTO getLoanProduct(Long productId) {
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BadRequestException(
			"존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.LOAN) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		return LoanProductDetailResponseDTO.fromEntity(product);
	}

	public LifeProductResponseDTO getLifeProduct(Long productId) {
		Product product = productRepository.findByProductId(productId).orElseThrow(() -> new BadRequestException(
			"존재하지 않는 id 입니다."));

		if(product.getCategory() != Product.Category.LIFE) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		return LifeProductResponseDTO.fromEntity(product);
	}
}
