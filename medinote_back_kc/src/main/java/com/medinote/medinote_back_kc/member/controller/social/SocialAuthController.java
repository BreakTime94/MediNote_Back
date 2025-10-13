package com.medinote.medinote_back_kc.member.controller.social;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.service.social.MemberSocialService;
import com.medinote.medinote_back_kc.security.service.TokenAuthService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/social/auth") // JWTToken이 발급되는 로직이 필요한건 auth로 명명 (Member도 동일)
@Log4j2
@RequiredArgsConstructor
public class SocialAuthController {

  private final MemberSocialService memberSocialService;
  private final CookieUtil cookieUtil;
  private final RedisUtil redisUtil;
  private final JWTUtil jwtUtil;
  private final TokenAuthService tokenAuthService;

  @PostMapping("/register")
  public ResponseEntity<?> register (@RequestBody SocialRegisterRequestDTO dto, HttpServletResponse response) {
    log.info("소셜 회원가입 컨트롤러 진입");
    MemberDTO memberDTO = memberSocialService.register(dto);
    //TokenAuthService를 통해 토큰 발급, 쿠키 & 레디스 세팅
    tokenAuthService.makeCookieWithToken(dto.getEmail(), response);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "SOCIAL_REGISTER_SUCCESS",
            "provider", dto.getProvider(),
            "member", memberDTO
    ));
  }
}
