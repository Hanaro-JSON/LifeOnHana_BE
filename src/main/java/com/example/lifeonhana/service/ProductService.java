package com.example.lifeonhana.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.ProductListResponseDTO;
import com.example.lifeonhana.dto.response.SimpleProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

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
}
