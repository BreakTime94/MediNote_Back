package com.medinote.medinote_back_kc.member.domain.dto.terms;

import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TermsDTO {
  private Long id;
  private String policyCode;
  private String version;
  private String title;
  private String content;
  private LocalDateTime effectiveFrom;
  boolean required;
}
