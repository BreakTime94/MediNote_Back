package com.medinote.medinote_back_kc.member.domain.dto.admin;

import com.medinote.medinote_back_kc.member.domain.dto.social.MemberSocialDTO;
import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Role;
import com.medinote.medinote_back_kc.member.domain.entity.member.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberForAdminDTO {
  private String email;
  private String extraEmail;
  private String nickname;
  private Role role;
  private String profileImagePath;
  private String profileMimeType;
  private Status status;
  private boolean fromSocial;
  private boolean extraEmailVerified;
  private LocalDateTime regDate;
  private LocalDateTime deletedAt;

  private List<MemberSocialDTO> socialAccounts;
  private List<MemberTermsDTO> memberTerms;
}
