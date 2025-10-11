package com.medinote.medinote_back_kc.member.service.member;

import com.medinote.medinote_back_kc.common.ErrorCode;
import com.medinote.medinote_back_kc.common.exception.CustomException;
import com.medinote.medinote_back_kc.common.exception.DuplicateEmailException;
import com.medinote.medinote_back_kc.common.exception.DuplicateNicknameException;
import com.medinote.medinote_back_kc.member.domain.dto.member.ChangePasswordRequestDTO;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
  public boolean isEmailAvailable(String email, Long currentMemberId) {

    if(currentMemberId == null) { //로그인 된 상태가 아님 -> 회원가입 중
      return !repository.existsByEmail(email) && !repository.existsByExtraEmail(email);
    }
    // 로그인 된 상태 -> update 중
    if(!repository.existsByEmail(email)) { //내가 기입한 email이 로그인용 이메일에 등록되지 않은 경우
      Member member = repository.findById(currentMemberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      //다른 사람의 extraEmail로 등록되지 않거나 또는 내 자신의 extraEmail인 경우는 허용
      if(!repository.existsByExtraEmail(email) || member.getExtraEmail().equals(email)) {
        return true;
      }
      //그게 아닌 경우는 false
      throw new CustomException(ErrorCode.EXTRA_EMAIL_DUPLICATED);
    }
    //이미 로그인용 email에 등록된 경우 그냥 false
    throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
  }
  // 기존 테이블에 닉네임 등록되었는지 확인
  @Override
  public boolean isNicknameAvailable(String nickname, Long currentMemberId) {
    if(currentMemberId == null) {
      return !repository.existsByNickname(nickname);
    }
    Member member = repository.findById(currentMemberId).orElseThrow(() -> new UsernameNotFoundException("JWT TOKEN이 손상되었습니다."));
    return !repository.existsByNickname(nickname) || member.getNickname().equals(nickname);
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
  // 인증번호 검증(회원가입용)
  @Override
  public boolean verifyCode(String email, String code) {
    String savedCode = redisUtil.get("email:verify:" + email);
    if(savedCode == null) {
      throw new CustomException(ErrorCode.CODE_EXPIRED);
    }
    if(!savedCode.equals(code)) {
      throw new CustomException(ErrorCode.CODE_INVALID);
    }
    redisUtil.delete("email:verify:" + email);
    return true;
  }

  // 인증번호 검증
  @Override
  public boolean verifyFindEmailCode(String email, String code) {
    String savedCode = redisUtil.get("email:find:" + email);
    if(savedCode == null) {
      throw new CustomException(ErrorCode.CODE_EXPIRED);
    }
    if(!savedCode.equals(code)) {
      throw new CustomException(ErrorCode.CODE_INVALID);
    }
    redisUtil.delete("email:find:" + email);
    return true;
  }

  @Override
  public MemberDTO get(String email) {
    return mapper.toMemberDTO(repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("email을 확인하여 주시기 바랍니다.")));
  }

  @Transactional
  @Override
  public void update(UpdateRequestDTO dto, Long id) {
    Member member = (repository.findById(id).orElseThrow(()-> new UsernameNotFoundException("이미 삭제되었거나 잘못된 멤버입니다.")));
    member.changeMyPage(dto);
    repository.save(member);
  }

  @Override
  @Transactional
  public void delete(String email) {
    Member member = repository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("이미 삭제되었거나 잘못된 멤버입니다."));
    redisUtil.delete("id" + member.getId().toString());
    repository.softDelete(email);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequestDTO dto, Long currentMemberId) {
    Member member = repository.findById(currentMemberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    // password 유효성 검사나 사전에 맞는지 확인하는 로직은 아래 checkPassword에서 진행
    member.changePassword(passwordEncoder.encode(dto.getPassword()));

    repository.save(member);
  }

  @Override
  public boolean checkPassword(String rawPassWord, Long currentId) {//raw -> 사용자가 입력한 값, encoded-> db에 저장된 값.
    Member member = repository.findById(currentId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND) );
    return passwordEncoder.matches(rawPassWord, member.getPassword());
  }

  @Override
  public void sendVerificationCodeForFindEmail(String extraEmail) {
    Optional<Member> optionalMember = repository.findByExtraEmail(extraEmail);

    if(optionalMember.isEmpty() || !optionalMember.get().isExtraEmailVerified()) {
      return;
    }

    if(redisUtil.get("email:find:" + extraEmail) != null) { // 기존 발급된 코드가 있는데 또 누른 경우 기존 code 삭제
      redisUtil.delete("email:find:" + extraEmail);
    }
    String code = String.format("%06d", new Random().nextInt(999999));
    redisUtil.set("email:find:" + extraEmail, code, 300000L);
    String subject = "Medinote 아이디를 찾기 위한 인증코드 6자리를 보내드립니다. 인증코드 작성란에 6자리를 입력하여 주세요.";
    String text = "인증 코드 : " + code + "\n 5분 이내로 인증하여 주시기 바랍니다.";
    mailService.sendEmail(extraEmail, subject, text);
  }

  @Override
  public String findEmailByExtraEmail(String extraEmail) {
    Member member = repository.findByExtraEmail(extraEmail).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return member.getEmail();
  }

  @Override
  public void sendVerificationCodeForResetPassword(String email) {
    Optional<Member> optionalMember = repository.findByEmail(email);

    //회원이 존재하지 않거나, social member이면 묻지도 따지지도 않고 return
    if(optionalMember.isEmpty() || optionalMember.get().isFromSocial()) {
      log.info("회원이 아니거나, 소셜 멤버는 비밀번호를 바꾸실 수 없습니다.");
      return;
    }

    String key = "email:reset:" + email;

    if(redisUtil.get(key) != null) {
      redisUtil.delete(key);
    }
    //
    String code = String.format("%06d", new Random().nextInt(999999));
    redisUtil.set("email:reset:" + email, code, 300000L);
    String subject = "Medinote 회원 비밀번호 재설증을 위한 인증코드를 송부드립니다. 인증코드 작성란에 6자리를 입력하여 주세요.";
    String text = "인증 코드 : " + code + "\n 5분 이내로 인증하여 주시기 바랍니다.";
    mailService.sendEmail(email, subject, text);
  }

  @Override
  public boolean verifyResetPassword(String email, String code) {
    String savedCode = redisUtil.get("email:reset:" + email);
    if(savedCode == null) {
      throw new CustomException(ErrorCode.CODE_EXPIRED);
    }
    if(!savedCode.equals(code)) {
      throw new CustomException(ErrorCode.CODE_INVALID);
    }
    redisUtil.delete("email:reset:" + email);

    return true;
  }

  @Override
  @Transactional
  public void resetPassword(String email) {
    Member member = repository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 임시 비밀번호 발급
    String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    String encodedTempPassword = passwordEncoder.encode(tempPassword);
    member.changePassword(encodedTempPassword);

    String subject = "Medinote 임시 비밀번호를 발급";
    String text = "임시 비밀번호 : " + tempPassword + " 입니다. 로그인 후에 마이페이지에서 비밀번호를 수정하여 주시기 바랍니다.";
    mailService.sendEmail(email, subject, text);
  }
}
