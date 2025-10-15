package com.medinote.medinote_back_khs.health.domain.repository;

import com.medinote.medinote_back_khs.health.domain.entity.MemberAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberAllergyRepository extends JpaRepository<MemberAllergy,Long> {

  // 특정 회원의 전체 알러지 조회
  List<MemberAllergy> findByMemberId(Long memberId);

  // 특정 회원이 특정 알러지를 가지고 있는지 확인
  Optional<MemberAllergy> findByMemberIdAndAllergyId(Long memberId, Long allergyId);


  @Modifying
  @Transactional
  void deleteByMemberId(Long memberId);
}
