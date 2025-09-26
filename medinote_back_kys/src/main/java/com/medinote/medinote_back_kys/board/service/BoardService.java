package com.medinote.medinote_back_kys.board.service;


import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.BoardCategory;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.repository.BoardSpecs;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;

    /** 정렬 허용 화이트리스트 (클라이언트 sort 필드 → 엔티티 컬럼) */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "id", "id",
            "regDate", "regDate",
            "title", "title"
    );

    // ========== 생성 ==========
    @Transactional
    public Long create(BoardCreateRequestDTO dto) {
        Board entity = boardMapper.toEntity(dto);
        Board saved  = boardRepository.save(entity);
        return saved.getId();
    }

    // ========== 단일 조회 ==========
    public BoardDetailResponseDTO getDetail(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + id));
        return boardMapper.toDetailResponse(board);
    }

    // ========== 부분 수정(Patch) ==========
    @Transactional
    public void update(BoardUpdateRequestDTO dto) {
        Objects.requireNonNull(dto.id(), "id is required");
        Board board = boardRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + dto.id()));

        // MapStruct가 null은 무시하고 덮어씀(NullValuePropertyMappingStrategy.IGNORE)
        boardMapper.updateEntityFromDto(dto, board);
        // 영속 상태이므로 save 호출 없이도 flush 시 반영되지만, 명시적 save 허용
        boardRepository.save(board);
    }

    // ========== 소프트 삭제 ==========
    @Transactional
    public void delete(BoardDeleteRequestDTO dto) {
        Board board = boardRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + dto.id()));

        // mapper 의 default 메서드(도메인 메서드 호출) 사용
        boardMapper.deleteEntityFromDto(dto, board);
        boardRepository.save(board);
    }

    // ===== 카테고리별 목록 =====
    public BoardListResponseDTO listNotice(BoardSearchCond cond, PageCriteria criteria) {
        return listByCategoryAndBaseline(cond, criteria, BoardCategory.NOTICE);
    }
    public BoardListResponseDTO listFaq(BoardSearchCond cond, PageCriteria criteria) {
        return listByCategoryAndBaseline(cond, criteria, BoardCategory.FAQ);
    }
    public BoardListResponseDTO listQna(BoardSearchCond cond, PageCriteria criteria) {
        return listByCategoryAndBaseline(cond, criteria, BoardCategory.QNA);
    }

    // ===== 공통 구현 =====
    private BoardListResponseDTO listByCategoryAndBaseline(BoardSearchCond cond,
                                                           PageCriteria criteria,
                                                           BoardCategory category) {
        BoardSearchCond c = ensureCond(cond);

        Specification<Board> spec = Specification.allOf(
                BoardSpecs.isPublicTrue(),
                BoardSpecs.statusIn(EnumSet.of(PostStatus.PUBLISHED)),
                BoardSpecs.categoryEquals(category.id())
        );

        spec = appendOptionalConditions(spec, c);

        var page = boardRepository.findAll(spec, criteria.toPageable(SORT_WHITELIST));
        return boardMapper.toListResponse(page, criteria, c.keyword());
    }

    /** null 방지용: cond가 null이면 빈 record를 생성 */
    private BoardSearchCond ensureCond(BoardSearchCond cond) {
        return (cond != null)
                ? cond
                : new BoardSearchCond(null, null, null, null, null, null);
    }

    /** record accessor(필드명())로 접근해서 선택 조건을 합성 */
    private Specification<Board> appendOptionalConditions(Specification<Board> base, BoardSearchCond c) {
        Specification<Board> spec = base;

        if (c.keyword() != null && !c.keyword().isBlank()) {
            spec = spec.and(BoardSpecs.keywordLike(c.keyword()));
        }
        if (c.qnaStatus() != null) {
            spec = spec.and(BoardSpecs.qnaStatusEquals(c.qnaStatus()));
        }
        if (c.from() != null || c.to() != null) {
            spec = spec.and(BoardSpecs.regBetween(c.from(), c.to()));
        }
        if (c.writerId() != null) {
            spec = spec.and(BoardSpecs.writerEquals(c.writerId()));
        }
        // categoryId()는 이 서비스 메서드에서 이미 category로 고정하므로 추가 합성은 불필요

        return spec;
    }
}
