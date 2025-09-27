package com.medinote.medinote_back_kc.member.service.social;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;

import java.util.Optional;

public interface MemberSocialService {

  MemberDTO register (SocialRegisterRequestDTO dto);

  // 연결 (최초 연결 + 재연결)
  void connect(Long memberId, SocialRegisterRequestDTO dto);

  // SoftDelete 느낌 disconnect가 null 이 아니면 해제로 간주, 소셜로 최초가입한 계정은 최초 소셜 계정 해제 불가
  void disconnect(Long memberId, Provider provider);

  // SocialMember 테이블에 존재하는지 여부
  boolean isSocialMember(SocialRegisterRequestDTO dto);

  //컨트롤러 단에서, isSocialMember로 분기 처리
  MemberDTO getSocialMember(SocialRegisterRequestDTO dto);


}

