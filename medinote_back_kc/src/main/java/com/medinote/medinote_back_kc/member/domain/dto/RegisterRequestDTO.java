package com.medinote.medinote_back_kc.member.domain.dto;

import lombok.Getter;

@Getter
public class RegisterRequestDTO {
  String email;
  String password;
  String extraEmail;
  String nickname;
}
