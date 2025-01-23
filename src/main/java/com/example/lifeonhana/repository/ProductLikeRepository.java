package com.example.lifeonhana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.entity.User;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
	Slice<ProductLike> findByUser_AuthIdAndIsLikeTrue(String authId, Pageable pageable);

	boolean existsById_ProductIdAndIsLikeTrue(Long productId);

	Optional<ProductLike> findByUserAndProduct(User user, Product product);

	List<ProductLike> findByUserAndIsLikeTrue(User user);
}
