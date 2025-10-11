package com.medinote.medinote_back_kys.news.domain.dto;

import jakarta.validation.constraints.NotNull;

public record NewsPublishUpdateRequestDTO(
        @NotNull Boolean isPublished
) {}
