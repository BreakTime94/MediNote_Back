package com.medinote.medinote_back_khs.health.domain.mapper;


import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MemberMedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMedicationMapper {

  //  requestDTO → Entity
  @Mapping(source = "medicationId", target = "medicationId")
  @Mapping(source = "dosage", target = "dosage")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "endDate", target = "endDate")
  @Mapping(source = "isCurrent", target = "isCurrent")
  MemberMedication toEntity(MedicationRequestDTO dto);

  //  Entity → 단일 회원 복용약 응답 DTO
  @Mapping(source = "id", target = "id")
  @Mapping(source = "memberId", target = "memberId")
  @Mapping(source = "medicationId", target = "medicationId")
  @Mapping(source = "dosage", target = "dosage")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "endDate", target = "endDate")
  @Mapping(source = "isCurrent", target = "isCurrent")
  MemberMedicationResponseDTO toMemberMedicationResponseDTO(MemberMedication entity);

  //  리스트 변환
  List<MemberMedicationResponseDTO> toMemberMedicationResponseDTOList(List<MemberMedication> listEntity);

  //  MemberMedication + Medication → 응답 DTO
  @Mapping(source = "memberMedication.id", target = "id")
  @Mapping(source = "memberMedication.memberId", target = "memberId")
  @Mapping(source = "memberMedication.dosage", target = "dosage")
  @Mapping(source = "memberMedication.startDate", target = "startDate")
  @Mapping(source = "memberMedication.endDate", target = "endDate")
  @Mapping(source = "memberMedication.isCurrent", target = "isCurrent")
  @Mapping(source = "medication.id", target = "medicationId")
  @Mapping(source = "medication.drugCode", target = "drugCode")
  @Mapping(source = "medication.nameKo", target = "nameKo")
  @Mapping(source = "medication.company", target = "company")
  @Mapping(source = "medication.effect", target = "effect")
  MemberMedicationResponseDTO toResponseDTO(MemberMedication memberMedication, Medication medication);
}