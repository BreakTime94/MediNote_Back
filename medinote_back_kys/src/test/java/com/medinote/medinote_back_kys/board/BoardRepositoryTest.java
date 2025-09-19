package com.medinote.medinote_back_kys.board;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
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
        // 테스트에서 Security 자동설정 제외(임시계정/비밀번호 메시지 억제)
        properties = {
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
    private EntityManager entityManager; // ✅ JPA 엔티티 매니저 주입

    @Test
    @Order(1)
    @DisplayName("Board 생성: DTO null → 엔티티 @Builder.Default & DB DEFAULT 유지 확인")
    @Transactional // 테스트 트랜잭션 (기본 롤백)
    void createBoard_withDefaults_success() {
        // given: 일부 필드는 null로 둬서 기본값 경로를 타게 함
        BoardCreateRequestDTO dto = BoardCreateRequestDTO.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("hello")
                .content("content")
                // isPublic, requireAdminPost, qnaStatus, postStatus intentionally null
                .build();

        // when
        Board entity = boardMapper.toEntity(dto);    // MapStruct: null 무시 → 엔티티 기본값 유지
        Board saved  = boardRepository.save(entity);

        // DB가 채운 DEFAULT를 검증하려면 반드시 flush/clear 후 재조회
        entityManager.flush();
        entityManager.clear();

        Board found = boardRepository.findById(saved.getId()).orElseThrow();

        // then (Boolean은 null-safe 비교)
        Assertions.assertAll(
                () -> Assertions.assertEquals(Boolean.TRUE,  found.getIsPublic()),
                () -> Assertions.assertEquals(Boolean.FALSE, found.getRequireAdminPost()),
                // NOTE: post_status 기본값은 현재 DB='PUBLISHED', 엔티티 빌더='DRAFT'로 불일치.
                // 둘 중 하나로 통일한 뒤 아래 단언을 해주세요.
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, found.getPostStatus()),
                () -> Assertions.assertEquals(QnaStatus.WAITING,   found.getQnaStatus())
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
                .postStatus(PostStatus.PUBLISHED)  // default(DRAFT) 덮어쓰기
                .build();

        // when
        Board saved = boardRepository.save(boardMapper.toEntity(dto));

        // then (영속성 컨텍스트 내 값 검증: DTO가 덮어쓴 값이 그대로 있어야 함)
        Assertions.assertAll(
                () -> Assertions.assertNotNull(saved.getId()),
                () -> Assertions.assertFalse(saved.getIsPublic()),
                () -> Assertions.assertTrue(saved.getRequireAdminPost()),
                () -> Assertions.assertEquals(QnaStatus.ANSWERED,  saved.getQnaStatus()),
                () -> Assertions.assertEquals(PostStatus.PUBLISHED, saved.getPostStatus())
        );
    }

    @Test
    @DisplayName("Board 단순 삽입 테스트")
    @Transactional
    void insertBoard() {
        // given
        Board board = Board.builder()
                .memberId(1L)           // 존재하는 member_id
                .boardCategoryId(2L)    // 존재하는 category_id
                .title("단순 삽입 테스트 제목")
                .content("단순 삽입 테스트 내용")
                .build();

        // when
        Board saved = boardRepository.save(board);

        // then
        Assertions.assertNotNull(saved.getId());
        System.out.println("==== 삽입된 Board ID: " + saved.getId() + " ====");
    }
}