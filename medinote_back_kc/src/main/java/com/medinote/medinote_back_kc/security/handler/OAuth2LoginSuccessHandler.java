//package com.medinote.medinote_back_kc.security.handler;
//
//import com.medinote.medinote_back_kc.security.util.CookieUtil;
//import com.medinote.medinote_back_kc.security.util.JWTUtil;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//  private final JWTUtil jwtUtil;
//  private final CookieUtil cookieUtil;
//
//  @Override
//  public void onAuthenticationSuccess(HttpServletRequest request,
//                                      HttpServletResponse response,
//                                      Authentication authentication) throws IOException, ServletException {
//
//    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//    String email = oAuth2User.getAttribute("email");
//
//    // JWT 발급
//    String accessToken = jwtUtil.generateAccessToken(email, ...);
//    String refreshToken = jwtUtil.generateRefreshToken(email, ...);
//
//    // 쿠키에 저장
//    cookieUtil.addCookie(response, "ACCESS_TOKEN", accessToken, 60 * 30); // 30분
//    cookieUtil.addCookie(response, "REFRESH_TOKEN", refreshToken, 60 * 60 * 24 * 7); // 7일
//
//    // 로그인 후 리다이렉트
//    getRedirectStrategy().sendRedirect(request, response, "/api/user");
//  }
//}
//
