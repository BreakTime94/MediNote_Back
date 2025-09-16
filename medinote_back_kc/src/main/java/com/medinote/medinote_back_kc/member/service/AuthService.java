package com.medinote.medinote_back_kc.member.service;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.MemberDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
  MemberDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
}
