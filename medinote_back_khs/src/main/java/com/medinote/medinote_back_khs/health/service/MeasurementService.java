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

  @Transactional
  public Long saveMeasurement(Long memberId, MeasurementRequestDTO dto) {
    //dto -> entity
    Measurement entity = measurementMapper.toEntity(dto);
    //entity -> db 저장(insert)
    entity.setMemberId(memberId); // 헤더에서 받은 memberId 세팅
    Measurement saved = measurementRepository.save(entity);
    return saved.getId();

  }

  //단일 조회(read)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO findById(Long memberId, Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));
    if (!entity.getMemberId().equals(memberId)) {
      throw new SecurityException("본인 데이터만 조회할 수 있습니다.");
    }

    return measurementMapper.toResponseDTO(entity); //entity -> dto 변환 후 반환
  }

  //수정(update)
  @Transactional
  public Long update(Long memberId, Long id, MeasurementRequestDTO dto) {
    Measurement entity = measurementRepository.findById(id) //기존id 불러옴
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));

    if (!entity.getMemberId().equals(memberId)) {
      throw new SecurityException("본인 데이터만 수정할 수 있습니다.");
    }

    // 필요한 필드만 업데이트
    entity.setHeight(dto.getHeight());
    entity.setWeight(dto.getWeight());
    entity.setBloodPressureSystolic(dto.getBloodPressureSystolic());
    entity.setBloodPressureDiastolic(dto.getBloodPressureDiastolic());
    entity.setBloodSugar(dto.getBloodSugar());
    entity.setHeartRate(dto.getHeartRate());
    entity.setSleepHours(dto.getSleepHours());

    return entity.getId();
  }

  //삭제(delete)
  public void deleteById(Long memberId,Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Not found: " + id));

    if (!entity.getMemberId().equals(memberId)) {
      throw new SecurityException("본인 데이터만 삭제할 수 있습니다.");
    }

    measurementRepository.delete(entity);
  }
}
