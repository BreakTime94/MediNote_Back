package com.medinote.medinote_back_kc.security.filter;

import com.medinote.medinote_back_kc.member.domain.entity.Role;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.service.CustomUserDetailsService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
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

  @PostConstruct
  public void init() {
    log.info("===== JWTAuthenticationFilter 빈으로 등록됨 =====");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    log.info("토큰 검증 필터 들어갑니다~");
    //1. 쿠키에서 AccessToken 추출
    String accessToken = getCookieValue(request,"ACCESS_COOKIE");
    String refreshToken = getCookieValue(request,"REFRESH_COOKIE"); //refreshToken도 무언가 조치가 필요해 보인다...?

    log.info(accessToken);
    log.info(refreshToken);

    try{
      // accessToken과 refreshToken 둘다 있는 경우!
      if(accessToken != null && refreshToken != null) {
        //1) accessToken이 곧 만료되는 경우 (5분 이내)
        if(jwtUtil.isExpiredSoon(accessToken)) {
            accessToken = reissueAccessToken(refreshToken);
            response.addHeader("Set-Cookie", cookieUtil.createAccessCookie(accessToken).toString());
            setAuthenticaionFromToken(accessToken);
        } else { //2. accessToken 유효기간 충분한 경우
          setAuthenticaionFromToken(accessToken);
        }

      } else if(accessToken == null && refreshToken != null) { //3) refreshToken만 살아 있는 경우
        accessToken = reissueAccessToken(refreshToken);
        response.addHeader("Set-Cookie", cookieUtil.createAccessCookie(accessToken).toString());
        setAuthenticaionFromToken(accessToken);
      } else{ // 4) 그 외의 경우는 쿠키를 지워주고, Anonymous Context로 처리한다. 재 로그인을 해야한다.
        response.addHeader("Set-Cookie", cookieUtil.deleteAccessCookie().toString());
        response.addHeader("Set-Cookie", cookieUtil.deleteRefreshCookie().toString());
        SecurityContextHolder.clearContext();
      }
    } catch (Exception e){
      SecurityContextHolder.clearContext();
    } finally {
      //다음 필터로 진행
      filterChain.doFilter(request, response);
    }

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

  private String reissueAccessToken(String refreshToken) {
    Long id = jwtUtil.getUserId(refreshToken);
    Role role = jwtUtil.getRole(refreshToken);

    return jwtUtil.createAccessToken(id, role);
  }

  private void setAuthenticaionFromToken(String accessToken) {
    //3. AccessToken에서 id 추출
    Long id = jwtUtil.getUserId(accessToken);

    // 4. DB에서 사용자 조회 -> UserDetails 변환 (Security Context 등록 준비)
    CustomUserDetails user = (CustomUserDetails) service.loadUserByUserId(id);

    // 5. Authentication 객체 생성
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    //6. Security Context 등록
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
