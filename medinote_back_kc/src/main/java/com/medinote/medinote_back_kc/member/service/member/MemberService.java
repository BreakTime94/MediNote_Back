package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.ChangePasswordRequestDTO;
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

  //비밀번호 변경(MyPage)
  void changePassword(ChangePasswordRequestDTO dto, Long currentMemberId);

  //비밀번호 db에 저장도니 값이랑 같은지 확인하는 메서드
  boolean checkPassword(String rawPassword, Long currentId);
  //기존에 등록된 이메일이 있을 경우에만 인증코드 보내줌
  void sendVerificationCodeForFindEmail(String extraEmail);

  //extraEmail로 원래 email 찾아주기
  String findEmailByExtraEmail(String extraEmail);
  // 이메일 찾기용 별도 인증 확인 메서드 구현
  boolean verifyFindEmailCode(String email, String code);

  //비밀번호 임시 비밀번호로 변경하기 위한 본인인증
  void sendVerificationCodeForResetPassword(String email);

  //비밀번호 코드 verify 단계
  boolean verifyResetPassword(String email, String code);

}
