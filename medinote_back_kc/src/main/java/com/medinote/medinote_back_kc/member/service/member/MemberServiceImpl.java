package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.common.ErrorCode;
import com.medinote.medinote_back_kc.common.exception.CustomException;
import com.medinote.medinote_back_kc.common.exception.DuplicateEmailException;
import com.medinote.medinote_back_kc.common.exception.DuplicateNicknameException;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.member.service.mail.MailService;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService{

  private final MemberRepository repository;
  private final MemberMapper mapper;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  private final RedisUtil redisUtil;

  @Override
  public void register(RegisterRequestDTO dto) {

    if(repository.existsByEmail(dto.getEmail()) || repository.existsByExtraEmail(dto.getExtraEmail())) {
      throw new DuplicateEmailException("이미 등록된 이메일입니다.");
    }

    if(repository.existsByNickname(dto.getNickname())) {
      throw new DuplicateNicknameException("이미 등록된 닉네임입니다.");
    }

    Member member = mapper.toRegister(dto, passwordEncoder);
    repository.save(member);
  }
  // 기존 테이블에 이메일 등록되었는지 확인
  @Override
  public boolean isEmailAvailable(String email) {
    return !repository.existsByEmail(email) && !repository.existsByExtraEmail(email);
  }
  // 기존 테이블에 닉네임 등록되었는지 확인
  @Override
  public boolean isNicknameAvailable(String nickname) {
    return !repository.existsByNickname(nickname);
  }
  //이메일 인증 코드 발송
  @Override
  public void sendVerificationCode(String email) {
    if(redisUtil.get("email:verify:" + email) != null) { // 기존 발급된 코드가 있는데 또 누른 경우 기존 code 삭제
      redisUtil.delete("email:verify:" + email);
    }
    String code = String.format("%06d", new Random().nextInt(999999));
    redisUtil.set("email:verify:" + email, code, 300000L);
    String subject = "Medinote 회원가입을 위한 인증코드 6자리를 보내드립니다. 인증코드 작성란에 6자리를 입력하여 주세요.";
    String text = "인증 코드 : " + code + "\n 5분 이내로 인증하여 주시기 바랍니다.";
    mailService.sendEmail(email, subject, text);
  }
  // 인증번호 검증
  @Override
  public boolean verifyCode(String email, String code) {
    String savedCode = redisUtil.get("email:verify:" + email);
    if(savedCode == null) {
      throw new CustomException(ErrorCode.EMAIL_EXPIRED);
    }
    if(!savedCode.equals(code)) {
      throw new CustomException(ErrorCode.EMAIL_INVALID);
    }

    return true;
  }

  @Override
  public MemberDTO get(String email) {
    return mapper.toMemberDTO(repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("email을 확인하여 주시기 바랍니다.")));
  }

  @Override
  public void update(UpdateRequestDTO dto, Long id) {
    Member member = (repository.findById(id).orElseThrow(()-> new UsernameNotFoundException("email을 확인하여 주시기 바랍니다.")));
    member.changeMyPage(dto);
    repository.save(member);
  }

  @Override
  @Transactional
  public void delete(String email) {
    repository.softDelete(email);
  }
}
