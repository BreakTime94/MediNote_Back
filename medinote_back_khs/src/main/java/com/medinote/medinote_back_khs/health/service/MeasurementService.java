package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeasurementService {
  //DTO 받아서 Entity로 변환 -> DB 저장

  private final MeasurementRepository measurementRepository;
  private final MeasurementMapper measurementMapper;

  public Long saveMeasurement(MeasurementRequestDTO dto) {

    //dto -> entity
  Measurement entity = measurementMapper.toEntity(dto);


  //entity -> db 저장
    Measurement savedMeasurement = measurementRepository.save(entity);

    return savedMeasurement.getId();

  }
}
