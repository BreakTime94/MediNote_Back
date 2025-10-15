package com.medinote.medinote_back_kc.member.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
public class AdminController {

  @GetMapping("/member/list")
  public ResponseEntity<?> getMemberList(){

    return ResponseEntity.status(HttpStatus.OK).body(Map.of(

    ));
  }
}
