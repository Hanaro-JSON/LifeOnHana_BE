package com.example.lifeonhana.dto.response;

import java.util.List;

public record WhilickResponseDTO(
	List<WhilickContentDTO> contents,
	PageableDTO pageable
) {}
