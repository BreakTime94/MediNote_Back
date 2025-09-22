package com.medinote.medinote_back_kys.board.mapper;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.dto.BoardUpdateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import org.mapstruct.*;

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
}
