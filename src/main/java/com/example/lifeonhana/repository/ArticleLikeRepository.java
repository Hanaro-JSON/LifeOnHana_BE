package com.example.lifeonhana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.ArticleLike;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, ArticleLike.ArticleLikeId> {
	Optional<ArticleLike> findByIdUserIdAndIdArticleId(Long userId, Long articleId);

	@Query(value = """
			SELECT a.category 
			FROM article a 
			JOIN article_like al ON a.article_id = al.article_id 
			JOIN user u ON al.user_id = u.user_id 
			WHERE u.auth_id = :authId 
			AND al.is_like = true 
			GROUP BY a.category 
			ORDER BY COUNT(a.category) DESC 
			LIMIT 1
			""", nativeQuery = true)
	Optional<String> findMostLikedCategory(@Param("authId") String authId);

	@Query("SELECT al FROM ArticleLike al WHERE al.id.userId = :userId AND al.isLike = true")
	List<ArticleLike> findByIdUserIdAndIsLikeTrue(@Param("userId") Long userId);
	@Query("SELECT al FROM ArticleLike al WHERE al.id.userId = :userId AND al.id.articleId IN :articleIds")
	List<ArticleLike> findByUserAndArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}
