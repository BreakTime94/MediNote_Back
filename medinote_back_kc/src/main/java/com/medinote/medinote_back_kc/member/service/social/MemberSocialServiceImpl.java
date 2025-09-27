package com.medinote.medinote_back_kc.member.service.social;

import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialToMemberRegisterDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.social.MemberSocial;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.mapper.MemberSocialMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.member.repository.MemberSocialRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Log4j2
public class MemberSocialServiceImpl implements MemberSocialService {
  @Autowired
  private MemberSocialRepository memberSocialRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private MemberSocialMapper memberSocialMapper;

  @Autowired
  private MemberMapper memberMapper;

  @Override
  @Transactional
  public MemberDTO register(SocialRegisterRequestDTO dto) {
    // 0. 구글 OAuth2에서 인증 (사전 SecurityContext에서 filter무효 처리, SocialRegisterRequestDTO에 맵핑 되었다고 가정)

    //1. 프론트에서 넘겨준 RequestDTO를 Member 등록용 dto로 변환, 이 단계에서 프론트는 추가 정보를 받는 컴포넌트로 변경된다.
    SocialToMemberRegisterDTO socialToMemberRegisterDTO = memberSocialMapper.socialToMemberLink(dto);

    //2. Member 등록용 dto로 MemberEntity 등록 & savedMember 반환
    Member member = memberMapper.socialToMemberRegister(socialToMemberRegisterDTO);

    Member savedMember = memberRepository.save(member);

    //3. member를 담아서 MemberSocial Entity로 변환
    MemberSocial memberSocial = memberSocialMapper.toMemberSocial(dto, savedMember);

    //4. MemberSocial Entity 테이블에 저장
    memberSocialRepository.save(memberSocial);

    //5. 등록절차는 끝이나, MemberResponseDTO를 반환해야함 (Social은 최초로그인 절차가 끝나면 바로 로그인이 되니까)
    return memberMapper.toMemberDTO(savedMember);
  }

  @Override
  @Transactional
  public void connect(Long memberId, SocialRegisterRequestDTO dto) {

  }

  @Override
  @Transactional
  public void disconnect(Long memberId, Provider provider) {

  }

  @Override
  public boolean isSocialMember(SocialRegisterRequestDTO dto) {
    Provider provider = dto.getProvider();
    String providerUserId = dto.getProviderUserId();
    return memberSocialRepository.findByProviderAndProviderUserId(provider, providerUserId).isPresent();
  }

  @Override
  public MemberDTO getSocialMember(SocialRegisterRequestDTO dto) {
    MemberSocial social = memberSocialRepository.findByProviderAndProviderUserId(dto.getProvider(), dto.getProviderUserId()).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 소셜 계정입니다."));
    return memberMapper.toMemberDTO(social.getMember());
  }
}
