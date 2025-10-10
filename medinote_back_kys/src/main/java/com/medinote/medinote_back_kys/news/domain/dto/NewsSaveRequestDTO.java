package com.medinote.medinote_back_kys.news.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NewsSaveRequestDTO(
        @NotBlank @Size(max = 255)
        String sourceName,

        @NotBlank @Size(max = 500)
        String feedUrl,

        @NotBlank @Size(max = 500)
        String title,

        /** 기사 원문 링크(UNIQUE) */
        @NotBlank @Size(max = 500)
        String link,

        @Size(max = 255)
        String author,

        @Size(max = 50)
        String sectionCode,

        @Size(max = 50)
        String subSectionCode,

        @Size(max = 50)
        String serialCode,

        /** RSS description 그대로 저장 (없어도 허용) */
        String description,

        @Size(max = 500)
        String image,

        /** RSS pubDate → 없을 수도 있어 nullable 허용 */
        LocalDateTime pubDate
) {}