package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public record BoardUpdateRequestDTO(
        @NotNull(message = "게시글 ID는 필수값입니다.")
        Long id,
        Long boardCategoryId,
        String title,
        String content,
        Boolean isPublic,
        Boolean requireAdminPost,
        QnaStatus qnaStatus,
        PostStatus postStatus
) {}
