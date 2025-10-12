package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.terms.MemberTermsRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.terms.MemberTerms;
import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberTermsMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "member", source = "member")
  @Mapping(target = "terms", source = "terms")
  @Mapping(target = "agreed", source = "dto.agreed")
  MemberTerms toMemberTerms(MemberTermsRegisterRequestDTO dto, Member member, Terms terms);
}
