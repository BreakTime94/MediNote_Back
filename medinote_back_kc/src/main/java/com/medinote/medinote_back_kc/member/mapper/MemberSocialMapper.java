package com.medinote.medinote_back_kc.member.mapper;

import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialToMemberRegisterDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberSocialMapper {

  //1. Member Entity를 통한 회원가입 이후, MemberSocial 필요정보 table 맵핑
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "member", source = "member")
  @Mapping(target = "provider", source = "dto.provider")
  @Mapping(target = "providerUserId", source = "dto.providerUserId")
  @Mapping(target = "email", source = "dto.email")
  @Mapping(target = "profileImageUrl", source = "dto.profileImageUrl")
  @Mapping(target = "nickname", source = "dto.nickname")
  @Mapping(target = "rawProfileJson", source = "dto.rawProfileJson")
  @Mapping(target = "connectedAt", ignore = true) // DB table 기본값
  @Mapping(target = "disconnectedAt", ignore = true)
  MemberSocial toMemberSocial(SocialRegisterRequestDTO dto, Member member);

  //구글 OAuth2가 보내주는 정보에 추가로 사용자에게 입력을 받아서 Member로 가입시키는 mapper 역할 // 프론트에서 추가 입력 받아야 함
  @Mapping(target = "profileImagePath", source = "profileImageUrl")
  @Mapping(target = "fromSocial", constant = "true")
  SocialToMemberRegisterDTO socialToMemberLink(SocialRegisterRequestDTO dto);

}
