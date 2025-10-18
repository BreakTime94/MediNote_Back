package com.medinote.medinote_back_kc.member.controller.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.LoginRequestDTO;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.service.member.AuthService;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
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

    log.info("로그인한 이메일: {} 로그인한 pw:{}", dto.getEmail(), dto.getPassword());
    MemberDTO respDto = service.login(dto, response);
    log.info(respDto);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "LOGIN_SUCCESS",
            "message", "로그인이 성공하였습니다.",
            "member", respDto
    ));
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

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "LOGOUT_SUCCESS",
            "message", "로그아웃이 성공하였습니다."
    ));
  }

}
