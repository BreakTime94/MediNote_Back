package com.medinote.medinote_back_kys.news.domain.dto;

import com.medinote.medinote_back_kys.news.domain.en.ContentType;

import java.time.LocalDateTime;

public record AdminNewsPublishResponseDTO(
        Long id,
        Boolean isPublished,
        String title,
        ContentType contentType,
        LocalDateTime modDate   // 변경 후 갱신시각 반환하면 관리에 유용
) {}
