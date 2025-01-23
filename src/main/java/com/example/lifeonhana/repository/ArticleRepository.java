package com.example.lifeonhana.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Article;
import org.springframework.data.jpa.domain.Specification;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
	Slice<Article> findByArticleIdIn(List<Long> articleIds, Pageable pageable);

	Slice<Article> findByArticleIdInAndCategory(List<Long> articleIds, Article.Category category, Pageable pageable);

	Page<Article> findByCategory(Article.Category category, Pageable pageable);

	Slice<Article> findSliceByCategory(Article.Category category, Pageable pageable);

	Slice<Article> findAllBy(Pageable pageable);

	@Query("SELECT COUNT(al) FROM ArticleLike al WHERE al.id.articleId = :articleId AND al.isLike = true")
	Integer findLikeCountByArticleId(@Param("articleId") Long articleId);

	@Query("SELECT CASE WHEN COUNT(al) > 0 THEN true ELSE false END FROM ArticleLike al WHERE al.id.articleId = :articleId AND al.id.userId = :userId AND al.isLike = true")
	Boolean isUserLikedArticle(@Param("articleId") Long articleId, @Param("userId") Long userId);

	@Query("SELECT a FROM Article a WHERE a.articleId IN :articleIds")
	List<Article> findAllByArticleIdIn(@Param("articleIds") List<Long> articleIds);
}

