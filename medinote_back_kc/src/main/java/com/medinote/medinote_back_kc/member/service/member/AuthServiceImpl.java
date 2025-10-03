package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.security.service.TokenAuthService;
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
  private final PasswordEncoder encoder;
  private final TokenAuthService tokenAuthService;

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

    //4. TokenAuthService의 토큰/쿠키/레디스 발급 과정 포함
    tokenAuthService.makeCookieWithToken(dto.getEmail(), response);

    //5. MemberDTO 반환
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
