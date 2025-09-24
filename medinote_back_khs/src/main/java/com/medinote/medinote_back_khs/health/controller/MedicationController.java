package com.medinote.medinote_back_khs.health.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.medinote.medinote_back_khs.health.service.MedicationApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medication")
@RequiredArgsConstructor
public class MedicationController {

  private final MedicationApiService medicationApiService;

  @PostMapping("/fetch")
  public ResponseEntity<String> fetchAndSave() throws JsonProcessingException {
    medicationApiService.fetchAndSaveMedication();
    return ResponseEntity.ok("Medication data fetched and saved successfully.");
  }
}
