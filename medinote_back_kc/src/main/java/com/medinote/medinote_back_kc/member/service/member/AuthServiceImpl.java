package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService{

  private final MemberRepository repository;
  private final MemberMapper mapper;
  private final JWTUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final RedisUtil redisUtil;
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
    String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole());
    String refreshToken = jwtUtil.createRefreshToken(member.getId(), member.getRole());
    log.info("accessToken : {}",accessToken);
    log.info("refreshToken : {}",refreshToken);

    //5. CookieUtil로 별도 cookie 생성 메서드 구현
    ResponseCookie accessCookie = cookieUtil.createAccessCookie(accessToken);
    ResponseCookie refreshCookie = cookieUtil.createRefreshCookie(refreshToken);

    //6. response header에 쿠키 추가
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());
    log.info("헤더의 쿠키 좀 볼까? : {}", response.getHeader("Set-Cookie"));

    //7. redis에 refresh token 추가
    redisUtil.set(member.getId().toString(), refreshToken, jwtUtil.getExpirationDate(refreshToken).getTime() - System.currentTimeMillis());
    //8. MemberDTO 반환
    return mapper.toMemberDTO(member);
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
