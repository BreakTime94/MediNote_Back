package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //json으로 요청/응답 처리
@RequestMapping("/health/measurement")
@RequiredArgsConstructor
public class MeasurementController {
  //HTTP 요청(JSON) → DTO로 변환해서 Service에 전달.

  private final MeasurementService measurementService;

  @PostMapping  //create
  public ResponseEntity<Long> createMeasurement(@RequestBody @Valid MeasurementRequestDTO dto) {
    Long id = measurementService.saveMeasurement(dto);
    return ResponseEntity.ok(id); //w저장된 pk 반환
  }

  //단일 조회(read)
  @GetMapping("/{id}")
  public MeasurementRequestDTO getMeasurement(@PathVariable @Valid Long id) {
    return measurementService.findById(id);
  }

  //수정(update)
  @PutMapping("/{id}")
  public Long updateMeasurement(@PathVariable Long id, @RequestBody @Valid MeasurementRequestDTO dto) {
    return measurementService.update(id, dto);
  }

  //삭제(delete)
  @DeleteMapping("/{id}")
  public void deleteMeasurement(@PathVariable @Valid Long id) {
    measurementService.deleteById(id);
  }

}
