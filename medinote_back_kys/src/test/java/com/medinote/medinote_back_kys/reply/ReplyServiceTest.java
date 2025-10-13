package com.medinote.medinote_back_kys.reply;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.common.paging.PageResponse;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyAdminListResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyCreateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyUpdateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import com.medinote.medinote_back_kys.reply.service.ReplyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;

    private static final Long TEST_MEMBER_ID = 1L;   // 실제 DB에 존재하는 member PK
    private static final Long TEST_LINK_ID   = 10L;  // 실제 DB에 존재하는 게시글/뉴스 PK

    @Test
    @DisplayName("댓글 등록 및 단일 조회 테스트")
    void createAndFindReply() {
        // given
        ReplyCreateRequestDTO dto = ReplyCreateRequestDTO.builder()
                .memberId(TEST_MEMBER_ID)
                .linkId(TEST_LINK_ID)
                .linkType(ReplyTargetType.BOARD)
                .content("서비스 계층 댓글 테스트")
                .isPublic(true)
                .title("서비스 테스트 제목")
                .build();

        // when
        ReplyResponseDTO saved = replyService.create(dto);
        ReplyResponseDTO found = replyService.getOne(saved.id());

        // then
        assertNotNull(saved.id());
        assertEquals("서비스 계층 댓글 테스트", found.content());
        assertEquals(ReplyTargetType.BOARD, found.linkType());
        assertTrue(found.isPublic());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateReply() {
        // given: 댓글 생성
        ReplyCreateRequestDTO createDto = ReplyCreateRequestDTO.builder()
                .memberId(TEST_MEMBER_ID)
                .linkId(TEST_LINK_ID)
                .linkType(ReplyTargetType.BOARD)
                .content("원본 서비스 댓글")
                .isPublic(true)
                .title("원본 제목")
                .build();

        ReplyResponseDTO created = replyService.create(createDto);

        // when: 수정 요청
        ReplyUpdateRequestDTO updateDto = ReplyUpdateRequestDTO.builder()
                .replyId(created.id())
                .content("수정된 서비스 댓글")
                .title("수정된 제목")
                .isPublic(false)
                .build();

        ReplyResponseDTO updated = replyService.update(updateDto);

        // then
        assertEquals("수정된 서비스 댓글", updated.content());
        assertEquals("수정된 제목", updated.title());
        assertFalse(updated.isPublic());
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteReply() {
        // given: 댓글 생성
        ReplyCreateRequestDTO dto = ReplyCreateRequestDTO.builder()
                .memberId(TEST_MEMBER_ID)
                .linkId(TEST_LINK_ID)
                .linkType(ReplyTargetType.BOARD)
                .content("삭제 대상 댓글")
                .isPublic(true)
                .title("삭제 제목")
                .build();

        ReplyResponseDTO created = replyService.create(dto);
        Long replyId = created.id();

        // when: 삭제
        replyService.delete(replyId);

        // then: 조회 시 예외 발생
        assertThrows(IllegalArgumentException.class,
                () -> replyService.getOne(replyId),
                "삭제된 댓글은 조회할 수 없어야 합니다.");
    }

    @Test
    @DisplayName("관리자용 전체 댓글 목록 페이징 조회 테스트")
    void adminListRepliesWithPaging() {
        // given: 댓글 15개 생성
        for (int i = 1; i <= 15; i++) {
            replyService.create(ReplyCreateRequestDTO.builder()
                    .memberId(TEST_MEMBER_ID)
                    .linkId(TEST_LINK_ID)
                    .linkType(ReplyTargetType.BOARD)
                    .content("페이징 댓글 " + i)
                    .isPublic(i % 2 == 0)
                    .title("댓글 제목 " + i)
                    .build());
        }

        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);

        // when
        PageResponse<ReplyAdminListResponseDTO> response = replyService.getAdminList(criteria);

        // then
        assertEquals(10, response.content().size(), "첫 페이지에는 10개의 댓글이 있어야 합니다.");
        assertEquals(15, response.page().totalElements(), "총 댓글 개수는 15개여야 합니다.");
        assertEquals(2, response.page().totalPages(), "총 페이지 수는 2여야 합니다.");
    }

    @Test
    @DisplayName("특정 대상(Board)에 속한 댓글 페이징 조회 테스트")
    void listRepliesByTargetPaging() {
        // given: 특정 linkId 에 댓글 15개 생성
        for (int i = 1; i <= 15; i++) {
            replyService.create(ReplyCreateRequestDTO.builder()
                    .memberId(TEST_MEMBER_ID)
                    .linkId(TEST_LINK_ID)
                    .linkType(ReplyTargetType.BOARD)
                    .content("타겟 댓글 " + i)
                    .isPublic(true)
                    .title("타겟 댓글 제목 " + i)
                    .build());
        }

        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);

        // when
        PageResponse<ReplyAdminListResponseDTO> response =
                replyService.getListByTarget(ReplyTargetType.BOARD, TEST_LINK_ID, criteria);

        // then
        assertEquals(10, response.content().size(), "첫 페이지에는 10개의 댓글이 있어야 합니다.");
        assertEquals(15, response.page().totalElements(), "총 댓글 개수는 15개여야 합니다.");
        assertEquals(2, response.page().totalPages(), "총 페이지 수는 2여야 합니다.");
    }
}
