package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.terms.TermsDTO;
import com.medinote.medinote_back_kc.member.domain.entity.terms.Terms;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TermsMapper {

  @Mapping(target = "policyCode", expression = "java(terms.getPolicyCode().name().toLowerCase())")
  TermsDTO toDTO(Terms terms);
}
