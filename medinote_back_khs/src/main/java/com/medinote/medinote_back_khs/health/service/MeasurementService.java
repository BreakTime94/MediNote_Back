package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeasurementService {
  //DTO 받아서 Entity로 변환 -> DB 저장
  //DTO → Entity로 변환(MapStruct) → repository.save(entity) 호출.

  private final MeasurementRepository measurementRepository;  //db접근
  private final MeasurementMapper measurementMapper;  //dto <-> entity

  // 등록 (create)
  @Transactional
  public MeasurementResponseDTO saveMeasurement(MeasurementRequestDTO dto, Long memberId) {

    Measurement entity = measurementMapper.toEntity(dto, memberId);
    Measurement saved = measurementRepository.save(entity);
    return measurementMapper.toResponseDTO(saved);
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
