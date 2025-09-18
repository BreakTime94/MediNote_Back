package com.medinote.medinote_back_khs.insurance.domain.entity;


import com.medinote.medinote_back_khs.common.entity.CreateEntity;
import com.medinote.medinote_back_khs.insurance.domain.en.LabResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_lab_result")
public class LabResult extends CreateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private Long visitId;

  private String apiId;

  private String testCode;  // 검사항목 구분 코드
  private String testName;  // testcode 설명 -> 이름으로
  private String value;  // 검사값
  private String unit;   // 단위(mg/dL, mmol/L, %, IU/L 등)
  private String refRange;   // 참고 범위

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LabResultStatus abnormalFlag;  // 결과 플래그 (H/L/N)

  private LocalDateTime resultedDate; // 검사일

}
