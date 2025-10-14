package com.medinote.medinote_back_khs.health.controller;

import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalResponseDTO;
import com.medinote.medinote_back_khs.health.service.HealthGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health/goals")
@RequiredArgsConstructor
@Log4j2
public class HealthGoalController {

//  private final HealthGoalService healthGoalService;
//
//  // 목표 체중 등록/수정
//  @PostMapping
//  public ResponseEntity<HealthGoalResponseDTO> saveOrUpdateGoal(
//          @RequestHeader("X-Member-Id") Long memberId,
//          @RequestBody HealthGoalRequestDTO requestDTO) {
//
//    log.info("목표 체중 저장/수정 API 호출 - memberId: {}", memberId);
//
//    HealthGoalResponseDTO response = healthGoalService.saveOrUpdateGoal(memberId, requestDTO);
//    return ResponseEntity.ok(response);
//  }
//
//  // 목표 체중 조회
//  @GetMapping
//  public ResponseEntity<HealthGoalResponseDTO> getGoal(
//          @RequestHeader("X-Member-Id") Long memberId) {
//
//    log.info("목표 체중 조회 API 호출 - memberId: {}", memberId);
//
//    HealthGoalResponseDTO response = healthGoalService.getGoal(memberId);
//    return ResponseEntity.ok(response);
//  }
//
//  // 목표 체중 삭제
//  @DeleteMapping
//  public ResponseEntity<Void> deleteGoal(
//          @RequestHeader("X-Member-Id") Long memberId) {
//
//    log.info("목표 체중 삭제 API 호출 - memberId: {}", memberId);
//
//    healthGoalService.deleteGoal(memberId);
//    return ResponseEntity.noContent().build();
//  }
}