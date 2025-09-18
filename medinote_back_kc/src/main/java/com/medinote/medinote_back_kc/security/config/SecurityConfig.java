package com.medinote.medinote_back_kc.security.config;

import com.medinote.medinote_back_kc.security.filter.JWTAuthenticationFilter;
import com.medinote.medinote_back_kc.security.service.CustomUserDetailsService;
import com.medinote.medinote_back_kc.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Log4j2
@RequiredArgsConstructor
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private final CORSConfig corsConfig;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.addFilterBefore(new JWTAuthenticationFilter(jwtUtil, customUserDetailsService), UsernamePasswordAuthenticationFilter.class)
        .csrf(c -> c.disable())
        .cors(c -> c.configurationSource(corsConfig.corsConfiguration()))
        .sessionManagement(s -> s.disable())
        .authorizeHttpRequests(a -> a
                .requestMatchers("/member/auth/login", "/member/register").permitAll()
                .requestMatchers("/member/update", "/member/delete").hasAnyRole("USER", "ADMIN", "PHARMACIST", "DOCTOR")
                .anyRequest().authenticated())
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable());

    return http.build();
  }


}
