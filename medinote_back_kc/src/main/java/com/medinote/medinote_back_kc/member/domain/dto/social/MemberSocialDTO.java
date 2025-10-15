package com.medinote.medinote_back_kc.member.domain.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSocialDTO {
  String provider;
  LocalDateTime connectedAt;
  LocalDateTime disconnectedAt;
}
