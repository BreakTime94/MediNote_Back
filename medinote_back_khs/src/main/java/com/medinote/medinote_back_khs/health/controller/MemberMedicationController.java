package com.medinote.medinote_back_khs.health.controller;


import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.service.MedicationApiService;
import com.medinote.medinote_back_khs.health.service.MemberMedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health/Mmedication")
@RequiredArgsConstructor
public class MemberMedicationController {
  //복용약을 등록 + 조회

  private final MedicationApiService memberMedicationApiService;
  private final MemberMedicationService memberMedicationService;

  @PostMapping
  public ResponseEntity<MedicationResponseDTO> registerMedication(@Valid @RequestBody MedicationRequestDTO requestDTO) {
    return ResponseEntity.ok(memberMedicationService.registerMedication(requestDTO));
  }

  //복용약 리스트 조회
  @GetMapping("/health/{memberId}")
  public ResponseEntity<List<MedicationResponseDTO>> getMedicationsByMember(@PathVariable Long memberId) {
    List<MedicationResponseDTO> responseList = memberMedicationService.getMedicationByMember(memberId);
    return ResponseEntity.ok(responseList);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MedicationResponseDTO> getMedication(@PathVariable Long id) {
    MedicationResponseDTO response = memberMedicationService.getMedication(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/health/{memberId}/medication/{medicationId}")
  public ResponseEntity<MedicationResponseDTO> getMedicationByMedicationResponseDTOResponseEntity(@PathVariable Long memberId, @PathVariable Long medicationId) {
    MedicationResponseDTO response = memberMedicationService.getMedicationByMemberAndMedication(memberId, medicationId);
    return ResponseEntity.ok(response);
  }
}
