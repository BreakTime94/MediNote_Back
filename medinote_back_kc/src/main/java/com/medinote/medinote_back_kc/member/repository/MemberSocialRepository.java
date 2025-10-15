package com.medinote.medinote_back_kc.member.repository;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {

  Optional<MemberSocial> findByProviderAndProviderUserId(Provider provider, String providerUserId);

  List<MemberSocial> findByMemberId(Long memberId);

  boolean existsByMemberAndProvider(Member member, Provider provider);
}
