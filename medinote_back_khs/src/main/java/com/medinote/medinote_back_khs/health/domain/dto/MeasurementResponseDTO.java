package com.medinote.medinote_back_khs.health.domain.dto;

import com.medinote.medinote_back_khs.health.domain.enums.GenderStatus;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementResponseDTO {

  private Long id;                // PK
  private Long memberId;          // 회원 ID
  private GenderStatus gender;
  private boolean smoking;
  private boolean drinking;
  private Integer drinkingPerWeek;
  private Integer drinkingPerOnce;

  private Boolean chronicDiseaseYn;
  private Boolean allergyYn;
  private Boolean medicationYn;

  //  이름 기반 리스트 (조회 시 표시용)
  private List<String> chronicDiseaseNames;  // 기저질환 이름 리스트
  private List<String> allergyNames;         // 알러지 이름 리스트
  private List<String> medicationNames;      // 복용약 이름 리스트 (검색용 선택 결과)

  //  상세 약정보 (필요 시만 사용)
  private List<MedicationResponseDTO> medications;

  private MeasurementStatus status;

  private Double height;
  private Double weight;
  private Integer bloodPressureSystolic;
  private Integer bloodPressureDiastolic;
  private Double bloodSugar;
  private Double sleepHours;

  private LocalDateTime measuredDate; // 측정일시
  private LocalDateTime regDate;      // 등록일
  private LocalDateTime modDate;      // 수정일

  private Double bmi;
  private String bmiStatus;
  private String bloodPressureStatus;
  private String bloodSugarStatus;
  private String sleepStatus;
  private String summary;

  private Integer healthScore;

  // 주간 트렌드용 필드 추가 (여기부터)
  private Double weightChange;          // 체중 변화량 (kg)
  private String weightTrend;           // "up", "down", "stable"
  private Double bloodSugarChange;      // 혈당 변화량
  private String bloodSugarTrend;       // "up", "down", "stable"
  private Double sleepHoursChange;      // 수면 변화량 (시간)
  private String sleepTrend;            // "up", "down", "stable"

}
