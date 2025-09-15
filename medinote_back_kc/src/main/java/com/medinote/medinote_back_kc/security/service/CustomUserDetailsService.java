package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.member.domain.entity.Member;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.security.dto.AuthMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    //Entity -> DTO 변환
    AuthMemberDTO dto = AuthMemberDTO.builder()

            .build();
    return null;
  }
}
