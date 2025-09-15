package com.medinote.medinote_back_khs.health.domain.entity;


import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.lang.Double;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name = "tbl_health_measurement")
public class Measurement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id" , nullable = false)
  private Long memberId;

  private String gender;
  private boolean smoking;
  private boolean drinking;
  private Integer drinkingPerWeek;
  private Integer drinkingPerOnce;
  private boolean chronicDiseaseYn; //기저질환
  private boolean allergyYn;
  private boolean medicationYn;

  @Column(precision = 5, scale = 2)
  private Double height;

  @Column(precision = 5, scale = 2)
  private Double weight;

  private Integer bloodPressureSystolic;  //혈압(수축기 mmHg)
  private Integer bloodPressureDiastolic;    // 혈압(이완기 mmHg)

  @Column(precision = 5, scale = 2)
  private Double bloodSugar;  // 혈당 (mg/dL)

  private Integer heartRate;   // 심박수 (bpm)

  @Column(precision = 4, scale = 2)
  private Double sleepHours;  // 평균 수면시간 (시간)

  @Column(nullable = false)
  private LocalDateTime measuredDate;

}
