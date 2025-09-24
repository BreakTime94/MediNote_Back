package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MedicationApiDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicationMapper {

  //api dto -> entity
  @Mapping(source = "itemSeq", target = "drugCode")
  @Mapping(source = "itemName", target = "nameKo")
  @Mapping(source = "entpName", target = "company")
  @Mapping(source = "efcyQesitm", target = "effect")
  @Mapping(source = "useMethodQesitm", target = "useMethod")
  @Mapping(source = "atpnWarnQesitm", target = "warning")
  @Mapping(source = "atpnQesitm", target = "caution")
  @Mapping(source = "intrcQesitm", target = "interaction")
  @Mapping(source = "seQesitm", target = "sideEffect")
  @Mapping(source = "depositMethodQesitm", target = "storage")
  @Mapping(source = "openDe", target = "openDate")
  @Mapping(source = "updateDe", target = "updateDate")
  @Mapping(source = "itemImage", target = "image")
  @Mapping(source = "bizrno", target = "bizNo")
  // DTO에는 없으니까 상수값 넣기
  @Mapping(target = "source", constant = "식약처 API")
  Medication toEntity(MedicationApiDTO dto);

  //entity -> dto
  MedicationResponseDTO toResponseDTO(Medication entity);

  List<MedicationResponseDTO> toResponseDTOList(List<Medication> listEntity);
}
