package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.member.Role;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.security.status.TokenStatus;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {

  private final JWTUtil jwtUtil;
  private final RedisUtil redisUtil;
  private final CookieUtil cookieUtil;
  private final MemberRepository memberRepository;

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

  @Override
  public void makeCookieWithToken(String email, HttpServletResponse response) {
    Member member = memberRepository.findByEmailOrExtraEmail(email, email).orElseThrow(()-> new UsernameNotFoundException("존재하지 않는 이메일입니다."));
    // 4-2. 토큰 발급
    String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole());
    String refreshToken = jwtUtil.createRefreshToken(member.getId(), member.getRole());
    // 쿠키에 저장
    ResponseCookie accessCookie = cookieUtil.createAccessCookie(accessToken);
    ResponseCookie refreshCookie = cookieUtil.createRefreshCookie(refreshToken);
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    redisUtil.set(member.getId().toString(), refreshToken, jwtUtil.getExpirationDate(refreshToken).getTime() - System.currentTimeMillis());
  }
}
