package com.medinote.medinote_back_kys.news.domain.dto;

import java.time.LocalDateTime;

public record NewsSaveRequestDTO(
        String sourceName,
        String feedUrl,
        String title,
        String link,
        String author,
        String sectionCode,
        String subSectionCode,
        String serialCode,
        String description,
        String image,
        LocalDateTime pubDate
) {}