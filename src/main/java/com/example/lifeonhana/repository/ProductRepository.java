package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
