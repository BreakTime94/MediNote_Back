package com.medinote.medinote_back_kc.member.controller.social;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.service.social.MemberSocialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/social/auth") // JWTToken이 발급되는 로직이 필요한건 auth로 명명 (Member도 동일)
@Log4j2
public class SocialAuthController {

  public MemberSocialService memberSocialService;

  @PostMapping("/login")
  public ResponseEntity<?> login (@RequestBody SocialRegisterRequestDTO dto) {

    if(memberSocialService.isSocialMember(dto)) {// socialMember가 존재한다면 MemberDTO 찾아서 반환
      MemberDTO memberDto = memberSocialService.getSocialMember(dto);
      return ResponseEntity.ok(memberDto);
    }

    return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body( // React에서 추가 정보 받는 컴포넌트를 꺼내는 기준
            Map.of("message", "추가정보가 필요합니다.", "email", dto.getEmail(), "provider", dto.getProvider())
    );
  }

  @PostMapping("/register")
  public ResponseEntity<?> register (@RequestBody SocialRegisterRequestDTO dto) {
    return ResponseEntity.ok(memberSocialService.register(dto));
  }
}
