package com.example.lifeonhana.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.ArticleLike;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, ArticleLike.ArticleLikeId> {
	Optional<ArticleLike> findByIdUserIdAndIdArticleId(Long userId, Long articleId);
}
