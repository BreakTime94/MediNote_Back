package com.medinote.medinote_back_kys.news.domain.dto;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;

import java.time.LocalDateTime;

public record NewsPublicListItemResponseDTO(
        Long id,
        String title,
        String link,
        String sourceName,
        ContentType contentType,
        LocalDateTime pubDate,
        String image,
        String description
) {}
