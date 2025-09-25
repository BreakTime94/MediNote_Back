package com.medinote.medinote_back_kys.board.service;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.repository.BoardSpecs;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository  boardRepository;
    private final BoardMapper boardMapper;

    @Transactional
    public Board createBoard(BoardCreateRequestDTO dto) {
        // DTO → Entity 변환
        Board entity = boardMapper.toEntity(dto);

        // 저장
        return boardRepository.save(entity);
    }

    @Transactional
    public Board updateBoard(BoardUpdateRequestDTO dto) {
        // 1) 대상 엔티티 조회 (없으면 404/400 성격 예외)
        Board entity = boardRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + dto.getId()));

        // 2) 부분 업데이트 (null 필드는 무시)
        //    BoardMapper#updateEntityFromDto 가
        //    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE) 로 설정되어 있어
        //    dto에서 null이 아닌 값만 entity에 반영됩니다.
        boardMapper.updateEntityFromDto(dto, entity);

        return entity;
    }

    /** 기본 화이트리스트 (클라이언트 정렬필드 → 실제 엔티티 프로퍼티) */
    private static final Map<String, String> DEFAULT_SORT_WHITELIST = Map.of(
            "id", "id",
            "title", "title",
            "regDate", "regDate",
            "postStatus", "postStatus",
            "qnaStatus", "qnaStatus"
    );

    /** ✅ 필터링 적용된 목록 조회(기본 화이트리스트 사용) */
    public BoardListResponseDTO listBoards(Criteria criteria) {
        return listBoards(criteria, DEFAULT_SORT_WHITELIST);
    }

    /** ✅ 필터링 적용된 목록 조회(사용자 정의 화이트리스트 지원) */
    public BoardListResponseDTO listBoards(Criteria criteria, Map<String, String> whitelist) {
        Pageable pageable = criteria.toPageable(whitelist);
        Specification<Board> spec = buildSpecFromCriteria(criteria);
        Page<Board> page = (spec == null)
                ? boardRepository.findAll(pageable)
                : boardRepository.findAll(spec, pageable);
        return boardMapper.toListResponse(page, criteria);
    }


    //단일조회
    public BoardDetailResponseDTO getBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id));

        // (선택) 접근 제어 필요 시 여기서 체크
        // if (!board.getIsPublic()) { ... 권한 체크 ... }
        // if (board.getPostStatus() == PostStatus.DELETED) { throw new ResourceNotFoundException(...); }
        //접근제한 삭제된 글
        if (board.getPostStatus() == PostStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found: " + id);
        }

        return boardMapper.toDetailResponse(board);
    }

    @Transactional
    public void deletedBoard(BoardDeleteRequestDTO dto) {
        // 1) 대상 엔티티 조회
        Board entity = boardRepository.findById(dto.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + dto.getId()));

        // 2) 작성자 검증 (옵션: 본인 또는 관리자만 삭제 가능)
        if (!entity.getMemberId().equals(dto.getMemberId())) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        // 3) Mapper를 통해 soft delete 적용
        boardMapper.deleteEntityFromDto(dto, entity);

        // 4) 저장 (isPublic=false, postStatus=DELETED 로 반영됨)
        boardRepository.save(entity);
    }


    // ===================== 내부 헬퍼 =====================

    /**
     * Criteria → Specification<Board>
     * - keyword: 제목/내용 like
     * - filters:
     *   categoryId, qnaStatus, writerId, requireAdminPost, from, to, statuses
     * - 기본은 userVisibleBaseline() 적용
     *   (단, filters.statuses 가 명시되면 해당 집합으로 대체)
     */
    private Specification<Board> buildSpecFromCriteria(Criteria c) {
        // 1) 기본 가시성 정책
        Specification<Board> spec = BoardSpecs.userVisibleBaseline();

        // 2) keyword
        if (c.getKeyword() != null && !c.getKeyword().isBlank()) {
            spec = Specification.allOf(spec, BoardSpecs.keywordLike(c.getKeyword()));
        }

        // 3) 동적 필터 해석
        Map<String, String> f = Optional.ofNullable(c.getFilters()).orElseGet(HashMap::new);

        // categoryId
        Long categoryId = parseLong(f.get("categoryId")).orElse(null);
        if (categoryId != null) {
            spec = Specification.allOf(spec, BoardSpecs.categoryEquals(categoryId));
        }

        // qnaStatus
        QnaStatus qna = parseEnum(f.get("qnaStatus"), QnaStatus.class).orElse(null);
        if (qna != null) {
            spec = Specification.allOf(spec, BoardSpecs.qnaStatusEquals(qna));
        }

        // writerId (memberId)
        Long writerId = parseLong(f.get("writerId")).orElse(null);
        if (writerId != null) {
            spec = Specification.allOf(spec, BoardSpecs.writerEquals(writerId));
        }

        // requireAdminPost
        Boolean require = parseBoolean(f.get("requireAdminPost")).orElse(null);
        if (require != null) {
            spec = Specification.allOf(spec, BoardSpecs.requireAdminPost(require));
        }

        // regDate between
        LocalDateTime from = parseDateTime(f.get("from")).orElse(null);
        LocalDateTime to   = parseDateTime(f.get("to")).orElse(null);
        if (from != null || to != null) {
            spec = Specification.allOf(spec, BoardSpecs.regBetween(from, to));
        }

        // statuses: 명시되면 기본 baseline 대체
        // 예: filters[statuses]=PUBLISHED,HIDDEN
        if (f.containsKey("statuses")) {
            Set<PostStatus> set = parseStatuses(f.get("statuses"));
            if (!set.isEmpty()) {
                // baseline 제거 효과를 위해: 새로 구성
                spec = Specification.allOf(
                        // 공개 여부는 유지(요구사항에 따라 제거 가능)
                        BoardSpecs.isPublicTrue(),
                        BoardSpecs.statusIn(set),
                        // 나머지 조건은 위에서 이미 spec에 누적되었으므로 OK
                        spec // 누적 조건 포함
                );
            }
        }

        return spec;
    }

    private Optional<Long> parseLong(String s) {
        try {
            return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(Long.parseLong(s.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Boolean> parseBoolean(String s) {
        if (s == null) return Optional.empty();
        String v = s.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "true", "1", "y", "yes", "on" -> Optional.of(true);
            case "false", "0", "n", "no", "off" -> Optional.of(false);
            default -> Optional.empty();
        };
    }

    private <E extends Enum<E>> Optional<E> parseEnum(String s, Class<E> type) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(Enum.valueOf(type, s.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> parseDateTime(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(LocalDateTime.parse(s.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Set<PostStatus> parseStatuses(String s) {
        if (s == null || s.isBlank()) return Collections.emptySet();
        Set<PostStatus> set = EnumSet.noneOf(PostStatus.class);
        for (String token : s.split(",")) {
            parseEnum(token, PostStatus.class).ifPresent(set::add);
        }
        return set;
    }


}
