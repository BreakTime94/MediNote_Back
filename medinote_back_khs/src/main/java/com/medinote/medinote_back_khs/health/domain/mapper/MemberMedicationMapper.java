package com.medinote.medinote_back_khs.health.domain.mapper;


import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMedicationMapper {

  //requestdto -> entity
  MemberMedication toEntity(MedicationRequestDTO dto);

  //entity -> requestdto
  MedicationResponseDTO toResponseDTO(MemberMedication entity);

  //list<entity> -> list<dto>
  List<MedicationResponseDTO> toResponseDTO(List<MemberMedication> listEntity);
}
