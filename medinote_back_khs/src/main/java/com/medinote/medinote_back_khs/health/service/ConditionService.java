package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.ConditionRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.ConditionResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Allergy;
import com.medinote.medinote_back_khs.health.domain.entity.ChronicDisease;
import com.medinote.medinote_back_khs.health.domain.mapper.ConditionMapper;
import com.medinote.medinote_back_khs.health.domain.repository.AllergyRepository;
import com.medinote.medinote_back_khs.health.domain.repository.ChronicDiseaseRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MemberAllergyRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MemberChronicDiseaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConditionService {

  private final MemberAllergyRepository memberAllergyRepository;
  private final MemberChronicDiseaseRepository memberCdRepository;
  private final AllergyRepository allergyRepository;
  private final ChronicDiseaseRepository cdRepository;
  private final ConditionMapper  conditionMapper;

  //알러지, 기저질환 등록(기존 데이터 삭제 후 새로 저장)
  @Transactional
  public ConditionResponseDTO registerCondition(Long memberId, ConditionRequestDTO requestDTO) {
    //기존 데이터 삭제
    memberAllergyRepository.deleteAll(memberAllergyRepository.findByMemberId(memberId));
    memberCdRepository.deleteAll(memberCdRepository.findByMemberId(memberId));

    //알러지 등록
    if(requestDTO.isAllergyYn() && requestDTO.getAllergyIds() != null) {
      requestDTO.getAllergyIds().forEach(allergyId ->
              memberAllergyRepository.save(conditionMapper.toMemberAllergy(memberId, allergyId)));
    }

    if(requestDTO.isChronicDiseaseYn() && requestDTO.getChronicDiseaseIds() != null) {
      requestDTO.getChronicDiseaseIds().forEach(chronicDiseaseId ->
              memberCdRepository.save(conditionMapper.toMemberChronicDisease(memberId, chronicDiseaseId)));
    }

    return getCondition(memberId);
  }

  //특정 회원 condition(기저질환, 알러지) 조회
  @Transactional
  public ConditionResponseDTO getCondition(Long memberId) {
    //알러지 이름 리스트
    List<String> allergyNames = memberAllergyRepository.findByMemberId(memberId)
            .stream()
            .map(ma -> allergyRepository.findById(ma.getAllergyId())
                    .map(Allergy::getNameKo)
                    .orElse("없음"))
            .toList();

    List<String> chronicDiseaseNames = memberCdRepository.findByMemberId(memberId)
            .stream()
            .map(mc -> cdRepository.findById(mc.getChronicDiseaseId())
                    .map(ChronicDisease::getNameKo)
                    .orElse("Unknown"))
            .toList();

    return conditionMapper.toResponseDTO(memberId, chronicDiseaseNames, allergyNames);
  }

  //condition 삭제
  @Transactional
  public void deleteCondition(Long memberId) {
    memberAllergyRepository.deleteAll(memberAllergyRepository.findByMemberId(memberId));
    memberCdRepository.deleteAll(memberCdRepository.findByMemberId(memberId));
  }

}
