package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.common.paging.PageMeta;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardListResponseDTO {
    private final List<BoardListItemDTO> items;
    private final PageMeta page;     // 페이지네이션 정보(현재 페이지/블록 등)
    private final String keyword;    // 검색어 에코백(옵션)
}
