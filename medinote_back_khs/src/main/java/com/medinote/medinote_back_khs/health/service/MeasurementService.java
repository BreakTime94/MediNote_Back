package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
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
  private final MedicationMapper medicationMapper;


  // 등록 (create)
  @Transactional
  public MeasurementResponseDTO saveMeasurement(MeasurementRequestDTO dto, Long memberId) {
    Measurement entity = measurementMapper.toEntity(dto, memberId);
    Measurement saved = measurementRepository.save(entity);

    MeasurementResponseDTO response = measurementMapper.toResponseDTO(saved);

    // 복용약 조회 후 세팅
//    List<MemberMedication> meds = memberMedicationRepository.findByMemberId(memberId);
//    List<MedicationResponseDTO> medDtos = meds.stream()
//            .map(mm -> {
//              Medication med = medicationRepository.findById(mm.getMedicationId())
//                      .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));
//              //한 개만 넘긴다
//              return medicationMapper.toResponseDTO(med);
//            })
//            .toList();
//
//    response.setMedications(medDtos);
//
//    return response;
    if (dto.getMedicationIds() != null && !dto.getMedicationIds().isEmpty()) {
      List<String> medicationNames = dto.getMedicationIds().stream()
              .map(id -> medicationRepository.findById(id)
                      .map(Medication::getNameKo)
                      .orElse("약품명 없음"))
              .toList();
      response.setMedicationNames(medicationNames);
    }

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
  public MeasurementResponseDTO addNewVersion(Long memberId, MeasurementRequestDTO dto) {
    // 새 이력 저장 (saveMeasurement 로직 재활용)
    return saveMeasurement(dto, memberId);
  }

  // 삭제 (delete)
  @Transactional
  public void deactivateMeasurement(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 데이터가 없습니다."));
    entity.setStatus(MeasurementStatus.INACTIVE);
    measurementRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<MeasurementResponseDTO> getMeasurementList(Long memberId) {
    List<Measurement> list = measurementRepository.findByMemberIdOrderByMeasuredDateDesc(memberId);
    return list.stream()
            .map(measurementMapper::toResponseDTO)
            .toList();
  }
}