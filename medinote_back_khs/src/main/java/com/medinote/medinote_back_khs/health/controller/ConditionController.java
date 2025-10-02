package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.ConditionRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.ConditionResponseDTO;
import com.medinote.medinote_back_khs.health.service.ConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
