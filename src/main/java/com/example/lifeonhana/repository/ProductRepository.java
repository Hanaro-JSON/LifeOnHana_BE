package com.example.lifeonhana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	Slice<Product> findByCategory(Product.Category productCategory, Pageable pageable);

	Optional<Product> findByProductId(Long productId);

	Slice<Product> findByProductIdIn(List<Long> productIds, Pageable pageable);
}
