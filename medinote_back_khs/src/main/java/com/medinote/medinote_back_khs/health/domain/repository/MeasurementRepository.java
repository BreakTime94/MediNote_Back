package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
  List<Measurement> findByMemberIdOrderByMeasuredDateDesc(Long memberId);
  //DB 저장

  //가장 최근 데이터 한건 -> 카드형으로 출력
  Optional<Measurement> findTopByMemberIdOrderByMeasuredDateDesc(Long memberId);

  Optional<Measurement> findTopByMemberIdAndMeasuredDateBeforeOrderByMeasuredDateDesc(
          Long memberId, LocalDateTime date);

}
