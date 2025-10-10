package com.medinote.medinote_back_kc.member.controller;

import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.service.MemberService;
import com.medinote.medinote_back_kc.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberService service;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
    service.register(dto);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/get")
  public ResponseEntity<?> get(Authentication authentication) {
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

    return ResponseEntity.ok(service.get(user.getEmail()));
  }

}
