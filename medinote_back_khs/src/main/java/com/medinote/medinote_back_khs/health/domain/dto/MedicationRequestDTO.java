package com.medinote.medinote_back_khs.health.domain.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequestDTO {

  @NotNull(message = "회원 ID는 필수입니다.")
  private Long memberId;  //회원id

  @NotNull(message = "약 ID는 필수입니다.")
  private Long medicationId;  //약품id
  private String dosage;
  private LocalDate startDate;  //복용 시작일
  private LocalDate endDate;  //복용 종료일
  private Boolean isCurrent;
}
