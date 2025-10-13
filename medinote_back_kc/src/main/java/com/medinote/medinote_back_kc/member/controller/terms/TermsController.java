package com.medinote.medinote_back_kc.member.controller.terms;

import com.medinote.medinote_back_kc.member.domain.dto.terms.TermsDTO;
import com.medinote.medinote_back_kc.member.service.Terms.TermsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/terms")
@Log4j2
@RequiredArgsConstructor
public class TermsController {

  private final TermsService termsService;

  @GetMapping("/list")
  public ResponseEntity<?> getList() {
    List<TermsDTO> dto = termsService.getList();
    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", "TERMS_LIST_GOT",
            "list",  dto
    ));
  }
}
