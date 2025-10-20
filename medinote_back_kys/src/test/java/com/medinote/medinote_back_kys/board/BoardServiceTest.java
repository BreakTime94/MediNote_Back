package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import com.medinote.medinote_back_kys.board.service.BoardService;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BoardServiceTest {

    @Autowired private BoardService boardService;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardMapper boardMapper;

    private static final Long EXISTING_MEMBER_ID = 1L; // FK 존재 전제 (작성자 본인)

    // ===== 공통 시드 유틸 =====
    private Long seed(String title, Long categoryId, boolean isPublic, PostStatus status, QnaStatus qna) {
        BoardCreateRequestDTO dto = new BoardCreateRequestDTO(
                EXISTING_MEMBER_ID, categoryId, title, "본문",
                false, isPublic, qna, status
        );
        return boardRepository.save(boardMapper.toEntity(dto)).getId();
    }

    @Test
    @DisplayName("create + getDetail 성공")
    void create_and_getDetail_success() {
        // when
        Long id = boardService.create(new BoardCreateRequestDTO(
                EXISTING_MEMBER_ID, 1L, "서비스-생성", "서비스-본문",
                null, null, QnaStatus.WAITING, PostStatus.PUBLISHED
        ));

        // then
        Assertions.assertNotNull(id);

        BoardDetailResponseDTO detail = boardService.getDetail(id);
        Assertions.assertEquals(id, detail.id());
        Assertions.assertEquals("서비스-생성", detail.title());
        Assertions.assertEquals("서비스-본문", detail.content());
        Assertions.assertTrue(detail.isPublic());
        Assertions.assertFalse(detail.requireAdminPost());
        Assertions.assertEquals(PostStatus.PUBLISHED, detail.postStatus());
    }

    @Test
    @DisplayName("update(Patch): null 무시, 지정 필드만 수정")
    void update_patch_success() {
        // given
        Long id = seed("수정 전", 2L, true, PostStatus.PUBLISHED, QnaStatus.WAITING);

        // when (요청자 = 작성자 동일)
        boardService.update(new BoardUpdateRequestDTO(
                id,
                null,                       // boardCategoryId 유지
                "수정 후",                   // title
                "수정된 본문",               // content
                null,                       // isPublic 유지
                null,                       // requireAdminPost 유지
                null,                       // qnaStatus 유지
                PostStatus.HIDDEN           // 상태 변경
        ), EXISTING_MEMBER_ID);              // ✅ 본인 ID 전달

        // then
        Board updated = boardRepository.findById(id).orElseThrow();
        Assertions.assertEquals("수정 후", updated.getTitle());
        Assertions.assertEquals("수정된 본문", updated.getContent());
        Assertions.assertEquals(PostStatus.HIDDEN, updated.getPostStatus());
        Assertions.assertEquals(2L, updated.getBoardCategoryId());
        Assertions.assertTrue(updated.getIsPublic());
        Assertions.assertFalse(updated.getRequireAdminPost());
        Assertions.assertEquals(QnaStatus.WAITING, updated.getQnaStatus());
    }

    @Test
    @DisplayName("delete(소프트): isPublic=false, postStatus=DELETED")
    void delete_soft_success() {
        // given
        Long id = seed("삭제 대상", 1L, true, PostStatus.PUBLISHED, QnaStatus.WAITING);

        // when (요청자 = 작성자 동일)
        boardService.delete(new BoardDeleteRequestDTO(id, EXISTING_MEMBER_ID, "사유"), EXISTING_MEMBER_ID);

        // then
        Board deleted = boardRepository.findById(id).orElseThrow();
        Assertions.assertFalse(deleted.getIsPublic(), "삭제 시 공개=false");
        Assertions.assertEquals(PostStatus.DELETED, deleted.getPostStatus(), "상태=DELETED");
    }

    @Test
    @DisplayName("공지 목록: category=1, 공개 & PUBLISHED만")
    void list_notice_published_public() {
        // given
        seed("공지-보임1", 1L, true,  PostStatus.PUBLISHED, QnaStatus.WAITING);
        seed("공지-보임2", 1L, true,  PostStatus.PUBLISHED, QnaStatus.WAITING);
        seed("공지-숨김(HIDDEN)", 1L, true,  PostStatus.HIDDEN,    QnaStatus.WAITING); // 제외
        seed("공지-비공개",     1L, false, PostStatus.PUBLISHED, QnaStatus.WAITING); // 제외
        seed("일반-보임", 2L, true, PostStatus.PUBLISHED, QnaStatus.WAITING); // 다른 카테고리

        PageCriteria c = new PageCriteria();
        c.setPage(1); c.setSize(10);

        // when
        BoardListResponseDTO resp = boardService.listNotice(null, c);

        // then
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.items().size() >= 2);
        resp.items().forEach(it -> {
            Assertions.assertEquals(1L, it.getBoardCategoryId());
            Assertions.assertTrue(it.getIsPublic());
            Assertions.assertEquals(PostStatus.PUBLISHED, it.getPostStatus());
        });
    }

    @Test
    @DisplayName("FAQ 목록: category=3, 공개 & PUBLISHED + 키워드 조건")
    void list_faq_keyword() {
        // given
        // 공개 FAQ (검색 대상)
        seed("FAQ: 비밀번호 초기화", 3L, true, PostStatus.PUBLISHED, QnaStatus.WAITING);
        seed("FAQ: 로그인 오류",    3L, true, PostStatus.PUBLISHED, QnaStatus.WAITING);
        // 비공개 FAQ (제외 대상)
        seed("FAQ: 내부문서",      3L, false, PostStatus.PUBLISHED, QnaStatus.WAITING);

        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);

        // 검색 조건: keyword = "로그인"
        BoardSearchCond cond = new BoardSearchCond(
                null, "로그인", null, null, null, null
        );

        // when
        BoardListResponseDTO resp = boardService.listFaq(cond, criteria);

        // then
        Assertions.assertNotNull(resp, "응답이 null이면 안 됩니다.");
        Assertions.assertFalse(resp.items().isEmpty(), "FAQ 검색 결과가 비어서는 안 됩니다.");

        boolean containsLogin = false;
        for (var it : resp.items()) {
            // 필터 검증
            Assertions.assertEquals(3L, it.getBoardCategoryId(), "카테고리 ID가 FAQ(3L) 이어야 합니다.");
            Assertions.assertTrue(it.getIsPublic(), "FAQ는 공개글이어야 합니다.");
            Assertions.assertEquals(PostStatus.PUBLISHED, it.getPostStatus(), "FAQ는 PUBLISHED 상태여야 합니다.");

            // 제목 내 '로그인' 포함 검증
            if (it.getTitle().contains("로그인")) {
                containsLogin = true;
            }
        }

        Assertions.assertTrue(containsLogin, "제목에 '로그인'이 포함된 FAQ가 최소 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("QnA 목록: category=2, 공개 & PUBLISHED + QnaStatus 필터")
    void list_qna_with_qnaStatus() {
        // given
        seed("QnA-대기-보임",   2L, true, PostStatus.PUBLISHED, QnaStatus.WAITING);
        seed("QnA-답변-보임",   2L, true, PostStatus.PUBLISHED, QnaStatus.ANSWERED);
        seed("QnA-대기-비공개", 2L, false, PostStatus.PUBLISHED, QnaStatus.WAITING); // 제외

        PageCriteria c = new PageCriteria();
        c.setPage(1); c.setSize(10);

        BoardSearchCond cond = new BoardSearchCond(
                null, null, QnaStatus.WAITING, null, null, null
        );

        // when
        BoardListResponseDTO resp = boardService.listQna(cond, c);

        // then
        Assertions.assertNotNull(resp);
        Assertions.assertTrue(resp.items().size() >= 1);
        resp.items().forEach(it -> {
            Assertions.assertEquals(2L, it.getBoardCategoryId());
            Assertions.assertTrue(it.getIsPublic());
            Assertions.assertEquals(PostStatus.PUBLISHED, it.getPostStatus());
            Assertions.assertEquals(QnaStatus.WAITING, it.getQnaStatus());
        });
    }
}
