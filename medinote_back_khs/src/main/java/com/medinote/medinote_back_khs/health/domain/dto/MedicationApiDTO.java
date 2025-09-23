package com.medinote.medinote_back_khs.health.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicationApiDTO {

  private String entpName;            // 업체명
  private String itemName;            // 제품명
  private String itemSeq;             // 의약품 일련번호
  private String efcyQesitm;          // 효능효과
  private String useMethodQesitm;     // 복용방법
  private String atpnWarnQesitm;      // 사용상 주의사항(경고)
  private String atpnQesitm;          // 사용상 주의사항(일반)
  private String intrcQesitm;         // 상호작용
  private String seQesitm;            // 부작용
  private String depositMethodQesitm; // 보관방법
  private String openDe;              // 공개일자
  private String updateDe;            // 수정일자
  private String itemImage;           // 이미지 URL
  private String bizrno;              // 사업자 등록번호
}
