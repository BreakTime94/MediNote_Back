package com.medinote.medinote_back_kys.news.mapper;

import com.medinote.medinote_back_kys.news.domain.dto.*;
import com.medinote.medinote_back_kys.news.domain.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", imports = {Optional.class})
public interface NewsMapper {

    // ===== 저장요청 DTO → Entity =====
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isPublished", constant = "false")
    // contentType / channelTitle 등 SaveRequestDTO에 없는 값은 null (수집 파이프라인에서 채움)
    News toEntity(NewsSaveRequestDTO dto);

    // ===== 응답 매핑 =====

    // 1) 일반 사용자용 리스트 아이템 (필드에 기본값 대상 없음)
    NewsPublicListItemResponseDTO toPublicListItem(News entity);
    List<NewsPublicListItemResponseDTO> toPublicList(List<News> entities);

    // 2) 관리자용 목록 아이템 (필드에 기본값 대상 없음)
    AdminNewsListItemResponseDTO toAdminListItem(News entity);
    List<AdminNewsListItemResponseDTO> toAdminList(List<News> entities);

    // 3) 관리자 노출(발행) 상태 변경 응답 (필드에 기본값 대상 없음)
    AdminNewsPublishResponseDTO toAdminPublishResponse(News entity);

    // 4) 관리자 상세보기 응답 — ★ 여기서만 기본값 주입 ★
    @Mapping(target = "channelTitle",   expression = "java(defaultChannelTitle(entity))")
    @Mapping(target = "subSectionCode", expression = "java(orDefault(entity.getSubSectionCode(), \"N/A\"))")
    @Mapping(target = "serialCode",     expression = "java(orDefault(entity.getSerialCode(), \"N/A\"))")
    AdminNewsDetailResponseDTO toAdminDetail(News entity);

    // (옵션) 수집/등록 완료 응답 — 로깅/백오피스 확인용
    NewsIngestResponseDTO toIngestResponse(News entity, LocalDateTime ingestedAt);

    // ===== 기본값/보조 헬퍼 =====
    /** channel_title -> (없으면) source_name -> (없으면) "헬스경향" */
    default String defaultChannelTitle(News e) {
        return Optional.ofNullable(e.getChannelTitle())
                .orElse(Optional.ofNullable(e.getSourceName()).orElse("헬스경향"));
    }

    /** NULL/빈문자열이면 fallback */
    default String orDefault(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
