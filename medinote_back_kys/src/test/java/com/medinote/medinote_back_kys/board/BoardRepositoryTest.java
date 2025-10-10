package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.repository.BoardSpecs;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMapper boardMapper;

    @Test
    @DisplayName("Board 생성 매핑 및 저장 성공")
    void createBoard_success() {
        // given
        BoardCreateRequestDTO dto = new BoardCreateRequestDTO(
                1L,                      // memberId
                2L,                      // boardCategoryId
                "테스트 제목",             // title
                "테스트 내용",             // content
                null,                    // requireAdminPost → 기본 false 기대
                null,                    // isPublic → 기본 true 기대
                QnaStatus.WAITING,       // qnaStatus
                PostStatus.PUBLISHED     // postStatus
        );

        // when
        Board entity = boardMapper.toEntity(dto);
        Board saved = boardRepository.save(entity);

        // then
        Assertions.assertNotNull(saved.getId(), "저장 후 ID는 null이 아니어야 한다.");
        Assertions.assertEquals(dto.memberId(), saved.getMemberId(), "memberId 매핑 실패");
        Assertions.assertEquals(dto.boardCategoryId(), saved.getBoardCategoryId(), "boardCategoryId 매핑 실패");
        Assertions.assertEquals(dto.title(), saved.getTitle(), "title 매핑 실패");
        Assertions.assertEquals(dto.content(), saved.getContent(), "content 매핑 실패");

        // 기본값 검증
        Assertions.assertFalse(saved.getRequireAdminPost(), "requireAdminPost 기본값은 false 여야 한다.");
        Assertions.assertTrue(saved.getIsPublic(), "isPublic 기본값은 true 여야 한다.");

        // 상태값 검증
        Assertions.assertEquals(dto.qnaStatus(), saved.getQnaStatus(), "qnaStatus 매핑 실패");
        Assertions.assertEquals(dto.postStatus(), saved.getPostStatus(), "postStatus 매핑 실패");
    }

    @Test
    @DisplayName("Board 단일 조회 매핑 성공")
    void getBoardDetail_success() {
        // given : 엔티티 저장
        BoardCreateRequestDTO dto = new BoardCreateRequestDTO(
                1L,
                2L,
                "단일조회 제목",
                "단일조회 내용",
                false,
                true,
                QnaStatus.WAITING,
                PostStatus.PUBLISHED
        );

        Board saved = boardRepository.save(boardMapper.toEntity(dto));

        // when : 엔티티 → DTO 변환
        BoardDetailResponseDTO detail = boardMapper.toDetailResponse(saved);

        // then : 값 검증
        Assertions.assertNotNull(detail, "DTO 변환 결과는 null 이 아니어야 한다.");
        Assertions.assertEquals(saved.getId(), detail.id());
        Assertions.assertEquals(saved.getMemberId(), detail.memberId());
        Assertions.assertEquals(saved.getBoardCategoryId(), detail.boardCategoryId());
        Assertions.assertEquals(saved.getTitle(), detail.title());
        Assertions.assertEquals(saved.getContent(), detail.content());
        Assertions.assertEquals(saved.getRequireAdminPost(), detail.requireAdminPost());
        Assertions.assertEquals(saved.getIsPublic(), detail.isPublic());
        Assertions.assertEquals(saved.getQnaStatus(), detail.qnaStatus());
        Assertions.assertEquals(saved.getPostStatus(), detail.postStatus());
        Assertions.assertEquals(saved.getRegDate(), detail.regDate());
        Assertions.assertEquals(saved.getModDate(), detail.modDate());
    }

    @Test
    @DisplayName("Board 수정 매핑 성공 - null 필드는 무시됨")
    void updateBoard_success() {
        // given : 최초 저장
        BoardCreateRequestDTO createDto = new BoardCreateRequestDTO(
                1L,
                2L,
                "수정 전 제목",
                "수정 전 내용",
                false,
                true,
                QnaStatus.WAITING,
                PostStatus.PUBLISHED
        );

        Board saved = boardRepository.save(boardMapper.toEntity(createDto));
        Long savedId = saved.getId();

        // when : 수정 DTO 생성 (title, content, postStatus만 수정)
        BoardUpdateRequestDTO updateDto = new BoardUpdateRequestDTO(
                savedId,
                null,                         // boardCategoryId 수정 안 함
                "수정 후 제목",                 // title 수정
                "수정 후 내용",                 // content 수정
                null,                         // isPublic 수정 안 함
                null,                         // requireAdminPost 수정 안 함
                null,                         // qnaStatus 수정 안 함
                PostStatus.HIDDEN             // postStatus 수정
        );

        // Mapper 적용
        boardMapper.updateEntityFromDto(updateDto, saved);
        Board updated = boardRepository.save(saved);

        // then : 값 검증
        Assertions.assertEquals(savedId, updated.getId(), "ID는 변하지 않아야 한다.");
        Assertions.assertEquals("수정 후 제목", updated.getTitle(), "title이 수정되어야 한다.");
        Assertions.assertEquals("수정 후 내용", updated.getContent(), "content가 수정되어야 한다.");
        Assertions.assertEquals(PostStatus.HIDDEN, updated.getPostStatus(), "postStatus가 수정되어야 한다.");

        // null 값은 원래 값 유지 확인
        Assertions.assertEquals(createDto.boardCategoryId(), updated.getBoardCategoryId(), "boardCategoryId는 유지되어야 한다.");
        Assertions.assertEquals(createDto.isPublic(), updated.getIsPublic(), "isPublic은 유지되어야 한다.");
        Assertions.assertEquals(createDto.requireAdminPost(), updated.getRequireAdminPost(), "requireAdminPost는 유지되어야 한다.");
        Assertions.assertEquals(createDto.qnaStatus(), updated.getQnaStatus(), "qnaStatus는 유지되어야 한다.");
    }

    /**
     * 공통 정렬 화이트리스트
     */
    private static final Map<String, String> SORT_WHITELIST = Map.of(
            "id", "id",
            "regDate", "regDate",
            "title", "title"
    );

    private Board save(BoardCreateRequestDTO dto) {
        Board entity = boardMapper.toEntity(dto);
        return boardRepository.save(entity);
    }

    @BeforeEach
    void seed() {
        // 카테고리 1 = 공지, 카테고리 2 = 일반(예시)
        // 공개 & PUBLISHED (공지)
        save(new BoardCreateRequestDTO(1L, 1L, "공지 1", "내용 A", false, true, QnaStatus.WAITING, PostStatus.PUBLISHED));
        save(new BoardCreateRequestDTO(1L, 1L, "공지 2", "내용 B", false, true, QnaStatus.WAITING, PostStatus.PUBLISHED));
        // 공개 & HIDDEN (공지) → 필터 1번 케이스에서 제외될 것
        save(new BoardCreateRequestDTO(1L, 1L, "공지 숨김", "내용 C", false, true, QnaStatus.WAITING, PostStatus.HIDDEN));
        // 비공개 & PUBLISHED (공지) → 제외
        save(new BoardCreateRequestDTO(1L, 1L, "공지 비공개", "내용 D", false, false, QnaStatus.WAITING, PostStatus.PUBLISHED));
        // 일반 카테고리 (2) 공개 & PUBLISHED
        save(new BoardCreateRequestDTO(2L, 2L, "일반 1", "공지와 무관", false, true, QnaStatus.WAITING, PostStatus.PUBLISHED));
        // 일반 카테고리 (2) 공개 & PUBLISHED - 키워드 "공지" 포함(제목)
        save(new BoardCreateRequestDTO(2L, 2L, "공지 키워드 포함", "본문", false, true, QnaStatus.WAITING, PostStatus.PUBLISHED));
    }

    @Test
    @DisplayName("공지(카테고리=1) & 공개 & PUBLISHED 만 페이징 조회")
    void list_notice_public_published() {
        // given
        PageCriteria c = new PageCriteria();
        c.setPage(1);
        c.setSize(10);
        c.setSort(List.of("id,asc")); // 화이트리스트에 정의된 필드만 허용

        // spec: category=1 AND isPublic=true AND status in (PUBLISHED)
        Specification<Board> spec = Specification.allOf(
                BoardSpecs.categoryEquals(1L),
                BoardSpecs.isPublicTrue(),
                BoardSpecs.statusIn(EnumSet.of(PostStatus.PUBLISHED))
        );

        // when
        var page = boardRepository.findAll(spec, c.toPageable(SORT_WHITELIST));
        BoardListResponseDTO resp = boardMapper.toListResponse(page, c, null);

        // then
        Assertions.assertNotNull(resp);
        Assertions.assertNotNull(resp.page());
        Assertions.assertEquals(5, resp.items().size(), "공지 + 공개 + PUBLISHED 만 2건이어야 함");
        Assertions.assertEquals(5, resp.page().totalElements(), "totalElements=2 이어야 함");

        // 결과 각 아이템 검증
        resp.items().forEach(item -> {
            Assertions.assertEquals(1L, item.getBoardCategoryId());
            Assertions.assertTrue(item.getIsPublic());
            Assertions.assertEquals(PostStatus.PUBLISHED, item.getPostStatus());
        });
    }

    @Test
    @DisplayName("키워드(공지) + 페이징(size=2) 조회")
    void list_with_keyword_and_paging() {
        // given
        final String keyword = "공지"; // 제목/내용 LIKE
        PageCriteria c = new PageCriteria();
        c.setPage(1);
        c.setSize(2);
        c.setSort(List.of("id,asc"));

        // spec: keyword like AND 공개글 baseline(공개 + PUBLISHED/HIDDEN)
        Specification<Board> spec = Specification.allOf(
                BoardSpecs.keywordLike(keyword),
                BoardSpecs.userVisibleBaseline()
        );

        // when
        var page = boardRepository.findAll(spec, c.toPageable(SORT_WHITELIST));
        BoardListResponseDTO resp = boardMapper.toListResponse(page, c, keyword);

        // then
        // 응답 기본 검증
        Assertions.assertNotNull(resp);

        // page=1, size=2 -> 첫 페이지는 정확히 2건
        Assertions.assertEquals(2, resp.items().size(), "size=2 이므로 첫 페이지 결과는 2건이어야 함");

        // 총 건수: 환경/데이터에 따라 변할 수 있으므로 하한만 체크
        Assertions.assertTrue(resp.page().totalElements() >= 3,
                "'공지' 키워드 매칭 총 건수는 최소 3건 이상이어야 함");

        // 총 페이지 수 = ceil(totalElements / size) 로 계산하여 검증
        int expectedTotalPages = (int) Math.ceil(resp.page().totalElements() / (double) c.getSize());
        Assertions.assertEquals(expectedTotalPages, resp.page().totalPages(), "총 페이지 수 계산 불일치");

        // 메타: 첫 페이지면 prevPage는 null
        Assertions.assertEquals(1, resp.page().page(), "현재 페이지는 1이어야 함");
        Assertions.assertNull(resp.page().prevPage(), "첫 페이지이므로 prevPage는 null");

        // 반환 아이템 검증: 공개글 + (PUBLISHED/HIDDEN)만
        resp.items().forEach(item -> {
            Assertions.assertTrue(Boolean.TRUE.equals(item.getIsPublic()),
                    "userVisibleBaseline로 공개글만 나와야 함");
            Assertions.assertNotEquals(PostStatus.DRAFT, item.getPostStatus(),
                    "DRAFT는 목록에 포함되면 안 됨");
            Assertions.assertNotEquals(PostStatus.DELETED, item.getPostStatus(),
                    "DELETED는 목록에 포함되면 안 됨");
        });

        // 다음 페이지 존재 여부 (총건수 > size 이면 nextPage 있어야 함)
        if (resp.page().totalElements() > c.getSize()) {
            Assertions.assertNotNull(resp.page().nextPage(), "다음 페이지가 존재해야 함");
        } else {
            Assertions.assertNull(resp.page().nextPage(), "다음 페이지가 없어야 함");
        }
    }

    @Test
    @DisplayName("삭제 요청 매핑: isPublic=false, postStatus=DELETED 로 변경")
    void deleteBoard_softDelete_success() {
        // given: 공개+PUBLISHED 게시글 저장
        BoardCreateRequestDTO createDto = new BoardCreateRequestDTO(
                1L,                // memberId
                1L,                  // categoryId
                "삭제 대상 제목",
                "삭제 대상 내용",
                false,               // requireAdminPost
                true,                // isPublic
                QnaStatus.WAITING,
                PostStatus.PUBLISHED
        );
        Board saved = boardRepository.save(boardMapper.toEntity(createDto));
        Long id = saved.getId();

        // when: 삭제 요청 DTO 생성 후 매핑 메서드 호출
        BoardDeleteRequestDTO deleteDto = new BoardDeleteRequestDTO(
                id,
                1L,                // requesterId (엔티티에는 반영되지 않음)
                "테스트용 삭제 사유"
        );

        // 매핑(AfterMapping 훅에서 도메인 메서드 호출)
        boardMapper.deleteEntityFromDto(deleteDto, saved);
        Board deleted = boardRepository.save(saved);

        // then: 소프트 삭제 효과 검증
        Assertions.assertEquals(id, deleted.getId(), "ID는 동일해야 한다.");
        Assertions.assertFalse(deleted.getIsPublic(), "삭제 시 isPublic=false 이어야 한다.");
        Assertions.assertEquals(PostStatus.DELETED, deleted.getPostStatus(), "삭제 시 상태는 DELETED 이어야 한다.");

        // 그 외 필드는 유지(예: 제목/내용/카테고리/작성자)
        Assertions.assertEquals(createDto.title(), deleted.getTitle(), "제목은 수정되면 안 된다.");
        Assertions.assertEquals(createDto.content(), deleted.getContent(), "내용은 수정되면 안 된다.");
        Assertions.assertEquals(createDto.boardCategoryId(), deleted.getBoardCategoryId(), "카테고리는 수정되면 안 된다.");
        Assertions.assertEquals(createDto.memberId(), deleted.getMemberId(), "작성자는 수정되면 안 된다.");
    }
}