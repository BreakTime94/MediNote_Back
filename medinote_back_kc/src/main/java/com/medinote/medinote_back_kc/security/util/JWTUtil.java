package com.medinote.medinote_back_kc.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Getter
public class JWTUtil {

  private final SecretKey secret ;

  private final Long accessTokenExpiration ;

  private final Long refreshTokenExpiration ;

  // secret jwt yaml 파일 가져오기 위한 과정
  public JWTUtil(JWTProperties jwtProperties) {
    this.secret = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)); //스트링으로 받아온 key를 SecretKey type으로 바꿔오는 작업
    this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
    this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
  }

  //accessToken 싱글톤 느낌으로

  public String createAccessToken(Long id, String email) {
    return generateToken(id, email, "access");
  }

  //refreshToken 싱글톤 느낌으로
  public String createRefreshToken(Long id, String email) {
    return generateToken(id , email, "refresh");
  }

  private String generateToken(Long id, String email, String category){
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + (category.equals("access") ? accessTokenExpiration : refreshTokenExpiration));
    return Jwts.builder()
            .subject(id.toString())
            .claim("email", email) // 이메일 값 (main key 값은 느낌, db의 pk를 쓸 때도 있다고 함)
            .claim("category", category) // 토큰 종류(access, refresh)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secret)
            .compact();
  }

  //payload 내 claim(subject 포함) 공통 추출 메서드
  private Claims getClaims(String token){
    return Jwts.parser()
            .verifyWith(secret)
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }
  // 토큰 내 payload 내에 저장되어 있는 이메일 값 추출
  public String getUserEmail(String token) {
    return getClaims(token).getSubject();
  }
  // access인지 refresh인지 확인하는 용도
  public String getCategory(String token) {
    return getClaims(token).get("category", String.class);
  }
  // 만료 여부
  public boolean isExpired(String token) {
    try {
      return getClaims(token).getExpiration().before(new Date());
    } catch (Exception e) { // getClaims 자체가 오류가 터질 상황을 대비하여, try-catch로 감싼다. 그냥 오류 전부를 하나로 묶어서 catch 처리
      return true;
    }
  }




}
