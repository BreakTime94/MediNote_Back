package com.medinote.medinote_back_kc.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

  private final SecretKey secret ;

  private final Long accessTokenExpiration ;

  private final Long refreshTokenExpiration ;

  public JWTUtil(JWTProperties jwtProperties) {
    this.secret = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)); //스트링으로 받아온 key를 SecretKey type으로 바꿔오는 작업
    this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
    this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
  }

  private String generateJWTToken(String email, String category){
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
    return Jwts.builder()
            .claim("email", email) // 이메일 값
            .claim("category", category) // 토큰 종류(acess, refresh)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secret)
            .compact();
  }

}
