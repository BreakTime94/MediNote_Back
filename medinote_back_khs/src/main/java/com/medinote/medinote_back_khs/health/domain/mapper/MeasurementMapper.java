package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MeasurementMapper {

  // RequestDTO -> Entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "measuredDate", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "status", expression = "java(com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus.ACTIVE)")
  Measurement toEntity(MeasurementRequestDTO dto, Long memberId);

  // Entity -> ResponseDTO
  @Mapping(target = "medications", ignore = true)
  @Mapping(target = "bmi", ignore = true)
  @Mapping(target = "bmiStatus", ignore = true)
  @Mapping(target = "bloodPressureStatus", ignore = true)
  @Mapping(target = "bloodSugarStatus", ignore = true)
  @Mapping(target = "sleepStatus", ignore = true)
  @Mapping(target = "healthScore", ignore = true)
  @Mapping(target = "summary", ignore = true)
  //  주간 트렌드 필드들도 ignore (Service에서 직접 set)
  @Mapping(target = "weightChange", ignore = true)
  @Mapping(target = "weightTrend", ignore = true)
  @Mapping(target = "bloodSugarChange", ignore = true)
  @Mapping(target = "bloodSugarTrend", ignore = true)
  @Mapping(target = "sleepHoursChange", ignore = true)
  @Mapping(target = "sleepTrend", ignore = true)
  @Mapping(target = "allergyIds", ignore = true)
  @Mapping(target = "allergyNames", ignore = true)
  @Mapping(target = "chronicDiseaseIds", ignore = true)
  @Mapping(target = "chronicDiseaseNames", ignore = true)
  @Mapping(target = "medicationIds", ignore = true)
  @Mapping(target = "medicationNames", ignore = true)
  MeasurementResponseDTO toResponseDTO(Measurement entity);

  // RequestDTO -> 기존 Entity 업데이트
  void updateFromDto(MeasurementRequestDTO dto, @MappingTarget Measurement entity);

  @AfterMapping
  default void handleDrinking(MeasurementRequestDTO dto, @MappingTarget Measurement entity) {
    if (!dto.isDrinking()) {
      entity.setDrinkingPerWeek(null);
      entity.setDrinkingPerOnce(null);
    }
  }
}