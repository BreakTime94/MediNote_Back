package com.medinote.medinote_back_kc.security.filter;

import com.medinote.medinote_back_kc.member.domain.entity.Role;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.service.CustomUserDetailsService;
import com.medinote.medinote_back_kc.security.service.TokenAuthService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class JWTAuthenticationFilter extends OncePerRequestFilter {//컨트롤러가 받는 매 요청마다 JWTToken의 유효성 검증
  private final JWTUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final CustomUserDetailsService service;
  private final RedisUtil redisUtil;
  private final TokenAuthService tokenAuthService;

  @PostConstruct
  public void init() {
    log.info("===== JWTAuthenticationFilter 빈으로 등록됨 =====");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    log.info("토큰 검증 필터 들어갑니다~");
    // 1. 쿠키에서 AccessToken & RefreshToken 추출
    String accessToken = getCookieValue(request,"ACCESS_COOKIE");
    String refreshToken = getCookieValue(request,"REFRESH_COOKIE");//refreshToken도 무언가 조치가 필요해 보인다...?

    //2. RefreshToken 유효성 검사 (만료, 사인검증, 구조 등 전부 포함)
    if(!tokenAuthService.refreshTokenIsValid(refreshToken)) {
      checkAndDeleteRedis(refreshToken);
      clearAuth(response);
      filterChain.doFilter(request, response);
      return;
    }
    // 3. AccessToken 유효성 검사 (Refresh는 정상으로 간주)

    switch (tokenAuthService.accessTokenStatus(accessToken)) {
      case VALID: //accessToken 존재
          // 1-1) accessToken이 곧 만료되는 경우 (5분 이내)
          if(jwtUtil.isExpiredSoon(accessToken)) {
            if(tokenAuthService.checkRedis(refreshToken)) {
              accessToken = tokenAuthService.reissueAccessToken(refreshToken);
              response.addHeader("Set-Cookie", cookieUtil.createAccessCookie(accessToken).toString());
              setAuthenticationFromToken(accessToken);
            } else{
              // 1-2) accessToken이 곧 만료되는데 redis가 정상이 아닌 경우
              redisUtil.delete(jwtUtil.getUserId(refreshToken).toString());
              clearAuth(response);
            }
          } else { // 2) accessToken 유효기간 충분
            setAuthenticationFromToken(accessToken);
          }
          break;
      case EXPIRED: // 3) accessToken 유효기간 만료
        if(tokenAuthService.checkRedis(refreshToken)) {
          accessToken = tokenAuthService.reissueAccessToken(refreshToken);
          response.addHeader("Set-Cookie", cookieUtil.createAccessCookie(accessToken).toString());
          setAuthenticationFromToken(accessToken);
        } else{ //redis가 정상이 아닌경우
          redisUtil.delete(jwtUtil.getUserId(refreshToken).toString());
          clearAuth(response);
        }
        break;
      case MALFORMED, INVALID, UNKNOWN: // 4) 그냥 비정상인 경우
        checkAndDeleteRedis(refreshToken);
        clearAuth(response);
        break;
    }
    filterChain.doFilter(request, response);
  }

  //Cookie 파싱 (Token 꺼냄)
  private String getCookieValue(HttpServletRequest request, String cookieName) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookieName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private void setAuthenticationFromToken(String accessToken) {
    //3. AccessToken에서 id 추출
    Long id = jwtUtil.getUserId(accessToken);

    // 4. DB에서 사용자 조회 -> UserDetails 변환 (Security Context 등록 준비)
    CustomUserDetails user = (CustomUserDetails) service.loadUserByUserId(id);

    // 5. Authentication 객체 생성
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    //6. Security Context 등록
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void clearAuth(HttpServletResponse response) {
    response.addHeader("Set-Cookie", cookieUtil.deleteAccessCookie().toString());
    response.addHeader("Set-Cookie", cookieUtil.deleteRefreshCookie().toString());
    SecurityContextHolder.clearContext();
  }

  private void checkAndDeleteRedis(String refreshToken) {
    if(tokenAuthService.checkRedis(refreshToken)) {
      redisUtil.delete(jwtUtil.getUserId(refreshToken).toString());
    }
  }

}
