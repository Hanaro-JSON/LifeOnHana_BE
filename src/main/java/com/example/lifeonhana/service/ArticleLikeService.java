package com.example.lifeonhana.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.dto.response.LikeResponseDto;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.ArticleLike;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
	private final ArticleLikeRepository articleLikeRepository;
	private final ArticleRepository articleRepository;
	private final UserRepository userRepository;

	@Transactional
	public LikeResponseDto toggleLike(Long userId, Long articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> new NotFoundException("해당 기사를 찾을 수 없습니다."));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

		ArticleLike articleLike = articleLikeRepository.findByIdUserIdAndIdArticleId(userId, articleId)
			.orElse(null);

		boolean isLiked;
		if (articleLike != null) {
			articleLikeRepository.delete(articleLike);
			article.setLikeCount(article.getLikeCount() - 1);
			isLiked = false;
		} else {
			ArticleLike newLike = new ArticleLike();
			newLike.setId(new ArticleLike.ArticleLikeId(userId, articleId));
			newLike.setArticle(article);
			newLike.setUser(user);
			newLike.setIsLike(true);
			articleLikeRepository.save(newLike);

			article.setLikeCount(article.getLikeCount() + 1);
			isLiked = true;
		}

		articleRepository.save(article);

		return LikeResponseDto.builder()
			.isLiked(isLiked)
			.likeCount(article.getLikeCount())
			.build();
	}

