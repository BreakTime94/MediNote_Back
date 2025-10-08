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
  //조회용 데이터 전용

  private Long id;          // 약품 PK
  private String drugCode;  // 약품 코드
  private String nameKo;    // 약품명 (한글)
  private String company;   // 제조회사
  private String effect;    // 효능
}
