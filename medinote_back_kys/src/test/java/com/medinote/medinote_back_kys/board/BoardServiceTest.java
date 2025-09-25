package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardDetailResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardListResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SpringBootTest(properties = {
        // 보안 자동 설정 제외 (테스트 시 불필요한 사용자/패스워드 로그 억제)
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Transactional // 각 테스트 종료 시 자동 롤백
class BoardServiceTest {

    @Autowired private BoardService boardService;
    @Autowired private BoardRepository boardRepository;
    @Autowired private EntityManager entityManager;

    // ========= 공통 유틸 =========
    private void flushClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void seedMany(int n, String titlePrefix) {
        for (int i = 1; i <= n; i++) {
            boardRepository.save(Board.builder()
                    .memberId(1L)
                    .boardCategoryId(2L)
                    .title(titlePrefix + "-" + i)
                    .content("내용-" + i)
                    .build());
        }
    }

    // ========= 생성 =========
    @Test
    @DisplayName("createBoard: DTO 지정값으로 기본값 덮어쓰기")
    void createBoard_overrideDefaults_success() {
        // given
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 공개글 아님 + ADMIN 승인 필요")
                .content("승인 필요한 글입니다.")
                .isPublic(false)
                .requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED)
                .postStatus(PostStatus.PUBLISHED)
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(saved.getId()),
                () -> Assertions.assertEquals(1L, saved.getMemberId()),
                () -> Assertions.assertEquals(2L, saved.getBoardCategoryId()),
                () -> Assertions.assertEquals("[테스트] 공개글 아님 + ADMIN 승인 필요", saved.getTitle()),
                () -> Assertions.assertEquals("승인 필요한 글입니다.", saved.getContent()),
                () -> Assertions.assertFalse(saved.getIsPublic()),
                () -> Assertions.assertTrue(saved.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, saved.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus())
        );
    }

    @Test
    @DisplayName("createBoard: 선택필드 미지정 → 엔티티/DB 기본값 적용")
    void createBoard_applyEntityDefaults_success() {
        // given
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 기본값 적용")
                .content("기본값 테스트")
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // then(실DB 기본값까지 확인하려면 flush/clear + 재조회)
        flushClear();
        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(found.getId()),
                () -> Assertions.assertTrue(found.getIsPublic(), "isPublic 기본값(true)"),
                () -> Assertions.assertFalse(found.getRequireAdminPost(), "requireAdminPost 기본값(false)"),
                () -> Assertions.assertEquals(QnaStatus.WAITING, found.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, found.getPostStatus())
        );
    }

    // ========= 수정 =========
    @Test
    @DisplayName("updateBoard: null은 무시하고 지정한 필드만 수정된다")
    void updateBoard_partialUpdate_success() {
        // seed
        BoardCreateRequestDTO createDto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("원본 제목")
                .content("원본 내용")
                .build();
        Board saved = boardService.createBoard(createDto);

        // when
        BoardUpdateRequestDTO updateDto = BoardUpdateRequestDTO.builder()
                .id(saved.getId())
                .title("수정된 제목")
                .content("수정된 내용")
                .isPublic(false)
                .requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED)
                .postStatus(PostStatus.DRAFT)
                .build();
        boardService.updateBoard(updateDto);

        flushClear();
        Board reloaded = boardRepository.findById(saved.getId()).orElseThrow();

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals("수정된 제목", reloaded.getTitle()),
                () -> Assertions.assertEquals("수정된 내용", reloaded.getContent()),
                () -> Assertions.assertFalse(reloaded.getIsPublic()),
                () -> Assertions.assertTrue(reloaded.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, reloaded.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.DRAFT, reloaded.getPostStatus()),
                // 유지 필드 검증
                () -> Assertions.assertEquals(1L, reloaded.getMemberId()),
                () -> Assertions.assertEquals(2L, reloaded.getBoardCategoryId())
        );
    }

    @Test
    @DisplayName("updateBoard: 존재하지 않는 ID 수정 시 IllegalArgumentException 발생")
    void updateBoard_notFound_throwsException() {
        BoardUpdateRequestDTO updateDto = BoardUpdateRequestDTO.builder()
                .id(999_999L)
                .title("아무 제목")
                .content("아무 내용")
                .build();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> boardService.updateBoard(updateDto));
    }

    // ========= 단일 조회 =========
    @Test
    @DisplayName("getBoard: 존재하는 게시글을 DTO로 반환")
    void getBoard_success() {
        // seed (Repository 사용: 서비스 로직 접근 제한과 무관)
        Board saved = boardRepository.save(Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("단일조회-제목")
                .content("단일조회-내용")
                .build());
        flushClear();

        // when
        BoardDetailResponseDTO dto = boardService.getBoard(saved.getId());

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(saved.getId(), dto.getId()),
                () -> Assertions.assertEquals(1L, dto.getMemberId()),
                () -> Assertions.assertEquals(2L, dto.getBoardCategoryId()),
                () -> Assertions.assertEquals("단일조회-제목", dto.getTitle()),
                () -> Assertions.assertEquals("단일조회-내용", dto.getContent()),
                () -> Assertions.assertNotNull(dto.getRegDate()),
                () -> Assertions.assertNotNull(dto.getModDate())
        );
    }

    @Test
    @DisplayName("getBoard: 없는 ID면 404(ResponseStatusException)")
    void getBoard_notFound_throws404() {
        ResponseStatusException ex = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> boardService.getBoard(9_999_999L)
        );
        Assertions.assertEquals(404, ex.getStatusCode().value());
    }

    // ========= 목록(listBoards) 스모크 =========
    @Test
    @DisplayName("listBoards: 기본 화이트리스트 + 키워드 페이징 매핑 동작")
    void listBoards_baseline_keyword_paging_success() {
        // seed
        seedMany(25, "목록키워드");

        // given
        Criteria c = new Criteria();
        c.setPage(2);                 // 2페이지
        c.setSize(10);                // 페이지 사이즈 10
        c.setSort(List.of("id,desc"));// id desc
        c.setKeyword("목록키워드");     // keyword echo & spec 반영

        // when
        BoardListResponseDTO dto = boardService.listBoards(c);

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(25L, dto.getPage().getTotalElements()),
                () -> Assertions.assertEquals(3, dto.getPage().getTotalPages()),
                () -> Assertions.assertEquals(10, dto.getItems().size()),
                () -> Assertions.assertEquals("목록키워드", dto.getKeyword())
        );

        Long first = dto.getItems().get(0).getId();
        Long last  = dto.getItems().get(dto.getItems().size() - 1).getId();
        Assertions.assertTrue(first > last, "정렬(id desc) 실패: " + first + " <= " + last);
    }

    // ========= 삭제 =========
    @Test
    @DisplayName("deleteBoard: 요청자 본인이면 soft delete 성공")
    void deleteBoard_success() {
        // given
        Board saved = boardRepository.save(Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("삭제 대상")
                .content("삭제될 내용")
                .isPublic(true)
                .postStatus(PostStatus.PUBLISHED)
                .build());
        flushClear();

        var deleteDto = com.medinote.medinote_back_kys.board.domain.dto.BoardDeleteRequestDTO.builder()
                .id(saved.getId())
                .memberId(1L) // 본인
                .build();

        // when
        boardService.deletedBoard(deleteDto);
        flushClear();

        // then
        Board reloaded = boardRepository.findById(saved.getId()).orElseThrow();
        Assertions.assertAll(
                () -> Assertions.assertFalse(reloaded.getIsPublic(), "삭제 후 공개여부는 false 여야 한다"),
                () -> Assertions.assertEquals(PostStatus.DELETED, reloaded.getPostStatus(), "삭제 후 상태는 DELETED 여야 한다"),
                () -> Assertions.assertEquals("삭제 대상", reloaded.getTitle(), "제목은 그대로 유지되어야 한다"),
                () -> Assertions.assertEquals("삭제될 내용", reloaded.getContent(), "본문은 그대로 유지되어야 한다")
        );
    }

    @Test
    @DisplayName("deleteBoard: 본인 아닌 사용자가 삭제 시 예외 발생")
    void deleteBoard_notOwner_throwsException() {
        // given
        Board saved = boardRepository.save(Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("삭제 대상")
                .content("삭제될 내용")
                .isPublic(true)
                .postStatus(PostStatus.PUBLISHED)
                .build());
        flushClear();

        var deleteDto = com.medinote.medinote_back_kys.board.domain.dto.BoardDeleteRequestDTO.builder()
                .id(saved.getId())
                .memberId(99L) // 다른 사용자
                .build();

        // when & then
        Assertions.assertThrows(IllegalStateException.class,
                () -> boardService.deletedBoard(deleteDto));
    }

    @Test
    @DisplayName("deleteBoard: 없는 ID 삭제 시 IllegalArgumentException 발생")
    void deleteBoard_notFound_throwsException() {
        var deleteDto = com.medinote.medinote_back_kys.board.domain.dto.BoardDeleteRequestDTO.builder()
                .id(9_999_999L)
                .memberId(1L)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> boardService.deletedBoard(deleteDto));
    }
}