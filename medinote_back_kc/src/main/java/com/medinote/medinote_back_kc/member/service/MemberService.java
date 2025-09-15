package com.medinote.medinote_back_kc.member.service;

import com.medinote.medinote_back_kc.member.domain.dto.MemberResponseDTO;
import com.medinote.medinote_back_kc.member.domain.dto.RegisterRequestDTO;

public interface MemberService {
  //등록
  Long register(RegisterRequestDTO dto);
  //이메일을 통한 로그인
  MemberResponseDTO get(Long id);
  //마이페이지 수정
  void update(MemberResponseDTO dto);
  // 로그인한 상태에서 삭제(softdelete)
  void delete(String email);
}
