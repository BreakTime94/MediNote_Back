package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementResponseDTO {

  private Long id;
  private Long memberId;
  private String gender;
  private Boolean smoking;
  private Boolean drinking;
  private Integer drinkingPerWeek;
  private Integer drinkingPerOnce;
  private Boolean chronicDiseaseYn;
  private String chronicDiseaseDetail;
  private Boolean allergyYn;
  private String allergyDetail;
  private Boolean medicationYn;
  private String medicationDetail;
  private Double height;
  private Double weight;
  private Integer bloodPressureSystolic;
  private Integer bloodPressureDiastolic;
  private Integer bloodSugar;
  private LocalDateTime measuredDate;
}
