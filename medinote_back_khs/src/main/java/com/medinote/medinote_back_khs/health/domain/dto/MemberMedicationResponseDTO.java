package com.medinote.medinote_back_khs.health.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberMedicationResponseDTO {

  private Long id;
  private Long memberId;
  private Long medicationId;

  private String dosage;   // 복용량 추가 ✅
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isCurrent;

  private String drugCode;
  private String nameKo;
  private String company;
  private String effect;
}
