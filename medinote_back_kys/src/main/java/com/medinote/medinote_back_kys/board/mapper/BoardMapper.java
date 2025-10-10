package com.medinote.medinote_back_kys.board.mapper;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.en.PostStatus;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.common.paging.PageCriteria;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BoardMapper {

    // === Create ===
    /**
     * Create DTO → Entity
     * MapStruct는 팩토리 호출을 직접 못 하므로 default 메서드에서 처리.
     */
    default Board toEntity(BoardCreateRequestDTO dto) {
        return Board.create(
                dto.memberId(),
                dto.boardCategoryId(),
                dto.title(),
                dto.content(),
                dto.requireAdminPost(),
                dto.isPublic(),
                dto.qnaStatus(),
                dto.postStatus()
        );
    }

    /** Entity → Create DTO (보통 잘 안 쓰지만 필요 시) */
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "boardCategoryId", source = "boardCategoryId")
    BoardCreateRequestDTO toCreateDTO(Board entity);


    // === Update ===
    /**
     * 부분 수정(Patch).
     * MapStruct의 자동 매핑은 세터 기반이라 동작하지 않으므로,
     * @AfterMapping 훅에서 엔티티의 도메인 메서드를 직접 호출.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BoardUpdateRequestDTO dto, @MappingTarget Board entity);

    @AfterMapping
    default void applyUpdates(BoardUpdateRequestDTO dto, @MappingTarget Board entity) {
        if (dto.title() != null) entity.changeTitle(dto.title());
        if (dto.content() != null) entity.changeContent(dto.content());
        if (dto.isPublic() != null) entity.changeVisibility(dto.isPublic());
        if (dto.requireAdminPost() != null) entity.changeRequireAdminPost(dto.requireAdminPost());
        if (dto.qnaStatus() != null) entity.changeQnaStatus(dto.qnaStatus());
        if (dto.postStatus() != null) entity.changePostStatus(dto.postStatus());
        if (dto.boardCategoryId() != null) entity.changeRequireAdminPost(dto.requireAdminPost());
    }

    /**
     * Entity → Update DTO (필요 시)
     */
    BoardUpdateRequestDTO toUpdateDTO(Board entity);


    // === 목록 조회 ===

    /** 단일 Entity → 목록 아이템 DTO */
    BoardListItemDTO toListItem(Board entity);

    /** Entity 리스트 → 목록 아이템 DTO 리스트 */
    List<BoardListItemDTO> toListItems(List<Board> entities);

    /**
     * Page<Board> → BoardListResponseDTO 변환
     * @param page     JPA Page 결과
     * @param criteria 페이징 요청 DTO
     * @param keyword  검색어(조건 DTO에서 전달)
     */
    default BoardListResponseDTO toListResponse(Page<Board> page,
                                                PageCriteria criteria,
                                                String keyword) {
        var items = toListItems(page.getContent());
        return new BoardListResponseDTO(
                items,
                criteria.toPageMeta(page.getTotalElements()),
                keyword
        );
    }



    // === 단일 조회 ===
    BoardDetailResponseDTO toDetailResponse(Board entity);


    /** 삭제 요청 적용: 소프트 삭제 (세터 없이 도메인 메서드만 사용) */
    default void deleteEntityFromDto(BoardDeleteRequestDTO dto, @MappingTarget Board entity) {
        entity.changeVisibility(false);
        entity.changePostStatus(PostStatus.DELETED);
        // 필요하면 감사 로그/메모 처리 등을 여기서 함께 수행 가능
        // requesterId, reason은 엔티티 필드가 없으니 처리 대상 아님
    }
}