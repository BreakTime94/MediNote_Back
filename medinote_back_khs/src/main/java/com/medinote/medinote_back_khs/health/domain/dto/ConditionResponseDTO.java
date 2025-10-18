package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionResponseDTO {
  //기저질환 + 알러지 합쳐서(보유 여부와 다중선택뿐이기 때문에)
  //memberId, chronicDiseases, allergies -> 회원 보유 정보 확인용

  private Long memberId;                   // 회원 ID
  private List<String> chronicDiseases;    // 기저질환 이름 리스트
  private List<String> allergies;          // 알러지 이름 리스트
}
