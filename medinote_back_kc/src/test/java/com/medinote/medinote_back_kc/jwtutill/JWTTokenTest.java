package com.medinote.medinote_back_kc.jwtutill;

import com.medinote.medinote_back_kc.security.util.JWTProperties;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class JWTTokenTest {

  @Autowired
  private JWTProperties jwtProperties;

  @Test
  public void testExist() {
    JWTUtil jwtUtil = new JWTUtil(jwtProperties);
    log.info("jwtproperties = {}", jwtProperties);
    log.info("jwtutil 이란? {}",jwtUtil.getSecret());
    log.info("jwtutil 이란? {}",jwtUtil.getRefreshTokenExpiration());
    log.info("jwtutil 이란? {}",jwtUtil.getAccessTokenExpiration());
  }

  @Test
  public void testGenerateToken() {
    JWTUtil jwtUtil = new JWTUtil(jwtProperties);
//   log.info(" accesstoken 값: {}", jwtUtil.createAccessToken("kimchan9408@gmail.com"));
//   log.info(" accesstoken 값: {}", jwtUtil.createRefreshToken("kimchan9408@gmail.com"));
  }

  @Test
  public void testGetProperties() {
    JWTUtil jwtUtil = new JWTUtil(jwtProperties);
//    String accTmp = jwtUtil.createAccessToken("kimchan9408@gmail.com");
//    String refTmp = jwtUtil.createRefreshToken("kimchan9408@gmail.com");
//    log.info("access token 내 payload 내 이메일 값 = {}",jwtUtil.getUserEmail(accTmp));
//    log.info("refresh token 내 payload 내 이메일 값 = {}",jwtUtil.getUserEmail(refTmp));
//
//    log.info("accessToken 내 payload 내 유효기간 만료됨? = {}",jwtUtil.isExpired(accTmp));
//    log.info("refreshToken 내 payload 내 유효기간 만료됨? = {}",jwtUtil.isExpired(refTmp));



  }
}
