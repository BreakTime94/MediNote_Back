package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
  boolean existsByCode(String code);

  // 알러지 이름 검색 (한글)
  List<Allergy> findByNameKoContainingIgnoreCase(String keyword);
}
