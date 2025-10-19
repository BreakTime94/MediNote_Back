package com.medinote.medinote_back_kys.board.service;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.BoardCategory;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.repository.BoardSpecs;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.common.security.RoleGuard;
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
    private final RoleGuard roleGuard; // ✅ 추가

    /** 정렬 허용 화이트리스트 */
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

    // ========== 수정 ==========
    @Transactional
    public void update(BoardUpdateRequestDTO dto, Long requesterId) {
        Objects.requireNonNull(dto.id(), "id is required");
        Board board = boardRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + dto.id()));

        // ✅ 본인만 수정 가능
        roleGuard.requireSelf(requesterId, board.getMemberId());

        // null 값은 무시하고 덮어쓰기
        boardMapper.updateEntityFromDto(dto, board);
        boardRepository.save(board);
    }

    @Transactional
    public void updateQnaStatusTemp(Long boardId, QnaStatus newStatus) {
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + boardId));

        // qnaStatus만 채운 DTO (나머지는 null)
        BoardUpdateRequestDTO patch = new BoardUpdateRequestDTO(
                boardId,   // id (필수)
                null,      // boardCategoryId
                null,      // title
                null,      // content
                null,      // isPublic
                null,      // requireAdminPost
                newStatus, // ✅ qnaStatus만 세팅
                null       // postStatus
        );

        // MapStruct 업데이트: null은 무시되므로 qnaStatus만 반영됨
        boardMapper.updateEntityFromDto(patch, board);
        boardRepository.save(board);

    }

    // ========== 삭제 ==========
    @Transactional
    public void delete(BoardDeleteRequestDTO dto, Long requesterId) {
        Board board = boardRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Board not found: " + dto.id()));

        // ✅ 본인만 삭제 가능
        roleGuard.requireSelf(requesterId, board.getMemberId());

        // mapper 의 default 메서드(도메인 메서드 호출)
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

    // ===== 공통 =====
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

    private BoardSearchCond ensureCond(BoardSearchCond cond) {
        return (cond != null)
                ? cond
                : new BoardSearchCond(null, null, null, null, null, null);
    }

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
        return spec;
    }
}
