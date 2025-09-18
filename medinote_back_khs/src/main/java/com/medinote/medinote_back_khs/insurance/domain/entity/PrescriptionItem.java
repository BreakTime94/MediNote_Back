package com.medinote.medinote_back_khs.insurance.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "tbl_prescription_item")
public class PrescriptionItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long prescriptionId;    // 처방 FK (필수)

  @Column(nullable = false)
  private String drugId;          // 외부 의약품 코드 (필수)

  @Column(nullable = false)
  private String drugName;        // 약품명 (필수)

  private String strength;        // 함량
  private String dosage;          // 1회 복용량
  private String frequency;       // 복용 주기
  private Integer days;           // 복용 기간

  @Column(columnDefinition = "TEXT")
  private String caution;         // 주의사항 (선택, TEXT 타입)
}