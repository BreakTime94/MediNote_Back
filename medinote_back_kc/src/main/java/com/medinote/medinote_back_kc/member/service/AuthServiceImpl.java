package com.medinote.medinote_back_kc.member.service;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.entity.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.security.dto.AuthMemberDTO;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService{

  private final MemberRepository repository;
  private final MemberMapper mapper;
  private final JWTUtil util;
  private final PasswordEncoder encoder;

  @Override
  public MemberDTO login(LoginRequestDTO dto, HttpServletResponse response) {

    // 1. member Email을 확인하여 없는 경우 예외를 던짐
    Member member = repository.findByEmail(dto.getEmail()).orElseThrow(() -> new UsernameNotFoundException("올바른 email이 아닙니다."));

    //2. password 확인
    if(!encoder.matches(dto.getPassword(), member.getPassword())) {
      throw new BadCredentialsException("비밀번호가 일치하지 않습니다") {
      };
    }

    //3. 계정 status 확인
    checkStatus(member);

    //4. 쿠키에 token 부여
    String accessToken = util.createAccessToken(member.getId(), member.getEmail(), member.getRole());
    String refreshToken = util.createRefreshToken(member.getId(), member.getEmail(), member.getRole());
    log.info("accessToken : {}",accessToken);
    log.info("refreshToken : {}",refreshToken);
    //아래 private Method로 Cookie 생성 메서드 구현
    createCookies(response,accessToken,refreshToken);
    log.info(response.getHeader("Set-Cookie"));
    //5. MemberDTO 반환

    return mapper.toMemberDTO(member);
  }

  private void createCookies(HttpServletResponse response, String accessToken, String refreshToken) {
    ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMillis(util.getAccessTokenExpiration()))
            .build();

    ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
            .httpOnly(true)
            .sameSite("Lax")
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMillis(util.getRefreshTokenExpiration()))
            .build();

    log.info( "accessToken이 담긴 쿠키 ~ :{}",accessCookie.toString());
    log.info("refreshToken이 담긴 쿠키 ~ :{}",refreshCookie.toString());
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());
  }

  private void checkStatus(Member member) {
    if(member.getStatus().equals("DISABLED")) {
      throw new AuthorizationDeniedException("현재 계정은 잠긴 상태입니다. 관리자에게 문의하세요.");
    }
    if(member.getStatus().equals("DELETED")) {
      throw new AuthorizationDeniedException("삭제된 계정입니다.");
    }
  }
}
