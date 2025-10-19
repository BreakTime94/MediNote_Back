package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.*;
import com.medinote.medinote_back_khs.health.domain.enums.DrinkingTypeStatus;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
import com.medinote.medinote_back_khs.health.domain.enums.GenderStatus;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasurementService {

  private final MeasurementRepository measurementRepository;
  private final MeasurementMapper measurementMapper;

  // 중간 테이블 Repository
  private final MemberMedicationRepository memberMedicationRepository;
  private final MemberAllergyRepository memberAllergyRepository;
  private final MemberChronicDiseaseRepository memberChronicDiseaseRepository;

  // 마스터 데이터 Repository
  private final MedicationRepository medicationRepository;
  private final AllergyRepository allergyRepository;
  private final ChronicDiseaseRepository chronicDiseaseRepository;

  // ================================ CRUD ================================

  // 등록 (create)
  @Transactional
  public MeasurementResponseDTO saveMeasurement(MeasurementRequestDTO dto, Long memberId) {
    log.info(">>> saveMeasurement 시작 - memberId: {}", memberId);

    // 1. Measurement 저장
    Measurement entity = measurementMapper.toEntity(dto, memberId);
    Measurement saved = measurementRepository.save(entity);
    log.info(">>> Measurement 저장 완료 - id: {}", saved.getId());

    // 2. 중간 테이블에 알러지/기저질환/복용약 저장
    saveHealthRelations(memberId, dto);

    // 3. Response 생성
    MeasurementResponseDTO response = measurementMapper.toResponseDTO(saved);

    // 4. 복용약 이름 세팅
    setMedicationNames(response, dto.getMedicationIds());

    // 5. 건강상태 평가
    response = evaluateHealthStatus(response);

    log.info(">>> saveMeasurement 완료");
    return response;
  }

  // 단일 조회 (read)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO findById(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));
    return measurementMapper.toResponseDTO(entity);
  }

  // 수정 (update) - 새 이력 생성
  @Transactional
  public MeasurementResponseDTO addNewVersion(Long memberId, MeasurementRequestDTO dto) {
    log.info(">>> addNewVersion - 새 이력 저장");
    return saveMeasurement(dto, memberId);
  }

  // 삭제 (delete) - 논리 삭제
  @Transactional
  public void deactivateMeasurement(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 데이터가 없습니다."));
    entity.setStatus(MeasurementStatus.INACTIVE);
    measurementRepository.save(entity);
  }

  // ================================ 리스트 조회 ================================

  // 회원의 전체 측정 리스트 조회
  @Transactional(readOnly = true)
  public List<MeasurementResponseDTO> getMeasurementList(Long memberId) {
    List<Measurement> list = measurementRepository.findByMemberIdOrderByMeasuredDateDesc(memberId);

    return list.stream()
            .map(m -> {
              MeasurementResponseDTO dto = measurementMapper.toResponseDTO(m);

              // 복용약/알러지/기저질환 정보 추가
              loadHealthRelations(dto, memberId, m);

              return dto;
            })
            .toList();
  }

  // 기간별 차트 데이터 조회
  @Transactional(readOnly = true)
  public List<MeasurementResponseDTO> getChartData(Long memberId, String period) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDate = calculateStartDate(period, now);

    List<Measurement> list = measurementRepository.findByMemberIdOrderByMeasuredDateDesc(memberId);

    return list.stream()
            .filter(m -> m.getMeasuredDate() != null && m.getMeasuredDate().isAfter(startDate))
            .map(measurementMapper::toResponseDTO)
            .map(this::evaluateHealthStatus)
            .sorted((a, b) -> a.getMeasuredDate().compareTo(b.getMeasuredDate()))
            .collect(Collectors.toList());
  }

  // 최근 측정 요약 (메인 대시보드용)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO getLatestSummary(Long memberId) {
    // 최근 측정 데이터
    Measurement latest = measurementRepository
            .findTopByMemberIdOrderByMeasuredDateDesc(memberId)
            .orElseThrow(() -> new IllegalArgumentException("최근 측정 데이터가 없습니다."));

    // 이전 데이터 (변화량 계산용)
    Measurement previousData = measurementRepository
            .findTopByMemberIdAndMeasuredDateBeforeOrderByMeasuredDateDesc(
                    memberId,
                    latest.getMeasuredDate()
            )
            .orElse(null);

    MeasurementResponseDTO response = measurementMapper.toResponseDTO(latest);
    response = evaluateHealthStatus(response);

    // 변화량 계산
    calculateTrends(response, latest, previousData);

    // 요약 문장 생성
    generateSummary(response);

    return response;
  }

  // 최신 측정 데이터 1개 조회 (수정 페이지용)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO getLatestMeasurement(Long memberId) {
    log.info(">>> getLatestMeasurement 시작 - memberId: {}", memberId);

    Measurement latest = measurementRepository
            .findTopByMemberIdOrderByMeasuredDateDesc(memberId)
            .orElseThrow(() -> new IllegalArgumentException("측정 데이터가 없습니다."));

    MeasurementResponseDTO response = measurementMapper.toResponseDTO(latest);

    // 복용약/알러지/기저질환 정보 추가
    loadHealthRelations(response, memberId, latest);

    // 건강상태 평가
    MeasurementResponseDTO evaluated = evaluateHealthStatus(response);

    log.info(">>> getLatestMeasurement 완료");
    return evaluated;
  }

  // ================================ Private 헬퍼 메서드 ================================

  /**
   * 중간 테이블에 알러지/기저질환/복용약 저장
   */
  private void saveHealthRelations(Long memberId, MeasurementRequestDTO dto) {
    log.info(">>> saveHealthRelations 시작");

    // 알러지 저장
    if (Boolean.TRUE.equals(dto.getAllergyYn()) && dto.getAllergyIds() != null && !dto.getAllergyIds().isEmpty()) {
      memberAllergyRepository.deleteByMemberId(memberId);
      memberAllergyRepository.flush();

      for (Long allergyId : dto.getAllergyIds()) {
        if (allergyId != null) {
          MemberAllergy ma = new MemberAllergy();
          ma.setMemberId(memberId);
          ma.setAllergyId(allergyId);
          memberAllergyRepository.save(ma);
        }
      }
    } else if (Boolean.FALSE.equals(dto.getAllergyYn())) {
      memberAllergyRepository.deleteByMemberId(memberId);
      memberAllergyRepository.flush();
    }

    // 기저질환 저장
    if (Boolean.TRUE.equals(dto.getChronicDiseaseYn()) && dto.getChronicDiseaseIds() != null && !dto.getChronicDiseaseIds().isEmpty()) {
      memberChronicDiseaseRepository.deleteByMemberId(memberId);
      memberChronicDiseaseRepository.flush();

      for (Long diseaseId : dto.getChronicDiseaseIds()) {
        if (diseaseId != null) {
          MemberChronicDisease md = new MemberChronicDisease();
          md.setMemberId(memberId);
          md.setChronicDiseaseId(diseaseId);
          memberChronicDiseaseRepository.save(md);
        }
      }
    } else if (Boolean.FALSE.equals(dto.getChronicDiseaseYn())) {
      memberChronicDiseaseRepository.deleteByMemberId(memberId);
      memberChronicDiseaseRepository.flush();
    }

    // 복용약 저장
    if (Boolean.TRUE.equals(dto.getMedicationYn()) && dto.getMedicationIds() != null && !dto.getMedicationIds().isEmpty()) {
      memberMedicationRepository.deleteByMemberId(memberId);
      memberMedicationRepository.flush();

      for (Long medicationId : dto.getMedicationIds()) {
        if (medicationId != null) {
          MemberMedication mm = new MemberMedication();
          mm.setMemberId(memberId);
          mm.setMedicationId(medicationId);
          memberMedicationRepository.save(mm);
        }
      }
    } else if (Boolean.FALSE.equals(dto.getMedicationYn())) {
      memberMedicationRepository.deleteByMemberId(memberId);
      memberMedicationRepository.flush();
    }

    log.info(">>> saveHealthRelations 완료");
  }

  /**
   * Response에 복용약 이름 세팅
   */
  private void setMedicationNames(MeasurementResponseDTO response, List<Long> medicationIds) {
    if (medicationIds != null && !medicationIds.isEmpty()) {
      List<String> medicationNames = medicationIds.stream()
              .filter(id -> id != null)
              .map(id -> medicationRepository.findById(id)
                      .map(Medication::getNameKo)
                      .orElse("약품명 없음"))
              .toList();
      response.setMedicationNames(medicationNames);
    }
  }

  /**
   * 리스트 조회 시 알러지/기저질환/복용약 정보 로드
   */
  private void loadHealthRelations(MeasurementResponseDTO dto, Long memberId, Measurement m) {

    // 복용약 정보
    if (m.isMedicationYn()) {
      List<MemberMedication> meds = memberMedicationRepository.findByMemberId(memberId);

      List<Long> medicationIds = meds.stream()
              .map(MemberMedication::getMedicationId)
              .filter(id -> id != null)
              .toList();
      dto.setMedicationIds(medicationIds);

      List<String> medicationNames = medicationIds.stream()
              .map(id -> medicationRepository.findById(id)
                      .map(Medication::getNameKo)
                      .orElse("약품명 없음"))
              .toList();
      dto.setMedicationNames(medicationNames);
    }

    // 알러지 정보
    if (m.isAllergyYn()) {
      List<MemberAllergy> allergies = memberAllergyRepository.findByMemberId(memberId);

      List<Long> allergyIds = allergies.stream()
              .map(MemberAllergy::getAllergyId)
              .filter(id -> id != null)
              .toList();
      dto.setAllergyIds(allergyIds);

      List<String> allergyNames = allergyIds.stream()
              .map(id -> allergyRepository.findById(id)
                      .map(Allergy::getNameKo)
                      .orElse("알러지명 없음"))
              .toList();
      dto.setAllergyNames(allergyNames);
    }

    // 기저질환 정보
    if (m.isChronicDiseaseYn()) {
      List<MemberChronicDisease> diseases = memberChronicDiseaseRepository.findByMemberId(memberId);

      List<Long> diseaseIds = diseases.stream()
              .map(MemberChronicDisease::getChronicDiseaseId)
              .filter(id -> id != null)
              .toList();
      dto.setChronicDiseaseIds(diseaseIds);

      List<String> diseaseNames = diseaseIds.stream()
              .map(id -> chronicDiseaseRepository.findById(id)
                      .map(ChronicDisease::getNameKo)
                      .orElse("질환명 없음"))
              .toList();
      dto.setChronicDiseaseNames(diseaseNames);
    }
  }

  // ================================ 건강상태 평가 (개선 버전) ================================

  /**
   * 건강상태 평가 - 연령대별 상세 점수 계산
   */
  private MeasurementResponseDTO evaluateHealthStatus(MeasurementResponseDTO response) {

    // ===== 1. BMI 계산 및 평가 =====
    if (response.getHeight() != null && response.getWeight() != null) {
      double heightM = response.getHeight() / 100.0;
      double bmi = Math.round((response.getWeight() / (heightM * heightM)) * 10) / 10.0;
      response.setBmi(bmi);

      String bmiStatus = evaluateBMI(bmi, response.getAgeGroup());
      response.setBmiStatus(bmiStatus);
    }

    // ===== 2. 혈압 평가 =====
    if (response.getBloodPressureSystolic() != null && response.getBloodPressureDiastolic() != null) {
      int sys = response.getBloodPressureSystolic();
      int dia = response.getBloodPressureDiastolic();

      String bpStatus = evaluateBloodPressure(sys, dia, response.getAgeGroup());
      response.setBloodPressureStatus(bpStatus);
    }

    // ===== 3. 혈당 평가 =====
    if (response.getBloodSugar() != null) {
      double sugar = response.getBloodSugar();

      String sugarStatus;
      if (sugar < 70) sugarStatus = "저혈당";
      else if (sugar <= 100) sugarStatus = "정상";
      else if (sugar <= 125) sugarStatus = "공복혈당장애";
      else sugarStatus = "당뇨 의심";
      response.setBloodSugarStatus(sugarStatus);
    }

    // ===== 4. 수면 평가 =====
    if (response.getSleepHours() != null) {
      double hours = response.getSleepHours();

      String sleepStatus = evaluateSleep(hours, response.getAgeGroup());
      response.setSleepStatus(sleepStatus);
    }

    // ===== 5. 건강 점수 계산 (개선된 로직) =====
    int score = calculateDetailedHealthScore(response);
    response.setHealthScore(Math.max(0, score));

    // ===== 6. 등급 계산 =====
    String grade = getHealthGrade(score);
    response.setHealthGrade(grade);
    response.setHealthGradeText(getHealthGradeText(score));

    // ===== 7. 요약 문장 =====
    response.setSummary(String.format(
            "BMI: %s / 혈압: %s / 혈당: %s / 수면: %s / 점수: %d점 (%s)",
            response.getBmiStatus() != null ? response.getBmiStatus() : "-",
            response.getBloodPressureStatus() != null ? response.getBloodPressureStatus() : "-",
            response.getBloodSugarStatus() != null ? response.getBloodSugarStatus() : "-",
            response.getSleepStatus() != null ? response.getSleepStatus() : "-",
            response.getHealthScore() != null ? response.getHealthScore() : 0,
            grade
    ));

    return response;
  }

  // ===== BMI 평가 (연령대별) =====
  private String evaluateBMI(double bmi, String ageGroup) {
    boolean isYoung = isYoungAge(ageGroup);
    boolean isMiddle = isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isYoung) {
      if (bmi < 16) return "매우 저체중";
      if (bmi < 18.5) return "저체중";
      if (bmi < 25) return "정상";
      if (bmi < 30) return "과체중";
      return "비만";
    } else if (isMiddle) {
      if (bmi < 18.5) return "저체중";
      if (bmi < 26) return "정상";
      if (bmi < 30) return "과체중";
      return "비만";
    } else if (isSenior) {
      if (bmi < 19) return "저체중";
      if (bmi < 27) return "정상";
      if (bmi < 30) return "과체중";
      return "비만";
    }

    // 연령대 정보 없을 경우 기본 기준
    if (bmi < 18.5) return "저체중";
    if (bmi < 23) return "정상";
    if (bmi < 25) return "과체중";
    return "비만";
  }

  // ===== 혈압 평가 (연령대별) =====
  private String evaluateBloodPressure(int sys, int dia, String ageGroup) {
    // 저혈압 (모든 연령 공통)
    if (sys < 90 || dia < 60) return "저혈압";

    boolean isYoung = isYoungAge(ageGroup);
    boolean isMiddle = isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isYoung) {
      if (sys <= 120 && dia <= 80) return "정상";
      if (sys <= 130 && dia <= 85) return "주의";
      if (sys <= 140 && dia <= 90) return "고혈압 전단계";
      return "고혈압";
    } else if (isMiddle) {
      if (sys <= 130 && dia <= 85) return "정상";
      if (sys <= 140 && dia <= 90) return "주의";
      if (sys <= 160 && dia <= 100) return "고혈압 1기";
      return "고혈압 2기";
    } else if (isSenior) {
      if (sys <= 140 && dia <= 90) return "정상";
      if (sys <= 160 && dia <= 100) return "경계";
      return "고혈압";
    }

    // 기본 기준
    if (sys <= 120 && dia <= 80) return "정상";
    if (sys <= 139 && dia <= 89) return "주의";
    return "고혈압";
  }

  // ===== 수면 평가 (연령대별) =====
  private String evaluateSleep(double hours, String ageGroup) {
    boolean isTeenager = ageGroup != null && (ageGroup.contains("10세 미만") || ageGroup.contains("10대"));
    boolean isAdult = isYoungAge(ageGroup) || isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isTeenager) {
      if (hours < 6) return "수면 부족";
      if (hours < 8) return "약간 부족";
      if (hours <= 10) return "적정 수면";
      return "수면 과다";
    } else if (isAdult) {
      if (hours < 5) return "심각한 부족";
      if (hours < 7) return "수면 부족";
      if (hours <= 9) return "적정 수면";
      return "수면 과다";
    } else if (isSenior) {
      if (hours < 6) return "수면 부족";
      if (hours <= 8) return "적정 수면";
      return "약간 과다";
    }

    // 기본 기준
    if (hours < 5) return "수면 부족";
    if (hours <= 8) return "적정 수면";
    return "수면 과다";
  }

  // ===== 상세 건강 점수 계산 (100점 만점) =====
  private int calculateDetailedHealthScore(MeasurementResponseDTO response) {
    int totalScore = 0;

    // 1. BMI 점수 (15점)
    totalScore += calculateBMIScore(response.getBmi(), response.getBmiStatus(), response.getAgeGroup());

    // 2. 혈압 점수 (20점)
    totalScore += calculateBloodPressureScore(response.getBloodPressureSystolic(),
            response.getBloodPressureDiastolic(),
            response.getAgeGroup());

    // 3. 혈당 점수 (15점)
    totalScore += calculateBloodSugarScore(response.getBloodSugar());

    // 4. 수면 점수 (10점)
    totalScore += calculateSleepScore(response.getSleepHours(), response.getAgeGroup());

    // 5. 흡연 점수 (15점)
    totalScore += (response.getSmoking() != null && response.getSmoking()) ? 0 : 15;

    // 6. 음주 점수 (15점)
    totalScore += calculateDrinkingScore(response);

    // 7. 기저질환 점수 (5점)
    totalScore += calculateChronicDiseaseScore(response.getChronicDiseaseYn(),
            response.getChronicDiseaseIds());

    // 8. 알러지 점수 (2점)
    totalScore += calculateAllergyScore(response.getAllergyYn(), response.getAllergyIds());

    // 9. 복용약 점수 (3점)
    totalScore += calculateMedicationScore(response.getMedicationYn(), response.getMedicationIds());

    return totalScore;
  }

  // ===== 개별 항목 점수 계산 메서드들 =====

  private int calculateBMIScore(Double bmi, String status, String ageGroup) {
    if (bmi == null || status == null) return 0;

    boolean isYoung = isYoungAge(ageGroup);
    boolean isMiddle = isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isYoung) {
      if (bmi < 16) return 5;
      if (bmi < 18.5) return 10;
      if (bmi < 25) return 15;
      if (bmi < 30) return 10;
      return 5;
    } else if (isMiddle) {
      if (bmi < 18.5) return 8;
      if (bmi < 26) return 15;
      if (bmi < 30) return 10;
      return 5;
    } else if (isSenior) {
      if (bmi < 19) return 8;
      if (bmi < 27) return 15;
      if (bmi < 30) return 12;
      return 8;
    }

    // 기본
    return status.equals("정상") ? 15 : status.equals("저체중") || status.equals("과체중") ? 10 : 5;
  }

  private int calculateBloodPressureScore(Integer sys, Integer dia, String ageGroup) {
    if (sys == null || dia == null) return 0;

    if (sys < 90 || dia < 60) return 10;

    boolean isYoung = isYoungAge(ageGroup);
    boolean isMiddle = isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isYoung) {
      if (sys <= 120 && dia <= 80) return 20;
      if (sys <= 130 && dia <= 85) return 15;
      if (sys <= 140 && dia <= 90) return 10;
      return 5;
    } else if (isMiddle) {
      if (sys <= 130 && dia <= 85) return 20;
      if (sys <= 140 && dia <= 90) return 15;
      if (sys <= 160 && dia <= 100) return 10;
      return 5;
    } else if (isSenior) {
      if (sys <= 140 && dia <= 90) return 20;
      if (sys <= 160 && dia <= 100) return 15;
      return 10;
    }

    // 기본
    if (sys <= 120 && dia <= 80) return 20;
    if (sys <= 139 && dia <= 89) return 15;
    return 10;
  }

  private int calculateBloodSugarScore(Double bloodSugar) {
    if (bloodSugar == null) return 0;

    if (bloodSugar < 70) return 8;
    if (bloodSugar <= 100) return 15;
    if (bloodSugar <= 125) return 10;
    if (bloodSugar <= 180) return 5;
    return 3;
  }

  private int calculateSleepScore(Double sleepHours, String ageGroup) {
    if (sleepHours == null) return 0;

    boolean isTeenager = ageGroup != null && (ageGroup.contains("10세 미만") || ageGroup.contains("10대"));
    boolean isAdult = isYoungAge(ageGroup) || isMiddleAge(ageGroup);
    boolean isSenior = isSeniorAge(ageGroup);

    if (isTeenager) {
      if (sleepHours < 6) return 3;
      if (sleepHours < 8) return 7;
      if (sleepHours <= 10) return 10;
      return 7;
    } else if (isAdult) {
      if (sleepHours < 5) return 3;
      if (sleepHours < 7) return 6;
      if (sleepHours <= 9) return 10;
      return 7;
    } else if (isSenior) {
      if (sleepHours < 6) return 5;
      if (sleepHours <= 8) return 10;
      return 8;
    }

    // 기본
    if (sleepHours < 5) return 3;
    if (sleepHours <= 8) return 10;
    return 7;
  }

  private int calculateDrinkingScore(MeasurementResponseDTO response) {
    if (response.getDrinking() == null || !response.getDrinking()) return 15;
    if (response.getDrinkingPerWeek() == null || response.getDrinkingPerOnce() == null) return 10;

    double weeklyAmount = response.getDrinkingPerWeek() * response.getDrinkingPerOnce();

    // 🔥 수정: String → Enum 비교
    boolean isFemale = response.getGender() != null && response.getGender() == GenderStatus.FEMALE;
    boolean isSenior = isSeniorAge(response.getAgeGroup());

    boolean isHighAlcohol = false;
    if (response.getDrinkingType() != null) {
      isHighAlcohol = response.getDrinkingType() == DrinkingTypeStatus.SOJU ||
              response.getDrinkingType() == DrinkingTypeStatus.WHISKY;
    }

    if (isHighAlcohol) {
      if (isFemale || isSenior) {
        if (weeklyAmount > 10) return 3;
        if (weeklyAmount > 7) return 8;
        if (weeklyAmount > 4) return 12;
        return 15;
      } else {
        if (weeklyAmount > 14) return 3;
        if (weeklyAmount > 10) return 8;
        if (weeklyAmount > 7) return 12;
        return 15;
      }
    } else {
      if (isFemale || isSenior) {
        if (weeklyAmount > 14) return 5;
        if (weeklyAmount > 10) return 10;
        return 15;
      } else {
        if (weeklyAmount > 21) return 5;
        if (weeklyAmount > 14) return 10;
        return 15;
      }
    }
  }

  private int calculateChronicDiseaseScore(Boolean hasDisease, List<Long> diseaseIds) {
    if (hasDisease == null || !hasDisease) return 5;

    int count = (diseaseIds != null) ? diseaseIds.size() : 0;
    if (count == 0) return 5;
    if (count == 1) return 4;
    if (count == 2) return 3;
    return 2;
  }

  private int calculateAllergyScore(Boolean hasAllergy, List<Long> allergyIds) {
    if (hasAllergy == null || !hasAllergy) return 2;

    int count = (allergyIds != null) ? allergyIds.size() : 0;
    if (count == 0) return 2;
    if (count <= 2) return 1;
    return 1;
  }
  private int calculateMedicationScore(Boolean hasMedication, List<Long> medicationIds) {
    if (hasMedication == null || !hasMedication) return 3;

    int count = (medicationIds != null) ? medicationIds.size() : 0;
    if (count == 0) return 3;
    if (count <= 2) return 2;
    if (count <= 4) return 2;
    return 1;
  }

  // ===== 헬퍼 메서드 =====

  private boolean isYoungAge(String ageGroup) {
    if (ageGroup == null) return false;
    return ageGroup.contains("20대") || ageGroup.contains("30대");
  }

  private boolean isMiddleAge(String ageGroup) {
    if (ageGroup == null) return false;
    return ageGroup.contains("40대") || ageGroup.contains("50대");
  }

  private boolean isSeniorAge(String ageGroup) {
    if (ageGroup == null) return false;
    return ageGroup.contains("60대") || ageGroup.contains("70대");
  }

  private String getHealthGrade(int score) {
    if (score >= 90) return "A+";
    if (score >= 80) return "A";
    if (score >= 70) return "B+";
    if (score >= 60) return "B";
    if (score >= 50) return "C+";
    if (score >= 40) return "C";
    return "D";
  }

  private String getHealthGradeText(int score) {
    if (score >= 90) return "매우 건강";
    if (score >= 80) return "건강";
    if (score >= 70) return "양호";
    if (score >= 60) return "보통";
    if (score >= 50) return "주의";
    if (score >= 40) return "관리필요";
    return "위험";
  }

  // ================================ 기존 헬퍼 메서드 ================================

  /**
   * 차트 기간 계산
   */
  private LocalDateTime calculateStartDate(String period, LocalDateTime now) {
    if (period == null || period.isBlank()) {
      period = "week";
    }

    return switch (period.toLowerCase()) {
      case "month" -> now.minusMonths(1);
      case "quarter" -> now.minusMonths(3);
      case "half" -> now.minusMonths(6);
      case "year" -> now.minusYears(1);
      default -> now.minusWeeks(1);
    };
  }

  /**
   * 이전 데이터 대비 변화량 계산
   */
  private void calculateTrends(MeasurementResponseDTO response, Measurement latest, Measurement previousData) {
    if (previousData == null) {
      response.setWeightChange(null);
      response.setWeightTrend("stable");
      response.setBloodSugarChange(null);
      response.setBloodSugarTrend("stable");
      response.setSleepHoursChange(null);
      response.setSleepTrend("stable");
      return;
    }

    // 체중 변화
    if (latest.getWeight() != null && previousData.getWeight() != null) {
      double weightDiff = latest.getWeight() - previousData.getWeight();
      response.setWeightChange(Math.round(weightDiff * 10) / 10.0);
      response.setWeightTrend(getTrend(weightDiff, 0.5));
    }

    // 혈당 변화
    if (latest.getBloodSugar() != null && previousData.getBloodSugar() != null) {
      double bloodSugarDiff = latest.getBloodSugar() - previousData.getBloodSugar();
      response.setBloodSugarChange(Math.round(bloodSugarDiff * 10) / 10.0);
      response.setBloodSugarTrend(getTrend(bloodSugarDiff, 5.0));
    }

    // 수면 변화
    if (latest.getSleepHours() != null && previousData.getSleepHours() != null) {
      double sleepDiff = latest.getSleepHours() - previousData.getSleepHours();
      response.setSleepHoursChange(Math.round(sleepDiff * 10) / 10.0);
      response.setSleepTrend(getTrend(sleepDiff, 0.5));
    }
  }

  /**
   * 트렌드 판단 (up/down/stable)
   */
  private String getTrend(double diff, double threshold) {
    if (Math.abs(diff) < threshold) return "stable";
    return diff > 0 ? "up" : "down";
  }

  /**
   * 요약 문장 생성
   */
  private void generateSummary(MeasurementResponseDTO response) {
    StringBuilder summary = new StringBuilder();
    summary.append("BMI: ")
            .append(response.getBmiStatus() != null ? response.getBmiStatus() : "정보 없음")
            .append(" / 혈당: ")
            .append(response.getBloodSugarStatus() != null ? response.getBloodSugarStatus() : "정보 없음")
            .append(" / 수면: ")
            .append(response.getSleepStatus() != null ? response.getSleepStatus() : "정보 없음");

    response.setSummary(summary.toString());
  }
}
