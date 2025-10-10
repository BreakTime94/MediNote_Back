package com.medinote.medinote_back_kc.security.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt") // yaml 파일에 계층구조로 되어 있는 key 값을 cammel로 바꿔서 필드에 저장
public class JWTProperties {
  private String secret;
  private long accessTokenExpiration;
  private long refreshTokenExpiration;
}
