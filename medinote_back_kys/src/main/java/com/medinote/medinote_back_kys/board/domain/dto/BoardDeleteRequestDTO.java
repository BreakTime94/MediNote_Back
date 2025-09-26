package com.medinote.medinote_back_kys.board.domain.dto;

import jakarta.validation.constraints.NotNull;


public record BoardDeleteRequestDTO(
        @NotNull Long id,
        Long requesterId,
        String reason
) {}
