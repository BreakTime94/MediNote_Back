package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.mapper.MemberMedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MeasurementRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MemberMedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementService {
  //DTO 받아서 Entity로 변환 -> DB 저장
  //DTO → Entity로 변환(MapStruct) → repository.save(entity) 호출.

  private final MeasurementRepository measurementRepository;
  private final MeasurementMapper measurementMapper;
  private final MemberMedicationRepository memberMedicationRepository;
  private final MedicationRepository medicationRepository;
  private final MemberMedicationMapper memberMedicationMapper;


  // 등록 (create)
  @Transactional
  public MeasurementResponseDTO saveMeasurement(MeasurementRequestDTO dto, Long memberId) {
    Measurement entity = measurementMapper.toEntity(dto, memberId);
    Measurement saved = measurementRepository.save(entity);

    MeasurementResponseDTO response = measurementMapper.toResponseDTO(saved);

    // ✅ 회원 복용약 조회 후 세팅
    List<MemberMedication> meds = memberMedicationRepository.findByMemberId(memberId);
    List<MedicationResponseDTO> medDtos = meds.stream()
            .map(mm -> {
              Medication med = medicationRepository.findById(mm.getMedicationId())
                      .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));
              return memberMedicationMapper.toResponseDTO(mm, med);
            })
            .toList();

    response.setMedications(medDtos);

    return response;
  }

  // 단일 조회 (read)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO findById(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));
    return measurementMapper.toResponseDTO(entity);
  }

  // 수정 (update)
  @Transactional
  public MeasurementResponseDTO update(Long id, MeasurementRequestDTO dto) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));

    measurementMapper.updateFromDto(dto, entity);
    Measurement updated = measurementRepository.save(entity);

    return measurementMapper.toResponseDTO(updated);
  }

  // 삭제 (delete)
  @Transactional
  public void deleteById(Long id) {
    if (!measurementRepository.existsById(id)) {
      throw new IllegalArgumentException("Id not found: " + id);
    }
    measurementRepository.deleteById(id);
  }
}
