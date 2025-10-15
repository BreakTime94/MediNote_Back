package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsDTO;
import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.terms.MemberTerms;
import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberTermsMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "member", source = "member")
  @Mapping(target = "terms", source = "terms")
  @Mapping(target = "agreed", source = "dto.agreed")
  @Mapping(target = "agreedAt", ignore = true)
  MemberTerms toMemberTerms(MemberTermsRegisterRequestDTO dto, Member member, Terms terms);

  //MemberTermsDTO 생성 (ManyToOne 필드에 대해서 처리까지 포함)
  @Mapping(target = "agreed", source = "agreed")
  @Mapping(target = "agreedAt", source = "agreedAt")
  @Mapping(target = "termsTitle", source = "terms.title")
  @Mapping(target = "termsVersion", source = "terms.version")
  @Mapping(target = "policyCode", source = "terms.policyCode")
  @Mapping(target = "required", source = "terms.required")
  @Mapping(target = "effectiveFrom", source = "terms.effectiveFrom")
  MemberTermsDTO toDto(MemberTerms memberTerms);
}
