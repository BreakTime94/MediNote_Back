package com.medinote.medinote_back_kc.security.config;

import com.medinote.medinote_back_kc.security.filter.JWTAuthenticationFilter;
import com.medinote.medinote_back_kc.security.handler.OAuth2LoginSuccessHandler;
import com.medinote.medinote_back_kc.security.service.CustomUserDetailsService;
import com.medinote.medinote_back_kc.security.service.TokenAuthService;
import com.medinote.medinote_back_kc.security.util.CookieUtil;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import com.medinote.medinote_back_kc.security.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Log4j2
@RequiredArgsConstructor
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private final RedisUtil redisUtil;
  private final TokenAuthService tokenAuthService;
  private final CORSConfig corsConfig;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Bean
  public JWTAuthenticationFilter jwtAuthenticationFilter() {
    return new JWTAuthenticationFilter(jwtUtil, cookieUtil, customUserDetailsService, redisUtil, tokenAuthService);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.info("security 필터 체인 들어왔다 이 자식아");
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .csrf(c -> c.disable())
        .cors(c -> c.configurationSource(corsConfig.corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 권한별 접근 가능한 controller 분리
        .authorizeHttpRequests(a -> a
                .requestMatchers("/member/auth/login", "/member/register", "/social/auth/register"
                , "/member/check/email", "/member/check/nickname", "/member/email/verify", "/member/email/send",
                        "member/find/email", "member/reset/password", "/terms/list").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**","/user").permitAll()
                .requestMatchers("/boards/read/**", "/boards/notice/list", "/boards/faq/list", "/boards/qna/list").permitAll()
                .anyRequest().authenticated())
            //form 로그인 불가
        .formLogin(f -> f.disable())
            //대신 oauth 로그인은 열어둠
        .oauth2Login(o -> o
                .authorizationEndpoint(auth -> auth.baseUri("/oauth2/authorization"))
                .redirectionEndpoint(red -> red.baseUri("/login/oauth2/code/*"))
                .successHandler(oAuth2LoginSuccessHandler))
        .httpBasic(b -> b.disable());

    return http.build();
  }


}
