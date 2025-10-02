package com.medinote.medinote_back_kc.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // ==== 이메일 인증 관련 ====
  EMAIL_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 다시 요청해주세요."),
  EMAIL_INVALID(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다."),

  // ==== JWT 관련 ====
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
  TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "JWT 토큰이 손상되었습니다."),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
  TOKEN_UNKNOWN(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 형식입니다."),

  // ==== 공통 ====
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
