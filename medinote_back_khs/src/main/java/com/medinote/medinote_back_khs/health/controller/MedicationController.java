package com.medinote.medinote_back_khs.health.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.service.MedicationApiService;
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
@CrossOrigin("http://localhost:6006")
public class MedicationController {

  private final MedicationApiService medicationApiService;

  //db 수집(api에서)
  @PostMapping("/fetch")
  public ResponseEntity<String> fetchAndSave() throws JsonProcessingException {
    medicationApiService.fetchAndSaveMedication();
    return ResponseEntity.ok("Medication data fetched and saved successfully.");
  }

  //복용약 리스트 조회
  @GetMapping("/list")
  public ResponseEntity<Page<MedicationResponseDTO>> getMedicationList(Pageable pageable) throws  JsonProcessingException {
    return ResponseEntity.ok(medicationApiService.getMedicationList(pageable));
  }

  @GetMapping("/search")
  public ResponseEntity<List<MedicationResponseDTO>> searchMedication(@RequestParam String keyword) {
    log.info(keyword);
    return ResponseEntity.ok(medicationApiService.searchMedication(keyword));
  }
}
