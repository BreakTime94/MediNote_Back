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
                () -> Assertions.assertEquals(Boolean.TRUE, found.getIsPublic()),
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

    @Test
    @Order(5)
    @DisplayName("목록 조회: Criteria → Pageable → Page → BoardListResponseDTO 매핑 검증")
    @Transactional
    void listBoards_pagination_and_mapping_success() {
        // given: 25건 심기 (정렬/페이지 검증용)
        for (int i = 1; i <= 25; i++) {
            Board b = Board.builder()
                    .memberId(1L)
                    .boardCategoryId(2L)
                    .title("목록테스트-" + i)
                    .content("내용-" + i)
                    // isPublic/requireAdminPost/qnaStatus/postStatus 는 엔티티 기본값 사용
                    .build();
            boardRepository.save(b);
        }

        // Criteria: 2페이지, size=10, 정렬: id,desc
        var c = new com.medinote.medinote_back_kys.common.paging.Criteria();
        c.setPage(2);
        c.setSize(10);
        c.setSort(java.util.List.of("id,desc")); // 네가 만든 검증 파이프 그대로 사용
        c.setKeyword("목록테스트");               // 굳이 필터 안 써도 keyword echo만 확인

        // 화이트리스트: 클라이언트 필드명 → 실제 컬럼/프로퍼티명
        var whitelist = java.util.Map.of(
                "id", "id",
                "title", "title",
                "regDate", "regDate",
                "postStatus", "postStatus",
                "qnaStatus", "qnaStatus"
        );

        var pageable = c.toPageable(whitelist);

        // when: 단순 전체 조회로 Page 얻기 (커스텀 검색 메서드 없으면 findAll로 충분)
        var page = boardRepository.findAll(pageable);

        // 매퍼로 DTO 변환
        var dto = boardMapper.toListResponse(page, c);

        // then
        // 1) 페이지 메타
        Assertions.assertAll(
                () -> Assertions.assertEquals(28L, dto.getPage().getTotalElements()),
                () -> Assertions.assertEquals(10, dto.getPage().getSize()),
                () -> Assertions.assertEquals(3, dto.getPage().getTotalPages()), // 25 / 10 => 3
                () -> Assertions.assertEquals(2, dto.getPage().getPage())        // 요청한 현재 페이지(2)
        );

        // 2) 아이템 수: 2페이지면 10건
        Assertions.assertEquals(10, dto.getItems().size());

        // 3) 정렬(id desc) 확인: 첫 아이템 id > 마지막 아이템 id
        var firstId = dto.getItems().get(0).getId();
        var lastId = dto.getItems().get(dto.getItems().size() - 1).getId();
        Assertions.assertTrue(firstId > lastId, "정렬(id desc) 실패: " + firstId + " <= " + lastId);

        // 4) 일부 필드 매핑 sanity check
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

        // 5) keyword echo
        Assertions.assertEquals("목록테스트", dto.getKeyword());
    }

    @Test
    @Order(6)
    @DisplayName("단일 조회: Repository → Entity → Mapper → BoardDetailResponseDTO 매핑 성공")
    @Transactional
    void findOne_and_map_to_detail_dto_success() {
        // given: 명시적 값으로 엔티티 저장
        Board seed = Board.builder()
                .memberId(1L)
                .boardCategoryId(2L)
                .title("단일조회-제목")
                .content("단일조회-본문")
                .isPublic(false)
                .requireAdminPost(true)
                .qnaStatus(QnaStatus.ANSWERED)
                .postStatus(PostStatus.DRAFT)
                .build();

        Board saved = boardRepository.save(seed);

        // 영속성 컨텍스트 비우고 실제 DB에서 다시 조회해 매핑 검증
        entityManager.flush();
        entityManager.clear();

        // when: 단일 조회 후 매퍼로 DTO 변환
        Board found = boardRepository.findById(saved.getId()).orElseThrow();
        var dto = boardMapper.toDetailResponse(found);

        // then: 주요 필드 매핑 검증
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
                () -> Assertions.assertNotNull(dto.getRegDate(), "regDate는 null이 아니어야 합니다."),
                () -> Assertions.assertNotNull(dto.getModDate(), "modDate는 null이 아니어야 합니다.")
        );
    }

    @Test
    @Order(7)
    @DisplayName("단일 조회: 존재하지 않는 ID는 Optional.empty() 반환")
    void findOne_not_found_returns_empty_optional() {
        // given
        long notExistId = 9_999_999L;

        // when
        var opt = boardRepository.findById(notExistId);

        // then
        Assertions.assertTrue(opt.isEmpty(), "존재하지 않는 ID는 Optional.empty()여야 합니다.");
    }
}