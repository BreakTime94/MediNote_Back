package com.medinote.medinote_back_kys.news.domain.dto;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;

import java.time.LocalDateTime;

public record AdminNewsDetailResponseDTO(
        Long id,
        ContentType contentType,
        String sourceName,
        String channelTitle,
        String feedUrl,
        String title,
        String link,
        LocalDateTime pubDate,
        String author,
        String sectionCode,
        String subSectionCode,
        String serialCode,
        String description,
        String image,
        Boolean isPublished,
        LocalDateTime regDate,
        LocalDateTime modDate
) {}
