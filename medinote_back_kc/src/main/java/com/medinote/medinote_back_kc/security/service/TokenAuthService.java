package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.security.status.TokenStatus;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenAuthService {

  TokenStatus accessTokenStatus(String accessToken);
  boolean refreshTokenIsValid(String refreshToken);
  boolean checkRedis(String token);
  String reissueAccessToken(String accessToken);
  void makeCookieWithToken(String email, HttpServletResponse response);
}
