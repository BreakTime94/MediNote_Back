package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberSocialMapper {
  MemberSocialMapper INSTANCE = Mappers.getMapper(MemberSocialMapper.class);

  //1. Member Entity를 통한 회원가입 이후, MemberSocial 필요정보 table 맵핑

  @Mapping(target = "id", ignore = true)
  MemberSocial toMemberSocial(SocialRegisterRequestDTO dto);
}
