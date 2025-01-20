package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.entity.enums.ArticleCategory;
import lombok.Builder;

public record UserNicknameResponseDTO(
    String nickname,
    ArticleCategory category
) {
    @Builder
    public UserNicknameResponseDTO {}
} 