package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController //json으로 요청/응답 처리
@RequestMapping("/health/measurement")
@RequiredArgsConstructor
@Slf4j
public class MeasurementController {
  //HTTP 요청(JSON) → DTO로 변환해서 Service에 전달.

  private final MeasurementService measurementService;

  @PostMapping //create
  public ResponseEntity<Long> createMeasurement(
          @RequestHeader("X-Member-Id") Long memberId,
          @RequestBody @Valid MeasurementRequestDTO dto) {
    log.info("측정 데이터 등록 요청 - memberId: {}", memberId);
    Long id = measurementService.saveMeasurement(memberId, dto);
    return ResponseEntity.ok(id); //w저장된 pk 반환
  }

  //단일 조회(read)
  @GetMapping("/{id}")
  public ResponseEntity<MeasurementResponseDTO> getMeasurement(@RequestHeader("X-Member-Id") Long memberId, @PathVariable Long id) {
    MeasurementResponseDTO response = measurementService.findById(memberId, id);
    return ResponseEntity.ok(response);
  }

  //수정(update)
  @PutMapping("/{id}")
  public ResponseEntity<Long> updateMeasurement(
          @RequestHeader("X-Member-Id") Long memberId,
          @PathVariable Long id, @RequestBody @Valid MeasurementRequestDTO dto) {
    log.info("측정 데이터 수정 요청 - memberId: {}, measurementId: {}", memberId, id);
    Long updatedId = measurementService.update(memberId, id, dto);
    return ResponseEntity.ok(updatedId);
  }

  //삭제(delete)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMeasurement(@RequestHeader("X-Member-Id") Long memberId, @PathVariable Long id) {
    log.info("측정 데이터 삭제 요청 - memberId: {}, measurementId: {}", memberId, id);
    measurementService.deleteById(memberId, id);
    return ResponseEntity.noContent().build(); // 204 반환
  }

}
