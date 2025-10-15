package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionRequestDTO {
  //기저질환 + 알러지 합쳐서(보유 여부와 다중선택뿐이기 때문에)

  private boolean chronicDiseaseYn;
  private List<Long> chronicDiseaseIds; //선택한 기저질환 id 리스트

  private boolean allergyYn;
  private List<Long> allergyIds;
}
