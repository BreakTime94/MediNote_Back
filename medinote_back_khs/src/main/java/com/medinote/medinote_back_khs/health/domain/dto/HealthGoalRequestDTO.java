package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalRequestDTO {
  //목표 체중 등록 + 수정

  private Double targetWeight;  // 목표 체중 (kg)
}