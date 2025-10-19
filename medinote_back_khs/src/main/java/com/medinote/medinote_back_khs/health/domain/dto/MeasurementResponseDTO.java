package com.medinote.medinote_back_khs.health.domain.dto;

import com.medinote.medinote_back_khs.health.domain.enums.DrinkingTypeStatus;
import com.medinote.medinote_back_khs.health.domain.enums.GenderStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeasurementResponseDTO {

  // ===== 기본 정보 =====
  private Long id;
  private Long memberId;
  private LocalDateTime measuredDate;

  // ===== 연령 정보 =====
  private LocalDate birthDate;          // 생년월일
  private Integer age;                  // 만 나이
  private String ageGroup;              // 연령대 (예: "30대")

  // ===== 신체 정보 =====
  private GenderStatus gender;
  private Double height;
  private Double weight;
  private Double bmi;
  private String bmiStatus;

  // ===== 생활 습관 =====
  private Boolean smoking;
  private Boolean drinking;
  private DrinkingTypeStatus drinkingType;          // 주종 (BEER, SOJU 등)
  private Integer drinkingPerWeek;      // 주당 음주 횟수
  private Integer drinkingPerOnce;      // 1회 음주량 (숫자만)

  // ===== 건강 수치 =====
  private Integer bloodPressureSystolic;
  private Integer bloodPressureDiastolic;
  private String bloodPressureStatus;
  private Double bloodSugar;
  private String bloodSugarStatus;
  private Double sleepHours;
  private String sleepStatus;

  // ===== 질환 정보 =====
  private Boolean chronicDiseaseYn;
  private List<Long> chronicDiseaseIds;
  private List<String> chronicDiseaseNames;

  private Boolean allergyYn;
  private List<Long> allergyIds;
  private List<String> allergyNames;

  private Boolean medicationYn;
  private List<Long> medicationIds;
  private List<String> medicationNames;

  // ===== 건강 점수 =====
  private Integer healthScore;          // 건강 점수 (0~100)
  private String healthGrade;           // 등급 (A+, A, B+, B, C+, C, D)
  private String healthGradeText;       // 등급 텍스트 (매우 건강, 건강 등)

  // ===== 요약 및 트렌드 =====
  private String summary;

  private Double weightChange;
  private String weightTrend;

  private Double bloodSugarChange;
  private String bloodSugarTrend;

  private Double sleepHoursChange;
  private String sleepTrend;

  // ===== 메타 정보 =====
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // ===== 헬퍼 메서드: 주종에 따른 단위 반환 =====
  public String getDrinkingUnit() {
    if (drinkingType == null) return "";

    return switch (drinkingType) {
      case BEER -> "캔";
      case SOJU, WINE, WHISKY, COCKTAIL -> "잔";
      case MAKGEOLLI -> "컵";
      case ETC -> "회";
    };
  }
}