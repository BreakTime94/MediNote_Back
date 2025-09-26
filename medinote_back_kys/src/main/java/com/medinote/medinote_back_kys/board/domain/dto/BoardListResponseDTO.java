package com.medinote.medinote_back_kys.board.domain.dto;

import com.medinote.medinote_back_kys.common.paging.PageMeta;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * BoardListResponseDTO
 *
 * 게시판 목록 조회 시 반환되는 응답 DTO입니다.
 * PageResponse<T>와 구조적으로 유사하지만,
 * Board 도메인 전용 응답으로서 의미를 명확히 하기 위해 분리했습니다.
 *
 * - items : 게시글 목록 (간단 정보)
 * - page : 페이징 메타 정보 (PageMeta)
 * - keyword : 검색어 에코백 (옵션, BoardSearchCond에서 넘어옴)
 */
public record BoardListResponseDTO(
        List<BoardListItemDTO> items,
        PageMeta page,
        String keyword
) {}
