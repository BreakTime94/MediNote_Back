package com.medinote.medinote_back_kc.security.filter;

import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.service.CustomUserDetailsService;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {//컨트롤러가 받는 매 요청마다 JWTToken의 유효성 검증
  private final JWTUtil util;
  private final CustomUserDetailsService service;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    //1. 쿠키에서 AccessToken 추출
    String accessToken = request.getHeader("ACCESS_TOKEN");
    String refreshToken = request.getHeader("REFRESH_TOKEN"); //refreshToken도 무언가 조치가 필요해 보인다...?

    //2. accessToken의 만료기간 확인

    if(accessToken != null && !util.isExpired(accessToken)) {
      try {
      //3-1. AccessToken에서 Email 추출
      String email = util.getUserEmail(accessToken);

      // 4. DB에서 사용자 조회 -> UserDetails 변환 (Security Context 등록 준비)
      CustomUserDetails user = (CustomUserDetails) service.loadUserByUsername(email);

      // 5. Authentication 객체 생성

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

      //6. Security Context 등록

      SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception e) {
        // 3-2. 토큰 내 클레임 파싱 실패 할 경우에 예외 발생
        // 원인은 다양하다, 토큰은 있으나 클레임이 없을 때, 토큰이 없을 때, 쿠키 자체가 발급이 안되어 있을 때 등
        SecurityContextHolder.clearContext();
      }
    }
    //7. 다음 필터로 진행
    filterChain.doFilter(request, response);
  }
}
