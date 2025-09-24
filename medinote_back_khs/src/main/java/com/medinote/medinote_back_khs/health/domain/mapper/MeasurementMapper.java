package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")  //mapper 인식
public interface MeasurementMapper {

  //dto -> entity
  @Mapping(target = "id", ignore = true)  //특정 필드 변환 방법 지정할 때 사용
  @Mapping(target = "measuredDate" , expression = "java(java.time.LocalDateTime.now())" ) //측정일시 자동 저장
  Measurement toEntity(MeasurementRequestDTO dto);

  //entity -> dto
  MeasurementRequestDTO toDTO(Measurement entity);

  void updateFromDto(MeasurementRequestDTO dto, @MappingTarget Measurement entity);
}
