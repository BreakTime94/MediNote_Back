package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health/measurement")
@RequiredArgsConstructor
public class MeasurementController {

  private final MeasurementService measurementService;

  @PostMapping
  public ResponseEntity<Long> saveMeasurement(@RequestBody MeasurementRequestDTO dto) {
    Long id = measurementService.saveMeasurement(dto);
    return ResponseEntity.ok(id); //w저장된 pk 반환
  }
}
