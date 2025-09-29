package com.medinote.medinote_back_kc.member.domain.dto.social;

import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialRegisterRequestDTO {
  Provider provider;
  String providerUserId;
  String email;
  String profileImageUrl;
  String profileMimeType;
  String nickname;
  String rawProfileJson;
  String extraEmail;
}
