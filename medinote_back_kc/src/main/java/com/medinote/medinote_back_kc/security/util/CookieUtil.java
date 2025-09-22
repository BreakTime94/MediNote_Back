package com.medinote.medinote_back_kc.security.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Date;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class CookieUtil {

  private final JWTUtil jwtUtil;

  // 최초 발급 및 5분 이내의 재발급 기능 및 Refresh만 들고 있을 경우의 재발급 통틀어서
  public ResponseCookie createAccessCookie(String accessToken) {
    Date expiration = jwtUtil.getExpirationDate(accessToken);
    long leftTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;
    ResponseCookie cookie = ResponseCookie.from("ACCESS_COOKIE", accessToken)
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(leftTime)
            .build();

    log.info("만들어진 ACCESS 쿠키 : {}", cookie);
    return cookie;
  }

  public ResponseCookie createRefreshCookie(String refreshToken) {
    Date expiration = jwtUtil.getExpirationDate(refreshToken);
    long leftTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;
    ResponseCookie cookie = ResponseCookie.from("REFRESH_COOKIE", refreshToken)
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(leftTime)
            .build();

    log.info("만들어진 REFRESH 쿠키 : {}", cookie);
    return cookie;
  }

  public ResponseCookie deleteAccessCookie() {
    log.info("ACCESS_COOKIE 삭제");
    return ResponseCookie.from("ACCESS_COOKIE", "")
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(0)
            .build();
  }

  public ResponseCookie deleteRefreshCookie() {
    log.info("REFRESH_COOKIE 삭제");
    return ResponseCookie.from("REFRESH_COOKIE", "")
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(0)
            .build();
  }
}
