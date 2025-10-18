package com.medinote.medinote_back_khs.health.service;

import com.medinote.medinote_back_khs.health.domain.dto.MedicationResponseDTO;
import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import com.medinote.medinote_back_khs.health.domain.mapper.MedicationMapper;
import com.medinote.medinote_back_khs.health.domain.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationService {
  private final MedicationRepository medicationRepository;
  private final MedicationMapper medicationMapper;

  //전체약품 리스트(페이징)
  @Transactional
  public Page<MedicationResponseDTO> getMedicationList(Pageable pageable) {
    Page<Medication> page = medicationRepository.findAll(pageable);
    return page.map(medicationMapper::toResponseDTO);
  }

  /**
   * 2. 키워드 검색 (약품명+회사명 기반 nameKoCompany 컬럼 사용)
   */
  @Transactional(readOnly = true)
  public List<MedicationResponseDTO> searchMedications(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      log.warn("⚠️ 빈 검색어 요청: '{}'", keyword);
      return List.of();
    }

    List<Medication> meds = medicationRepository.findByNameKoCompanyContainingIgnoreCase(keyword.trim());
    log.info("🔍 '{}' 검색 결과 {}건", keyword, meds.size());
    return meds.stream()
            .map(medicationMapper::toResponseDTO)
            .toList();
  }

  /**
   *  3. 단일 약품 조회
   */
  @Transactional(readOnly = true)
  public MedicationResponseDTO getMedicationById(Long id) {
    Medication med = medicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 약품이 존재하지 않습니다. (id=" + id + ")"));
    return medicationMapper.toResponseDTO(med);
  }
}
