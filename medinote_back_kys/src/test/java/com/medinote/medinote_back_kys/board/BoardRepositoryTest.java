package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardListResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.repository.BoardSpecs;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Transactional
class BoardRepositoryTest {

    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardMapper boardMapper;
    @Autowired private EntityManager em;

    // ===== 유틸 =====
    private void flushClear() {
        em.flush();
        em.clear();
    }

    private Map<String, String> whitelist() {
        return Map.of(
                "id", "id",
                "title", "title",
                "regDate", "regDate",
                "postStatus", "postStatus",
                "qnaStatus", "qnaStatus"
        );
    }

    private BoardListResponseDTO toListDTO(Specification<Board> spec, Criteria c) {
        Pageable pageable = c.toPageable(whitelist());
        var page = boardRepository.findAll(spec, pageable);
        return boardMapper.toListResponse(page, c);
    }

    private void seedMany(int count, String titlePrefix) {
        for (int i = 1; i <= count; i++) {
            boardRepository.save(Board.builder()
                    .memberId(1L)
                    .boardCategoryId(2L)
                    .title(titlePrefix + "-" + i)
                    .content("내용-" + i)
                    .build());
        }
    }

    // ===== 생성 =====
    @Test
    @DisplayName("생성: DTO null 필드는 엔티티/DB 기본값이 적용된다")
    void create_withDefaults_success() {
        var dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("hello")
                .content("content")
                .build();

        Board saved = boardRepository.save(boardMapper.toEntity(dto));
        flushClear();
        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals(Boolean.TRUE, found.getIsPublic()),
                () -> Assertions.assertEquals(Boolean.FALSE, found.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.WAITING, found.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, found.getPostStatus())
        );
    }

    @Test
    @DisplayName("생성: DTO 지정값으로 기본값을 덮어쓴다")
    void create_overrideDefaults_success() {
        var dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 공개글 아님 + ADMIN 승인 필요")
                .content("승인 필요한 글입니다.")
                .isPublic(false)
                .requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED)
                .postStatus(PostStatus.PUBLISHED)
                .build();

        Board saved = boardRepository.save(boardMapper.toEntity(dto));

        Assertions.assertAll(
                () -> Assertions.assertNotNull(saved.getId()),
                () -> Assertions.assertFalse(saved.getIsPublic()),
                () -> Assertions.assertTrue(saved.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, saved.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus())
        );
    }

    @Test
    @DisplayName("단순 삽입(Entity 직접)도 정상 동작한다")
    void insert_entityDirect_success() {
        Board saved = boardRepository.save(Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("단순 삽입 테스트 제목")
                .content("단순 삽입 테스트 내용")
                .build());
        Assertions.assertNotNull(saved.getId());
    }

    // ===== 수정 =====
    @Test
    @DisplayName("수정: Update DTO 적용 후 반영 확인")
    void update_success() {
        Board base = boardRepository.save(Board.builder()
                .memberId(1L).boardCategoryId(2L)
                .title("원본 제목").content("원본 내용")
                .isPublic(true).requireAdminPost(false)
                .qnaStatus(QnaStatus.WAITING).postStatus(PostStatus.PUBLISHED)
                .build());

        var updateDto = BoardUpdateRequestDTO.builder()
                .id(base.getId())
                .boardCategoryId(2L)
                .title("수정된 제목")
                .content("수정된 내용")
                .isPublic(false)
                .requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED)
                .postStatus(PostStatus.DRAFT)
                .build();

        Board found = boardRepository.findById(updateDto.getId()).orElseThrow();
        boardMapper.updateEntityFromDto(updateDto, found);
        boardRepository.save(found);

        flushClear();
        Board reloaded = boardRepository.findById(base.getId()).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertEquals("수정된 제목", reloaded.getTitle()),
                () -> Assertions.assertEquals("수정된 내용", reloaded.getContent()),
                () -> Assertions.assertFalse(reloaded.getIsPublic()),
                () -> Assertions.assertTrue(reloaded.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, reloaded.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.DRAFT, reloaded.getPostStatus())
        );
    }

    // ===== 단일 조회 =====
    @Test
    @DisplayName("단일 조회: Entity → BoardDetailResponseDTO 매핑 성공")
    void findOne_and_map_to_detail_dto_success() {
        Board saved = boardRepository.save(Board.builder()
                .memberId(1L).boardCategoryId(2L)
                .title("단일조회-제목").content("단일조회-본문")
                .isPublic(false).requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED).postStatus(PostStatus.DRAFT)
                .build());

        flushClear();

        Board found = boardRepository.findById(saved.getId()).orElseThrow();
        var dto = boardMapper.toDetailResponse(found);

        Assertions.assertAll(
                () -> Assertions.assertEquals(saved.getId(), dto.getId()),
                () -> Assertions.assertEquals(1L, dto.getMemberId()),
                () -> Assertions.assertEquals(2L, dto.getBoardCategoryId()),
                () -> Assertions.assertEquals("단일조회-제목", dto.getTitle()),
                () -> Assertions.assertEquals("단일조회-본문", dto.getContent()),
                () -> Assertions.assertFalse(dto.getIsPublic()),
                () -> Assertions.assertTrue(dto.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, dto.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.DRAFT, dto.getPostStatus()),
                () -> Assertions.assertNotNull(dto.getRegDate()),
                () -> Assertions.assertNotNull(dto.getModDate())
        );
    }

    @Test
    @DisplayName("단일 조회: 존재하지 않는 ID는 Optional.empty()")
    void findOne_not_found_returns_empty_optional() {
        var opt = boardRepository.findById(9_999_999L);
        Assertions.assertTrue(opt.isEmpty());
    }

    // ===== 목록 & 필터 =====
    @Test
    @DisplayName("목록: 스펙 + 페이지네이션 + 매핑 + 정렬")
    void list_with_specs_pagination_mapping_sort_success() {
        seedMany(25, "목록테스트");

        var spec = Specification.allOf(
                BoardSpecs.userVisibleBaseline(),
                BoardSpecs.keywordLike("목록테스트")
        );

        var c = new Criteria();
        c.setPage(2);
        c.setSize(10);
        c.setSort(List.of("id,desc"));
        c.setKeyword("목록테스트");

        var dto = toListDTO(spec, c);

        Assertions.assertAll(
                () -> Assertions.assertEquals(25L, dto.getPage().getTotalElements()),
                () -> Assertions.assertEquals(10, dto.getPage().getSize()),
                () -> Assertions.assertEquals(3, dto.getPage().getTotalPages()),
                () -> Assertions.assertEquals(2, dto.getPage().getPage()),
                () -> Assertions.assertEquals(10, dto.getItems().size())
        );

        var firstId = dto.getItems().get(0).getId();
        var lastId  = dto.getItems().get(dto.getItems().size() - 1).getId();
        Assertions.assertTrue(firstId > lastId, "정렬(id desc) 실패");

        var any = dto.getItems().get(0);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(any.getTitle()),
                () -> Assertions.assertNotNull(any.getMemberId()),
                () -> Assertions.assertNotNull(any.getBoardCategoryId()),
                () -> Assertions.assertNotNull(any.getIsPublic()),
                () -> Assertions.assertNotNull(any.getRequireAdminPost()),
                () -> Assertions.assertNotNull(any.getQnaStatus()),
                () -> Assertions.assertNotNull(any.getPostStatus())
        );
        Assertions.assertEquals("목록테스트", dto.getKeyword());
    }

    @Test
    @DisplayName("필터: 공개글 + 상태(PUBLISHED/HIDDEN)만 노출")
    void filter_user_visible_baseline_only() {
        Board pub = boardRepository.save(Board.builder().memberId(1L).boardCategoryId(2L)
                .title("pub").content("c").isPublic(true).postStatus(PostStatus.PUBLISHED).build());
        Board hidden = boardRepository.save(Board.builder().memberId(1L).boardCategoryId(2L)
                .title("hidden").content("c").isPublic(true).postStatus(PostStatus.HIDDEN).build());
        boardRepository.save(Board.builder().memberId(1L).boardCategoryId(2L)
                .title("draft").content("c").isPublic(true).postStatus(PostStatus.DRAFT).build());
        boardRepository.save(Board.builder().memberId(1L).boardCategoryId(2L)
                .title("deleted").content("c").isPublic(true).postStatus(PostStatus.DELETED).build());
        boardRepository.save(Board.builder().memberId(1L).boardCategoryId(2L)
                .title("private").content("c").isPublic(false).postStatus(PostStatus.PUBLISHED).build());

        var c = new Criteria();
        c.setPage(1);
        c.setSize(50);
        c.setSort(List.of("id,asc"));

        var dto = toListDTO(BoardSpecs.userVisibleBaseline(), c);

        var titles = dto.getItems().stream().map(i -> i.getTitle()).toList();
        Assertions.assertTrue(titles.contains("pub"));
        Assertions.assertTrue(titles.contains("hidden"));
        Assertions.assertFalse(titles.contains("draft"));
        Assertions.assertFalse(titles.contains("deleted"));
        Assertions.assertFalse(titles.contains("private"));
    }

    @Test
    @DisplayName("필터: 카테고리(2) + QnA 상태(ANSWERED) + 키워드(바나나) + 등록일 범위")
    void filter_category_qna_keyword_regDate() {
        LocalDateTime now = LocalDateTime.now();

        // 카테고리 1 (사과) 5건
        for (int i = 1; i <= 5; i++) {
            boardRepository.save(Board.builder()
                    .memberId(1L).boardCategoryId(1L)
                    .title("필터테스트-사과-" + i).content("사과 내용 " + i)
                    .qnaStatus(QnaStatus.WAITING)
                    .isPublic(true).postStatus(PostStatus.PUBLISHED)
                    .build());
        }

        // 카테고리 2 (바나나) 5건 - QnA 상태 사용되는 게시판
        for (int i = 1; i <= 5; i++) {
            boardRepository.save(Board.builder()
                    .memberId(2L).boardCategoryId(2L)
                    .title("필터테스트-바나나-" + i).content("바나나 내용 " + i)
                    .qnaStatus(QnaStatus.ANSWERED)
                    .isPublic(true).postStatus(PostStatus.PUBLISHED)
                    .build());
        }

        var c = new Criteria();
        c.setPage(1);
        c.setSize(100);
        c.setSort(List.of("id,asc"));
        // 굳이 Criteria에도 넣고 싶다면 유지, 아니면 주석 처리
        c.setKeyword("바나나");

        var spec = Specification.allOf(
                BoardSpecs.userVisibleBaseline(),         // 공개/PUBLISHED 등 기본 가시성
                BoardSpecs.categoryEquals(2L),            // ✅ 존재하는 카테고리로 수정
                BoardSpecs.qnaStatusEquals(QnaStatus.ANSWERED),
                BoardSpecs.keywordLike("바나나"),
                BoardSpecs.regBetween(now.minusHours(1), now.plusHours(1))
        );

        var dto = toListDTO(spec, c);

        // 결과가 존재해야 함 (카테고리2 + ANSWERED + 바나나 = 5건 예상)
        Assertions.assertTrue(dto.getItems().size() > 0, "필터 결과가 비어 있습니다.");

        // 전 항목 검증
        dto.getItems().forEach(item -> {
            Assertions.assertEquals(2L, item.getBoardCategoryId(), "카테고리가 2가 아닙니다.");
            Assertions.assertEquals(QnaStatus.ANSWERED, item.getQnaStatus(), "QnA 상태가 ANSWERED가 아닙니다.");
            Assertions.assertTrue(item.getTitle().contains("바나나"), "제목에 '바나나'가 없습니다.");
        });

        // Criteria 반영 검증(선택)
        Assertions.assertEquals("바나나", dto.getKeyword(), "DTO의 keyword 반영이 예상과 다릅니다.");
    }

    @Test
    @DisplayName("삭제: Delete DTO 적용 후 isPublic=false, postStatus=DELETED 반영")
    void softDelete_success() {
        // given: 기본 게시글 생성
        Board base = boardRepository.save(Board.builder()
                .memberId(1L).boardCategoryId(2L)
                .title("삭제 대상").content("삭제될 내용")
                .isPublic(true).requireAdminPost(false)
                .qnaStatus(QnaStatus.WAITING).postStatus(PostStatus.PUBLISHED)
                .build());

        flushClear();

        // when: DeleteRequestDTO 로 소프트 삭제 적용
        var deleteDto = com.medinote.medinote_back_kys.board.domain.dto.BoardDeleteRequestDTO.builder()
                .id(base.getId())
                .memberId(1L) // 요청자 id
                .build();

        Board found = boardRepository.findById(deleteDto.getId()).orElseThrow();
        boardMapper.deleteEntityFromDto(deleteDto, found);
        boardRepository.save(found);

        flushClear();

        // then: isPublic=false, postStatus=DELETED 로 변경되었는지 확인
        Board reloaded = boardRepository.findById(base.getId()).orElseThrow();
        Assertions.assertAll(
                () -> Assertions.assertFalse(reloaded.getIsPublic(), "isPublic 값이 false가 아님"),
                () -> Assertions.assertEquals(PostStatus.DELETED, reloaded.getPostStatus(), "postStatus 가 DELETED 가 아님"),
                () -> Assertions.assertEquals("삭제 대상", reloaded.getTitle(), "제목은 그대로 유지되어야 함"),
                () -> Assertions.assertEquals("삭제될 내용", reloaded.getContent(), "본문은 그대로 유지되어야 함")
        );
    }
}