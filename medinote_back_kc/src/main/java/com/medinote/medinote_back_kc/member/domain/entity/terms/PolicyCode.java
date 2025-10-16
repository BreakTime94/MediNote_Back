package com.medinote.medinote_back_kc.member.domain.entity.terms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyCode {

  SERVICE("서비스 이용약관"), PRIVACY("개인정보 수집 및 이용"), MARKETING("마케팅 정보 수신 동의");

  private final String description;
}
