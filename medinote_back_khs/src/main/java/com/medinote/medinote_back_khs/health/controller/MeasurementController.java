package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //json으로 요청/응답 처리
@RequestMapping("/health/measurement")
@RequiredArgsConstructor
@Slf4j
public class MeasurementController {
  //HTTP 요청(JSON) → DTO로 변환해서 Service에 전달.

  private final MeasurementService measurementService;

  @PostMapping  //create
  public ResponseEntity<MeasurementResponseDTO> createMeasurement(
          @RequestBody @Valid MeasurementRequestDTO dto,  @RequestHeader("X-Member-Id") Long memberId) {
    MeasurementResponseDTO response = measurementService.saveMeasurement(dto, memberId);
    return ResponseEntity.ok(response);
  }

  // 단일 조회 (read)
  @GetMapping("/{id}")
  public ResponseEntity<MeasurementResponseDTO> getMeasurement(@PathVariable Long id) {
    MeasurementResponseDTO response = measurementService.findById(id);
    return ResponseEntity.ok(response);
  }

  // 수정 (update)
  @PutMapping("/update")
  public ResponseEntity<MeasurementResponseDTO> addNewVersion(
          @RequestHeader("X-Member-Id") Long memberId,
          @RequestBody MeasurementRequestDTO dto) {

    log.info("=== /update 요청 들어옴 ===");
    log.info("memberId: {}", memberId);
    log.info("gender: {}", dto.getGender());
    log.info("chronicDiseaseYn: {}", dto.getChronicDiseaseYn());
    log.info("chronicDiseaseIds: {}", dto.getChronicDiseaseIds());
    log.info("allergyYn: {}", dto.getAllergyYn());
    log.info("allergyIds: {}", dto.getAllergyIds());
    log.info("medicationYn: {}", dto.getMedicationYn());
    log.info("medicationIds: {}", dto.getMedicationIds());

    try {
      log.info(">>> Service 호출 전");
      MeasurementResponseDTO response = measurementService.addNewVersion(memberId, dto);
      log.info(">>> Service 호출 후");
      log.info("=== 저장 성공! ===");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("=== 에러 발생! ===", e);
      throw e;
    }
  }

  // 삭제 (delete)
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deactivateMeasurement(@PathVariable Long id) {
    measurementService.deactivateMeasurement(id);
    return ResponseEntity.noContent().build(); // 204 반환
  }

  //회원의 모든 리스트 조회
  @GetMapping("/list")
  public ResponseEntity<List<MeasurementResponseDTO>> getMeasurementList(
          @RequestHeader("X-Member-Id") Long memberId) {
    List<MeasurementResponseDTO> list = measurementService.getMeasurementList(memberId);
    return ResponseEntity.ok(list);
  }

  @GetMapping("/chart")
  public ResponseEntity<List<MeasurementResponseDTO>> getChartData(
          @RequestHeader("X-Member-Id") Long memberId,
          @RequestParam(required = false) String period) {

    List<MeasurementResponseDTO> list = measurementService.getChartData(memberId, period);
    return ResponseEntity.ok(list);
  }

  @GetMapping("/summary")
  public ResponseEntity<MeasurementResponseDTO> getLatestSummary(
          @RequestHeader("X-Member-Id") Long memberId) {
    MeasurementResponseDTO summary = measurementService.getLatestSummary(memberId);
    return ResponseEntity.ok(summary);

  }

  // 최신 측정 데이터 1개 조회 (수정 페이지용)
  @GetMapping("/latest")
  public ResponseEntity<MeasurementResponseDTO> getLatestMeasurement(
          @RequestHeader("X-Member-Id") Long memberId) {
    log.info("=== 최신 측정 데이터 조회: memberId={} ===", memberId);
    MeasurementResponseDTO latest = measurementService.getLatestMeasurement(memberId);
    return ResponseEntity.ok(latest);
  }
}
