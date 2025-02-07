package com.example.lifeonhana.dto.event;

import java.time.Instant;

public record LikeEvent(
    Long userId,
    Long articleId,
    boolean newStatus,
    Instant timestamp
) {} 