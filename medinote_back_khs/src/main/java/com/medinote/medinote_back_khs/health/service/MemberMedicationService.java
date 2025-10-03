package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import com.medinote.medinote_back_khs.health.domain.mapper.MemberMedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MemberMedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberMedicationService {
  //특정 회원 복용약 기록 관련 로직

  private final MemberMedicationRepository memberMedicationRepository;
  private final MedicationRepository medicationRepository;
  private final MemberMedicationMapper memberMedicationMapper;
  //MedicationRequestDTO <-> MemberMedication <-> MedicationResponseDTO

  // 회원 복용약 등록 (memberId는 Controller에서 헤더로 받아 DTO에 세팅됨)
  @Transactional
  public MedicationResponseDTO registerMedication(Long memberId, MedicationRequestDTO requestDTO) {

    // 약 존재 여부 확인
    Medication medication = medicationRepository.findById(requestDTO.getMedicationId())
            .orElseThrow(() -> new IllegalArgumentException("해당 약품이 존재하지 않습니다."));

    MemberMedication entity = memberMedicationMapper.toEntity(requestDTO);
    MemberMedication saved = memberMedicationRepository.save(entity);

    // MemberMedication + Medication -> ResponseDTO
    return memberMedicationMapper.toResponseDTO(saved, medication);
  }

  // 특정 복용약 단건 조회
  @Transactional(readOnly = true)
  public MedicationResponseDTO getMedication(Long id) {
    MemberMedication memberMedication = memberMedicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 복용약 기록 없음"));

    Medication medication = medicationRepository.findById(memberMedication.getMedicationId())
            .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));

    return memberMedicationMapper.toResponseDTO(memberMedication, medication);
  }

  // 특정 회원이 복용하고 있는 약 리스트 조회
  @Transactional(readOnly = true)
  public List<MedicationResponseDTO> getMedicationsByMember(Long memberId) {
    List<MemberMedication> list = memberMedicationRepository.findByMemberId(memberId);

    return list.stream()
            .map(mm -> {
              Medication med = medicationRepository.findById(mm.getMedicationId())
                      .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));
              return memberMedicationMapper.toResponseDTO(mm, med);
            })
            .toList();
  }

  // 삭제
  @Transactional
  public void deleteMedication(Long id) {
    memberMedicationRepository.deleteById(id);
  }

  // 특정 회원이 특정 약을 복용 중인지 조회
  @Transactional(readOnly = true)
  public MedicationResponseDTO getMedicationByMemberAndMedication(Long memberId, Long medicationId) {
    MemberMedication entity = memberMedicationRepository.findByMemberIdAndMedicationId(memberId, medicationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원의 복용약 기록이 존재하지 않습니다."));

    Medication medication = medicationRepository.findById(medicationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));

    return memberMedicationMapper.toResponseDTO(entity, medication);

  }

}
