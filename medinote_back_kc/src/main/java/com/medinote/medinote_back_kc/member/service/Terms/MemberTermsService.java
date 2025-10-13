package com.medinote.medinote_back_kc.member.service.Terms;

import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;

import java.util.List;

public interface MemberTermsService {
  void agreeWithTerms(List<MemberTermsRegisterRequestDTO> agreements, Member savedMembers);
}
