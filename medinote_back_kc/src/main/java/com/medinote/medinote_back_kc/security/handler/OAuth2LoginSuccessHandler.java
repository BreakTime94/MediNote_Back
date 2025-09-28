package com.medinote.medinote_back_kc.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medinote.medinote_back_kc.member.domain.dto.social.SocialRegisterRequestDTO;
import com.medinote.medinote_back_kc.member.domain.entity.member.Member;
import com.medinote.medinote_back_kc.member.domain.entity.social.Provider;
import com.medinote.medinote_back_kc.member.repository.MemberRepository;
import com.medinote.medinote_back_kc.member.service.social.MemberSocialService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JWTUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final MemberSocialService memberSocialService;
  private final MemberRepository memberRepository;
  private final RedisUtil redisUtil;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

    //1. OAuth2AuthenticationToken 보유 여부 확인, 이게 없다면 구글 OAuth2 쪽에서 인증 문제가 생긴 것

    if(!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
      throw new IllegalStateException("구글 측의 토큰 발급 과정에 이상이 있습니다. 재시도를 통해 확인하여주시기 바랍니다.");
    }

    //2. 로그인 & 회원등록에 필요한 정보 OAuth2Token을 통해 security context에 등록된 인증정보 가져옴
    OAuth2User oAuth2User = oauth2Token.getPrincipal();
    Provider provider = Provider.valueOf(oauth2Token.getAuthorizedClientRegistrationId().toUpperCase());
    Map<String, Object> attributes = oAuth2User.getAttributes(); // 내려주는 value 값이 단순 String만 있지 않음 그래서 value 값에 object 사용

    //3. SocialRegisterRequestDTO에 Mapping
    SocialRegisterRequestDTO dto = SocialRegisterRequestDTO.builder()
            .provider(provider)
            .providerUserId(oAuth2User.getName())
            .email((String)attributes.get("email"))
            .profileImageUrl((String)attributes.get("picture"))
            .nickname((String)attributes.get("name"))
            .rawProfileJson(new ObjectMapper().writeValueAsString(attributes))
            .build();

    // 4. 기존회원이 맞는지 check
    if(memberSocialService.isSocialMember(dto)) {
      // 4-1. 회원 검증
      Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()-> new UsernameNotFoundException("존재하지 않는 이메일입니다."));
      // 4-2. 토큰 발급
      String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole());
      String refreshToken = jwtUtil.createRefreshToken(member.getId(), member.getRole());
      // 쿠키에 저장
      ResponseCookie accessCookie = cookieUtil.createAccessCookie(accessToken);
      ResponseCookie refreshCookie = cookieUtil.createRefreshCookie(refreshToken);
      response.addHeader("Set-Cookie", accessCookie.toString());
      response.addHeader("Set-Cookie", refreshCookie.toString());

      redisUtil.set(member.getId().toString(), refreshToken, jwtUtil.getExpirationDate(refreshToken).getTime() - System.currentTimeMillis());

    } else {
      //프론트가 바뀌고, 들어오는 값을 통해서, SocialRegisterRequestDTO를 SocialToMemberRegisterDTO로 Mapping 시키는 작업 필요
    }

  }
}

