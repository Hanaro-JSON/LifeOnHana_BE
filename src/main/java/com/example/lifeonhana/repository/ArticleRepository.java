package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
