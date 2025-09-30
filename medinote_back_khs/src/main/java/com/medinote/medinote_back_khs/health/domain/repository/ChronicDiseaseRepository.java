package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.ChronicDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChronicDiseaseRepository extends JpaRepository<ChronicDisease, Long> {

  boolean existsByCode(String code);

  // 기저질환 이름 검색 (한글)
  List<ChronicDisease> findByNameKoContainingIgnoreCase(String keyword);
}
