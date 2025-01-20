package com.example.lifeonhana.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.ProductLike;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
	Page<ProductLike> findByUser_AuthIdAndIsLikeTrue(String authId, Pageable pageable);
}
