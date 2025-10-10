package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicationResponseDTO {

  private Long id;
  private Long memberId;
  private Long medicationId;

  private String drugCode;  //약품코드
  private String nameKo;  //약품명(한글)
  private String company; //제조회사
  private String effect;  //효능

  private String dosage;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isCurrent;
}
