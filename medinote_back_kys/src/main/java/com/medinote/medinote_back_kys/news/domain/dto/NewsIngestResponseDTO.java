package com.medinote.medinote_back_kys.news.domain.dto;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;

import java.time.LocalDateTime;

/** 메인 DB 컬럼 아님: ingestedAt = 응답 생성/로깅 시각 */
public record NewsIngestResponseDTO(
        Long id,
        ContentType contentType,
        String sourceName,
        String title,
        String link,
        String author,
        String description,
        String image,
        LocalDateTime pubDate,
        Boolean isPublished,
        LocalDateTime regDate,     // = createdAt
        LocalDateTime modDate,     // = updatedAt
        LocalDateTime ingestedAt
) {}
