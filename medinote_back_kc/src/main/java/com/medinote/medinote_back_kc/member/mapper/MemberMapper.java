package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.member.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterDTO;
import com.medinote.medinote_back_kc.member.domain.entity.Member;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface MemberMapper {
  MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

  //1. 일반 회원가입
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", expression = "java(passwordEncoder.encode(dto.getPassword()))")
  @Mapping(target = "role", ignore = true) // table 기본값 USER
  @Mapping(target = "extraEmail", ignore = true)
  @Mapping(target= "profileImagePath", ignore = true)
  @Mapping(target = "profileMimeType", ignore = true)
  @Mapping(target = "status", ignore = true) // table 기본값 ACTIVE
  @Mapping(target = "regDate", ignore = true) // table 기본값 current timestamp
  @Mapping(target = "deletedAt", ignore = true) // 별도 삭제했을 때 처리
  @Mapping(target = "fromSocial", ignore = true) // fromSocial false/true 기본값 false
  Member toRegister(RegisterRequestDTO dto, @Context PasswordEncoder passwordEncoder);

  //id(pk), password, status, deleted_at 을 제외한 dto 구성
  MemberDTO toMemberDTO(Member member);

  //2. 소셜 회원가입 (Social로 가입해도 일반 회원 내 데이터 값을 하나 추가한다.)
//  @Mapping(target = "id", ignore = true)
//  @Mapping(target = "password", ignore = true) // password 없음
//  @Mapping(target = "role", ignore = true)
//  @Mapping(target = "fromSocial", constant = "true") // 소셜 가입임을 표시
//  Member toSocialRegister(SocialRegisterDTO dto);

}
