package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.HealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthGoalRepository extends JpaRepository<HealthGoal, Long> {

  // memberId로 목표 조회
  Optional<HealthGoal> findByMemberId(Long memberId);

  // memberId로 목표 존재 여부 확인
  boolean existsByMemberId(Long memberId);

  // memberId로 목표 삭제
  void deleteByMemberId(Long memberId);
}