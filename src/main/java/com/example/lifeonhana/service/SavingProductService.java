package com.example.lifeonhana.service;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.SavingProductResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.SavingProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingProductService {
	private final SavingProductRepository savingProductRepository;

	public SavingProductResponseDTO getSavingsProduct(Long productId) {
		Product product =
			savingProductRepository.findProductByProductId(productId)
				.orElseThrow(() -> new BadRequestException("존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.SAVINGS) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		return SavingProductResponseDTO.fromEntity(product);
	}
}
