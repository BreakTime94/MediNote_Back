package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportResponseDTO {
  //개인 건강정보 리포트용

  private Long memberId;
  private Double bmi; //체질량지수 (BMI, = 체중(kg) / [키(m)]²)
  private String bmiStatus;

  private Integer systolic;
  private Integer diastolic;
  private String bloodPressureStatus;

  private Integer bloodSugar; // 혈당 수치 (mg/dL 단위)
  private String bloodSugarStatus;

  private Double sleepHours;
  private String sleepStatus;

  private String summary;  // 종합 요약 (BMI, 혈압, 혈당, 수면 상태를 종합한 문장 요약)
}
