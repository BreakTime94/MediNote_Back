package com.medinote.medinote_back_khs.health.controller;


import com.medinote.medinote_back_khs.health.domain.dto.MedicationRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.service.MemberMedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health/medications")
@RequiredArgsConstructor
public class MemberMedicationController {
  //복용약을 등록 + 조회

  private final MemberMedicationService memberMedicationService;

  // 복용약 등록
  @PostMapping("/register")
  public ResponseEntity<MedicationResponseDTO> registerMedication(
          @RequestHeader("X-Member-Id") Long memberId,
          @Valid @RequestBody MedicationRequestDTO requestDTO) {
    return ResponseEntity.ok(memberMedicationService.registerMedication(memberId , requestDTO));
  }

  // 회원별 복용약 리스트 조회
  @GetMapping
  public ResponseEntity<List<MedicationResponseDTO>> getMedicationsByMember(
          @RequestHeader("X-Member-Id") Long memberId) {
    return ResponseEntity.ok(memberMedicationService.getMedicationsByMember(memberId));
  }

  // 특정 복용약 단건 조회 (MemberMedication PK)
  @GetMapping("/{id}")
  public ResponseEntity<MedicationResponseDTO> getMedication(@PathVariable Long id) {
    return ResponseEntity.ok(memberMedicationService.getMedication(id));
  }

  // 특정 회원이 특정 약 복용중인지 조회
  @GetMapping("/check/{medicationId}")
  public ResponseEntity<MedicationResponseDTO> getMedicationByMemberAndMedication(
          @RequestHeader("X-Member-Id") Long memberId,
          @PathVariable Long medicationId) {
    return ResponseEntity.ok(
            memberMedicationService.getMedicationByMemberAndMedication(memberId, medicationId)
    );
  }

  // 복용약 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteMedication(@PathVariable Long id) {
    memberMedicationService.deleteMedication(id);
    return ResponseEntity.ok("삭제 완료");
  }
}
