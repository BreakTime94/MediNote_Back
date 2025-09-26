package com.medinote.medinote_back_kc.security.util;

import com.medinote.medinote_back_kc.member.domain.entity.member.Role;
import com.medinote.medinote_back_kc.security.status.TokenStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Getter
@Log4j2
public class JWTUtil {

  private final SecretKey secret;
  private final Long accessTokenExpiration;
  private final Long refreshTokenExpiration;

  // secret jwt yaml 파일 가져오기 위한 과정
  public JWTUtil(JWTProperties jwtProperties) {
    this.secret = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)); //스트링으로 받아온 key를 SecretKey type으로 바꿔오는 작업
    this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
    this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
  }

  //accessToken 공통 generateToken 메서드에서 parameter를 통해 토큰 종류를 나눠서 발급
  public String createAccessToken(Long id, Role role) {
    return generateToken(id, role, "access");
  }

  //refreshToken 공통 generateToken 메서드에서 parameter를 통해 토큰 종류를 나눠서 발급
  public String createRefreshToken(Long id, Role role) {
    return generateToken(id , role, "refresh");
  }

  private String generateToken(Long id, Role role, String category){
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + (category.equals("access") ? accessTokenExpiration : refreshTokenExpiration));
    return Jwts.builder()
            .subject(id.toString()) // pk
            .claim("role", role)
            .claim("category", category) // 토큰 종류(access, refresh)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secret)
            .compact();
  }

  //payload 내 claim(subject 포함) 공통 추출 메서드
  // parser를 통해 유효성 검증을 한다.
  private Claims getClaims(String token){
    return Jwts.parser()
            .verifyWith(secret)
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }
  // 토큰 내 payload 내에 저장되어 있는 이메일 값 추출
  public Long getUserId(String token) {
    return Long.parseLong(getClaims(token).getSubject());
  }

  public Role getRole(String token) {
    return Role.valueOf(getClaims(token).get("role").toString());
  }

  public Date getExpirationDate(String token) {
    return getClaims(token).getExpiration();
  }

  // 만료 여부
  public boolean isExpired(String token) {
    try {
      return getClaims(token).getExpiration().before(new Date());
    } catch (Exception e) { // getClaims 자체가 오류가 터질 상황을 대비하여, try-catch로 감싼다. 그냥 오류 전부를 하나로 묶어서 catch 처리
      return true;
    }
  }

  //만료시간이 5분 이내인지 여부
  public boolean isExpiredSoon(String token) {
    try {
      return (getClaims(token).getExpiration().getTime() - new Date().getTime()) <= 5 * 60 * 1000;
    } catch (Exception e) {
      return true;
    }
  }

  public TokenStatus validateToken(String token) {
    try {
      getClaims(token); // 여기서 예외 터지면 catch로 감
      return TokenStatus.VALID;
    } catch (ExpiredJwtException e) {
      log.warn("토큰 만료: {}", e.getMessage());
      return TokenStatus.EXPIRED;
    } catch (SecurityException e) {
      log.warn("토큰 서명 불일치(위조 가능): {}", e.getMessage());
      return TokenStatus.INVALID;
    } catch (MalformedJwtException e) {
      log.warn("토큰 구조 이상: {}", e.getMessage());
      return TokenStatus.MALFORMED;
    } catch (Exception e) {
      log.error("토큰이 없거나 알 수 없는 토큰 오류", e);
      return TokenStatus.UNKNOWN;
    }
  }

}
