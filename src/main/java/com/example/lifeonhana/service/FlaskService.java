package com.example.lifeonhana.service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class FlaskService {

	private static final String flaskUrl = "http://127.0.0.1:5000/api/ask_claude";

	private final RestTemplate restTemplate;

	public FlaskService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public Map<String, Object> getRecommendationsFromFlask(String reason, BigDecimal amount) {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		Map<String, Object> requestBody = Map.of("reason", reason, "amount", amount);
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, request, Map.class);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return response.getBody();
		} else {
			throw new RuntimeException("Flask API 호출 실패");
		}
	}
}
