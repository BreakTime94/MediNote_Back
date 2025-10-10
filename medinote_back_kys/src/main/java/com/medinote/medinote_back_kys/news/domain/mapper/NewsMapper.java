package com.medinote.medinote_back_kys.news.domain.mapper;

import com.medinote.medinote_back_kys.news.domain.dto.NewsResponseDTO;
import com.medinote.medinote_back_kys.news.domain.dto.NewsSaveRequestDTO;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    // 저장요청 DTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isPublished", constant = "false")
    News toEntity(NewsSaveRequestDTO dto);

    // Entity → 응답 DTO
    NewsResponseDTO toResponseDTO(News entity);
}
