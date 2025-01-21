package com.example.lifeonhana.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Product;

@Repository
public interface SavingProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findProductByProductId(Long productId);
}
