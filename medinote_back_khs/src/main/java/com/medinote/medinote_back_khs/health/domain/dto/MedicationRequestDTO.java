package com.medinote.medinote_back_khs.health.domain.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
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

  @NotNull(message = "약 ID는 필수입니다.")
  private Long medicationId;  //약품id

  private String dosage;

  @FutureOrPresent(message = "복용 시작일은 오늘 이후여야 합니다.")
  private LocalDate startDate;  //복용 시작일

  @Future(message = "복용 종료일은 미래여야 합니다.")
  private LocalDate endDate;  //복용 종료일
  private Boolean isCurrent;
}
