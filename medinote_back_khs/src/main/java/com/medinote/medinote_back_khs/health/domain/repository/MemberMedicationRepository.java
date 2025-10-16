package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.MemberMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberMedicationRepository extends JpaRepository<MemberMedication, Long> {

  //특정 회원이 복용하는 약 전체 조회
  List<MemberMedication> findByMemberId(Long memberId);

  //특정 회원이 현재 복용중인 약만 조회
  List<MemberMedication> findByMemberIdAndIsCurrentTrue(Long memberId);

  //특정 회원이 특정 약을 먹고 있는지 -> memberId + medicationId
  Optional<MemberMedication> findByMemberIdAndMedicationId(Long memberId, Long medicationId);

  @Modifying
  @Transactional
  void deleteByMemberId(Long memberId);
}
