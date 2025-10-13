package com.medinote.medinote_back_kys.reply.domain.dto;

import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReplyCreateRequestDTO(
        @NotNull
        Long memberId,          // 작성자 PK

        @NotNull
        Long linkId,            // 대상 엔티티 PK

        @NotNull
        ReplyTargetType linkType,   // 댓글 대상 (BOARD | NEWS)

        @NotBlank
        String content,         // 댓글 내용

        String title,           // 선택: 제목

        Boolean isPublic        // 공개 여부 (null이면 기본 true)
) {}
