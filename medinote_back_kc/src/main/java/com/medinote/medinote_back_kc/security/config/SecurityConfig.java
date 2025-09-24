package com.medinote.medinote_back_kc.security.config;

import com.medinote.medinote_back_kc.security.filter.JWTAuthenticationFilter;
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

  @Bean
  public JWTAuthenticationFilter jwtAuthenticationFilter() {
    return new JWTAuthenticationFilter(jwtUtil, cookieUtil, customUserDetailsService, redisUtil, tokenAuthService);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.info("security 필터 체인 들어왔다 이 자식아");
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .csrf(c -> c.disable())
        .cors(c -> c.configurationSource(corsConfig.corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
                .requestMatchers("/member/auth/login", "/member/register").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/member/update", "/member/delete", "/member/get", "member/auth/logout").hasAnyRole("USER", "ADMIN", "PHARMACIST", "DOCTOR")
                .anyRequest().authenticated())
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable());

    return http.build();
  }


}
