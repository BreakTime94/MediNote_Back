package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.MemberChronicDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberChronicDiseaseRepository extends JpaRepository<MemberChronicDisease, Long> {

  // 특정 회원의 전체 기저질환 조회
  List<MemberChronicDisease> findByMemberId(Long memberId);

  // 특정 회원이 특정 기저질환을 가지고 있는지 확인
  Optional<MemberChronicDisease> findByMemberIdAndChronicDiseaseId(Long memberId, Long chronicDiseaseId);


  @Modifying
  @Transactional
  void deleteByMemberId(Long memberId);
}
