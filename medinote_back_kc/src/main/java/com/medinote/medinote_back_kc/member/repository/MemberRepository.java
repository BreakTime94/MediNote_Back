package com.medinote.medinote_back_kc.member.repository;

import com.medinote.medinote_back_kc.member.domain.dto.MemberResponseDTO;
import com.medinote.medinote_back_kc.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  @Modifying
  @Query("update Member m set m.status = 'DELETED' where m.email = :email")
  void softDelete(String email);

}
