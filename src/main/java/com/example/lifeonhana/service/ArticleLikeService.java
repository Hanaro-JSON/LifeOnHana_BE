package com.example.lifeonhana.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.lifeonhana.dto.response.ArticleResponse;
import com.example.lifeonhana.dto.response.LikeResponseDto;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.entity.ArticleLike;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.dto.event.LikeEvent;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ArticleRepository articleRepository;
	private final ArticleLikeRepository articleLikeRepository;
	private final KafkaTemplate<String, LikeEvent> kafkaTemplate;
	private static final Logger log = LoggerFactory.getLogger(ArticleLikeService.class);

	public LikeResponseDto toggleLike(Long userId, Long articleId) {
		Boolean currentStatus = redisTemplate.opsForHash()
			.get("user:"+userId+":likes", articleId.toString()) != null;
		
		// Kafka 이벤트 발행
		kafkaTemplate.send("like-events", 
			new LikeEvent(userId, articleId, !currentStatus, Instant.now()));
		
		// 예상 값 계산
		int projectedCount = (Integer) redisTemplate.opsForValue().get("article:"+articleId+":likeCount") 
			+ (currentStatus ? -1 : 1);
		
		return LikeResponseDto.builder()
			.isLiked(!currentStatus)
			.likeCount(Math.max(0, projectedCount))
			.build();
	}

	public LikeResponseDto getLikeInfo(Long userId, Long articleId) {
		String articleLikeCountKey = "article:" + articleId + ":likeCount";
		String userLikesKey = "user:" + userId + ":likes";

		// 1. 좋아요 수 조회
		Integer likeCount = (Integer) redisTemplate.opsForValue().get(articleLikeCountKey);
		if (likeCount == null) {
			// DB에서 조회
			likeCount = articleRepository.findArticleById(articleId).getLikeCount();
			// Redis에 저장 (1시간 유효)
			redisTemplate.opsForValue().set(articleLikeCountKey, likeCount, Duration.ofHours(1));
			log.info("Like count loaded from DB for articleId {}: {}", articleId, likeCount);
		}

		// 2. 사용자의 좋아요 상태 조회
		Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, articleId.toString());
		if (isLiked == null) {
			// DB에서 조회
			isLiked = articleRepository.isUserLikedArticle(articleId, userId);
			// Redis에 저장
			redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), isLiked);
			log.info("User like status loaded from DB for userId {}, articleId {}: {}", userId, articleId, isLiked);
		}

		return LikeResponseDto.builder()
			.isLiked(isLiked)
			.likeCount(likeCount)
			.build();
	}

	private Integer getOrInitializeLikeCount(Long articleId, String articleLikeCountKey) {
		Integer likeCount = (Integer) redisTemplate.opsForValue().get(articleLikeCountKey);

		if (likeCount == null) {
			// DB에서 실제 좋아요 수 조회
			likeCount = articleRepository.findArticleById(articleId).getLikeCount();
			// Redis에 저장
			redisTemplate.opsForValue().set(articleLikeCountKey, likeCount);
		}

		return likeCount;
	}

	public Slice<ArticleResponse> getLikedArticles(Long userId, int page, int size, String category) {
		String userLikesKey = "user:" + userId + ":likes";
		Map<Object, Object> likedArticlesMap = redisTemplate.opsForHash().entries(userLikesKey);

		// Redis에 데이터가 없으면 DB에서 조회하고 캐싱
		if (likedArticlesMap.isEmpty()) {
			List<ArticleLike> likes = articleLikeRepository.findByIdUserIdAndIsLikeTrue(userId);
			for (ArticleLike like : likes) {
				Long articleId = like.getArticle().getArticleId();
				redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), true);
				// 변경된 좋아요 상태를 동기화 대상으로 표시
				redisTemplate.opsForSet().add("changedArticleLikes", userId + ":" + articleId);
			}
			likedArticlesMap = redisTemplate.opsForHash().entries(userLikesKey);
			log.info("Liked articles loaded from DB for userId {}", userId);
		}

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
