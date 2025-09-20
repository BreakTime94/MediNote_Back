package com.medinote.medinote_back_kc.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CORSConfig {//CORSFilter가 스프링에 내장되어 있으므로, 별도의 설정만 해주면 됨.

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedOrigins(List.of( "http://localhost:8080",
            "http://localhost:8081",
            "http://localhost:8082",
            "http://localhost:5173",
            "http://localhost:6006"));
    config.setExposedHeaders(List.of("Set-Cookie"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", config);

    return source;
  }

//  @Bean
//  public CorsFilter corsFilter() {
//    CorsConfiguration config = new CorsConfiguration();
//    config.setAllowedOrigins(List.of(
//            "http://localhost:8080",
//            "http://localhost:8081",
//            "http://localhost:8082",
//            "http://localhost:5173",
//            "http://localhost:6006"
//    ));
//    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//    config.setAllowedHeaders(List.of("*"));
//    config.setAllowCredentials(true);
//
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//
//    source.registerCorsConfiguration("/**", config);
//
//    return new CorsFilter(source);
//  }
}
