package com.medinote.medinote_back_kc.member.domain.dto.terms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberTermsRegisterRequestDTO {
  private Long termsId;
  private boolean agreed;
}
