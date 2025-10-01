package com.medinote.medinote_back_khs.health.domain.mapper;

import com.medinote.medinote_back_khs.health.domain.dto.ConditionResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.MemberAllergy;
import com.medinote.medinote_back_khs.health.domain.entity.MemberChronicDisease;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConditionMapper {

  //dto -> memberAllergy
  @Mapping(target = "id" , ignore = true) //pk자동생성
  @Mapping(source = "memberId", target = "memberId")
  @Mapping(source = "allergyId", target = "allergyId")
  MemberAllergy toMemberAllergy(Long memberId, Long allergyId);

  //DTO -> MemberChronicDisease
  @Mapping(target = "id" , ignore = true)
  @Mapping(source = "memberId", target = "memberId")
  @Mapping(source = "chronicDiseaseId", target = "chronicDiseaseId")
  MemberChronicDisease toMemberChronicDisease(Long memberId, Long chronicDiseaseId);


  //조회결과(엔티티 리스트에서 추출한 이름들을 ResponseDTO로 변환)
  default ConditionResponseDTO toResponseDTO(Long memberId, List<String> chronicDiseases, List<String> allergies) {
    return new ConditionResponseDTO(memberId, chronicDiseases, allergies);
  }


}
