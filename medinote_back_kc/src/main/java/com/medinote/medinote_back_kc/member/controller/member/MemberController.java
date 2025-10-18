package com.medinote.medinote_back_kc.member.controller.member;

import com.medinote.medinote_back_kc.member.domain.dto.member.ChangePasswordRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.CheckPasswordRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.service.member.MemberService;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberService service;
  private final CookieUtil cookieUtil;

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
  public ResponseEntity<?> checkEmail(@RequestParam String email) { // 로그인이 안되어 있어도 authentication이 null은 아니다.
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long currentMemberId = null;
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
    currentMemberId = principal.getId(); // 현재 로그인한 사용자의 PK
    }

    boolean available = service.isEmailAvailable(email, currentMemberId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "EMAIL_CHECKED",
            "available", available
    ));
  }
  //닉네임 중복 체크
  @GetMapping("/check/nickname")
  public ResponseEntity<?> checkNickName(@RequestParam String nickname) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long currentMemberId = null;
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
      currentMemberId = principal.getId(); // 현재 로그인한 사용자의 PK
    }

    boolean available = service.isNicknameAvailable(nickname, currentMemberId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "NICKNAME_CHECKED",
            "available", available
    ));
  }

  //이메일 인증 코드 송부
  @PostMapping("/email/send")
  public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> data) {
    String email = data.get("email"); // 가입시에는 email, find에서는 extraEmail 값이 날라온다.
    String type = data.get("type"); // 이메일 인증 코드를 요구하는 서비스 종류 (find, signUp 등등)

    switch(type){
      case "find":
        service.sendVerificationCodeForFindEmail(email);
        break;
      case "signUp", "extraEmailVerify":
        service.sendVerificationCode(email);
        break;
      case "reset":
        service.sendVerificationCodeForResetPassword(email);
        break;
    }

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
    String type = data.get("type");
    boolean verified = false;

    switch(type){
      case "find":
        verified = service.verifyFindEmailCode(email, code);
        break;
      case "signUp", "extraEmailVerify":
        verified = service.verifyCode(email, code);
        break;
      case "reset":
        verified = service.verifyResetPassword(email, code);
        break;
    }

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status" , "VERIFICATION_SUCCESS",
            "message", "이메일 인증이 완료되었습니다.",
            "available", verified
    ));
  }

  //단일 객체 취득
  @GetMapping("/get")
  public ResponseEntity<?> get(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "MEMBER_INFO_GOT",
              "message", "로그인을 성공하여, 회원 정보를 가져옵니다.",
            "member", service.get(user.getEmail())
    ));
  }

  //MyPage 부분수정
  @PatchMapping("/modify")
  public ResponseEntity<?> modify(@Valid @RequestBody UpdateRequestDTO dto) {
    log.info(dto.isExtraEmailVerified());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    Long currentMemberId = principal.getId(); // 현재 로그인한 사용자의 PK
    service.update(dto, currentMemberId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status" , "UPDATE_SUCCESS",
            "message", "정보수정이 완료되었습니다."
    ));
  }

  @DeleteMapping("/remove")
  public ResponseEntity<?> withdraw(@RequestBody Map<String, String> data, HttpServletResponse response) {
    String email = data.get("email");
    service.delete(email);
    ResponseCookie accessCookie = cookieUtil.deleteAccessCookie();
    ResponseCookie refreshCookie = cookieUtil.deleteRefreshCookie();
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status" , "DELETE_SUCCESS",
            "message", "삭제가 완료되었습니다."
    ));
  }

  @PostMapping("/check/password")
  public ResponseEntity<?> checkPassword(@RequestBody CheckPasswordRequestDTO dto) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    Long currentMemberId = principal.getId();

     boolean available = service.checkPassword(dto.getPassword(), currentMemberId);
     return ResponseEntity.status(HttpStatus.OK).body(Map.of(
             "status", "PASSWORD_CHECKED",
             "available", available
     ));
  }

  @PatchMapping("/change/password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    Long currentMemberId = principal.getId();

    service.changePassword(dto, currentMemberId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "PASSWORD_CHANGED",
            "message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인하여주시기 바랍니다."
    ));
  }

  @PostMapping("/find/email")
  public ResponseEntity<?> findEmail(@RequestBody Map<String, String> data) {
    String extraEmail = data.get("extraEmail");
    String originalEmail = service.findEmailByExtraEmail(extraEmail);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "EMAIL_FOUND",
            "message", "이메일을 찾았습니다.",
            "email", originalEmail
    ));
  }

  @PostMapping("/reset/password")
  public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> data) {
    service.resetPassword(data.get("email"));
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "RESET_PASSWORD",
            "message", "임시 비밀번호를 발급하였습니다."
    ));
  }

  @GetMapping("/list/info")
  public ResponseEntity<?> infoList() {
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "MEMBER_INFO_LIST",
            "memberInfoList", service.memberInfoList()
    ));
  }

}
