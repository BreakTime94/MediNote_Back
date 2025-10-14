package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.HealthGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HealthGoalMapper {

  // Entity -> ResponseDTO
  HealthGoalResponseDTO toResponseDTO(HealthGoal healthGoal);

  // RequestDTO -> Entity (신규 생성 시)
  @Mapping(target = "regDate", ignore = true)
  @Mapping(target = "modDate", ignore = true)
  HealthGoal HealthGoalControllertoEntity(HealthGoalRequestDTO requestDTO);

  // RequestDTO로 기존 Entity 업데이트
  @Mapping(target = "regDate", ignore = true)
  @Mapping(target = "modDate", ignore = true)
  void updateEntityFromDTO(HealthGoalRequestDTO requestDTO, @MappingTarget HealthGoal healthGoal);
}