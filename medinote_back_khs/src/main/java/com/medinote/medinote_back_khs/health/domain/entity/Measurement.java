package com.medinote.medinote_back_khs.health.domain.entity;


import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import com.medinote.medinote_back_khs.health.domain.en.MeasurementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.Double;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)  // 추가
@Table(name = "tbl_health_measurement")
public class Measurement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  //MALE, FEMALE, OTHER
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MeasurementStatus gender;

  @Column(nullable = false)
  private boolean smoking;          // 필수: 기본 건강 정보

  @Column(nullable = false)
  private boolean drinking;         // 필수: 기본 건강 정보

  private Integer drinkingPerWeek;
  private Integer drinkingPerOnce;

  @Column(nullable = false)
  private boolean chronicDiseaseYn; // 필수: 기저질환 여부

  @Column(nullable = false)
  private boolean allergyYn;        // 필수: 알레르기 여부

  @Column(nullable = false)
  private boolean medicationYn;     // 필수: 복용약물 여부

  private Double height;
  private Double weight;
  private Integer bloodPressureSystolic;  // 혈압(수축기 mmHg)
  private Integer bloodPressureDiastolic; // 혈압(이완기 mmHg)
  private Double bloodSugar;              // 혈당 (mg/dL)
  private Integer heartRate;              // 심박수 (bpm)
  private Double sleepHours;              // 평균 수면시간 (시간)

  @Column(nullable = false)
  private LocalDateTime measuredDate;     // 측정일시

}
