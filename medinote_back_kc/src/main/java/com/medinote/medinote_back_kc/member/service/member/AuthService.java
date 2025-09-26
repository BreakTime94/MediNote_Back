package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
  MemberDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
}
