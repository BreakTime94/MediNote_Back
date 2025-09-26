package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import lombok.Data;

import java.time.LocalDateTime;

public record BoardSearchCond(
        Long categoryId,
        String keyword,
        QnaStatus qnaStatus,
        LocalDateTime from,
        LocalDateTime to,
        Long writerId
) {}
