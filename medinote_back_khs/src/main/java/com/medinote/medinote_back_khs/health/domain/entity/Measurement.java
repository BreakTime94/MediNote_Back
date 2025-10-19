package com.medinote.medinote_back_khs.health.domain.entity;

import com.medinote.medinote_back_khs.common.entity.BaseEntity;
import com.medinote.medinote_back_khs.health.domain.enums.DrinkingTypeStatus;
import com.medinote.medinote_back_khs.health.domain.enums.GenderStatus;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tbl_health_measurement")
public class Measurement extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  // ===== 기본 정보 =====
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GenderStatus gender;

  private LocalDate birthDate; // 생년월일 (자동 연령 계산용)

  @Transient  // 🔥 DB 저장 안 함, 계산용
  private Integer age;

  @Transient  // 🔥 DB에 저장 안 함, 계산 결과만 응답 DTO로 보냄
  private String ageGroup;

  // ===== 생활 습관 =====
  @Column(nullable = false)
  private boolean smoking;

  @Column(nullable = false)
  private boolean drinking;

  @Enumerated(EnumType.STRING)
  private DrinkingTypeStatus drinkingType;

  private Integer drinkingPerWeek;  // 주당 음주 횟수
  private Integer drinkingPerOnce;  // 1회 음주량 (숫자만, 단위는 drinkingType에 따라 결정)

  // ===== 질환 정보 =====
  @Column(nullable = false)
  private boolean chronicDiseaseYn;

  @Column(nullable = false)
  private boolean allergyYn;

  @Column(nullable = false)
  private boolean medicationYn;

  // ===== 신체 측정 =====
  private Double height;
  private Double weight;
  private Integer bloodPressureSystolic;
  private Integer bloodPressureDiastolic;
  private Double bloodSugar;
  private Double sleepHours;

  // ===== 🔥 건강 점수 추가 =====
  private Integer healthScore;  // 건강 점수 (0~100점)

  // ===== 메타 정보 =====
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MeasurementStatus status;

  @Column(nullable = false)
  private LocalDateTime measuredDate;

  // ===== 🔥 생명주기 콜백: 저장 전 나이/연령대 자동 계산 =====
  @PrePersist
  @PreUpdate
  public void calculateAgeInfo() {
    if (this.birthDate != null) {
      this.age = calculateAge(this.birthDate);
      this.ageGroup = getAgeGroup(this.age);
    }
  }

  // 🔥 나이 계산 메서드
  private Integer calculateAge(LocalDate birthDate) {
    if (birthDate == null) return null;
    LocalDate today = LocalDate.now();
    int age = today.getYear() - birthDate.getYear();

    // 생일이 지나지 않았으면 -1
    if (today.getMonthValue() < birthDate.getMonthValue() ||
            (today.getMonthValue() == birthDate.getMonthValue() &&
                    today.getDayOfMonth() < birthDate.getDayOfMonth())) {
      age--;
    }
    return age;
  }

  // 🔥 연령대 계산 메서드
  private String getAgeGroup(Integer age) {
    if (age == null) return null;
    if (age < 10) return "10세 미만";
    if (age < 20) return "10대";
    if (age < 30) return "20대";
    if (age < 40) return "30대";
    if (age < 50) return "40대";
    if (age < 60) return "50대";
    if (age < 70) return "60대";
    return "70대 이상";
  }

  // 🔥 주종에 따른 단위 반환 (Transient 메서드)
  @Transient
  public String getDrinkingUnit() {
    if (drinkingType == null) return "";

    return switch (drinkingType) {
      case BEER -> "캔";
      case SOJU, WINE, WHISKY, COCKTAIL -> "잔";
      case MAKGEOLLI -> "컵";
      case ETC -> "회";
      default -> "";
    };
  }
}