package com.medinote.medinote_back_kys.reply;

import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import com.medinote.medinote_back_kys.common.paging.PageResponse;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyAdminListResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyCreateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyResponseDTO;
import com.medinote.medinote_back_kys.reply.domain.dto.ReplyUpdateRequestDTO;
import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import com.medinote.medinote_back_kys.reply.domain.entity.Reply;
import com.medinote.medinote_back_kys.reply.mapper.ReplyMapper;
import com.medinote.medinote_back_kys.reply.repository.ReplyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 각 테스트 실행 후 롤백
public class ReplyRepositoryTest {

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private ReplyMapper replyMapper;

    private static final Long TEST_MEMBER_ID = 1L;   // 실제 DB에 존재하는 member PK
    private static final Long TEST_LINK_ID   = 10L;  // 실제 DB에 존재하는 게시글/뉴스 PK

    @Test
    @DisplayName("댓글 등록 및 조회 테스트")
    void createAndFindReply() {
        // given
        ReplyCreateRequestDTO dto = ReplyCreateRequestDTO.builder()
                .memberId(TEST_MEMBER_ID)
                .linkId(TEST_LINK_ID)
                .linkType(ReplyTargetType.BOARD) // ✅ enum 직접 사용
                .content("테스트 댓글입니다.")
                .isPublic(true)
                .title("테스트 제목")
                .build();

        // when: DTO → Entity 매핑 후 저장
        Reply entity = replyMapper.toEntity(dto);
        Reply saved = replyRepository.save(entity);

        // then
        assertNotNull(saved.getId());
        assertEquals("테스트 댓글입니다.", saved.getContent());
        assertEquals(ReplyTargetType.BOARD, saved.getLinkType());
        assertTrue(saved.getIsPublic());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    void updateReply() {
        // given: 댓글 등록
        Reply reply = replyRepository.save(Reply.builder()
                .linkType(ReplyTargetType.BOARD) // ✅ enum 직접 사용
                .linkId(TEST_LINK_ID)
                .memberId(TEST_MEMBER_ID)
                .content("원본 내용")
                .isPublic(true)
                .title("원본 제목")
                .build());

        // when: Update DTO를 이용하여 엔티티 수정 메서드 호출
        ReplyUpdateRequestDTO updateDto = ReplyUpdateRequestDTO.builder()
                .replyId(reply.getId())
                .content("수정된 내용")
                .title("수정된 제목")
                .isPublic(false)
                .build();

        reply.changeContent(updateDto.content());
        reply.changeTitle(updateDto.title());
        reply.changeVisibility(updateDto.isPublic());

        Reply updated = replyRepository.save(reply);

        // then
        assertEquals("수정된 내용", updated.getContent());
        assertEquals("수정된 제목", updated.getTitle());
        assertFalse(updated.getIsPublic());
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteReply() {
        // given: 댓글 등록
        Reply reply = replyRepository.save(Reply.builder()
                .linkType(ReplyTargetType.BOARD) // ✅ enum 직접 사용
                .linkId(TEST_LINK_ID)
                .memberId(TEST_MEMBER_ID)
                .content("삭제 대상 댓글")
                .isPublic(true)
                .build());

        Long id = reply.getId();
        assertNotNull(id);

        // when: 삭제
        replyRepository.deleteById(id);

        // then: 조회 시 존재하지 않아야 함
        Optional<Reply> deleted = replyRepository.findById(id);
        assertTrue(deleted.isEmpty(), "댓글이 삭제되어야 합니다.");
    }

    @Test
    @DisplayName("Mapper로 엔티티 → DTO 변환 테스트")
    void entityToDtoTest() {
        // given: 댓글 등록
        Reply reply = replyRepository.save(Reply.builder()
                .linkType(ReplyTargetType.BOARD) // ✅ enum 직접 사용
                .linkId(TEST_LINK_ID)
                .memberId(TEST_MEMBER_ID)
                .content("매퍼 테스트 댓글")
                .isPublic(true)
                .title("매퍼 테스트 제목")
                .build());

        // when: 엔티티 → DTO 변환
        ReplyResponseDTO dto = replyMapper.toResponseDto(reply);

        // then
        assertEquals(reply.getId(), dto.id());
        assertEquals(reply.getContent(), dto.content());
        assertEquals(reply.getLinkType(), dto.linkType());
        assertEquals(reply.getMemberId(), dto.memberId());
    }

    @Test
    @DisplayName("관리자용 댓글 목록 조회 테스트")
    void adminListReplies() {
        // given: 댓글 2개 저장
        Reply reply1 = replyRepository.save(Reply.builder()
                .linkType(ReplyTargetType.BOARD)
                .linkId(TEST_LINK_ID)
                .memberId(TEST_MEMBER_ID)
                .content("관리자 댓글1")
                .isPublic(true)
                .title("댓글1 제목")
                .build());

        Reply reply2 = replyRepository.save(Reply.builder()
                .linkType(ReplyTargetType.BOARD)
                .linkId(TEST_LINK_ID)
                .memberId(TEST_MEMBER_ID)
                .content("관리자 댓글2")
                .isPublic(false)
                .title("댓글2 제목")
                .build());

        // when: 관리자용 DTO 리스트 변환
        var replies = replyRepository.findByLinkTypeAndLinkIdOrderByIdAsc(
                ReplyTargetType.BOARD, TEST_LINK_ID);

        var dtoList = replies.stream()
                .map(replyMapper::toAdminResponseDto)
                .toList();

        // then
        assertEquals(2, dtoList.size(), "저장된 댓글 개수가 일치해야 합니다.");

        ReplyAdminListResponseDTO dto1 = dtoList.get(0);
        ReplyAdminListResponseDTO dto2 = dtoList.get(1);

        assertEquals(reply1.getId(), dto1.id());
        assertEquals(reply1.getContent(), dto1.content());
        assertEquals(reply1.getTitle(), dto1.title());
        assertNull(dto1.memberNickname(), "memberNickname은 아직 null이어야 합니다.");

        assertEquals(reply2.getId(), dto2.id());
        assertEquals(reply2.getContent(), dto2.content());
        assertEquals(reply2.getTitle(), dto2.title());
        assertNull(dto2.memberNickname(), "memberNickname은 아직 null이어야 합니다.");
    }

    @Test
    @DisplayName("관리자용 댓글 목록 페이징 조회 테스트")
    void adminListRepliesWithPaging() {
        // given: 댓글 15개 저장
        for (int i = 1; i <= 15; i++) {
            replyRepository.save(Reply.builder()
                    .linkType(ReplyTargetType.BOARD)
                    .linkId(TEST_LINK_ID)
                    .memberId(TEST_MEMBER_ID)
                    .content("페이징 테스트 댓글 " + i)
                    .isPublic(i % 2 == 0) // 짝수만 공개
                    .title("댓글 제목 " + i)
                    .build());
        }

        // PageCriteria 설정
        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);   // 1페이지
        criteria.setSize(10);  // 10개씩

        // 정렬 화이트리스트
        var whitelist = Map.of(
                "id", "id",
                "regDate", "regDate",
                "modDate", "modDate"
        );

        Pageable pageable = criteria.toPageable(whitelist);

        // when: Page 조회
        Page<Reply> page = replyRepository.findAll(pageable);

        List<ReplyAdminListResponseDTO> dtoList = page.getContent()
                .stream()
                .map(replyMapper::toAdminResponseDto)
                .toList();

        PageResponse<ReplyAdminListResponseDTO> response = new PageResponse<>(
                dtoList,
                criteria.toPageMeta(page.getTotalElements()),
                criteria.getSort(),
                null,                 // keyword 없음
                criteria.getFilters()
        );

        // then
        assertEquals(10, response.content().size(), "첫 페이지에는 10개의 댓글이 있어야 합니다.");
        assertEquals(15, response.page().totalElements(), "총 댓글 개수는 15개여야 합니다.");
        assertEquals(2, response.page().totalPages(), "총 페이지 수는 2여야 합니다.");
        assertEquals(1, response.page().firstPage(), "첫 페이지는 항상 1이어야 합니다.");
        assertEquals(2, response.page().lastPage(), "마지막 페이지는 2여야 합니다.");
        assertEquals(List.of("id,desc"), response.sorted(), "정렬 조건이 그대로 echo 되어야 합니다.");
    }

    @Test
    @DisplayName("특정 대상(Board)에 속한 댓글 페이징 조회 테스트")
    void adminListRepliesWithTargetPaging() {
        // given: 특정 linkId 에 댓글 15개 저장
        for (int i = 1; i <= 15; i++) {
            replyRepository.save(Reply.builder()
                    .linkType(ReplyTargetType.BOARD)
                    .linkId(TEST_LINK_ID)
                    .memberId(TEST_MEMBER_ID)
                    .content("타겟 페이징 댓글 " + i)
                    .isPublic(true)
                    .title("댓글 제목 " + i)
                    .build());
        }

        // PageCriteria 설정 (1페이지, size=10)
        PageCriteria criteria = new PageCriteria();
        criteria.setPage(1);
        criteria.setSize(10);

        // 정렬 화이트리스트
        var whitelist = Map.of(
                "id", "id",
                "regDate", "regDate",
                "modDate", "modDate"
        );

        Pageable pageable = criteria.toPageable(whitelist);

        // when: 특정 대상에 대한 Page 조회
        Page<Reply> page = replyRepository.findByLinkTypeAndLinkId(
                ReplyTargetType.BOARD, TEST_LINK_ID, pageable);

        List<ReplyAdminListResponseDTO> dtoList = page.getContent()
                .stream()
                .map(replyMapper::toAdminResponseDto)
                .toList();

        PageResponse<ReplyAdminListResponseDTO> response = new PageResponse<>(
                dtoList,
                criteria.toPageMeta(page.getTotalElements()),
                criteria.getSort(),
                null,
                criteria.getFilters()
        );

        // then
        assertEquals(10, response.content().size(), "첫 페이지에는 10개의 댓글이 있어야 합니다.");
        assertEquals(15, response.page().totalElements(), "총 댓글 개수는 15개여야 합니다.");
        assertEquals(2, response.page().totalPages(), "총 페이지 수는 2여야 합니다.");
        assertEquals(1, response.page().firstPage(), "첫 페이지는 1이어야 합니다.");
        assertEquals(2, response.page().lastPage(), "마지막 페이지는 2여야 합니다.");
    }
}