package com.medinote.medinote_back_kys.news.domain.dto;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;

import java.time.LocalDateTime;

public record AdminNewsListItemResponseDTO(
        Long id,
        String title,
        String sourceName,
        ContentType contentType,
        String link,
        LocalDateTime pubDate,
        Boolean isPublished,
        LocalDateTime regDate,   // 생성시각
        LocalDateTime modDate    // 수정시각
) {}
