package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MeasurementRequestDTO;
import com.medinote.medinote_back_khs.health.domain.dto.MeasurementResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.enums.MeasurementStatus;
import com.medinote.medinote_back_khs.health.domain.mapper.MeasurementMapper;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
import com.medinote.medinote_back_khs.health.domain.mapper.MemberMedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MeasurementRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import com.medinote.medinote_back_khs.health.domain.repository.MemberMedicationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter @Setter
@Slf4j
public class MeasurementService {
  //DTO 받아서 Entity로 변환 -> DB 저장
  //DTO → Entity로 변환(MapStruct) → repository.save(entity) 호출.

  private final MeasurementRepository measurementRepository;
  private final MeasurementMapper measurementMapper;
  private final MemberMedicationRepository memberMedicationRepository;
  private final MedicationRepository medicationRepository;
  private final MemberMedicationMapper memberMedicationMapper;
  private final MedicationMapper medicationMapper;


  // 등록 (create)
  @Transactional
  public MeasurementResponseDTO saveMeasurement(MeasurementRequestDTO dto, Long memberId) {
    Measurement entity = measurementMapper.toEntity(dto, memberId);
    Measurement saved = measurementRepository.save(entity);

    MeasurementResponseDTO response = measurementMapper.toResponseDTO(saved);

    // 복용약 조회 후 세팅
//    List<MemberMedication> meds = memberMedicationRepository.findByMemberId(memberId);
//    List<MedicationResponseDTO> medDtos = meds.stream()
//            .map(mm -> {
//              Medication med = medicationRepository.findById(mm.getMedicationId())
//                      .orElseThrow(() -> new IllegalArgumentException("해당 약 존재하지 않음"));
//              //한 개만 넘긴다
//              return medicationMapper.toResponseDTO(med);
//            })
//            .toList();
//
//    response.setMedications(medDtos);
//
//    return response;
    if (dto.getMedicationIds() != null && !dto.getMedicationIds().isEmpty()) {
      List<String> medicationNames = dto.getMedicationIds().stream()
              .map(id -> medicationRepository.findById(id)
                      .map(Medication::getNameKo)
                      .orElse("약품명 없음"))
              .toList();
      response.setMedicationNames(medicationNames);
    }

    //건강상태평가
    response = evaluateHealthStatus(response);

    return response;
  }

  // 단일 조회 (read)
  @Transactional(readOnly = true)
  public MeasurementResponseDTO findById(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Id not found: " + id));
    return measurementMapper.toResponseDTO(entity);
  }

  // 수정 (update)
  @Transactional
  public MeasurementResponseDTO addNewVersion(Long memberId, MeasurementRequestDTO dto) {
    // 새 이력 저장 (saveMeasurement 로직 재활용)
    return saveMeasurement(dto, memberId);
  }

