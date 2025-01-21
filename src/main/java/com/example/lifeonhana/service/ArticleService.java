package com.example.lifeonhana.service;
import com.example.lifeonhana.dto.response.ArticleSearchResponseDTO;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.ArticleLike;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.NotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.lifeonhana.dto.response.ArticleListItemResponse;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.format.DateTimeFormatter;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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

	public Slice<ArticleListItemResponse> getArticles(String category, int page, int size, String authId) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publishedAt").descending());
		
		Slice<Article> articlesSlice;
		if (category != null && !category.isEmpty()) {
			Article.Category articleCategory = Article.Category.valueOf(category.toUpperCase());
			articlesSlice = articleRepository.findSliceByCategory(articleCategory, pageRequest);
		} else {
			articlesSlice = articleRepository.findAllBy(pageRequest);
		}
		
		// 사용자의 좋아요 정보 조회
		final Set<Long> likedArticleIds = getLikedArticleIds(authId);
		
		List<ArticleListItemResponse> articleResponses = articlesSlice.getContent().stream()
			.map(article -> new ArticleListItemResponse(
				article.getArticleId(),
				article.getTitle(),
				article.getCategory().toString(),
				article.getThumbnailS3Key(),
				article.getPublishedAt().format(DateTimeFormatter.ISO_DATE),
				likedArticleIds.contains(article.getArticleId())
			))
			.toList();
		
		return new SliceImpl<>(articleResponses, pageRequest, articlesSlice.hasNext());
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

	@Transactional(readOnly = true)
	public Slice<ArticleSearchResponseDTO> searchArticles(String query, int page, int size, String authId) {
		User user = findUser(authId);
		// size + 1을 요청하여 다음 페이지 존재 여부를 확인
		Pageable pageable = PageRequest.of(page, size + 1, Sort.by(Sort.Direction.DESC, "publishedAt"));
		
		Slice<Article> articlesSlice;
		if (!StringUtils.hasText(query)) {
			articlesSlice = articleRepository.findAllBy(pageable);
		} else {
			Specification<Article> spec = createSearchSpecification(createSearchPattern(query));
			List<Article> articles = articleRepository.findAll(spec, pageable).getContent();
			
			// 다음 페이지 존재 여부 확인
			boolean hasNext = articles.size() > size;
			
			// 실제로 필요한 개수만큼만 남기기
			List<Article> content = hasNext ? articles.subList(0, size) : articles;
			
			articlesSlice = new SliceImpl<>(content, PageRequest.of(page, size), hasNext);
		}

		Map<Long, Boolean> likeStatusMap = getLikeStatusMap(articlesSlice.getContent(), user);
		
		List<ArticleSearchResponseDTO> articles = articlesSlice.getContent().stream()
			.map(article -> ArticleSearchResponseDTO.from(article,
				likeStatusMap.getOrDefault(article.getArticleId(), false)))
			.toList();

		return new SliceImpl<>(articles, PageRequest.of(page, size), articlesSlice.hasNext());
	}

	private String createSearchPattern(String query) {
		return "%" + query.trim().toLowerCase() + "%";
	}

	private Specification<Article> createSearchSpecification(String searchPattern) {
		return (root, criteriaQuery, cb) -> {
			criteriaQuery.distinct(true);
			criteriaQuery.orderBy(cb.desc(root.get("publishedAt")));

			jakarta.persistence.criteria.Predicate textSearch = createTextSearchPredicates(root, cb, searchPattern);
			jakarta.persistence.criteria.Predicate jsonSearch = createJsonSearchPredicates(root, cb, searchPattern);

			return cb.or(textSearch, jsonSearch);
		};
	}

	private jakarta.persistence.criteria.Predicate createTextSearchPredicates(Root<Article> root, CriteriaBuilder cb, String searchPattern) {
		return cb.or(
				cb.like(cb.lower(root.get("title")), searchPattern),
				cb.like(cb.lower(root.get("shorts")), searchPattern)
		);
	}

	private jakarta.persistence.criteria.Predicate createJsonSearchPredicates(Root<Article> root, CriteriaBuilder cb, String searchPattern) {
		return cb.or(
				cb.like(cb.lower(cb.function(
						"JSON_EXTRACT",
						String.class,
						root.get("content"),
						cb.literal("$[*].content")
				)), searchPattern),
				cb.like(cb.lower(cb.function(
						"JSON_EXTRACT",
						String.class,
						root.get("content"),
						cb.literal("$[*].description")
				)), searchPattern)
		);
	}

	private User findUser(String authId) {
		return userRepository.findByAuthId(authId)
				.orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
	}

	private Map<Long, Boolean> getLikeStatusMap(List<Article> articles, User user) {
		List<Long> articleIds = articles.stream()
				.map(Article::getArticleId)
				.toList();

		return articleLikeRepository.findByUserAndArticleIds(user.getUserId(), articleIds).stream()
				.collect(Collectors.toMap(
						like -> like.getId().getArticleId(),
						ArticleLike::getIsLike,
						(existing, replacement) -> existing
				));
	}
}
