package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.en.QnaStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.board.mapper.BoardMapper;
import com.medinote.medinote_back_kys.board.repository.BoardRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                // Security 자동설정 제외 (테스트 시 불필요한 계정 메시지 제거)
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
        }
)
@TestMethodOrder(OrderAnnotation.class)
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Order(1)
    @DisplayName("Board 생성: DTO null → 엔티티 기본값 & DB DEFAULT 확인")
    @Transactional
    void createBoard_withDefaults_success() {
        // given
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("hello")
                .content("content")
                // 나머지 필드 null → 엔티티의 @Builder.Default 또는 DB default 사용
                .build();

        // when
        Board entity = boardMapper.toEntity(dto);
        Board saved = boardRepository.save(entity);

        // flush & clear 후 재조회 (DB default 확인)
        entityManager.flush();
        entityManager.clear();
        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(Boolean.TRUE,  found.getIsPublic()),
                () -> Assertions.assertEquals(Boolean.FALSE, found.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.WAITING, found.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, found.getPostStatus()) // DB/엔티티 default 통일 필요
        );
    }

    @Test
    @Order(2)
    @DisplayName("Board 생성: DTO 지정값으로 기본값 덮어쓰기")
    @Transactional
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
                .postStatus(PostStatus.PUBLISHED)  // default(DRAFT or PUBLISHED 중 택1)
                .build();

        // when
        Board saved = boardRepository.save(boardMapper.toEntity(dto));

        // then
        Assertions.assertAll(
                () -> Assertions.assertNotNull(saved.getId()),
                () -> Assertions.assertFalse(saved.getIsPublic()),
                () -> Assertions.assertTrue(saved.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, saved.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus())
        );
    }

    @Test
    @Order(3)
    @DisplayName("Board 단순 삽입 테스트 (Entity 직접 사용)")
    @Transactional
    void insertBoard_entityDirect_success() {
        // given
        Board board = Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("단순 삽입 테스트 제목")
                .content("단순 삽입 테스트 내용")
                .build();

        // when
        Board saved = boardRepository.save(board);

        // then
        Assertions.assertNotNull(saved.getId());
        System.out.println("==== 삽입된 Board ID: " + saved.getId() + " ====");
    }

    @Test
    @Order(4)
    @DisplayName("Board 수정: Update DTO 적용 후 반영 확인")
    @Transactional
    void updateBoard_success() {
        // given: 기존 데이터(id=10)가 존재한다고 가정
        BoardUpdateRequestDTO updateDto = BoardUpdateRequestDTO.builder()
                .id(10L)                          // 수정 대상 게시글 번호
                .boardCategoryId(2L)             // 카테고리 유지
                .title("수정된 제목")
                .content("수정된 내용")
                .isPublic(false)                 // 공개 여부 변경
                .requireAdminPost(true)          // 관리자 승인 필요로 변경
                .qnaStatus(QnaStatus.ANSWERED)   // 상태 변경
                .postStatus(PostStatus.DRAFT)    // 임시저장 상태로 변경
                .build();

        // when: 기존 엔티티 조회 후 DTO 값 반영
        Board found = boardRepository.findById(updateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // BoardMapper 활용해 부분 수정
        boardMapper.updateEntityFromDto(updateDto, found);

        // 저장
        Board updated = boardRepository.save(found);

        // flush & clear 후 재조회
        entityManager.flush();
        entityManager.clear();
        Board reloaded = boardRepository.findById(10L).orElseThrow();

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals("수정된 제목", reloaded.getTitle()),
                () -> Assertions.assertEquals("수정된 내용", reloaded.getContent()),
                () -> Assertions.assertFalse(reloaded.getIsPublic()),
                () -> Assertions.assertTrue(reloaded.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED, reloaded.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.DRAFT, reloaded.getPostStatus())
        );
    }


}