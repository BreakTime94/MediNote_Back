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

  @GetMapping("/check-email")
  public ResponseEntity<?> checkEmail(@RequestParam String email) {
    boolean available = service.isEmailAvailable(email);

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "EMAIL_CHECKED",
            "available", available
    ));
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<?> checkNickName(@RequestParam String nickname) {
    boolean available = service.isNicknameAvailable(nickname);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "NICKNAME_CHECKED",
            "available", available
    ));
  }

  @GetMapping("/get")
  public ResponseEntity<?> get(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

    return ResponseEntity.ok(service.get(user.getEmail()));
  }

}
