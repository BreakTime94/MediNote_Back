package com.medinote.medinote_back_kc.member.domain.dto.social;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialToMemberRegisterDTO {
  String email;
  String extraEmail;
  String nickname;
  String profileImagePath;
  String profileMimeType;
  @Builder.Default
  boolean fromSocial = true;
}
