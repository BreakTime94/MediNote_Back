package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.MedicationApiDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicationMapper {

  //api dto -> entity
  @Mapping(target = "drugCode", source = "itemSeq")
  @Mapping(target = "nameKo", source = "itemName")
  @Mapping(target = "company", source = "entpName")
  @Mapping(target = "effect", source = "efcyQesitm")
  @Mapping(target = "useMethod", source = "useMethodQesitm")
  @Mapping(target = "caution", source = "atpnQesitm")
  @Mapping(target = "sideEffect", source = "seQesitm")
  @Mapping(target = "storage", source = "depositMethodQesitm")
  @Mapping(target = "interaction", source = "intrcQesitm")
  @Mapping(target = "warning", source = "atpnWarnQesitm")
  @Mapping(target = "image", source = "itemImage")
  @Mapping(target = "source", constant = "공공데이터포털")
  @Mapping(target = "bizNo", source = "bizrno")
  @Mapping(target = "openDate", source = "openDe")
  @Mapping(target = "updateDate", source = "updateDe")
  @Mapping(target = "nameKoCompany", ignore = true)
  Medication toEntity(MedicationApiDTO dto);

  // Entity → 프론트 ResponseDTO 변환
  @Mapping(target = "nameKoCompany", source = "nameKoCompany")
  MedicationResponseDTO toResponseDTO(Medication entity);

  // 리스트 변환
  List<MedicationResponseDTO> toResponseDTOList(List<Medication> listEntity);
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromApiDTO(MedicationApiDTO dto, @MappingTarget Medication entity);
}
