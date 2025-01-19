package com.example.lifeonhana.service;
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

@Service
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final RestTemplate restTemplate;
	private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

	public ArticleDetailResponse getArticleDetails(Long articleId) {
		// 1. 데이터베이스에서 기사 조회
		var article = articleRepository.findById(articleId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID입니다."));

		try {
			String flaskUrl = "http://127.0.0.1:5000/related_products";

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
}
