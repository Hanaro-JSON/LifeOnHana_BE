package com.example.lifeonhana.batch;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.ArticleLike;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.ProductLike;
import com.example.lifeonhana.entity.ProductLikeId;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.global.exception.InternalServerException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.ProductLikeRepository;
import com.example.lifeonhana.repository.ProductRepository;
import com.example.lifeonhana.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisToDatabaseSynchronizer {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ArticleRepository articleRepository;
	private final ArticleLikeRepository articleLikeRepository;
	private final ProductLikeRepository productLikeRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	@Scheduled(fixedRate = 21600000)
	@Transactional
	public void syncArticleLikesToDatabase() {
		Set<Object> changedLikes = redisTemplate.opsForSet().members("changedArticleLikes");
		if (changedLikes == null || changedLikes.isEmpty()) {
			log.info("No article likes changes to synchronize");
			return;
		}

		for (Object change : changedLikes) {
			try {
				String[] parts = change.toString().split(":");
				Long userId = Long.parseLong(parts[0]);
				Long articleId = Long.parseLong(parts[1]);
				
				String userLikesKey = "user:" + userId + ":likes";
				Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, articleId.toString());
				
				if (isLiked == null) continue;

				Article article = articleRepository.findById(articleId)
					.orElseThrow(() -> new NotFoundException(ErrorCode.ARTICLE_NOT_FOUND));

				ArticleLike articleLike = articleLikeRepository.findByIdUserIdAndIdArticleId(userId, articleId)
					.orElse(new ArticleLike());
				
				if (articleLike.getId() == null) {
					articleLike.setId(new ArticleLike.ArticleLikeId(userId, articleId));
					articleLike.setUser(userRepository.findById(userId)
						.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND)));
					articleLike.setArticle(article);
				}
				
				articleLike.setIsLike(isLiked);
				articleLikeRepository.save(articleLike);

				// 좋아요 수 업데이트
				String articleLikeCountKey = "article:" + articleId + ":likeCount";
				Integer likeCount = (Integer) redisTemplate.opsForValue().get(articleLikeCountKey);
				if (likeCount != null) {
					article.setLikeCount(likeCount);
					articleRepository.save(article);
				}
			} catch (Exception e) {
				log.error("[Sync Error] Code: {} | Message: {}", 
					ErrorCode.SYNC_ARTICLE_LIKE_FAILED.getCode(), 
					e.getMessage());
				throw new InternalServerException(ErrorCode.SYNC_ARTICLE_LIKE_FAILED, e);
			}
		}
		
		redisTemplate.delete("changedArticleLikes");
		log.info("Completed article likes synchronization with database");
	}

	@Scheduled(fixedRate = 3600000) // 1시간마다 실행
	@Transactional
	public void syncProductLikesToDatabase() {
		log.info("Starting product likes synchronization with database");
		
		Set<Object> changedLikes = redisTemplate.opsForSet().members("changedProductLikes");
		if (changedLikes == null || changedLikes.isEmpty()) {
			log.info("No product likes changes to synchronize");
			return;
		}

		for (Object change : changedLikes) {
			try {
				String[] parts = change.toString().split(":");
				Long userId = Long.parseLong(parts[0]);
				Long productId = Long.parseLong(parts[1]);
				
				String userLikesKey = "user:" + userId + ":productLikes";
				Boolean isLiked = (Boolean) redisTemplate.opsForHash().get(userLikesKey, productId.toString());
				
				if (isLiked == null) continue;

				User user = userRepository.findById(userId)
					.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
				Product product = productRepository.findById(productId)
					.orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

				ProductLike productLike = productLikeRepository.findByUserAndProduct(user, product)
					.orElse(new ProductLike());
				
				if (productLike.getId() == null) {
					productLike.setId(new ProductLikeId(userId, productId));
					productLike.setUser(user);
					productLike.setProduct(product);
				}
				
				productLike.setIsLike(isLiked);
				productLikeRepository.save(productLike);
			} catch (Exception e) {
				log.error("Error synchronizing product like for change {}: {}", change, e.getMessage());
			}
		}
		
		redisTemplate.delete("changedProductLikes");
		log.info("Completed product likes synchronization with database");
	}
}
