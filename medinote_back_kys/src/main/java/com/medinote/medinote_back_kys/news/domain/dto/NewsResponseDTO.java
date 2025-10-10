package com.medinote.medinote_back_kys.news.domain.dto;

import java.time.LocalDateTime;

public record NewsResponseDTO(
        Long id,
        String sourceName,
        String title,
        String link,
        String author,
        String description,
        String image,
        LocalDateTime pubDate,
        boolean isPublished
) {}
