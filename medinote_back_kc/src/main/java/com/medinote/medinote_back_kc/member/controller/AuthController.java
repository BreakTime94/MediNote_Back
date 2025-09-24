package com.medinote.medinote_back_kc.member.controller;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;

import com.medinote.medinote_back_kc.member.domain.dto.MemberDTO;
import com.medinote.medinote_back_kc.member.service.AuthService;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/member/auth")
public class AuthController {

  private final AuthService service;
  private final CookieUtil cookieUtil;
  private final RedisUtil redisUtil;

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
    log.info("======Logout Controller======");
    // 1. 쿠키 삭제
    ResponseCookie accessCookie = cookieUtil.deleteAccessCookie();
    ResponseCookie refreshCookie = cookieUtil.deleteRefreshCookie();
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    // 2. redis 리프레시 토큰 삭제
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if(auth != null && auth.getPrincipal() instanceof CustomUserDetails user){
      // social 등의 logout 구조에서 확장을 위해 instanceof를 둔다.
      Long memberId = user.getId();
      redisUtil.delete(memberId.toString());

      log.info("로그아웃 완료: memberId={}", memberId);
    }

    // 3. Security Context 정리
    SecurityContextHolder.clearContext();

    return ResponseEntity.ok("로그아웃 완료");
  }

}
