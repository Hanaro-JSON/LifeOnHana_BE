package com.example.lifeonhana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.Duration;

import com.example.lifeonhana.dto.response.WhilickResponseDTO;
import com.example.lifeonhana.dto.response.WhilickContentDTO;
import com.example.lifeonhana.dto.response.WhilickTextDTO;
import com.example.lifeonhana.dto.response.PageableDTO;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Whilick;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WhilickRepository;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.client.RecommendationClient;
import com.example.lifeonhana.entity.ArticleLike;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhilickService {
	private final WhilickRepository whilickRepository;
	private final ArticleRepository articleRepository;
	private final ArticleLikeRepository articleLikeRepository;
	private final RecommendationClient recommendationClient;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	@Transactional(readOnly = true)
	public WhilickResponseDTO getShortsByArticleId(Long articleId, int size, String authId) {
		User user = userRepository.getUserByAuthId(authId);

		// 요청된 article이 존재하는지 확인
		Article targetArticle = articleRepository.findById(articleId)
			.orElseThrow(() -> new NotFoundException("컨텐츠를 찾을 수 없습니다."));

		String cacheKey = "recommended_articles:" + user.getUserId();
		List<Long> recommendedArticleIds;

		try {
			recommendedArticleIds = getRecommendedArticleIds(user.getUserId(), cacheKey);
			// 타겟 article을 리스트의 첫번째로 이동
			recommendedArticleIds.remove(articleId);
			recommendedArticleIds.add(0, articleId);
		} catch (Exception e) {
			log.error("추천 시스템 서버 호출 실패", e);
			return getDefaultSortsByArticleId(articleId, size, user.getUserId());
		}

		return processArticles(recommendedArticleIds, 0, size, user.getUserId());
	}

	@Transactional(readOnly = true)
	public WhilickResponseDTO getShorts(int page, int size, String authId) {
		User user = userRepository.getUserByAuthId(authId);

		String cacheKey = "recommended_articles:" + user.getUserId();
		List<Long> recommendedArticleIds;

		try {
			recommendedArticleIds = getRecommendedArticleIds(user.getUserId(), cacheKey);
		} catch (Exception e) {
			log.error("추천 시스템 서버 호출 실패", e);
			return getDefaultSortedShorts(page, size, user.getUserId());
		}

		return processArticles(recommendedArticleIds, page, size, user.getUserId());
	}

	private WhilickResponseDTO processArticles(List<Long> articleIds, int page, int size, Long userId) {
		int start = page * size;
		int end = Math.min(start + size, articleIds.size());

		if (start >= articleIds.size()) {
			throw new NotFoundException("컨텐츠를 찾을 수 없습니다.");
		}

		List<Long> pageArticleIds = articleIds.subList(start, end);

		log.info("Page article IDs: {}, Size: {}", pageArticleIds, pageArticleIds.size());

		Set<Article> articlesWithWhilicks = whilickRepository.findArticlesWithWhilicksByIdIn(pageArticleIds);
		Set<Article> articlesWithLikes = whilickRepository.findArticlesWithLikesByIdIn(pageArticleIds);
		log.info("Articles with whilicks size: {}", articlesWithWhilicks.size());
		log.info("Articles with likes size: {}", articlesWithLikes.size());


		Map<Long, Article> whilicksMap = articlesWithWhilicks.stream()
			.collect(Collectors.toMap(Article::getArticleId, a -> a));

		Map<Long, Article> likesMap = articlesWithLikes.stream()
			.collect(Collectors.toMap(Article::getArticleId, a -> a));

		List<Article> sortedArticles = pageArticleIds.stream()
			.map(id -> {
				Article articleWithWhilicks = whilicksMap.get(id);
				Article articleWithLikes = likesMap.get(id);
				if (articleWithWhilicks != null && articleWithLikes != null) {
					articleWithWhilicks.setArticleLikes(articleWithLikes.getArticleLikes());
					return articleWithWhilicks;
				}
				return null;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		List<WhilickContentDTO> contents = sortedArticles.stream()
			.map(article -> createWhilickContent(article, userId))
			.collect(Collectors.toList());

		log.info("Sorted articles size: {}, Final contents size: {}", sortedArticles.size(), contents.size());

		return new WhilickResponseDTO(
			contents,
			new PageableDTO(
				page,
				size,
				(int) Math.ceil((double) articleIds.size() / size),
				articleIds.size(),
				page == 0,
				(page + 1) * size >= articleIds.size()
			)
		);
	}

	private WhilickResponseDTO getDefaultSortedShorts(int page, int size, Long userId) {
		Page<Article> articlesPage = whilickRepository.findAllArticles(
			PageRequest.of(page, size, Sort.by("publishedAt").descending())
		);

		List<WhilickContentDTO> contents = articlesPage.getContent().stream()
			.map(article -> createWhilickContent(article, userId))
			.collect(Collectors.toList());

		return new WhilickResponseDTO(
			contents,
			new PageableDTO(
				page,
				size,
				articlesPage.getTotalPages(),
				articlesPage.getTotalElements(),
				articlesPage.isFirst(),
				articlesPage.isLast()
			)
		);
	}

	@Transactional(readOnly = true)
	protected WhilickResponseDTO getDefaultSortsByArticleId(Long articleId, int size, Long userId) {
		// 요청된 article 먼저 가져오기
		Article targetArticle = articleRepository.findById(articleId)
			.orElseThrow(() -> new NotFoundException("컨텐츠를 찾을 수 없습니다."));

		// 나머지 article들을 최신순으로 가져오기 (targetArticle 제외)
		Page<Article> articlesPage = whilickRepository.findAllArticlesExcept(
			articleId,
			PageRequest.of(0, size - 1, Sort.by("publishedAt").descending())
		);

		// 결과 합치기
		List<Article> allArticles = new ArrayList<>();
		allArticles.add(targetArticle);
		allArticles.addAll(articlesPage.getContent());

		List<WhilickContentDTO> contents = allArticles.stream()
			.map(article -> createWhilickContent(article, userId))
			.collect(Collectors.toList());

		return new WhilickResponseDTO(
			contents,
			new PageableDTO(
				0,
				size,
				articlesPage.getTotalPages(),
				articlesPage.getTotalElements() + 1, // targetArticle 포함
				true,
				size >= articlesPage.getTotalElements() + 1
			)
		);
	}

	@SuppressWarnings("unchecked")
	private List<Long> getRecommendedArticleIds(Long userId, String cacheKey) {
		List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
		if (cachedIds != null) {
			return cachedIds;
		}

		List<Long> recommendedIds = recommendationClient.getRecommendedArticleIds(userId);
		redisTemplate.opsForValue().set(cacheKey, recommendedIds, 1, TimeUnit.HOURS);

		return recommendedIds;
	}

	private WhilickContentDTO createWhilickContent(Article article, Long userId) {
		List<WhilickTextDTO> texts = article.getWhilicks().stream()
			.sorted(Comparator.comparing(Whilick::getStartTime))
			.map(whilick -> new WhilickTextDTO(
				whilick.getParagraphId(),
				whilick.getParagraph(),
				whilick.getStartTime(),
				whilick.getEndTime()
			))
			.collect(Collectors.toList());

		Long articleId = article.getArticleId();
		String articleLikeCountKey = "article:" + articleId + ":likeCount";
		String userLikesKey = "user:" + userId + ":likes";

		Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, articleId.toString());
		if (isLiked == null) {
			isLiked = articleRepository.isUserLikedArticle(articleId, userId);
			redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), isLiked);
			log.info("User like status loaded from DB for userId {}, articleId {}: {}", userId, articleId, isLiked);
		}

		Float totalDuration = article.getWhilicks().stream()
			.findFirst()
			.map(Whilick::getTotalDuration)
			.orElse(0.0f);

		Integer likeCount = (Integer) redisTemplate.opsForValue().get(articleLikeCountKey);
		
		if (likeCount == null) {
			likeCount = articleRepository.findLikeCountByArticleId(article.getArticleId());
			likeCount = likeCount != null ? likeCount : 0;
			redisTemplate.opsForValue().set(
				articleLikeCountKey, 
				likeCount, 
				Duration.ofHours(1)
			);
			log.info("DB에서 좋아요 수 동기화 - 게시글 ID: {}, 좋아요 수: {}", article.getArticleId(), likeCount);
		}

		return WhilickContentDTO.builder()
			.articleId(article.getArticleId())
			.title(article.getTitle())
			.text(texts)
			.ttsUrl(article.getTtsS3Key())
			.totalDuration(totalDuration)
			.likeCount(likeCount)
			.isLiked(isLiked)
			.build();
	}
}
