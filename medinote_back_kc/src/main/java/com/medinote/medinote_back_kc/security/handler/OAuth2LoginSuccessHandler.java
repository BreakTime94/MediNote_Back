package com.medinote.medinote_back_kc.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medinote.medinote_back_kc.common.ErrorCode;
import com.medinote.medinote_back_kc.common.exception.CustomException;
import com.medinote.medinote_back_kc.member.domain.dto.member.MemberDTO;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.member.Status;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import com.medinote.medinote_back_kc.member.mapper.MemberMapper;
import com.medinote.medinote_back_kc.member.mapper.MemberSocialMapper;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.member.service.member.AuthServiceImpl;
import com.medinote.medinote_back_kc.member.service.member.MemberService;
import com.medinote.medinote_back_kc.member.service.social.MemberSocialService;
import com.medinote.medinote_back_kc.security.service.TokenAuthService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final MemberSocialService memberSocialService;
  private final ObjectMapper objectMapper;
  private final TokenAuthService tokenAuthService;
  private final MemberRepository memberRepository;
  private final AuthServiceImpl authService;
  private final MemberMapper memberMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

    //1. OAuth2AuthenticationToken 보유 여부 확인, 이게 없다면 구글 OAuth2 쪽에서 인증 문제가 생긴 것

    if(!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
      throw new CustomException(ErrorCode.SOCIAL_TOKEN_ERROR);
    }

    //2. 로그인 & 회원등록에 필요한 정보 OAuth2Token을 통해 security context에 등록된 인증정보 가져옴
    OAuth2User oAuth2User = oauth2Token.getPrincipal();
    Provider provider = Provider.valueOf(oauth2Token.getAuthorizedClientRegistrationId().toUpperCase());
    Map<String, Object> attributes = oAuth2User.getAttributes(); // 내려주는 value 값이 단순 String만 있지 않음 그래서 value 값에 object 사용
    Map<String, Object> result = new HashMap<>();

    //3. SocialRegisterRequestDTO에 Mapping
    SocialRegisterRequestDTO dto = SocialRegisterRequestDTO.builder()
            .provider(provider)
            .providerUserId(oAuth2User.getName())
            .email((String)attributes.get("email"))
            .profileImageUrl((String)attributes.get("picture"))
            .profileMimeType(memberSocialService.resolveMimeType((String)attributes.get("picture")))
            .nickname((String)attributes.get("name"))
            .rawProfileJson(objectMapper.writeValueAsString(attributes))
            .build();

    // 4. 기존 소셜회원이 맞는지 check
    if(memberSocialService.isSocialMember(dto)) {

      Member member = memberRepository.findByEmailOrExtraEmail(dto.getEmail(), dto.getEmail()).orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      // authService에 존재하는 Status 체크하는 메서드 public으로 변경

      try{
        authService.checkStatus(member); //status만 check해서 넘기면 됨. 내부는 CustomException 로직 존재
      } catch (CustomException e){
        sendPostMessage(response, Map.of(
                "status", e.getErrorCode().getStatus(),
                "code", e.getErrorCode().name(),
                "message", e.getMessage()
        ));
        return;
      }

      log.info("기존에 존재하는 SocialMember");
      // TokenAuthService에 전부 위임
      tokenAuthService.makeCookieWithToken(dto.getEmail(), response);

      MemberDTO memberDTO = memberMapper.toMemberDTO(member);
      result = Map.of(
              "status", "LOGIN_SUCCESS",
              "provider", provider.name(),
              "member", memberDTO
      );

    } else { // 소셜 테이블에 없는 경우는 2가지 경우가 있다. member table에 email/extraEmail이 존재하는가? 아닌가?
      Optional<Member> optionalMember = memberRepository.findByEmailOrExtraEmail(dto.getEmail(), dto.getEmail());
      if(optionalMember.isPresent()) {
        //table에 main email이나 extraEmail로 되어 있는 경우는 소셜 연동을 시켜버리고 로그인 성공
        Member member = optionalMember.get();
        //status 검사 하고
        try{
          authService.checkStatus(member);
        } catch(CustomException e){
          sendPostMessage(response, Map.of(
                  "status", e.getErrorCode().getStatus(),
                  "code", e.getErrorCode().name(),
                  "message", e.getMessage()
          ));
          return;
        }

        //dto + id를 socialmember table에 insert만 하면 됨!
        memberSocialService.linkSocialAccount(member, dto);
        tokenAuthService.makeCookieWithToken(dto.getEmail(), response);

        MemberDTO memberDTO = memberMapper.toMemberDTO(member);
        result = Map.of(
                "status", "LOGIN_SUCCESS",
                "message", "기존에 일반 이메일로 가입되어 있으며 소셜 연동을 진행합니다.",
                "provider", provider.name(),
                "member", memberDTO
        );

      } else { // 둘다 없는 경우는 신규 가입?
        log.info("기존에 등록 안된 Social Member");
        //프론트가 바뀌고, 들어오는 값을 통해서, SocialRegisterRequestDTO를 SocialToMemberRegisterDTO로 Mapping 시키는 작업 필요
        result = Map.of(
                "status", "NEED_REGISTER",
                "member", dto
        );
        log.info(result);
      }
    }
    sendPostMessage(response, result);
  }

  private void sendPostMessage(HttpServletResponse response, Map<String, Object> payload) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    String script = "<script>" +
            "window.opener.postMessage(" +
            objectMapper.writeValueAsString(payload) +
            ", '*');" +
            "window.close();" +
            "</script>";
    response.getWriter().write(script);
  }
}