package com.example.lifeonhana.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> , JpaSpecificationExecutor<Article> {
	Slice<Article> findByArticleIdIn(List<Long> articleIds, Pageable pageable);

	Slice<Article> findByArticleIdInAndCategory(List<Long> articleIds, Article.Category category, Pageable pageable);

	Page<Article> findByCategory(Article.Category category, Pageable pageable);
}

