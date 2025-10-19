package com.medinote.medinote_back_kc.member.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberForBoardsDTO {
  Long id;
  String nickname;
  String role;
}
