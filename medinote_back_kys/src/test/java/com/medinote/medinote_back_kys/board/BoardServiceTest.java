package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardDetailResponseDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.service.BoardService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {
        // 보안 자동 설정 제외 (테스트 시 불필요한 사용자/패스워드 로그 억제)
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Transactional
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("createBoard: DTO 지정값으로 기본값 덮어쓰기")
    void createBoard_overrideDefaults_success() {
        // given
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 공개글 아님 + ADMIN 승인 필요")
                .content("승인 필요한 글입니다.")
                .isPublic(false)                    // default(true) 덮어쓰기
                .requireAdminPost(true)             // default(false) 덮어쓰기
                .qnaStatus(QnaStatus.ANSWERED)      // default(WAITING) 덮어쓰기
                .postStatus(PostStatus.PUBLISHED)   // default(PUBLISHED) 유지/명시
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // then (영속성 컨텍스트 내 값 검증)
        Assertions.assertNotNull(saved.getId(), "저장 후 ID가 존재해야 합니다.");
        Assertions.assertEquals(1L, saved.getMemberId());
        Assertions.assertEquals(2L, saved.getBoardCategoryId());
        Assertions.assertEquals("[테스트] 공개글 아님 + ADMIN 승인 필요", saved.getTitle());
        Assertions.assertEquals("승인 필요한 글입니다.", saved.getContent());
        Assertions.assertFalse(saved.getIsPublic(), "isPublic=false로 저장되어야 합니다.");
        Assertions.assertTrue(saved.getRequireAdminPost(), "requireAdminPost=true로 저장되어야 합니다.");
        Assertions.assertEquals(QnaStatus.ANSWERED, saved.getQnaStatus());
        Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus());
    }

    @Test
    @DisplayName("createBoard: 선택필드 미지정 → 엔티티/DB 기본값 적용")
    @Rollback// 기본적으로 @Transactional 롤백이지만 가독성용
    void createBoard_applyEntityDefaults_success() {
        // given: 선택 필드(null) → 엔티티 @Builder.Default 또는 DB DEFAULT 적용 기대
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 기본값 적용")
                .content("기본값 테스트")
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // DB DEFAULT까지 확실히 확인하려면 flush/clear 후 재조회
        entityManager.flush();
        entityManager.clear();
        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        // then
        Assertions.assertNotNull(found.getId());
        Assertions.assertTrue(found.getIsPublic(), "isPublic 기본값(true)이 적용되어야 합니다.");
        Assertions.assertFalse(found.getRequireAdminPost(), "requireAdminPost 기본값(false)이 적용되어야 합니다.");
        Assertions.assertEquals(QnaStatus.WAITING, found.getQnaStatus(), "QnA 기본상태는 WAITING 이어야 합니다.");
        Assertions.assertEquals(PostStatus.PUBLISHED, found.getPostStatus(), "게시글 기본상태는 PUBLISHED 이어야 합니다.");
    }

    @Test
    @DisplayName("updateBoard: null은 무시하고 지정한 필드만 수정된다")
    void updateBoard_partialUpdate_success() {
        // 1) given: 우선 게시글을 하나 생성(시드 데이터)
        BoardCreateRequestDTO createDto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("원본 제목")
                .content("원본 내용")
                // 선택필드는 비워 기본값 적용(isPublic=true, requireAdminPost=false, WAITING, PUBLISHED 등)
                .build();

        Board saved = boardService.createBoard(createDto);

        // 2) when: 일부 필드만 채운 Update DTO로 수정 호출 (null은 무시되어 원본 유지)
        BoardUpdateRequestDTO updateDto = BoardUpdateRequestDTO.builder()
                .id(saved.getId())
                // boardCategoryId는 null로 두어 기존(2L) 유지
                .title("수정된 제목")
                .content("수정된 내용")
                .isPublic(false)                 // 공개 → 비공개
                .requireAdminPost(true)          // 승인 필요로 변경
                .qnaStatus(QnaStatus.ANSWERED)   // WAITING → ANSWERED
                .postStatus(PostStatus.DRAFT)    // PUBLISHED → DRAFT
                .build();

        Board updatedManaged = boardService.updateBoard(updateDto);

        // 3) then: DB 반영 확인을 위해 flush/clear 후 재조회
        entityManager.flush();
        entityManager.clear();

        Board reloaded = boardRepository.findById(saved.getId()).orElseThrow();

        // 변경된 필드들
        Assertions.assertAll(
                () -> Assertions.assertEquals("수정된 제목", reloaded.getTitle()),
                () -> Assertions.assertEquals("수정된 내용", reloaded.getContent()),
                () -> Assertions.assertFalse(reloaded.getIsPublic()),
                () -> Assertions.assertTrue(reloaded.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, reloaded.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.DRAFT, reloaded.getPostStatus())
        );

        // 변경하지 않은 필드들은 원본 유지(NullValuePropertyMappingStrategy.IGNORE 검증)
        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, reloaded.getMemberId(), "memberId는 유지되어야 합니다."),
                () -> Assertions.assertEquals(2L, reloaded.getBoardCategoryId(), "boardCategoryId는 유지되어야 합니다.")
        );
    }

    @Test
    @DisplayName("updateBoard: 존재하지 않는 ID 수정 시 IllegalArgumentException 발생")
    void updateBoard_notFound_throwsException() {
        // given
        BoardUpdateRequestDTO updateDto = BoardUpdateRequestDTO.builder()
                .id(999_999L) // 존재하지 않는 ID 가정
                .title("아무 제목")
                .content("아무 내용")
                .build();

        // when & then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> boardService.updateBoard(updateDto),
                "존재하지 않는 게시글 ID면 IllegalArgumentException이 발생해야 합니다.");
    }

    @Test
    @DisplayName("단일 조회: 존재하는 게시글을 DTO로 반환")
    @Transactional
    void getBoard_success() {
        // given: 테스트용 게시글 저장
        Board saved = boardRepository.save(
                Board.builder()
                        .memberId(1L)
                        .boardCategoryId(2L)
                        .title("단일조회-제목")
                        .content("단일조회-내용")
                        .build()
        );
        entityManager.flush();
        entityManager.clear();

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
    @DisplayName("단일 조회: 없는 ID면 404(ResponseStatusException)")
    void getBoard_notFound_throws404() {
        // given
        long notExistId = 9_999_999L;

        // when & then
        ResponseStatusException ex = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> boardService.getBoard(notExistId)
        );
        Assertions.assertEquals(404, ex.getStatusCode().value());
    }
}