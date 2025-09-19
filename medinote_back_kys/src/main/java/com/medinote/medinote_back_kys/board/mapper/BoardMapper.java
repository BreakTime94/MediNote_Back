package com.medinote.medinote_back_kys.board.mapper;

import com.medinote.medinote_back_kys.board.domain.dto.BoardCreateRequestDTO;
import com.medinote.medinote_back_kys.board.domain.entity.Board;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface BoardMapper {

    // 신규 생성: id 등 DTO에 없는 필드는 무시
    @Mapping(target = "id", ignore = true)
    Board toEntity(BoardCreateRequestDTO dto);

    // 부분 갱신(옵션): dto의 null 필드는 건드리지 않음 → 엔티티 기존값 유지
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(BoardCreateRequestDTO dto, @MappingTarget Board entity);
}
