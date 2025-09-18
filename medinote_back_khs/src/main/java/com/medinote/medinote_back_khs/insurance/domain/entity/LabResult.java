package com.medinote.medinote_back_khs.insurance.domain.entity;


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
public class LabResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private Long visitId;

  private String apiId;

  private String testCode;  // 검사 코드
  private String testName;
  private String value;  // 검사값
  private String unit;   // 단위
  private String refRange;   // 참고 범위
  private String abnormalFlag;  // 결과 플래그 (H/L/N)

  private LocalDateTime resultedDate; // 검사일
  private LocalDateTime regDate;  // 수집일

}
