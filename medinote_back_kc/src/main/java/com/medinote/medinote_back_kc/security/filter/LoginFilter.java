package com.medinote.medinote_back_kc.security.filter;

import com.medinote.medinote_back_kc.security.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Log4j2
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private JWTUtil jwtUtil;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
   log.info("attemptAuthentication");

   try {
     //JSON 방식이면 email이랑 password 가져오는 방법의 변화가 필요함
     String email = request.getParameter("email");
     String pw = request.getParameter("password");

     UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, pw);
      return getAuthenticationManager().authenticate(token);
   } catch (Exception e) {
     throw new AuthenticationServiceException("인증실패요");
   }
  }
}
