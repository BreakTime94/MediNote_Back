package com.medinote.medinote_back_khs.health.domain.mapper;


import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMedicationMapper {

  //requestdto -> entity
  MemberMedication toEntity(MedicationRequestDTO dto);

  // 단일 Entity -> DTO
  @Mapping(source = "id", target = "id")
  @Mapping(source = "memberId", target = "memberId")
  @Mapping(source = "medicationId", target = "medicationId")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "endDate", target = "endDate")
  MedicationResponseDTO toResponseDTO(MemberMedication entity);

  //list<entity> -> list<dto>
  List<MedicationResponseDTO> toResponseDTO(List<MemberMedication> listEntity);

  // MemberMedication + Medication -> ResponseDTO
  @Mapping(source = "memberMedication.id", target = "id")
  @Mapping(source = "memberMedication.memberId", target = "memberId")
  @Mapping(source = "memberMedication.startDate", target = "startDate")
  @Mapping(source = "memberMedication.endDate", target = "endDate")
  @Mapping(source = "medication.id", target = "medicationId")
  @Mapping(source = "medication.drugCode", target = "drugCode")
  @Mapping(source = "medication.nameKo", target = "nameKo")
  @Mapping(source = "medication.company", target = "company")
  @Mapping(source = "medication.effect", target = "effect")
  MedicationResponseDTO toResponseDTO(MemberMedication memberMedication, Medication medication);
}
