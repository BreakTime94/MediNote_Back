package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.MemberResponseDTO;
import com.medinote.medinote_back_kc.member.domain.dto.RegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.UpdateRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.Member;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface MemberMapper {
  MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", expression = "java(passwordEncoder.encode(dto.getPassword()))")
  @Mapping(target = "role", ignore = true) // table 기본값 USER
  @Mapping(target= "profileImagePath", ignore = true)
  @Mapping(target = "profileMimeType", ignore = true)
  @Mapping(target = "status", ignore = true) // table 기본값 ACTIVE
  @Mapping(target = "regDate", ignore = true) // table 기본값 current timestamp
  @Mapping(target = "deletedAt", ignore = true) // 별도 삭제했을 때 처리
  Member toMember(RegisterRequestDTO dto, @Context PasswordEncoder passwordEncoder);

  //id(pk), password, status, deleted_at 을 제외한 dto 구성
  MemberResponseDTO toMemberDTO(Member member);
}
