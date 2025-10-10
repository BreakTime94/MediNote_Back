package com.medinote.medinote_back_kc.member.domain.dto.member;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
  String email;
  String password;
}
