
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "like-events")
    public void handleLikeEvent(LikeEvent event) {
        // Redis 원자적 업데이트
        redisTemplate.execute(new SessionCallback<>() {
            public Object execute(RedisOperations operations) {
                operations.multi();
                operations.opsForHash().put(
                    "user:"+event.userId()+":likes", 
                    event.articleId().toString(), 
                    event.newStatus()
                );
                operations.opsForValue().increment(
                    "article:"+event.articleId()+":likeCount", 
                    event.newStatus() ? 1 : -1
                );
                return operations.exec();
            }
        });
    }
} 