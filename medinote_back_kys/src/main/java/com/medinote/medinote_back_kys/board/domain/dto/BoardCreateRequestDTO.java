package com.medinote.medinote_back_kys.board.domain.dto;


import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardCreateRequestDTO (
    @NotNull Long memberId,
    @NotNull Long boardCategoryId,
    @NotBlank String title,
    String content,
    Boolean requireAdminPost,        // null -> 기본 false
    Boolean isPublic,                // null -> 기본 true
    QnaStatus qnaStatus,
    PostStatus postStatus
) {}
