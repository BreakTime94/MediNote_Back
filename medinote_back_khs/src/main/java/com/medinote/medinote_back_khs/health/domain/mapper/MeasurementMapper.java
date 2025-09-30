package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")  //mapper 인식
public interface MeasurementMapper {

  // RequestDTO -> Entity
  @Mapping(target = "id", ignore = true)  // PK는 자동 생성
  @Mapping(target = "measuredDate", expression = "java(java.time.LocalDateTime.now())") // 측정일시 자동 세팅
  Measurement toEntity(MeasurementRequestDTO dto);

  // Entity -> ResponseDTO
  MeasurementResponseDTO toResponseDTO(Measurement entity);

  // RequestDTO -> 기존 Entity 업데이트
  void updateFromDto(MeasurementRequestDTO dto, @MappingTarget Measurement entity);
}
