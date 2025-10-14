package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalResponseDTO {
  //조회시 반환 + 추가 계산 필드

  private Long id;
  private Long memberId;
  private Double targetWeight;  // 목표 체중 (kg)
  private LocalDateTime regDate;
  private LocalDateTime modDate;

  // 추가 계산 필드 (프론트엔드 표시용)
  private Double currentWeight;      // 현재 체중
  private Double remainingWeight;    // 남은 체중 (목표까지)
  private Integer progressPercent;   // 달성률 (%)
}