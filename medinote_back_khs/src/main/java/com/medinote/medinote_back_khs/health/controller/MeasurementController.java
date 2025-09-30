package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
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

  @PostMapping ("/create") //create
  public ResponseEntity<MeasurementResponseDTO> createMeasurement(@RequestBody @Valid MeasurementRequestDTO dto) {
    MeasurementResponseDTO response = measurementService.saveMeasurement(dto);
    return ResponseEntity.ok(response);
  }

  // 단일 조회 (read)
  @GetMapping("/{id}")
  public ResponseEntity<MeasurementResponseDTO> getMeasurement(@PathVariable Long id) {
    MeasurementResponseDTO response = measurementService.findById(id);
    return ResponseEntity.ok(response);
  }

  // 수정 (update)
  @PutMapping("/{id}")
  public ResponseEntity<MeasurementResponseDTO> updateMeasurement(@PathVariable Long id,
                                                                  @RequestBody @Valid MeasurementRequestDTO dto) {
    MeasurementResponseDTO response = measurementService.update(id, dto);
    return ResponseEntity.ok(response);
  }

  // 삭제 (delete)
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteMeasurement(@PathVariable Long id) {
    measurementService.deleteById(id);
    return ResponseEntity.ok("삭제 완료");
  }

}
