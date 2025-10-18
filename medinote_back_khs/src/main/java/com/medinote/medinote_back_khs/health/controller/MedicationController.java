package com.medinote.medinote_back_khs.health.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.service.MedicationApiService;
import com.medinote.medinote_back_khs.health.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health/medication")
@RequiredArgsConstructor
@Slf4j
@Validated //입력값 검증
public class MedicationController {

  private final MedicationApiService medicationApiService;
  private final MedicationService medicationService;

  //db 수집(api에서)
  @PostMapping("/fetch")
  public ResponseEntity<String> fetchAndSave() throws JsonProcessingException {
    medicationApiService.fetchAndSaveMedication();
    return ResponseEntity.ok("데이터 수집 완료 .");
  }

  // 전체 약품 리스트 (페이징)
  @GetMapping
  public ResponseEntity<Page<MedicationResponseDTO>> getMedicationList(Pageable pageable) {
    Page<MedicationResponseDTO> page = medicationService.getMedicationList(pageable);
    return ResponseEntity.ok(page);
  }

  // 키워드 검색
  @GetMapping("/search")
  public ResponseEntity<List<MedicationResponseDTO>> searchMedication(@RequestParam String keyword) {
    String decodedKeyword = java.net.URLDecoder.decode(keyword, java.nio.charset.StandardCharsets.UTF_8);
    log.info("🔍 검색 요청 keyword = {}", decodedKeyword);

    List<MedicationResponseDTO> results = medicationService.searchMedications(decodedKeyword);
    return ResponseEntity.ok(results);
  }

  // 단일 약품 조회
  @GetMapping("/{id}")
  public ResponseEntity<MedicationResponseDTO> getMedicationById(@PathVariable Long id) {
    MedicationResponseDTO dto = medicationService.getMedicationById(id);
    return ResponseEntity.ok(dto);
  }

}
