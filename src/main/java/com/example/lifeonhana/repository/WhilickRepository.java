package com.example.lifeonhana.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.Whilick;

import java.util.List;
import java.util.Set;

@Repository
public interface WhilickRepository extends JpaRepository<Whilick, Long> {

	@Query("SELECT DISTINCT a FROM Article a " +
		"LEFT JOIN FETCH a.whilicks w " +
		"WHERE a.articleId IN :articleIds")
	Set<Article> findArticlesWithWhilicksByIdIn(@Param("articleIds") List<Long> articleIds);

	@Query("SELECT DISTINCT a FROM Article a " +
		"LEFT JOIN FETCH a.articleLikes " +
		"WHERE a.articleId IN :articleIds")
	Set<Article> findArticlesWithLikesByIdIn(@Param("articleIds") List<Long> articleIds);

	@Query("SELECT DISTINCT a FROM Article a " +
		"ORDER BY a.publishedAt DESC")
	Page<Article> findAllArticles(Pageable pageable);

	@Query("SELECT DISTINCT a FROM Article a " +
		"WHERE a.articleId != :articleId " +
		"ORDER BY a.publishedAt DESC")
	Page<Article> findAllArticlesExcept(@Param("articleId") Long articleId, Pageable pageable);
}
