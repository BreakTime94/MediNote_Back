package com.medinote.medinote_back_kys.reply.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReplyUpdateRequestDTO(
        Long replyId,           // 수정할 댓글 PK
        @NotBlank String content,        // 수정할 내용
        String title,                    // 수정할 제목 (옵션)
        Boolean isPublic                 // 공개 여부 변경 (옵션)
) {}
