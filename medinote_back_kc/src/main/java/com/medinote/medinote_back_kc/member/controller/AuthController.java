package com.medinote.medinote_back_kc.member.controller;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;

import com.medinote.medinote_back_kc.member.domain.dto.MemberDTO;
import com.medinote.medinote_back_kc.member.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/member/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

  private final AuthService service;

  @PostMapping("/login")
  public ResponseEntity<?> Login (LoginRequestDTO dto, HttpServletResponse response) {
    log.info(dto);
    MemberDTO respDto = service.login(dto, response);
    log.info(respDto);
    return ResponseEntity.ok(respDto);

  }
}
