package com.medinote.medinote_back_kys.reply.domain.dto;

import jakarta.validation.constraints.NotNull;

public record ReplyDeleteRequestDTO(
        @NotNull Long replyId,           // 삭제할 댓글 PK
        @NotNull Long memberId           // 작성자 PK (검증용)
) {}
