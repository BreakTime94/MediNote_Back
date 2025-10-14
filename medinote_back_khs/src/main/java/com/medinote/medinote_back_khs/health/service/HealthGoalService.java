package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.HealthGoalResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.HealthGoal;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.mapper.HealthGoalMapper;
import com.medinote.medinote_back_khs.health.domain.repository.HealthGoalRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class HealthGoalService {

//  private final HealthGoalRepository healthGoalRepository;
//  private final MeasurementRepository measurementRepository;
//  private final HealthGoalMapper healthGoalMapper;
//
//  // 목표 체중 등록 또는 수정
//  public HealthGoalResponseDTO saveOrUpdateGoal(Long memberId, HealthGoalRequestDTO requestDTO) {
//    log.info("목표 체중 저장/수정 - memberId: {}, targetWeight: {}", memberId, requestDTO.getTargetWeight());
//
//    Optional<HealthGoal> existingGoal = healthGoalRepository.findByMemberId(memberId);
//
//    HealthGoal healthGoal;
//    if (existingGoal.isPresent()) {
//      // 수정
//      healthGoal = existingGoal.get();
//      healthGoalMapper.updateEntityFromDTO(requestDTO, healthGoal);
//      log.info("기존 목표 수정");
//    } else {
//      // 신규 등록
//      healthGoal = healthGoalMapper.toEntity(requestDTO);
//      healthGoal.setMemberId(memberId);
//      log.info("새로운 목표 등록");
//    }
//
//    HealthGoal saved = healthGoalRepository.save(healthGoal);
//    return enrichResponseDTO(saved);
//  }
//
//  // 목표 체중 조회
//  @Transactional(readOnly = true)
//  public HealthGoalResponseDTO getGoal(Long memberId) {
//    log.info("목표 체중 조회 - memberId: {}", memberId);
//
//    HealthGoal healthGoal = healthGoalRepository.findByMemberId(memberId)
//            .orElseThrow(() -> new RuntimeException("목표 체중이 설정되지 않았습니다."));
//
//    return enrichResponseDTO(healthGoal);
//  }
//
//  // 목표 체중 삭제
//  public void deleteGoal(Long memberId) {
//    log.info("목표 체중 삭제 - memberId: {}", memberId);
//
//    if (!healthGoalRepository.existsByMemberId(memberId)) {
//      throw new RuntimeException("삭제할 목표가 없습니다.");
//    }
//
//    healthGoalRepository.deleteByMemberId(memberId);
//  }
//
//  // ResponseDTO에 추가 정보 enrichment (현재 체중, 진행률 등)
//  private HealthGoalResponseDTO enrichResponseDTO(HealthGoal healthGoal) {
//    HealthGoalResponseDTO dto = healthGoalMapper.toResponseDTO(healthGoal);
//
//    // 현재 체중과 진행률 계산 (최신 측정 데이터 사용)
//    Optional<Measurement> latestMeasurement = measurementRepository
//            .findTopByMemberIdOrderByMeasuredDateDesc(healthGoal.getMemberId());
//
//    if (latestMeasurement.isPresent() && latestMeasurement.get().getWeight() != null) {
//      Double currentWeight = latestMeasurement.get().getWeight();
//      dto.setCurrentWeight(currentWeight);
//
//      Double remainingWeight = currentWeight - healthGoal.getTargetWeight();
//      dto.setRemainingWeight(remainingWeight);
//
//      // 진행률 계산 (간단한 예시)
//      // TODO: 시작 체중 기준으로 계산 로직 추가
//    }
//
//    return dto;
//  }
}