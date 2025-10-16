package com.medinote.medinote_back_kc.member.service.Terms;

import com.medinote.medinote_back_kc.common.ErrorCode;
import com.medinote.medinote_back_kc.common.exception.CustomException;
import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.terms.MemberTerms;
import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import com.medinote.medinote_back_kc.member.mapper.MemberTermsMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.member.repository.MemberTermsRepository;
import com.medinote.medinote_back_kc.member.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MemberTermsServiceImpl implements MemberTermsService {

  private final TermsRepository termsRepository;
  private final MemberTermsMapper memberTermsMapper;
  private final MemberTermsRepository memberTermsRepository;

  @Override
  public void agreeWithTerms(List<MemberTermsRegisterRequestDTO> agreements, Member savedMembers) {
    if (agreements!= null && !agreements.isEmpty()) {
      log.info(agreements);
      for (MemberTermsRegisterRequestDTO agreeDto : agreements) {

        Terms terms = termsRepository.findById(agreeDto.getTermsId())
                .orElseThrow(() -> new CustomException(ErrorCode.TERMS_NOT_FOUND));

        MemberTerms memberTerms = memberTermsMapper.toMemberTerms(agreeDto, savedMembers, terms);

        memberTermsRepository.save(memberTerms);
      }
    }
  }
}
