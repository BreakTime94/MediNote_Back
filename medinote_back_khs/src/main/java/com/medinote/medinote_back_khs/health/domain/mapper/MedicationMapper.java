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
  @Mapping(source = "itemSeq", target = "drugCode")        // 약품 코드
  @Mapping(source = "itemName", target = "nameKo")         // 약품명(한글)
  @Mapping(source = "entpName", target = "company")        // 제조회사
  @Mapping(source = "efcyQesitm", target = "effect")       // 효능
  @Mapping(source = "useMethodQesitm", target = "useMethod") // 복용법
  @Mapping(source = "atpnWarnQesitm", target = "warning")    // 주의사항(경고)
  @Mapping(source = "atpnQesitm", target = "caution")        // 주의사항
  @Mapping(source = "intrcQesitm", target = "interaction")   // 상호작용
  @Mapping(source = "seQesitm", target = "sideEffect")       // 부작용
  @Mapping(source = "depositMethodQesitm", target = "storage") // 보관방법
  @Mapping(source = "openDe", target = "openDate")          // 공개일
  @Mapping(source = "updateDe", target = "updateDate")      // 업데이트일
  @Mapping(source = "itemImage", target = "image")          // 이미지 URL
  @Mapping(source = "bizrno", target = "bizNo")             // 사업자번호
  @Mapping(target = "source", constant = "식약처 API")      // 데이터 출처 고정
  Medication toEntity(MedicationApiDTO dto);

  // Medication 엔티티 → 검색 응답 DTO
  MedicationResponseDTO toResponseDTO(Medication entity);

  // 리스트 변환
  List<MedicationResponseDTO> toResponseDTOList(List<Medication> listEntity);
}
