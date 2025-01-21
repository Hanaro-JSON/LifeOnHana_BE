package com.example.lifeonhana.service;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.LoanProductDetailResponseDTO;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.LoanProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanProductService {
	private final LoanProductRepository loanProductRepository;

	public LoanProductDetailResponseDTO getLoanProduct(Long productId) {
		Product product = loanProductRepository.findByProductId(productId).orElseThrow(() -> new BadRequestException(
			"존재하지 않는 id 입니다."));

		if (product.getCategory() != Product.Category.LOAN) {
			throw new BadRequestException("존재하지 않는 id 입니다.");
		}

		return LoanProductDetailResponseDTO.fromEntity(product);
	}
}
