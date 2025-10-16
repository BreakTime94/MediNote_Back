package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.*;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
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
        if (allergyId != null) { // ✅ null 체크
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
        if (diseaseId != null) { // ✅ null 체크
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
        if (medicationId != null) { // ✅ null 체크
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
              .filter(id -> id != null) // ✅ null 필터링
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

  /**
   * 건강상태 평가 (BMI, 혈압, 혈당, 수면, 건강점수)
   */
  private MeasurementResponseDTO evaluateHealthStatus(MeasurementResponseDTO response) {

    // BMI 계산
    if (response.getHeight() != null && response.getWeight() != null) {
      double heightM = response.getHeight() / 100.0;
      double bmi = Math.round((response.getWeight() / (heightM * heightM)) * 10) / 10.0;
      response.setBmi(bmi);

      String bmiStatus;
      if (bmi < 18.5) bmiStatus = "저체중";
      else if (bmi < 23) bmiStatus = "정상";
      else if (bmi < 25) bmiStatus = "과체중";
      else bmiStatus = "비만";
      response.setBmiStatus(bmiStatus);
    }

    // 혈압 평가
    if (response.getBloodPressureSystolic() != null && response.getBloodPressureDiastolic() != null) {
      int sys = response.getBloodPressureSystolic();
      int dia = response.getBloodPressureDiastolic();

      String bpStatus;
      if (sys < 90 || dia < 60) bpStatus = "저혈압";
      else if (sys <= 120 && dia <= 80) bpStatus = "정상";
      else if (sys <= 139 && dia <= 89) bpStatus = "주의";
      else bpStatus = "고혈압";
      response.setBloodPressureStatus(bpStatus);
    }

    // 혈당 평가
    if (response.getBloodSugar() != null) {
      double sugar = response.getBloodSugar();

      String sugarStatus;
      if (sugar < 70) sugarStatus = "저혈당";
      else if (sugar <= 99) sugarStatus = "정상";
      else if (sugar <= 125) sugarStatus = "공복혈당장애";
      else sugarStatus = "당뇨 의심";
      response.setBloodSugarStatus(sugarStatus);
    }

    // 수면 평가
    if (response.getSleepHours() != null) {
      double hours = response.getSleepHours();

      String sleepStatus;
      if (hours < 5) sleepStatus = "수면 부족";
      else if (hours <= 8) sleepStatus = "적정 수면";
      else sleepStatus = "수면 과다";
      response.setSleepStatus(sleepStatus);
    }

    // 건강 점수 계산
    int score = 100;

    if (response.getBmiStatus() != null) {
      switch (response.getBmiStatus()) {
        case "정상" -> score -= 0;
        case "저체중", "과체중" -> score -= 10;
        case "비만" -> score -= 20;
      }
    }

    if (response.getBloodSugarStatus() != null) {
      switch (response.getBloodSugarStatus()) {
        case "정상" -> score -= 0;
        case "저혈당", "공복혈당장애" -> score -= 10;
        case "당뇨 의심" -> score -= 20;
      }
    }

    if (response.getSleepStatus() != null) {
      switch (response.getSleepStatus()) {
        case "적정 수면" -> score -= 0;
        case "수면 부족" -> score -= 10;
        case "수면 과다" -> score -= 5;
      }
    }

    if (response.isSmoking()) score -= 10;
    if (response.isDrinking() && response.getDrinkingPerWeek() != null && response.getDrinkingPerWeek() > 3) {
      score -= 5;
    }

    response.setHealthScore(Math.max(score, 0));

    // 요약 문장
    response.setSummary(String.format(
            "BMI: %s / 혈압: %s / 혈당: %s / 수면: %s / 점수: %d점",
            response.getBmiStatus() != null ? response.getBmiStatus() : "-",
            response.getBloodPressureStatus() != null ? response.getBloodPressureStatus() : "-",
            response.getBloodSugarStatus() != null ? response.getBloodSugarStatus() : "-",
            response.getSleepStatus() != null ? response.getSleepStatus() : "-",
            response.getHealthScore() != null ? response.getHealthScore() : 0
    ));

    return response;
  }

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

  /* 트렌드 판단 (up/down/stable)*/
  private String getTrend(double diff, double threshold) {
    if (Math.abs(diff) < threshold) return "stable";
    return diff > 0 ? "up" : "down";
  }

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