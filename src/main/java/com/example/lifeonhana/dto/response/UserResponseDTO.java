package com.example.lifeonhana.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponseDTO(
    @JsonProperty("userId") Long userId,
    @JsonProperty("name") String name,
    @JsonProperty("authId") String authId,
    @JsonProperty("isFirst") Boolean isFirst
) {} 