package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.Period;

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
  @Mapping(target = "ageGroup", ignore = true)
  MeasurementResponseDTO toResponseDTO(Measurement entity);

  // RequestDTO -> 기존 Entity 업데이트
  void updateFromDto(MeasurementRequestDTO dto, @MappingTarget Measurement entity);


  @AfterMapping //음주 주종 관련
  default void handleDrinking(MeasurementRequestDTO dto, @MappingTarget Measurement entity) {
    if (!dto.isDrinking()) {
      entity.setDrinkingPerWeek(null);
      entity.setDrinkingPerOnce(null);
      entity.setDrinkingType(null);
      entity.setDrinkingUnit(null);
    }
  }

  @AfterMapping //연령대 자동 계산용
  default void handleAgeGroup(Measurement entity, @MappingTarget MeasurementResponseDTO dto) {
    LocalDate birthDate = entity.getBirthDate();
    if (birthDate == null) {
      dto.setAgeGroup("정보 없음");
      return;
    }

    int age = Period.between(birthDate, LocalDate.now()).getYears();

    if (age < 10) dto.setAgeGroup("10세 미만");
    else if (age < 20) dto.setAgeGroup("10대");
    else if (age < 30) dto.setAgeGroup("20대");
    else if (age < 40) dto.setAgeGroup("30대");
    else if (age < 50) dto.setAgeGroup("40대");
    else if (age < 60) dto.setAgeGroup("50대");
    else if (age < 70) dto.setAgeGroup("60대");
    else dto.setAgeGroup("70대 이상");
  }
}