package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;

import java.time.LocalDateTime;

public record BoardDetailResponseDTO(
        Long id,
        Long memberId,
        Long boardCategoryId,
        String title,
        String content,
        Boolean requireAdminPost,
        Boolean isPublic,
        QnaStatus qnaStatus,
        PostStatus postStatus,
        LocalDateTime regDate,
        LocalDateTime modDate
) {}
