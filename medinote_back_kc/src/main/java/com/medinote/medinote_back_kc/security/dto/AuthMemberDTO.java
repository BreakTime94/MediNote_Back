package com.medinote.medinote_back_kc.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthMemberDTO {
  private Long id;
  private String email;
  private String role;
}
