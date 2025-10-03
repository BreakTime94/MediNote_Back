package com.medinote.medinote_back_kc.member.controller.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.service.member.MemberService;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberService service;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
    service.register(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "status", "REGISTER_SUCCESS",
            "message", "회원가입이 완료되었습니다. 로그인 해주세요."
    ));
  }
  //이메일 중복 체크
  @GetMapping("/check/email")
  public ResponseEntity<?> checkEmail(@RequestParam String email) {
    boolean available = service.isEmailAvailable(email);

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "EMAIL_CHECKED",
            "available", available
    ));
  }
  //닉네임 중복 체크
  @GetMapping("/check/nickname")
  public ResponseEntity<?> checkNickName(@RequestParam String nickname) {
    boolean available = service.isNicknameAvailable(nickname);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "NICKNAME_CHECKED",
            "available", available
    ));
  }

  //이메일 인증 코드 송부
  @PostMapping("/email/send")
  public ResponseEntity<?> verifyEmail(@RequestParam String email) {
    service.sendVerificationCode(email);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "EMAIL_SENT",
            "message", "인증번호를 발송드렸습니다.",
            "desc", "메일함을 확인하여 주시기 바랍니다."
    ));
  }
  //인증코드 검증
  @PostMapping("/email/verify")
  public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> data) {
    String email = data.get("email");
    String code = data.get("code");

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status" , "VERIFICATION_SUCCESS",
            "message", "이메일 인증이 완료되었습니다.",
            "available", service.verifyCode(email, code)
    ));
  }

  //단일 객체 취득
  @GetMapping("/get")
  public ResponseEntity<?> get(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

    return ResponseEntity.ok(service.get(user.getEmail()));
  }

}
