package com.medinote.medinote_back_kc.member.domain.dto.terms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberTermsDTO {
  private boolean agreed;
  private LocalDateTime agreedAt;
  private String termsTitle;
  private String termsVersion;
  private String policyCode;
  private boolean required;
  private LocalDateTime effectiveFrom;
}
