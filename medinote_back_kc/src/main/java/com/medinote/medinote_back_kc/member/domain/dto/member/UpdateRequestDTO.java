package com.medinote.medinote_back_kc.member.domain.dto.member;

import lombok.Getter;

@Getter
public class UpdateRequestDTO {
  private String email;
  private String extraEmail;
  private String nickname;
  private String profileImagePath;
  private String profileMimeType;
}
