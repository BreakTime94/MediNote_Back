package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
  boolean existsByDrugCode(String drugCode);

  // 약이름 검색 (대소문자 구분 안 함)
  List<Medication> findByNameKoContaining(String keyword);

  //약품 단일 조회(복용약 등록 시 medicationId 확인)
//  Optional<Medication> findById(Long id);

}
