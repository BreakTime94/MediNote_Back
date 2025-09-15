package com.medinote.medinote_back_kc.member.controller;

import com.medinote.medinote_back_kc.member.domain.dto.LoginRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/board")
@Log4j2
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MemberController {

  private final MemberService service;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
    return ResponseEntity.ok(service.register(dto));
  }

  @GetMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
    return null;
  }
}
