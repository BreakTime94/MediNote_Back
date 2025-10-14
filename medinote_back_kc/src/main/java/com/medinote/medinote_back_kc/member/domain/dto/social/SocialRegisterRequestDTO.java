package com.medinote.medinote_back_kc.member.domain.dto.social;

import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
  @Email
  @NotBlank(message = "이메일은 필수입니다.")
  String extraEmail;
  private List<MemberTermsRegisterRequestDTO> agreements;
}
