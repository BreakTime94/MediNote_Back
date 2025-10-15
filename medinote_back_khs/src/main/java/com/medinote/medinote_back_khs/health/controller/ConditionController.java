package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.ConditionItemDTO;
import com.medinote.medinote_back_khs.health.domain.dto.ConditionRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.ConditionResponseDTO;
import com.medinote.medinote_back_khs.health.service.ConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health/condition")
@RequiredArgsConstructor
public class ConditionController {

  private final ConditionService conditionService;

  //알러지, 기저질환 등록(기존 이력 삭제 후 등록)
  @PostMapping
  public ResponseEntity<ConditionResponseDTO> registerCondition(
          @RequestHeader("X-Member-Id") Long memberId, @RequestBody ConditionRequestDTO conditionRequestDTO) {
    ConditionResponseDTO response = conditionService.registerCondition(memberId, conditionRequestDTO);
    return ResponseEntity.ok(response);
  }

  //특정 회원의 알러지, 기저질환 조회
  @GetMapping
  public ResponseEntity<ConditionResponseDTO> getCondition(@RequestHeader("X-Member-Id") Long memberId){
    ConditionResponseDTO response = conditionService.getCondition(memberId);
    return ResponseEntity.ok(response);
  }

  //회원의 알러지, 기저질환 삭제
  @DeleteMapping
  public ResponseEntity<Void> deleteCondition(
          @RequestHeader("X-Member-Id") Long memberId) {
    conditionService.deleteCondition(memberId);
    return ResponseEntity.noContent().build();
  }

  //기저질환리스트
  @GetMapping("/chronicDiseases")
  public ResponseEntity<List<ConditionItemDTO>> getALLChronicDiseases() {
    return ResponseEntity.ok(conditionService.getAllChronicDiseass());
  }

  //기저질환 키워드 검색
  @GetMapping("/chronicDiseases/search")
  public ResponseEntity<List<ConditionItemDTO>> searchByCondition(@RequestParam String keyword) {
    return ResponseEntity.ok(conditionService.searchChronicDiseases(keyword));
  }

  //알러지 리스트
  @GetMapping("/allergies")
  public ResponseEntity<List<ConditionItemDTO>> getALLAllergies() {
    return ResponseEntity.ok(conditionService.getAllAllergies());
  }

  //알러지 키워드
  @GetMapping("/allergies/search")
  public ResponseEntity<List<ConditionItemDTO>> searchAllergies(@RequestParam String keyword) {
    return ResponseEntity.ok(conditionService.searchAllergies(keyword));
  }
}
