package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.member.domain.entity.member.Role;
import com.medinote.medinote_back_kc.security.status.TokenStatus;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {

  private final JWTUtil jwtUtil;
  private final RedisUtil redisUtil;

  @Override
  public TokenStatus accessTokenStatus(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) {
      log.info("AccessToken 없음 → EXPIRED로 처리");
      return TokenStatus.EXPIRED;
    }
    return jwtUtil.validateToken(accessToken);
  }

  @Override
  public boolean refreshTokenIsValid(String refreshToken) {
    return jwtUtil.validateToken(refreshToken) == TokenStatus.VALID;
  }

  @Override
  public boolean checkRedis(String refreshToken) {
    if(refreshToken == null) {
      return false;
    } else {
      try{
        String redisToken = redisUtil.get(jwtUtil.getUserId(refreshToken).toString());
        log.info("Redis 검증 중: {}", redisToken);
        return redisToken != null && redisToken.equals(refreshToken);
      } catch (Exception e) {
        log.error(e.getMessage());
        return false;
      }
    }
  }

  @Override
  public String reissueAccessToken(String refreshToken) {

    Long id = jwtUtil.getUserId(refreshToken);
    Role role = jwtUtil.getRole(refreshToken);

    return jwtUtil.createAccessToken(id, role);
  }
}
