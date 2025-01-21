package com.example.lifeonhana.dto.response;

public record WhilickTextDTO(
	Long paragraphId,
	String content,
	Float startTime,
	Float endTime
) {}
