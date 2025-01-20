package com.example.lifeonhana.service;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.enums.ArticleCategory;
import com.example.lifeonhana.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.lifeonhana.dto.response.ArticleDetailResponse;
import com.example.lifeonhana.repository.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.lifeonhana.dto.response.ArticleListResponse;
import com.example.lifeonhana.dto.response.ArticleListItemResponse;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.format.DateTimeFormatter;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.ArticleLikeRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {

	private static final String flaskUrl = "http://127.0.0.1:5000/related_products";

	private final ArticleRepository articleRepository;
	private final RestTemplate restTemplate;
	private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
	private final RedisTemplate<String, Object> redisTemplate;
	private final UserRepository userRepository;
	private final ArticleLikeRepository articleLikeRepository;

	public ArticleDetailResponse getArticleDetails(Long articleId) {
		// 1. 데이터베이스에서 기사 조회
		var article = articleRepository.findById(articleId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID입니다."));

		try {
			// `content` 데이터를 JSON 배열로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			List<Object> contentList = objectMapper.readValue(article.getContent(), List.class);

			// Flask 요청 데이터 생성
			Map<String, Object> request = Map.of("content", contentList);
			log.info("Sending request to Flask: {}", request);

			// Flask로 요청 전송
			var response = restTemplate.postForEntity(flaskUrl, request, Map.class);
			log.info("Received response from Flask: {}", response);

			// Flask 응답 처리
			List<Map<String, Object>> relatedProducts = (List<Map<String, Object>>) response.getBody().get("products");

			if (relatedProducts == null) {
				log.warn("No related products found in Flask response");
				relatedProducts = List.of();
			}

			// 결과 생성 및 반환
			return new ArticleDetailResponse(
				article.getArticleId(),
				article.getTitle(),
				article.getCategory().toString(),
				article.getThumbnailS3Key(),
				article.getContent(),
				article.getPublishedAt().toString(),
				false,
				article.getLikeCount(),
				relatedProducts.stream()
					.map(product -> new ArticleDetailResponse.RelatedProduct(
						Long.parseLong(product.get("product_id").toString()),
						product.get("name").toString(),
						product.get("category").toString(),
						product.get("link").toString()
					))
					.toList()
			);
		} catch (HttpClientErrorException e) {
			log.error("HTTP error occurred while calling Flask: {}", e.getMessage(), e);
			throw new RuntimeException("Flask API 호출 중 오류가 발생했습니다.", e);
		} catch (JsonProcessingException e) {
			log.error("Error occurred while processing JSON: {}", e.getMessage(), e);
			throw new RuntimeException("JSON 처리 중 오류가 발생했습니다.", e);
		} catch (Exception e) {
			log.error("Unexpected error occurred: {}", e.getMessage(), e);
			throw new RuntimeException("예상치 못한 오류가 발생했습니다.", e);
		}

	}

	public ArticleListResponse getArticles(String category, int page, int size, String authId) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publishedAt").descending());
		
		Page<Article> articlesPage;
		if (category != null && !category.isEmpty()) {
			Article.Category articleCategory = Article.Category.valueOf(category.toUpperCase());
			articlesPage = articleRepository.findByCategory(articleCategory, pageRequest);
		} else {
			articlesPage = articleRepository.findAll(pageRequest);
		}
		
		// 사용자의 좋아요 정보 조회
		final Set<Long> likedArticleIds = getLikedArticleIds(authId);
		
		List<ArticleListItemResponse> articleResponses = articlesPage.getContent().stream()
			.map(article -> new ArticleListItemResponse(
				article.getArticleId(),
				article.getTitle(),
				article.getCategory().toString(),
				article.getThumbnailS3Key(),
				article.getPublishedAt().format(DateTimeFormatter.ISO_DATE),
				likedArticleIds.contains(article.getArticleId())
			))
			.toList();
		
		return new ArticleListResponse(
			articleResponses,
			page + 1,
			size,
			articlesPage.getTotalPages(),
			articlesPage.getTotalElements()
		);
	}

	private Set<Long> getLikedArticleIds(String authId) {
		if (authId == null) {
			return new HashSet<>();
		}

		Long userId = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."))
			.getUserId();
		
		String userLikesKey = "user:" + userId + ":likes";
		Map<Object, Object> redisLikes = redisTemplate.opsForHash().entries(userLikesKey);
		
		// Redis에 데이터가 없는 경우 DB에서 조회
		if (redisLikes.isEmpty()) {
			Set<Long> dbLikedArticleIds = articleLikeRepository.findByIdUserIdAndIsLikeTrue(userId)
				.stream()
				.map(like -> like.getId().getArticleId())
				.collect(Collectors.toSet());
				
			// DB 데이터를 Redis에 캐싱
			dbLikedArticleIds.forEach(articleId -> 
				redisTemplate.opsForHash().put(userLikesKey, articleId.toString(), true)
			);
			
			return dbLikedArticleIds;
		}
		
		// Redis 데이터 반환
		return redisLikes.entrySet().stream()
			.filter(entry -> Boolean.TRUE.equals(entry.getValue()))
			.map(entry -> Long.parseLong(entry.getKey().toString()))
			.collect(Collectors.toSet());
	}
}
