package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionItemDTO {
  //기저질환, 알러지 db에서 리스트 조회, 검색용으로 따로 뺌
  //전체 리스트 / 검색 결과 응답용

  private Long id;
  private String nameKo;
}
