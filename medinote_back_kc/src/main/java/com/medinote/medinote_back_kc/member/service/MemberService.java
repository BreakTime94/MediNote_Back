package com.medinote.medinote_back_kc.member.service;

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
}
