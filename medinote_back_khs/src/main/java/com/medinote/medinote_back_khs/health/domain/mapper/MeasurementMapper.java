package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")  //mapper 인식
public interface MeasurementMapper {

  // RequestDTO -> Entity
  @Mapping(target = "id", ignore = true)  // PK는 자동 생성
  @Mapping(target = "measuredDate", expression = "java(java.time.LocalDateTime.now())") // 측정일시 자동 세팅
  @Mapping(target = "memberId", source = "memberId") // 헤더 값 → Entity
  @Mapping(target = "status", expression = "java(com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus.ACTIVE)") // 기본 ACTIVE
  Measurement toEntity(MeasurementRequestDTO dto, Long memberId);

  // Entity -> ResponseDTO
  // medications 필드는 Service에서 따로 세팅할 거니까 무시 처리
  @Mapping(target = "medications", ignore = true)
  MeasurementResponseDTO toResponseDTO(Measurement entity);

  // RequestDTO -> 기존 Entity 업데이트
  void updateFromDto(MeasurementRequestDTO dto, @MappingTarget Measurement entity);

  @AfterMapping
  default void handleDrinking(MeasurementRequestDTO dto, @MappingTarget Measurement entity) {
    if (! dto.isDrinking()) {
      entity.setDrinkingPerWeek(null);
      entity.setDrinkingPerOnce(null);
    }
  }
}
