package com.medinote.medinote_back_kys.news.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NewsPublishUpdateRequestDTO(
        @NotEmpty List<Long> ids,
        @NotNull Boolean isPublished
) {}
