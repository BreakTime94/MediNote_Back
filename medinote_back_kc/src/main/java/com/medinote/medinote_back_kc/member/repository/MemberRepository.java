package com.medinote.medinote_back_kc.member.repository;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  @Modifying
  @Query("update Member m set m.status = 'DELETED', m.deletedAt = current_timestamp where m.email = :email")
  void softDelete(String email);

  @Query("select m from Member m where m.email = :email")
  Optional<Member> findByEmail(String email);

  Optional<Member> findByExtraEmail(String email);

  //Social 가입용
  Optional<Member> findByEmailOrExtraEmail(String email, String extraEmail);

  boolean existsByEmail(String email);
  boolean existsByExtraEmail(String email);
  boolean existsByNickname(String nickname);
}
