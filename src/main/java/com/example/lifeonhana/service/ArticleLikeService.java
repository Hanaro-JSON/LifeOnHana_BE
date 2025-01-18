package com.example.lifeonhana.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.response.ArticleResponse;
import com.example.lifeonhana.dto.response.LikeResponseDto;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.ArticleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ArticleRepository articleRepository;

	public LikeResponseDto toggleLike(Long userId, Long articleId) {
		String articleLikeCountKey = "article:" + articleId + ":likeCount";
		String userLikesKey = "user:" + userId + ":likes";

		Integer likeCount = getOrInitializeLikeCount(articleId, articleLikeCountKey);

		Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, articleId.toString());
		if (isLiked == null) isLiked = false;

		if (isLiked) {
			redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), false);
			redisTemplate.opsForValue().decrement(articleLikeCountKey);
			likeCount--;
		} else {
			redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), true);
			redisTemplate.opsForValue().increment(articleLikeCountKey);
			likeCount++;
		}

		redisTemplate.opsForSet().add("changedArticles", articleId.toString());

		return LikeResponseDto.builder()
			.isLiked(!isLiked)
			.likeCount(likeCount)
			.build();
	}

	public LikeResponseDto getLikeInfo(Long userId, Long articleId) {
		String articleLikeCountKey = "article:" + articleId + ":likeCount";
		String userLikesKey = "user:" + userId + ":likes";

		Integer likeCount = getOrInitializeLikeCount(articleId, articleLikeCountKey);

		Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, articleId.toString());
		if (isLiked == null) isLiked = false;

		return LikeResponseDto.builder()
			.isLiked(isLiked)
			.likeCount(likeCount)
			.build();
	}

	private Integer getOrInitializeLikeCount(Long articleId, String articleLikeCountKey) {
		Integer likeCount = (Integer) redisTemplate.opsForValue().get(articleLikeCountKey);

		if (likeCount == null) {
			Article article = articleRepository.findById(articleId)
				.orElseThrow(() -> new NotFoundException("해당 기사를 찾을 수 없습니다."));
			likeCount = article.getLikeCount();
			redisTemplate.opsForValue().set(articleLikeCountKey, likeCount);
		}

		return likeCount;
	}

	public Slice<ArticleResponse> getLikedArticles(Long userId, int page, int size, String category) {
		String userLikesKey = "user:" + userId + ":likes";
		Map<Object, Object> likedArticlesMap = redisTemplate.opsForHash().entries(userLikesKey);

		List<Long> likedArticleIds = likedArticlesMap.entrySet().stream()
			.filter(entry -> Boolean.TRUE.equals(entry.getValue()))
			.map(entry -> Long.parseLong(entry.getKey().toString()))
			.collect(Collectors.toList());

		if (likedArticleIds.isEmpty()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.of(page, size), false);
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

		Slice<Article> articlesSlice;
		if (category != null) {
			articlesSlice = articleRepository.findByArticleIdInAndCategory(likedArticleIds, Article.Category.valueOf(category.toUpperCase()), pageable);
		} else {
			articlesSlice = articleRepository.findByArticleIdIn(likedArticleIds, pageable);
		}

		return articlesSlice.map(this::convertToResponse);
	}

	private ArticleResponse convertToResponse(Article article) {
		return new ArticleResponse(
			article.getArticleId(),
			article.getTitle(),
			article.getCategory().toString(),
			article.getThumbnailS3Key(),
			article.getPublishedAt()
		);
	}


}
