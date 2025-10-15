package com.medinote.medinote_back_kc.security.service;

import com.medinote.medinote_back_kc.common.ErrorCode;
import com.medinote.medinote_back_kc.common.exception.CustomException;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.member.Status;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;

  public UserDetails loadUserByUserId(Long id) throws UsernameNotFoundException {
    // CustomUserDetails(UserDetail 구현하여서 암묵적 형변환 가능)
    Member member = memberRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("등록되지 않은 회원 입니다."));

    if(member.getStatus() == Status.DISABLED) {
      throw new CustomException(ErrorCode.MEMBER_DISABLED);
    } else if(member.getStatus() == Status.DELETED) {
      throw new  CustomException(ErrorCode.MEMBER_DELETED);
    }

    return new CustomUserDetails(member);
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Member member = memberRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("등록되지 않은 회원 입니다."));

    if(member.getStatus() == Status.DISABLED) {
      throw new CustomException(ErrorCode.MEMBER_DISABLED);
    } else if(member.getStatus() == Status.DELETED) {
      throw new  CustomException(ErrorCode.MEMBER_DELETED);
    }

    return new CustomUserDetails(member);
  }
}
