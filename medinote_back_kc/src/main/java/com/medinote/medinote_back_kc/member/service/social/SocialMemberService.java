package com.medinote.medinote_back_kc.member.service.social;

import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;

import java.util.Optional;

public interface SocialMemberService {
  void register (SocialRegisterRequestDTO dto);
  void remove (Long memberId);
  void disconnect(Long memberId, Provider provider);

  // 재연결 (connected_at 갱신, disconnected_at null 처리)
  void reconnect(Long memberId, Provider provider);

  // 조회 (로그인 시 필요)
  Optional<MemberSocial> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}

