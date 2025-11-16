package com.medinote.medinote_back_kc.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
  // 점(.)이 없는 모든 경로 → index.html 로 포워드
  @GetMapping(value = {"/", "/{path:[^\\.]*}"})
  public String forward() {
    return "forward:/api/index.html";
  }
}

