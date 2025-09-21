package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
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
}