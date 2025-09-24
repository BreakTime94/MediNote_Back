package com.medinote.medinote_back_kys.board.mapper;

import com.medinote.medinote_back_kys.board.domain.dto.*;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import com.medinote.medinote_back_kys.common.paging.Criteria;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    /** Create DTO → Entity */
    Board toEntity(BoardCreateRequestDTO dto);

    /** Entity → Create DTO (필요 시) */
    BoardCreateRequestDTO toCreateDTO(Board entity);

    /** Update DTO → Entity (id 포함) */
    @Mapping(target = "id", source = "id")
    Board toEntity(BoardUpdateRequestDTO dto);

    /** Entity → Update DTO (필요 시) */
    BoardUpdateRequestDTO toUpdateDTO(Board entity);

    /**
     * 부분 수정 (Patch 성격)
     * null 값은 무시하고 기존 엔티티에 덮어씀
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BoardUpdateRequestDTO dto, @MappingTarget Board entity);


    // 목록 매핑
    BoardListItemDTO toListItem(Board entity);

    // @IterableMapping의 qualifiedByName는 쓰지 말자(지금 네임드 없음). 단순 리스트 변환이면 이걸로 충분.
    List<BoardListItemDTO> toListItems(List<Board> entities);

    default BoardListResponseDTO toListResponse(org.springframework.data.domain.Page<Board> page,
                                                com.medinote.medinote_back_kys.common.paging.Criteria c) {
        var items = toListItems(page.getContent());
        return BoardListResponseDTO.builder()
                .items(items)
                .page(c.toPageMeta(page.getTotalElements()))
                .keyword(c.getKeyword())
                .build();
    }

    //단일조회
    BoardDetailResponseDTO toDetailResponse(Board entity);
}
