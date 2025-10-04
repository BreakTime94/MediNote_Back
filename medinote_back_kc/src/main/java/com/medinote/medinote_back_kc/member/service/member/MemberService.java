package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;

public interface MemberService {
  //등록
  void register(RegisterRequestDTO dto);
  //이메일을 통한 로그인
  MemberDTO get(String email);
  //마이페이지 수정
  void update(UpdateRequestDTO dto, Long id);
  // 로그인한 상태에서 삭제(softdelete)
  void delete(String email);
  //이메일 중복검사
  boolean isEmailAvailable(String email, Long currentMemberId);
  //닉네임 중복검사
  boolean isNicknameAvailable(String nickname, Long currentMemberId);
  // Redis 및 회원가입하려는 이메일에 인증코드 발송
  void sendVerificationCode(String email);

  //Redis와 이메일 받은 인증코드 일치여부 확인 응답
  boolean verifyCode(String email, String code);
}
