package com.medinote.medinote_back_kc.member.controller;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;

import com.medinote.medinote_back_kc.member.domain.dto.MemberDTO;
import com.medinote.medinote_back_kc.member.service.AuthService;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/member/auth")
public class AuthController {

  private final AuthService service;
  private final JWTUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<?> Login (@RequestBody LoginRequestDTO dto, HttpServletResponse response) {
    log.info("dto: {}" , dto);
    log.info(dto.getEmail());
    log.info(dto.getPassword());
    MemberDTO respDto = service.login(dto, response);
    log.info(respDto);
    return ResponseEntity.ok(respDto);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> Logout (HttpServletResponse response) {
    ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(0)
            .build();

    ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(0)
            .build();

    log.info("쿠키 재발급의 영역");
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return ResponseEntity.ok("로그아웃 완료");
  }
}
