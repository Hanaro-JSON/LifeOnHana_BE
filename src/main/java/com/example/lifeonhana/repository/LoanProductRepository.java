package com.example.lifeonhana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Product;

@Repository
public interface LoanProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategory(Product.Category category);
}
