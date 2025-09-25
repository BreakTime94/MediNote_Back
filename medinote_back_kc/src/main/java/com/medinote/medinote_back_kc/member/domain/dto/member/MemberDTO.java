package com.medinote.medinote_back_kc.member.domain.dto.member;

import com.medinote.medinote_back_kc.member.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
  private String email;
  private String extraEmail;
  private String nickname;
  private Role role;
  private String profileImagePath;
  private String profileMimeType;
  private boolean fromSocial;
  private LocalDateTime regDate;
}
