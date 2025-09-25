package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
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

  //회원 복용약 등록
  @Transactional
  public MedicationResponseDTO registerMedication(MedicationRequestDTO requestDTO) {

    //약 존재 여부
    Medication medication = medicationRepository.findById(requestDTO.getMedicationId()).orElseThrow(() -> new IllegalArgumentException("해당 약품이 존재하지 않습니다."));

    MemberMedication entity = memberMedicationMapper.toEntity(requestDTO);

    MemberMedication saved = memberMedicationRepository.save(entity);

    MedicationResponseDTO responseDTO = memberMedicationMapper.toResponseDTO(saved);

    responseDTO.setDrugCode(medication.getDrugCode());
    responseDTO.setNameKo(medication.getNameKo());
    responseDTO.setCompany(medication.getCompany());
    responseDTO.setEffect(medication.getEffect());

    return responseDTO;
  }

  //특정 회원이 복용하고 있는 약 리스트 조회
  @Transactional
  public List<MedicationResponseDTO> getMedicationByMember(Long memberId) {
    List<MemberMedication> list = memberMedicationRepository.findByMemberId(memberId);

    List<MedicationResponseDTO> dtoList = memberMedicationMapper.toResponseDTO(list);

    for(MedicationResponseDTO dto : dtoList) {
      medicationRepository.findById(dto.getMedicationId()).ifPresent(med -> {
        dto.setDrugCode(med.getDrugCode());
        dto.setNameKo(med.getNameKo());
        dto.setCompany(med.getCompany());
        dto.setEffect(med.getEffect());
      });
    }
    return  dtoList;
  }

  // 특정 복용약 단건 조회
  @Transactional(readOnly = true)
  public MedicationResponseDTO getMedication(Long id) {
    MemberMedication entity = memberMedicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 복용약 기록이 존재하지 않습니다."));

    MedicationResponseDTO dto = memberMedicationMapper.toResponseDTO(entity);

    // 약품 상세정보도 같이 세팅
    medicationRepository.findById(dto.getMedicationId()).ifPresent(med -> {
      dto.setDrugCode(med.getDrugCode());
      dto.setNameKo(med.getNameKo());
      dto.setCompany(med.getCompany());
      dto.setEffect(med.getEffect());
    });

    return dto;
  }


  //특정 회원이 특정 약을 먹고 있는지 -> memberId + medicationId
  @Transactional
  public MedicationResponseDTO getMedicationByMemberAndMedication(Long memberId, Long medicationId) {
    MemberMedication entity = memberMedicationRepository.findByMemberIdAndMedicationId(memberId, medicationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원의 복용약 기록이 존재하지 않습니다."));

    MedicationResponseDTO dto = memberMedicationMapper.toResponseDTO(entity);

    // 약 상세정보 세팅
    medicationRepository.findById(dto.getMedicationId()).ifPresent(med -> {
      dto.setDrugCode(med.getDrugCode());
      dto.setNameKo(med.getNameKo());
      dto.setCompany(med.getCompany());
      dto.setEffect(med.getEffect());
    });

    return dto;
  }

}