  // 삭제 (delete)
  @Transactional
  public void deactivateMeasurement(Long id) {
    Measurement entity = measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 데이터가 없습니다."));
    entity.setStatus(MeasurementStatus.INACTIVE);
    measurementRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<MeasurementResponseDTO> getMeasurementList(Long memberId) {
    List<Measurement> list = measurementRepository.findByMemberIdOrderByMeasuredDateDesc(memberId);
    return list.stream()
            .map(measurementMapper::toResponseDTO)
            .toList();
  }

  //==================================== 개인건강정보리스트관련 로직===========================================
  private MeasurementResponseDTO evaluateHealthStatus(MeasurementResponseDTO response) {

    // BMI
    if (response.getHeight() != null && response.getWeight() != null) {
      double heightM = response.getHeight() / 100.0;
      double bmi = response.getWeight() / (heightM * heightM);
      bmi = Math.round(bmi * 10) / 10.0;

      response.setBmi(bmi);

      String bmiStatus;
      if (bmi < 18.5) bmiStatus = "저체중";
      else if (bmi < 23) bmiStatus = "정상";
      else if (bmi < 25) bmiStatus = "과체중";
      else bmiStatus = "비만";

      response.setBmiStatus(bmiStatus);
    }

    // 혈압
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

    // 혈당
    if (response.getBloodSugar() != null) {
      double sugar = response.getBloodSugar();
      String sugarStatus;
      if (sugar < 70) sugarStatus = "저혈당";
      else if (sugar <= 99) sugarStatus = "정상";
      else if (sugar <= 125) sugarStatus = "공복혈당장애";
      else sugarStatus = "당뇨 의심";
      response.setBloodSugarStatus(sugarStatus);
    }

    // 수면
    if (response.getSleepHours() != null) {
      double hours = response.getSleepHours();
      String sleepStatus;
      if (hours < 5) sleepStatus = "수면 부족";
      else if (hours <= 8) sleepStatus = "적정 수면";
      else sleepStatus = "수면 과다";
      response.setSleepStatus(sleepStatus);
    }

    // ✅ Health Score 계산
    int score = 100;

    switch (response.getBmiStatus()) {
      case "정상" -> score -= 0;
      case "저체중", "과체중" -> score -= 10;
      case "비만" -> score -= 20;
    }

    switch (response.getBloodSugarStatus()) {
      case "정상" -> score -= 0;
      case "저혈당", "공복혈당장애" -> score -= 10;
      case "당뇨 의심" -> score -= 20;
    }

    switch (response.getSleepStatus()) {
      case "적정 수면" -> score -= 0;
      case "수면 부족" -> score -= 10;
      case "수면 과다" -> score -= 5;
    }

    if (response.isSmoking()) score -= 10;
    if (response.isDrinking() && response.getDrinkingPerWeek() != null && response.getDrinkingPerWeek() > 3)
      score -= 5;

    if (score < 0) score = 0;

    response.setHealthScore(score);

    // ✅ summary 문장 보완 (기존 getStatus → getBmiStatus)
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

  //기간별 차트 데이터 조회
  @Transactional(readOnly = true)
  public List<MeasurementResponseDTO> getChartData(Long memberId, String period) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDate;

    if(period == null || period.isBlank()) {
      period = "week";
    }

    switch (period.toLowerCase()) {
      case "week":
        startDate = now.minusWeeks(1);
        break;
      case "month":
        startDate = now.minusMonths(1);
        break;
      case "quarter":
        startDate = now.minusMonths(3);
        break;
      case "half":
        startDate = now.minusMonths(6);
        break;
      case "year":
        startDate = now.minusYears(1);
        break;
      default:
        startDate = now.minusWeeks(1);
    }

    //회원의 측정 데이터 중 measuredDate가 기간 내인 것만 필터링
    List<Measurement> list = measurementRepository.findByMemberIdOrderByMeasuredDateDesc(memberId);

    List<MeasurementResponseDTO> filtered = list.stream()
            .filter(m -> m.getMeasuredDate() != null && m.getMeasuredDate().isAfter(startDate))
            .map(measurementMapper::toResponseDTO)
            .map(this::evaluateHealthStatus)
            .sorted((a, b) -> a.getMeasuredDate().compareTo(b.getMeasuredDate())) // 날짜 오름차순 정렬
            .collect(Collectors.toList());

    return filtered;

  }


  //index 의 카드형 ui 생성 메서드
  @Transactional(readOnly = true)
  public MeasurementResponseDTO getLatestSummary(Long memberId) {
    // 회원의 최근 측정 데이터 1건만 가져오기
    Measurement latest = measurementRepository
            .findTopByMemberIdOrderByMeasuredDateDesc(memberId)
            .orElseThrow(() -> new IllegalArgumentException("최근 측정 데이터가 없습니다."));

    // DTO 변환
    MeasurementResponseDTO response = measurementMapper.toResponseDTO(latest);

    // 건강상태 평가 (BMI, 혈당, 수면)
    MeasurementResponseDTO evaluated = evaluateHealthStatus(response);

    // 요약 문장 생성 (혈압 → 제외, 수면 강조)
    StringBuilder summary = new StringBuilder();

    summary.append("BMI: ")
            .append(evaluated.getBmiStatus() != null ? evaluated.getBmiStatus() : "정보 없음")
            .append(" / 혈당: ")
            .append(evaluated.getBloodSugarStatus() != null ? evaluated.getBloodSugarStatus() : "정보 없음")
            .append(" / 수면: ")
            .append(evaluated.getSleepStatus() != null ? evaluated.getSleepStatus() : "정보 없음");

    evaluated.setSummary(summary.toString());
    return evaluated;
}
}