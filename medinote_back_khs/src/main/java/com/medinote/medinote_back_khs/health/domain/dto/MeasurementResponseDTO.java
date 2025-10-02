package com.medinote.medinote_back_khs.health.domain.dto;

import com.medinote.medinote_back_khs.health.domain.en.MeasurementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementResponseDTO {

  private Long id;                // PK
  private Long memberId;          // 회원 ID
  private MeasurementStatus gender;
  private boolean smoking;
  private boolean drinking;
  private Integer drinkingPerWeek;
  private Integer drinkingPerOnce;
  private Boolean chronicDiseaseYn;
  private Boolean allergyYn;
  private Boolean medicationYn;

  private Double height;
  private Double weight;
  private Integer bloodPressureSystolic;
  private Integer bloodPressureDiastolic;
  private Double bloodSugar;
  private Double sleepHours;

  private LocalDateTime measuredDate; // 측정일시
  private LocalDateTime regDate;      // 등록일
  private LocalDateTime modDate;      // 수정일
}
