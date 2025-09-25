package com.medinote.medinote_back_kys.board.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDeleteRequestDTO {

    @NotNull(message = "게시글의 ID는 필수입니다")
    private Long id;

    @NotNull(message = "요청자의 ID는 필수입니다")
    private Long memberId;
}
