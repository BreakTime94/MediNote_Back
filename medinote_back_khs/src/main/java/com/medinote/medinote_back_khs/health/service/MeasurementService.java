package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
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
  public Long saveMeasurement(MeasurementRequestDTO dto) {
    //dto -> entity
    Measurement entity = measurementMapper.toEntity(dto);
    //entity -> db 저장(insert)
    Measurement savedMeasurement = measurementRepository.save(entity);
    return savedMeasurement.getId();

  }

  //단일 조회(read)
  @Transactional(readOnly = true)
  public MeasurementRequestDTO  findById(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));
    return measurementMapper.toDTO(entity); //entity -> dto 변환 후 반환
  }

  //수정(update)
  @Transactional
  public Long update(Long id, MeasurementRequestDTO dto) {
    Measurement entity = measurementRepository.findById(id) //기존id 불러옴
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));

    measurementMapper.updateFromDto(dto, entity);

    Measurement saveMeasurement = measurementRepository.save(entity);
    return saveMeasurement.getId();
  }

  //삭제(delete)
  public void deleteById(Long id) {
    if(!measurementRepository.existsById(id)) {
      throw new IllegalArgumentException("Id not found: " + id);
    }
    measurementRepository.deleteById(id);
  }
}
