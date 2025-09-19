package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.service.BoardService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Transactional
public class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Test
    @DisplayName("createBoard: DTO에서 기본값을 덮어쓰면 그대로 저장된다")
    void createBoard_overrideDefaults_success() {
        // given
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 공개글 아님 + ADMIN 승인 필요")
                .content("승인 필요한 글입니다.")
                .isPublic(false)                   // default(true) 덮어쓰기
                .requireAdminPost(true)            // default(false) 덮어쓰기
                .qnaStatus(QnaStatus.ANSWERED)     // default(WAITING) 덮어쓰기
                .postStatus(PostStatus.PUBLISHED)  // default(DRAFT) 덮어쓰기
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // then
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
    @DisplayName("createBoard: DTO가 선택필드를 비우면 엔티티 기본값이 적용된다")
    @Rollback
        // 명시적 표시(기본값이 롤백이긴 하지만 가독성용)
    void createBoard_applyEntityDefaults_success() {
        // given: 선택 필드(null) -> 엔티티 @Builder.Default 또는 DB 디폴트가 적용되는지 확인
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("[테스트] 기본값 적용")
                .content("기본값 테스트")
                .build();

        // when
        Board saved = boardService.createBoard(dto);

        // then
        Assertions.assertNotNull(saved.getId());
        Assertions.assertTrue(saved.getIsPublic(), "isPublic 기본값(true)이 적용되어야 합니다.");
        Assertions.assertFalse(saved.getRequireAdminPost(), "requireAdminPost 기본값(false)이 적용되어야 합니다.");
        Assertions.assertEquals(QnaStatus.WAITING, saved.getQnaStatus(), "QnA 기본상태는 WAITING 이어야 합니다.");
        Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus(), "게시글 기본상태는 PUBLISHED 이어야 합니다.");


    }
}
